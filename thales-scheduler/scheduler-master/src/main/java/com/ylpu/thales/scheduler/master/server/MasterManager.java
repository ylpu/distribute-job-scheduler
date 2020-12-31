package com.ylpu.thales.scheduler.master.server;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.curator.CuratorHelper;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.ByteUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.core.utils.SSHUtils;
import com.ylpu.thales.scheduler.enums.NodeType;
//import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.master.api.server.MasterApiServer;
//import com.ylpu.thales.scheduler.master.api.service.SchedulerService;
import com.ylpu.thales.scheduler.master.jmx.MasterJmxServer;
import com.ylpu.thales.scheduler.master.rpc.server.MasterRpcServer;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.schedule.JobScheduler;
import com.ylpu.thales.scheduler.master.strategy.JobStrategy;
import com.ylpu.thales.scheduler.master.strategy.ResourceStrategy;
import com.ylpu.thales.scheduler.master.strategy.ResourceStrategyContext;
import com.ylpu.thales.scheduler.master.strategy.WorkerSelectStrategy;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.zookeeper.CreateMode;
import java.util.*;
import org.apache.curator.framework.recipes.cache.*;

public class MasterManager {

    private static Log LOG = LogFactory.getLog(MasterManager.class);

    private static MasterManager resourceManager = new MasterManager();

    public static final int DEFAULT_MASTER_SERVER_PORT = 8081;
    public static final int DEFAULT_JMX_PORT = 9095;

    // key is worker group path,value is server list
    private Map<String, List<String>> groups = new HashMap<String, List<String>>();

    // key is hostname,value is host info
    private Map<String, WorkerResponse> resourceMap = new HashMap<String, WorkerResponse>();
    
    private Map<String, String> workerGroupMap = new HashMap<String, String>();

    // key is hostname,value is tasknumbers
    private Map<String, Integer> taskMap = new HashMap<String, Integer>();

    private String activeMaster;

    private MasterApiServer jettyServer = null;

    private MasterRpcServer server = null;

    private MasterJmxServer agent = null;

    private MasterManager() {
    }

    public static MasterManager getInstance() {
        return resourceManager;
    }


