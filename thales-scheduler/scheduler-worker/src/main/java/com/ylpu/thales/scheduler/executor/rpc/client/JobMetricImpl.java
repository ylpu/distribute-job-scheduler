package com.ylpu.thales.scheduler.executor.rpc.client;

import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.curator.CuratorHelper;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.worker.WorkerServer;

/**
 * 增加或减少任务个数，用于worker选择策略
 *
 */
public class JobMetricImpl implements IJobMetric {

    private static Log LOG = LogFactory.getLog(JobMetricImpl.class);
    
    private static final int RETRY_COUNT = 3;
    
    private static final long RETRY_INTERVAL = 3000;
    
    public static final String INC_TASK = "INC";
    
    public static final String DEC_TASK = "DEC";

    public void incOrDecTaskNumber(String master,String methodName) {
        WorkerGrpcClient client = null;
        int workerServerPort = WorkerServer.workerServerPort;
        int i = 1;
        while (i <= RETRY_COUNT) {
            try {
                if (StringUtils.isNoneBlank(master)) {
                    String[] hostAndPort = master.split(":");
                    if(hostAndPort != null && hostAndPort.length == 2) {
                        WorkerParameter parameter = WorkerParameter.newBuilder().setHostname(MetricsUtils.getHostName() + ":" + workerServerPort)
                                .build();
                        client = new WorkerGrpcClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
                        if(methodName.equalsIgnoreCase(INC_TASK)) {
                            client.incTask(parameter);
                        } else if (methodName.equalsIgnoreCase(DEC_TASK)) {
                            client.decTask(parameter);
                        } else {
                            LOG.warn("does not support method " + methodName);
                        }
                        break; 
                    }
                }
            } catch (Exception e) {
                if(methodName.equalsIgnoreCase(INC_TASK)) {
                    LOG.error("fail to inc task for worker " + WorkerServer.hostname + ":" + WorkerServer.workerServerPort + " " + i + 
                            " times with exception " + e.getMessage() + ",cause is " + e.getCause());
                } else if (methodName.equalsIgnoreCase(DEC_TASK)) {
                    LOG.error("fail to dec task for worker " + WorkerServer.hostname + ":" + WorkerServer.workerServerPort + " " + i +
                            " times with exception " + e.getMessage() + ",cause is " + e.getCause());
                }
            } finally {
                if (client != null) {
                    try {
                        client.shutdown();
                    } catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
            }
            i++;
            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e1) {
                LOG.error(e1);
            }
            try {
                master = getActiveMaster();
            } catch (Exception e) {
                LOG.error("fail to get master with exception " + e.getMessage());
                master = "";
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
                throw new RuntimeException("master is empty");
            }
            return masters.get(0);
        } catch(Exception e) {
            throw new RuntimeException("fail to get master with exception " + e.getMessage());
        }finally {
            CuratorHelper.close(client);
        }
    }
}
