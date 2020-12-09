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
    
    private Map<String, String> groupStrategyMap = new HashMap<String, String>();

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

    /**
     * 竞选节点为active resource manager
     */
    public void init() throws Exception {
        Properties prop = new Properties();
        prop.put("thales.zookeeper.quorum", GlobalConstants.DEFAULT_ZKQUORUM);
        prop.put("thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        prop.put("thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        init(prop);
    }

    /**
     * 竞选节点为active master
     */
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

            // 保证在此实例释放领导权之后还可能获得领导权。
            leaderSelector.autoRequeue();

            leaderSelector.start();
        }

        public void takeLeadership(CuratorFramework client) throws Exception {
            // 另外一个master有可能出现假死的情况，首先删除节点，其次强制杀掉进程
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
            // 启动master rpc服务
            server = new MasterRpcServer(masterServerPort);
            server.start();
            //当前节点竞选成为active节点
            activeMaster = MetricsUtils.getHostName() + ":" + masterServerPort;
            String masterPath = GlobalConstants.MASTER_GROUP + "/" + activeMaster;
            LOG.info("active master is " + activeMaster);
            CuratorHelper.createNodeIfNotExist(client, masterPath, CreateMode.PERSISTENT, null);
            
            server.blockUntilShutdown();
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
        List<String> strategyList = CuratorHelper.getChildren(client, GlobalConstants.STRATEGY_GROUP);
        if (strategyList != null && strategyList.size() > 0) {
            for (String groupName : strategyList) {
                byte[] bytes = CuratorHelper.getData(strategyClient, GlobalConstants.STRATEGY_GROUP + "/" + groupName);
                groupStrategyMap.put(groupName, new String(bytes));
            }
            addStrategyChangeListener(client, GlobalConstants.STRATEGY_GROUP);
        }
        //设置所有的worker为removed
//        WorkerGroupRequest param = new WorkerGroupRequest();
//        param.setStatus(WorkerStatus.REMOVED);
//        WorkerManager.updateWorkersStatus(param);
//        恢复任务状态，比较耗时
//        restoreTaskState();
//      启动master http service
        jettyServer = new MasterApiServer(prop);
        jettyServer.startJettyServer();
        // 启动任务状态检查线程
        JobStatusChecker.start();
        // 标识以前的任务状态为失败
        JobManager.markStatus();
        // 加载任务实例状态，比较耗时
        restoreTaskState();
        // 调度所有任务
        JobScheduler.startJobs();
        // 初始化每台机器运行的任务个数,供监控使用
        initTaskCount();
        // 启动jmx服务
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
                    taskMap.put(map.get("worker").toString().split(":")[0],
                            NumberUtils.toInt(String.valueOf(map.get("cnt"))));
                }
            }
        }
    }

    /**
     * 目前任务状态都保存在mysql中，master在启动的时候需要从mysql中恢复任务状态, 对于非最终状态和非running状态的任务需要再次提交。
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
     * 加载历史任务状态（最近1个月的），比较耗时，后面改成分页方式加载
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
     * 根据心跳更新资源信息
     * 
     * @param serverName
     * @param resourceParams
     */
    public void updateResource(WorkerRequest request) {
        synchronized (resourceMap) {
            WorkerResponse workerInfo = resourceMap.get(request.getHost());
            if (workerInfo == null) {
                workerInfo = new WorkerResponse();
                resourceMap.put(request.getHost(), workerInfo);
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
                        String addedIp = addedPath.substring(addedPath.lastIndexOf("/") + 1);
                        groups.get(groupPath).add(addedIp);
                        break;
                    case CHILD_REMOVED:
                        String removedPath = pathChildrenCacheEvent.getData().getPath();
                        LOG.info("removed node" + removedPath);
                        String removedIp = removedPath.substring(removedPath.lastIndexOf("/") + 1);
                        groups.get(groupPath).remove(removedIp);
                        releaseResource(groupPath, Arrays.asList(removedIp));
                        break;
                    case CHILD_UPDATED:
                        String udpatedPath = pathChildrenCacheEvent.getData().getPath();
                        byte[] bytes = CuratorHelper.getData(curatorFramework, udpatedPath);
                        WorkerRequest request = (WorkerRequest) ByteUtils.byteArrayToObject(bytes);
//                        WorkerManager.insertOrUpdateWorker(request);
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
    private void addStrategyChangeListener(CuratorFramework client, final String groupPath) {
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
                        groupStrategyMap.put(addGroupName, addedValue);
                        break;
                    case CHILD_REMOVED:
                        String removedPath = pathChildrenCacheEvent.getData().getPath();
                        LOG.info("removed node" + removedPath);
                        String removeGroupName = removedPath.substring(removedPath.lastIndexOf("/") + 1);
                        groupStrategyMap.remove(removeGroupName);
                        break;
                    case CHILD_UPDATED:
                        String udpatedPath = pathChildrenCacheEvent.getData().getPath();
                        byte[] updateBytes = CuratorHelper.getData(curatorFramework, udpatedPath);
                        String updateValue = new String(updateBytes);
                        String updateGroupName = udpatedPath.substring(udpatedPath.lastIndexOf("/") + 1);
                        groupStrategyMap.put(updateGroupName, new String(updateValue));
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
     * 开始处理任务
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
     * 处理完任务
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
     * 根据策略获取worker group中的空闲机器
     * 
     * @param input
     * @param lastFailedHosts
     * @return
     * @throws Exception 
     */
    public synchronized WorkerResponse getIdleWorker(String groupName, String... lastFailedWorkers) throws Exception {
//        String workerStrategy = GroupStrategyManager.getGroupStrategy(groupName).getGroupStrategy();
        String workerStrategy = groupStrategyMap.get(groupName);
        WorkerSelectStrategy workerSelectStrategy = ResourceStrategy
                .getStrategy(JobStrategy.getJobStrategyByName(workerStrategy));
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