    public void init() throws Exception {
        Properties prop = new Properties();
        prop.put("thales.zookeeper.quorum", GlobalConstants.DEFAULT_ZKQUORUM);
        prop.put("thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        prop.put("thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        init(prop);
    }

    public void init(Properties prop) throws Exception {
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        CuratorHelper.createNodeIfNotExist(client, GlobalConstants.ROOT_GROUP, CreateMode.PERSISTENT, null);
        CuratorHelper.createNodeIfNotExist(client, GlobalConstants.MASTER_GROUP, CreateMode.PERSISTENT, null);
        CuratorHelper.createNodeIfNotExist(client, GlobalConstants.WORKER_GROUP, CreateMode.PERSISTENT, null);
        
        new MyLeaderSelectorListenerAdapter(client, GlobalConstants.MASTER_LOCK, prop).start();
    }

    private class MyLeaderSelectorListenerAdapter extends LeaderSelectorListenerAdapter {

        private final LeaderSelector leaderSelector;

        private Properties prop = null;

        public MyLeaderSelectorListenerAdapter(CuratorFramework client, String path, Properties prop) {

            this.prop = prop;

            leaderSelector = new LeaderSelector(client, path, this);

        }

        public void start() {

            leaderSelector.autoRequeue();

            leaderSelector.start();
        }

        public void takeLeadership(CuratorFramework client) throws Exception {
            List<String> masterList = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
            if (masterList != null && masterList.size() > 0) {
                for (String master : masterList) {
                    CuratorHelper.delete(client, GlobalConstants.MASTER_GROUP + "/" + master);
                    String masterIp = master.split(":")[0];
                    String username = Configuration.getString(prop, "thales.master.username", "default");
                    String password = Configuration.getString(prop, "thales.master.password", "default");
                    String command = "ps -ef | grep MasterServer | grep -v grep | awk '{print $2}' | xargs kill -15";
                    int returnCode = SSHUtils.executeCommand(masterIp, username, password, command);
                    if (returnCode != 0) {
                        LOG.error("failed to kill standy by master " + masterIp);
                    }
                }
            }

            
            init(GlobalConstants.WORKER_GROUP, prop);
            int masterServerPort = Configuration.getInt(prop, "thales.master.server.port", DEFAULT_MASTER_SERVER_PORT);
            long masterHeartBeatInterval = Configuration.getLong(prop, "thales.master.heartbeat.interval", 3000l);
            // start rpc service
            server = new MasterRpcServer(masterServerPort);
            server.start();
            //elect as master and regist to zookeeper
            activeMaster = MetricsUtils.getHostName() + ":" + masterServerPort;
            String masterPath = GlobalConstants.MASTER_GROUP + "/" + activeMaster;
            LOG.info("active master is " + activeMaster);
            CuratorHelper.createNodeIfNotExist(client, masterPath, CreateMode.PERSISTENT, null);
            
            new MasterHeartBeatThread(masterPath,masterServerPort,masterHeartBeatInterval).start();
            
            server.blockUntilShutdown();
        }
    }
    
    private static class MasterHeartBeatThread extends Thread {
        private String masterPath;
        private int masterPort;
        private long heartBeatInterval;

        public MasterHeartBeatThread(String masterPath, int masterPort, long heartBeatInterval) {
            this.masterPath = masterPath;
            this.masterPort = masterPort;
            this.heartBeatInterval = heartBeatInterval;
        }

        public void run() {
            CuratorFramework client = null;
            Properties prop = Configuration.getConfig();
            String quorum = prop.getProperty("thales.zookeeper.quorum");
            int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                    GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
            int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                    GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
            while (true) {
                try {
                    client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
                    WorkerRequest workerRequest = new WorkerRequest();
                    workerRequest.setHost(MetricsUtils.getHostName());
                    workerRequest.setCpuUsage(MetricsUtils.getCpuUsage());
                    workerRequest.setMemoryUsage(MetricsUtils.getMemoryUsage());
                    workerRequest.setWorkerStatus(WorkerStatus.UPDATED.getCode());
                    workerRequest.setWorkerType(NodeType.MASTER.getCode());
                    workerRequest.setPort(masterPort);
                    workerRequest.setZkdirectory(masterPath);
                    workerRequest.setLastHeartbeatTime(new Date());
                    CuratorHelper.setData(client, masterPath, ByteUtils.objectToByteArray(workerRequest));
                } catch (Exception e) {
                    LOG.error(e);
                } finally {
                    CuratorHelper.close(client);
                }
                try {
                    Thread.sleep(heartBeatInterval);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }
    }

    public void init(String workerGroup, Properties prop) throws Exception {
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> list = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP);
        if (list != null && list.size() > 0) {
            for (String groupName : list) {
                String groupPath = workerGroup + "/" + groupName;
                initGroup(groupPath);
                addNodeChangeListener(client, groupPath);
            }
        }
        
        CuratorFramework strategyClient = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> strategyList = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP);
        if (strategyList != null && strategyList.size() > 0) {
            for (String groupName : strategyList) {
                byte[] bytes = CuratorHelper.getData(strategyClient, GlobalConstants.WORKER_GROUP + "/" + groupName);
                workerGroupMap.put(groupName, new String(bytes));
            }
            addGroupChangeListener(client, GlobalConstants.WORKER_GROUP);
        }

//        restore task state
//        restoreTaskState();
//      start master http service
        jettyServer = new MasterApiServer(prop);
        jettyServer.startJettyServer();
        // mark status to fail if status in (1,2,3,4,5,6)
        JobManager.markStatus();
        // restore task state
        restoreTaskState();
        // start job status check thread
        JobStatusChecker.init();
        // start to schedule all jobs
        JobScheduler.startJobs();
        // init task count of work
        initTaskCount();
        // start jmx service
        agent = new MasterJmxServer(Configuration.getInt(prop, "thales.master.jmx.port", DEFAULT_JMX_PORT));
        agent.start();

    }

    private void initGroup(String groupPath) throws Exception {
        List<String> workers = groups.get(groupPath);
        if (workers == null) {
            workers = new ArrayList<String>();
            groups.put(groupPath, workers);
        }
    }

    private void initTaskCount() throws Exception {
        synchronized (taskMap) {
            List<Map<String, Object>> list = JobManager.getTaskCountByWorker();
            for (Map<String, Object> map : list) {
                Object worker = map.get("worker");
                if (worker != null) {
                    taskMap.put(map.get("worker").toString(),
                            NumberUtils.toInt(String.valueOf(map.get("cnt"))));
                }
            }
        }
    }

    /**
     * 
     * @throws Exception
     */
//    private void restoreTaskState(){
//        try {
//            JobInstanceResponseRpc responseRpc = null;
//            SchedulerService schedulerService = new SchedulerService();
//            List<JobInstanceStateResponse> list = JobManager.getAllJobStatus();
//            if (list != null && list.size() > 0) {
//                for (JobInstanceStateResponse response : list) {
//                    if(response.getTaskState() == TaskState.SUBMIT.getCode() || response.getTaskState() == TaskState.SCHEDULED.getCode()
//                            || response.getTaskState() == TaskState.WAITING_DEPENDENCY.getCode() || response.getTaskState() == TaskState.QUEUED.getCode()
//                            || response.getTaskState() == TaskState.WAITING_RESOURCE.getCode()) {
//                        schedulerService.rerun(response.getId());
//                    }else {
//                        String responseId = response.getJobId() + "-"
//                                + DateUtils.getDateAsString(response.getScheduleTime(), DateUtils.MINUTE_TIME_FORMAT);
//                        responseRpc = JobInstanceResponseRpc.newBuilder().setId(response.getId()).setResponseId(responseId)
//                                .setTaskState(response.getTaskState()).build();
//                        JobStatusChecker.addResponse(responseRpc);
//                    }
//                }
//                
//            }
//        }catch(Exception e) {
//            LOG.error(e);
//        }
//    }
    /**
     * load history task status(latest one month)
     * @throws Exception
     */
    private void restoreTaskState() throws Exception {
        JobInstanceResponseRpc responseRpc = null;
        List<JobInstanceStateResponse> list = JobManager.getAllJobStatus();
        if (list != null && list.size() > 0) {
            for (JobInstanceStateResponse response : list) {
                String responseId = response.getJobId() + "-"
                        + DateUtils.getDateAsString(response.getScheduleTime(), DateUtils.MINUTE_TIME_FORMAT);
                responseRpc = JobInstanceResponseRpc.newBuilder().setId(response.getId()).setResponseId(responseId)
                        .setTaskState(response.getTaskState()).build();
                JobStatusChecker.addResponse(responseRpc);
            }
        }
    }

    private synchronized void releaseResource(String groupPath, List<String> disconnectedChildren) throws Exception {
        if (disconnectedChildren != null && disconnectedChildren.size() > 0) {
            for (String child : disconnectedChildren) {
                resourceMap.remove(child);
            }
        }
    }

    /**
     * update worker information according to heartbeat
     * 
     * @param serverName
     * @param resourceParams
     */
    public void updateResource(WorkerRequest request) {
        synchronized (resourceMap) {
            WorkerResponse workerInfo = resourceMap.get(request.getHost());
            if (workerInfo == null) {
                workerInfo = new WorkerResponse();
                resourceMap.put(request.getHost() + ":" + request.getPort(), workerInfo);
            }
            workerInfo.setHost(request.getHost());
            workerInfo.setCpuUsage(request.getCpuUsage());
            workerInfo.setMemoryUsage(request.getMemoryUsage());
            workerInfo.setWorkerStatus(WorkerStatus.getWorkerStatus(request.getWorkerStatus()));
            workerInfo.setWorkerGroup(request.getWorkerGroup());
            workerInfo.setLastHeartbeatTime(
                    DateUtils.getDateAsString(request.getLastHeartbeatTime(), DateUtils.DATE_TIME_FORMAT));
            workerInfo.setPort(request.getPort());
            workerInfo.setZkdirectory(request.getZkdirectory());
            workerInfo.setWorkerType(request.getWorkerType());
        }
    }

    public void insertOrUpdateGroup(String groupName) throws Exception {
        String groupPath = GlobalConstants.WORKER_GROUP + "/" + groupName;
        synchronized(groups) {
            if (groups.get(groupPath) == null) {
                Properties prop = Configuration.getConfig();
                String quorum = prop.getProperty("thales.zookeeper.quorum");
                int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                        GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
                int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                        GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
                CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);

                CuratorHelper.createNodeIfNotExist(client, groupPath, CreateMode.PERSISTENT, null);
                groups.put(groupPath, new ArrayList<String>());
                addNodeChangeListener(client, groupPath);
            } 
        }
    }

