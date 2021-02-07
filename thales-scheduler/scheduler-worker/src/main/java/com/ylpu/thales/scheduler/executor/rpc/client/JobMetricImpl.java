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
    
    private static final long RETRY_INTERVAL = 30000;

    public void increaseTask() {
        WorkerGrpcClient client = null;
        int workerServerPort = WorkerServer.workerServerPort;
        int i = 0;
        while (i < RETRY_COUNT) {
            try {
                String master = getActiveMaster();
                if (StringUtils.isNoneBlank(master)) {
                    String[] hostAndPort = master.split(":");
                    WorkerParameter parameter = WorkerParameter.newBuilder().setHostname(MetricsUtils.getHostName() + ":" + workerServerPort)
                            .build();
                    client = new WorkerGrpcClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
                    client.incTask(parameter);
                    break;
                }
            } catch (Exception e) {
                LOG.error(e);
                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException e1) {
                    LOG.error(e1);
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
        }
    }

    public void decreaseTask() {
        WorkerGrpcClient client = null;
        int workerServerPort = WorkerServer.workerServerPort;
        int i = 0;
        while (i < RETRY_COUNT) {
            try {
                String master = getActiveMaster();
                if (StringUtils.isNoneBlank(master)) {
                    String[] hostAndPort = master.split(":");
                    WorkerParameter parameter = WorkerParameter.newBuilder().setHostname(MetricsUtils.getHostName() + ":" + workerServerPort)
                            .build();
                    client = new WorkerGrpcClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
                    client.decTask(parameter);
                    break;
                }
            } catch (Exception e) {
                LOG.error(e);
                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException e1) {
                    LOG.error(e1);
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
}
