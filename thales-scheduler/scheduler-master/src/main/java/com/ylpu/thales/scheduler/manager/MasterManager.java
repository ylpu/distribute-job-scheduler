package com.ylpu.thales.scheduler.manager;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rest.WorkerManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.core.zk.CuratorHelper;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.jmx.MasterJmxServer;
import com.ylpu.thales.scheduler.manager.strategy.JobStrategy;
import com.ylpu.thales.scheduler.manager.strategy.ResourceStrategy;
import com.ylpu.thales.scheduler.manager.strategy.ResourceStrategyContext;
import com.ylpu.thales.scheduler.manager.strategy.WorkerSelectStrategy;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.rest.MasterRestServer;
import com.ylpu.thales.scheduler.rpc.client.JobCallBackScan;
import com.ylpu.thales.scheduler.rpc.server.MasterRpcServer;
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
import java.util.concurrent.ConcurrentHashMap;
import org.apache.curator.framework.recipes.cache.*;

public class MasterManager{
    
    private static Log LOG = LogFactory.getLog(MasterManager.class);   
    
    private static MasterManager resourceManager = new MasterManager(); 
     
    public static final int DEFAULT_MASTER_SERVER_PORT = 9091;
    public static final int DEFAULT_JMX_PORT = 9095;
    
    //key is worker group path,value is server list
    private Map<String,List<String>> groups = new ConcurrentHashMap<String,List<String>>(); 
    
    //key is hostname,value is host info
    private Map<String,WorkerResponse> resourceMap = new HashMap<String,WorkerResponse>();   
    
    //key is hostname,value is tasknumbers
    private Map<String,Integer> taskMap = new HashMap<String,Integer>();
    
    private String activeMaster;
    
    private MasterRestServer jettyServer = null;
    
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
    public void init() throws Exception{
        Properties prop = new Properties();
        prop.put("thales.zookeeper.quorum", GlobalConstants.DEFAULT_ZKQUORUM);
        prop.put("thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        prop.put("thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        init(prop);
    }
    
    /**
     * 竞选节点为active master
     */    
    public void init(Properties prop) throws Exception{
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        CuratorHelper.createNodeIfNotExist(client,  GlobalConstants.ROOT_GROUP, CreateMode.PERSISTENT, null);
        CuratorHelper.createNodeIfNotExist(client,  GlobalConstants.MASTER_GROUP, CreateMode.PERSISTENT, null);
        CuratorHelper.createNodeIfNotExist(client,  GlobalConstants.WORKER_GROUP, CreateMode.PERSISTENT, null);
       
        new MyLeaderSelectorListenerAdapter(client,GlobalConstants.MASTER_LOCK,prop).start();
    }
    
    private class MyLeaderSelectorListenerAdapter extends LeaderSelectorListenerAdapter{
    	
        private final LeaderSelector leaderSelector;
        
        private Properties prop = null;
        
        public MyLeaderSelectorListenerAdapter(CuratorFramework client, String path,Properties prop){
        	
        	this.prop = prop;

        	leaderSelector = new LeaderSelector(client, path, this);

        }
        
        public void start() {
        	
        	//保证在此实例释放领导权之后还可能获得领导权。
        	leaderSelector.autoRequeue();
        	
        	leaderSelector.start();
        }
        
        public void close(){
        	
        	leaderSelector.close();
        }

    	public void takeLeadership(CuratorFramework client) throws Exception{
            int masterServerPort = Configuration.getInt(prop,"thales.master.server.port",DEFAULT_MASTER_SERVER_PORT);
    		activeMaster = MetricsUtils.getHostIpAddress() + ":" + masterServerPort;
    		String masterPath = GlobalConstants.MASTER_GROUP + "/" + activeMaster;
    		LOG.info("active master is " + activeMaster);
    		CuratorHelper.createNodeIfNotExist(client, masterPath, CreateMode.EPHEMERAL, null);
    		MasterManager.getInstance().init(GlobalConstants.WORKER_GROUP, prop);
    	}
      }
    
    public void init(String workerGroup,Properties prop) throws Exception{
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        int masterServerPort = Configuration.getInt(prop,"thales.master.server.port",DEFAULT_MASTER_SERVER_PORT);
        
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> groups = CuratorHelper.getChildren(client,workerGroup);
        
        if(groups != null && groups.size() > 0) {
            for(String groupName : groups) {
                String groupPath = workerGroup + "/" + groupName;
                initGroup(client,groupPath);
                addNodeChangeListener(client,groupPath);  
            }
        }
//      加载任务实例状态，比较耗时
        restoreTaskState();
//      调度所有任务
        JobScheduler.startJobs();
//      启动master服务
        jettyServer = new MasterRestServer(prop);
        jettyServer.startJettyServer();
//      启动任务状态检查线程
        JobCallBackScan.start();
//      初始化每台机器运行的任务个数,供监控使用
        initTaskCount();
//      启动jmx服务
        agent = new MasterJmxServer(Configuration.getInt(prop, "thales.master.jmx.port", DEFAULT_JMX_PORT));
        agent.start();
//      启动master rpc服务
        server = new MasterRpcServer(masterServerPort);
        server.start();
        server.blockUntilShutdown();
    }

    private void initGroup(CuratorFramework client,String groupPath) throws Exception {
        List<String> workers = groups.get(groupPath);
        if(workers == null) {
            workers = new ArrayList<String>();
            groups.put(groupPath, workers);
        }
    }
    
    private void addNodeChangeListener(CuratorFramework client,final String groupPath) {
    	PathChildrenCache pcCache = new PathChildrenCache(client,groupPath,true);
		try {
			pcCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
			pcCache.getListenable().addListener(new PathChildrenCacheListener() {
				public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent)
						throws Exception {
					switch (pathChildrenCacheEvent.getType()){
					case CHILD_ADDED:
						LOG.info("add node" + pathChildrenCacheEvent.getData().getPath());
						String addedNodeData = new String(CuratorHelper.getData(client, pathChildrenCacheEvent.getData().getPath()));
						groups.get(groupPath).add(addedNodeData);
						break;
					case CHILD_REMOVED:
						LOG.info("remove node"+pathChildrenCacheEvent.getData().getPath());
						String removedNodeData = new String(CuratorHelper.getData(client, pathChildrenCacheEvent.getData().getPath()));
						groups.get(groupPath).remove(removedNodeData);
						releaseResource(groupPath,Arrays.asList(removedNodeData));
						break;
					default:
						break;
					}
				}
			});
		} catch (Exception e) {
			LOG.error(e);
		}
    } 
    