    @SuppressWarnings({ "resource", "deprecation" })
    private void addNodeChangeListener(CuratorFramework client, final String groupPath) {
        PathChildrenCache pcCache = new PathChildrenCache(client, groupPath, true);
        try {
            pcCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            pcCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent)
                        throws Exception {
                    switch (pathChildrenCacheEvent.getType()) {
                    case CHILD_ADDED:
                        String addedPath = pathChildrenCacheEvent.getData().getPath();
                        LOG.info("added node" + addedPath);
                        String addedHost = addedPath.substring(addedPath.lastIndexOf("/") + 1);
                        groups.get(groupPath).add(addedHost);
                        taskMap.put(addedHost, 0);
                        break;
                    case CHILD_REMOVED:
                        String removedPath = pathChildrenCacheEvent.getData().getPath();
                        LOG.info("removed node" + removedPath);
                        String removedHost = removedPath.substring(removedPath.lastIndexOf("/") + 1);
                        groups.get(groupPath).remove(removedHost);
                        taskMap.remove(removedHost);
                        releaseResource(groupPath, Arrays.asList(removedHost));
                        break;
                    case CHILD_UPDATED:
                        String udpatedPath = pathChildrenCacheEvent.getData().getPath();
                        byte[] bytes = CuratorHelper.getData(curatorFramework, udpatedPath);
                        WorkerRequest request = (WorkerRequest) ByteUtils.byteArrayToObject(bytes);
                        updateResource(request);
                    default:
                        break;
                    }
                }
            });
        } catch (Exception e) {
            LOG.error(e);
        }
    }
    
    @SuppressWarnings({ "resource", "deprecation" })
    private void addGroupChangeListener(CuratorFramework client, final String groupPath) {
        PathChildrenCache pcCache = new PathChildrenCache(client, groupPath, true);
        try {
            pcCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            pcCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent)
                        throws Exception {
                    switch (pathChildrenCacheEvent.getType()) {
                    case CHILD_ADDED:
                        String addedPath = pathChildrenCacheEvent.getData().getPath();
                        byte[] addedBytes = CuratorHelper.getData(client, addedPath);
                        String addedValue = new String(addedBytes);
                        String addGroupName = addedPath.substring(addedPath.lastIndexOf("/") + 1);
                        workerGroupMap.put(addGroupName, addedValue);
                        break;
                    case CHILD_REMOVED:
                        String removedPath = pathChildrenCacheEvent.getData().getPath();
                        LOG.info("removed node" + removedPath);
                        String removeGroupName = removedPath.substring(removedPath.lastIndexOf("/") + 1);
                        workerGroupMap.remove(removeGroupName);
                        break;
                    case CHILD_UPDATED:
                        String udpatedPath = pathChildrenCacheEvent.getData().getPath();
                        byte[] updateBytes = CuratorHelper.getData(curatorFramework, udpatedPath);
                        String updateValue = new String(updateBytes);
                        String updateGroupName = udpatedPath.substring(udpatedPath.lastIndexOf("/") + 1);
                        workerGroupMap.put(updateGroupName, new String(updateValue));
                    default:
                        break;
                    }
                }
            });
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * increase task number of worker
     * 
     * @param serverName
     * @param taskMap
     */
    public void increaseTask(String serverName) {
        synchronized (taskMap) {
            if (taskMap.get(serverName) != null) {
                taskMap.put(serverName, taskMap.get(serverName) + 1);
            } else {
                taskMap.put(serverName, 1);
            }
        }
    }

    /**
     * decrease task number of worker
     * 
     * @param serverName
     * @param taskMap
     */
    public void decreaseTask(String serverName) {
        synchronized (taskMap) {
            if (taskMap.get(serverName) != null) {
                taskMap.put(serverName, taskMap.get(serverName) - 1);
            }
        }
    }

    /**
     * choose idle worker from worker group according to strategy
     * 
     * @param input
     * @param lastFailedHosts
     * @return
     * @throws Exception 
     */
    public synchronized WorkerResponse getIdleWorker(String groupName, String... lastFailedWorkers) throws Exception {
//        String workerStrategy = GroupStrategyManager.getGroupStrategy(groupName).getGroupStrategy();
        String groupStrategy = workerGroupMap.get(groupName);
        WorkerSelectStrategy workerSelectStrategy = ResourceStrategy
                .getStrategy(JobStrategy.getJobStrategyByName(groupStrategy),groupName);
        return new ResourceStrategyContext(workerSelectStrategy).select(this, groupName, lastFailedWorkers);

    }

    public void addGroup(String groupName) {
        groups.put(groupName, new ArrayList<String>());
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }

    public Map<String, WorkerResponse> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, WorkerResponse> resourceMap) {
        this.resourceMap = resourceMap;
    }

    public Map<String, Integer> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<String, Integer> taskMap) {
        this.taskMap = taskMap;
    }

    public String getActiveMaster() {
        return this.activeMaster;
    }

    public MasterApiServer getJettyServer() {
        return jettyServer;
    }

    public void setJettyServer(MasterApiServer jettyServer) {
        this.jettyServer = jettyServer;
    }

    public MasterRpcServer getServer() {
        return server;
    }

    public void setServer(MasterRpcServer server) {
        this.server = server;
    }

    public MasterJmxServer getAgent() {
        return agent;
    }

    public void setAgent(MasterJmxServer agent) {
        this.agent = agent;
    }
}