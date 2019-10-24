package com.ylpu.thales.scheduler.manager;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rest.WorkerManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.core.zk.ZKHelper;
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

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        String workerGroup = GlobalConstants.WORKER_GROUP;
        String masterGroup = GlobalConstants.MASTER_GROUP;
        int masterServerPort = Configuration.getInt(prop,"thales.master.server.port",DEFAULT_MASTER_SERVER_PORT);
        
        ZkClient zkClient = ZKHelper.getClient(quorum,sessionTimeout,connectionTimeout);
        List<String> root = zkClient.getChildren("/");
        if(root == null || !root.contains(GlobalConstants.THALES)) {
            ZKHelper.createNode(zkClient, GlobalConstants.ROOT_GROUP, null);
        }
        List<String> masters = zkClient.getChildren(GlobalConstants.ROOT_GROUP);
        if(masters == null || !masters.contains(GlobalConstants.MASTERS)) {
           ZKHelper.createNode(zkClient, masterGroup, null);        	
        }
        List<String> workers = zkClient.getChildren(GlobalConstants.ROOT_GROUP);
        if(masters == null || !workers.contains(GlobalConstants.WORKERS)) {
           ZKHelper.createNode(zkClient, workerGroup, null);            
        }
        List<String> masterNodes = zkClient.getChildren(masterGroup);
        if(masterNodes == null || masterNodes.size() == 0) {
           activeMaster = MetricsUtils.getHostIpAddress() + ":" + masterServerPort;
           ZKHelper.createEphemeralNode(zkClient, masterGroup + "/" + activeMaster, null);
           init(workerGroup,prop);
        }else {
           zkClient.subscribeChildChanges(masterGroup, new IZkChildListener() {              
                public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception { 
                    LOG.warn(String.format("[ZookeeperRegistry] service list change: path=%s, currentChildren=%s at %s",
                            parentPath, currentChildren.toString(),DateUtils.getDateAsString(new Date(),DateUtils.DATE_TIME_FORMAT)));                    
                    try {
                        if(currentChildren == null || currentChildren.size() == 0) {
                            activeMaster = MetricsUtils.getHostIpAddress() + ":" + masterServerPort;
                            ZKHelper.createEphemeralNode(zkClient, parentPath + "/" + activeMaster, null); 
                            init(workerGroup,prop); 
                        }
                    }catch(Exception e) {
                    	    LOG.error(MessageFormat.format("{0} can not compain as an active master with exception {1}", MetricsUtils.getHostIpAddress(),e.getMessage()));
                    }
                }  
            });  
        }
    }
    
    public void init(String workerGroup,Properties prop) throws Exception{
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        int masterServerPort = Configuration.getInt(prop,"thales.master.server.port",DEFAULT_MASTER_SERVER_PORT);
        ZkClient zkClient = ZKHelper.getClient(quorum,sessionTimeout,connectionTimeout);
        List<String> groups = zkClient.getChildren(workerGroup);
        if(groups != null && groups.size() > 0) {
            for(String groupName : groups) {
                String groupPath = workerGroup + "/" + groupName;
                initGroup(zkClient,groupPath);
                addNodeChangeListener(zkClient,groupPath);  
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

    private void initGroup(ZkClient zkClient,String groupPath) {
        List<String> workers = groups.get(groupPath);
        if(workers == null) {
            workers = new ArrayList<String>();
            groups.put(groupPath, workers);
        }
        List<String> children = zkClient.getChildren(groupPath);
        if(children != null && children.size() > 0) {
            for(String worker : children) { 
                workers.add(worker);  
            }
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
    
    /**
     * zk中节点有变动更新pool中机器
     * @param zkClient
     * @param poolPath
     */
    public void addNodeChangeListener(final ZkClient zkClient,final String groupPath) {
        List<String> oldChildren = new ArrayList<String>();
        List<String> children = zkClient.getChildren(groupPath);        
        if(children != null && children.size() > 0) {
            for(String childPath : children) {
                oldChildren.add(childPath);
            }
        }
        zkClient.subscribeChildChanges(groupPath, new IZkChildListener() {              
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception { 
                LOG.warn(String.format("[ZookeeperRegistry] service list change: path=%s, currentChildren=%s",
                        parentPath, currentChildren.toString())); 
                refreshGroup(zkClient,parentPath,oldChildren,currentChildren); 
                resetOldChild(oldChildren,currentChildren);
            }  
        });          
    }  
    /**
     * 把新的节点付给老的节点
     * @param oldChildren
     * @param currentChildren
     */
    private void resetOldChild(List<String> oldChildren,List<String> currentChildren) {
        oldChildren.clear();
        if(currentChildren != null && currentChildren.size() > 0) {
            for(String child : currentChildren) {
                oldChildren.add(child);
            }
        }
    }
    
    /**
     * 刷新pool
     * @param zkClient
     * @param groupPath
     * @param oldChildren
     * @param currentChildren
     */
    public synchronized void refreshGroup(ZkClient zkClient,String groupPath,List<String> oldChildren,List<String> currentChildren) { 
        groups.put(groupPath, currentChildren);
        List<String> disconnectedChildren = getRemovedChildren(oldChildren,currentChildren);
        releaseResource(groupPath,disconnectedChildren,WorkerStatus.REMOVED);
    }
    
    private List<String> getRemovedChildren(List<String> oldChildren,List<String> currentChildren){
        return oldChildren.stream().filter(t-> !currentChildren.contains(t)).collect(Collectors.toList());
    }
    
    private synchronized void releaseResource(String groupPath,List<String> disconnectedChildren,WorkerStatus status) {
        if(disconnectedChildren != null && disconnectedChildren.size() > 0) {
            for(String child : disconnectedChildren) {
                resourceMap.remove(child);                
            }
        }
        String workerGroup = groupPath.substring(groupPath.lastIndexOf("/") + 1);
        WorkerGroupRequest param = new WorkerGroupRequest();
        param.setGroupName(workerGroup);
        param.setStatus(status);
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