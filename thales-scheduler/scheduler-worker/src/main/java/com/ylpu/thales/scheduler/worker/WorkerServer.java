package com.ylpu.thales.scheduler.worker;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.curator.CuratorHelper;
import com.ylpu.thales.scheduler.core.rest.GroupStrategyManager;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc;
import com.ylpu.thales.scheduler.core.utils.ByteUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.enums.NodeType;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.executor.log.LogServer;
import com.ylpu.thales.scheduler.executor.rpc.client.WorkerGrpcClient;
import com.ylpu.thales.scheduler.executor.rpc.server.WorkerRpcServer;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class WorkerServer {

    private static Log LOG = LogFactory.getLog(WorkerServer.class);

    public static int DEFAULT_WORKER_SERVER_PORT = 8090;

    private WorkerRpcServer rpcServer;

    private LogServer logServer;

    private ZkClient zkClient;

    public static final String DEFAULT_WORKER_GROUP = "default";

    public static final long WORKER_HEARTBEAT_INTERVAL = 3000;

    private static volatile boolean stop = false;
    
    public static int workerServerPort;

    public void start() {
        Properties prop = Configuration.getConfig();
        workerServerPort = MetricsUtils.getAvailablePort(Configuration.getInt(prop, "thales.worker.server.port", DEFAULT_WORKER_SERVER_PORT));
        long heartBeatInterval = Configuration.getLong(prop, "thales.worker.heartbeat.interval",
                WORKER_HEARTBEAT_INTERVAL);
        try {
            String workerGroup = Configuration.getString(prop, "thales.worker.group", DEFAULT_WORKER_GROUP);
            if(isValidateGroup(workerGroup)) {
                Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());
                // 注册自己到zk
                String quorum = prop.getProperty("thales.zookeeper.quorum");
                int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                        GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
                int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                        GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
                String workerPath = regist(quorum,sessionTimeout,connectionTimeout,workerGroup,workerServerPort);
                // 启动心跳线程
                WorkerHeartBeatThread heartBeatThread = new WorkerHeartBeatThread(workerPath, workerServerPort,
                        heartBeatInterval);
                heartBeatThread.setDaemon(true);
                heartBeatThread.start();
                // 启动日志服务
                logServer = new LogServer(prop);
                logServer.startLogServer();
                // 启动rpc服务
                rpcServer = new WorkerRpcServer(workerServerPort);
                rpcServer.startServer();
                rpcServer.blockUntilShutdown();
            }
        } catch (Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }


    private String regist(String quorum, int sessionTimeout, int connectionTimeout,String workerGroup,int workerServerPort) throws Exception {

        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        CuratorHelper.createNodeIfNotExist(client, GlobalConstants.ROOT_GROUP, CreateMode.PERSISTENT, null);
        CuratorHelper.createNodeIfNotExist(client, GlobalConstants.WORKER_GROUP, CreateMode.PERSISTENT, null);

        insertOrUpdateGroup(workerGroup);

        String hostname = MetricsUtils.getHostName();
        String workerPath = GlobalConstants.WORKER_GROUP + "/" + workerGroup + "/" + hostname + ":" + workerServerPort;
        CuratorHelper.createNodeIfNotExist(client, workerPath, CreateMode.EPHEMERAL, null);
        return workerPath;
    }

    private void insertOrUpdateGroup(String workerGroup) throws Exception {
        String master;
        WorkerGrpcClient client = null;
        try {
            master = getActiveMaster();
            String[] masters = master.split(":");
            client = new WorkerGrpcClient(masters[0], NumberUtils.toInt(masters[1]));
            WorkerRequestRpc request = WorkerRequestRpc.newBuilder().setWorkerGroup(workerGroup).build();
            client.insertOrUpdateGroup(request);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
    
    private String getActiveMaster() throws Exception {
        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = null;
        List<String> masters = null;
        try {
            client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
            masters = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
            if (masters == null || masters.size() == 0) {
                throw new RuntimeException("can not get active master");
            }
        } finally {
            CuratorHelper.close(client);
        }
        return masters.get(0);
    }

    public void stopHeartBeat() {
        stop = true;
    }
    
    private boolean isValidateGroup(String groupName) throws Exception {
        GroupStrategyResponse groupStrategy = GroupStrategyManager.getGroupStrategy(groupName);
        if(groupStrategy == null) {
            return false;
        }
        return true;
    }

    private static class WorkerHeartBeatThread extends Thread {
        private String workerPath;
        private int workerPort;
        private long heartBeatInterval;

        public WorkerHeartBeatThread(String workerPath, int workerPort, long heartBeatInterval) {
            this.workerPath = workerPath;
            this.workerPort = workerPort;
            this.heartBeatInterval = heartBeatInterval;
        }

        public void run() {
            CuratorFramework client = null;
            boolean isFirstReport = true;
            int nodeStatus = WorkerStatus.ADDED.getCode();

            Properties prop = Configuration.getConfig();
            String quorum = prop.getProperty("thales.zookeeper.quorum");
            int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                    GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
            int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                    GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
            String workerGroup = Configuration.getString(prop, "thales.worker.group", DEFAULT_WORKER_GROUP);
            while (!stop) {
                try {
                    if (isFirstReport) {
                        nodeStatus = WorkerStatus.ADDED.getCode();
                        isFirstReport = false;
                    } else {
                        nodeStatus = WorkerStatus.UPDATED.getCode();
                    }
                    client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
                    WorkerRequest workerRequest = new WorkerRequest();
                    workerRequest.setHost(MetricsUtils.getHostName());
                    workerRequest.setCpuUsage(MetricsUtils.getCpuUsage());
                    workerRequest.setMemoryUsage(MetricsUtils.getMemoryUsage());
                    workerRequest.setWorkerGroup(workerGroup);
                    workerRequest.setWorkerStatus(nodeStatus);
                    workerRequest.setWorkerType(NodeType.WORKER.getCode());
                    workerRequest.setPort(workerPort);
                    workerRequest.setZkdirectory(workerPath);
                    workerRequest.setLastHeartbeatTime(new Date());
                    CuratorHelper.setData(client, workerPath, ByteUtils.objectToByteArray(workerRequest));
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
    
    private class ShutDownHookThread extends Thread {
        @Override
        public void run() {
            if (logServer != null) {
                LOG.warn("*** shutting down log server since JVM is shutting down");
                logServer.stop();
                LOG.warn("*** log server shut down");
            }
            if (rpcServer != null) {
                LOG.warn("*** shutting down gRPC server since JVM is shutting down");
                rpcServer.shutdownNow();
                LOG.warn("*** rpc server shut down");
            }
            if (zkClient != null) {
                LOG.warn("*** close zkClient since JVM is shutting down");
                zkClient.close();
                LOG.warn("*** close zkclient");
            }
            LOG.warn("*** stop heartbeat since JVM is shutting down");
            stopHeartBeat();
        }
    }
    
    public static void main(String[] args) {
        WorkerServer jobExecutorServer = new WorkerServer();
        jobExecutorServer.start();
    }
}