    private void initTaskCount() {
        synchronized(taskMap) {
            List<Map<String, Object>> list = WorkerManager.getTaskCountByWorker();
            for(Map<String, Object> map : list) {
                Object worker = map.get("worker");
                if(worker != null) {
                    taskMap.put(map.get("worker").toString().split(":")[0], NumberUtils.toInt(String.valueOf(map.get("cnt"))));
                }
            }
        }
    }
    
    /**
     *目前任务状态都保存在mysql中，master在启动的时候需要从mysql中恢复任务状态
     */
    private void restoreTaskState() {
        JobInstanceResponseRpc responseRpc = null;
        List<JobInstanceStateResponse> list = JobManager.getAllJobStatus();
        if(list != null && list.size() > 0) {
            for(JobInstanceStateResponse response : list) {
                String responseId = response.getJobId() + "-" + DateUtils.getDateAsString(response.getScheduleTime(),DateUtils.TIME_FORMAT);
                 responseRpc = JobInstanceResponseRpc.newBuilder()
                        .setId(response.getId())
                        .setResponseId(responseId)
                        .setTaskState(response.getTaskState())
                        .build();
                 JobCallBackScan.addResponse(responseRpc);
            }
        }
    }
    
    private synchronized void releaseResource(String groupPath,List<String> disconnectedChildren) {
        if(disconnectedChildren != null && disconnectedChildren.size() > 0) {
            for(String child : disconnectedChildren) {
                resourceMap.remove(child);                
            }
        }
        String workerGroup = groupPath.substring(groupPath.lastIndexOf("/") + 1);
        WorkerGroupRequest param = new WorkerGroupRequest();
        param.setGroupName(workerGroup);
        param.setStatus(WorkerStatus.REMOVED);
        param.setWorkers(disconnectedChildren);
        WorkerManager.updateWorkersStatusByGroup(param);
    }

    /**
     * 根据心跳更新资源信息
     * @param serverName
     * @param resourceParams
     */
    public void updateResource(WorkerRequest request) {
        synchronized(resourceMap){
            WorkerResponse workerInfo = resourceMap.get(request.getHost());
            if(workerInfo == null) {
                workerInfo = new WorkerResponse();
                resourceMap.put(request.getHost(),workerInfo);
            }
            workerInfo.setHost(request.getHost());
            workerInfo.setCpuUsage(request.getCpuUsage());
            workerInfo.setMemoryUsage(request.getMemoryUsage());
            workerInfo.setNodeStatus(request.getNodeStatus());
            workerInfo.setLastHeartbeatTime(request.getLastHeartbeatTime());
            workerInfo.setPort(request.getPort());
            workerInfo.setZkdirectory(request.getZkdirectory());
            workerInfo.setNodeType(request.getNodeType());
            workerInfo.setNodeStatus(request.getNodeStatus());
        }
    }
    
    /**
     * 开始处理任务
     * @param serverName
     * @param taskMap
     */
    public void increaseTask(String serverName) {
        synchronized(taskMap){
            if(taskMap.get(serverName) != null) {
                taskMap.put(serverName, taskMap.get(serverName) + 1);
            }else {
                taskMap.put(serverName, 0);
            }
        }
    }
    
    /**
     * 处理完任务
     * @param serverName
     * @param taskMap
     */
    public void decreaseTask(String serverName) {
        synchronized(taskMap){
            if(taskMap.get(serverName) != null) {
                taskMap.put(serverName, taskMap.get(serverName) - 1);
            }
        }
    }
    /**
     * 根据策略获取worker group中的空闲机器 
     * @param input
     * @param lastFailedHosts
     * @return
     */
    public synchronized WorkerResponse getIdleWorker(String groupName,String... lastFailedWorkers) {
        String workerStrategy = Configuration.getString(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                "thales.scheduler.worker.strategy",GlobalConstants.DEFAULT_WORKER_STRATEGY);
        WorkerSelectStrategy workerSelectStrategy = ResourceStrategy.getStrategy(
                JobStrategy.getJobStrategyByName(workerStrategy));
        return new ResourceStrategyContext(workerSelectStrategy).select(this,groupName,lastFailedWorkers);

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

    public MasterRestServer getJettyServer() {
        return jettyServer;
    }

    public void setJettyServer(MasterRestServer jettyServer) {
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