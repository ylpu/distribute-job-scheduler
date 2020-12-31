package com.ylpu.thales.scheduler.master.server;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.curator.CuratorHelper;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.master.schedule.JobScheduler;
import com.ylpu.thales.scheduler.master.schedule.JobSubmission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterServer {
    /**
     * @param port
     * @throws Exception
     */
    private static Log LOG = LogFactory.getLog(MasterServer.class);

    public static void main(String[] args) {
        Properties prop = Configuration.getConfig();
        try {
            Runtime.getRuntime().addShutdownHook(new ShutDownHookThread(prop));
            MasterManager.getInstance().init(prop);
            TimeUnit.DAYS.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    private static class ShutDownHookThread extends Thread {

        private Properties prop;

        public ShutDownHookThread(Properties prop) {
            this.prop = prop;
        }

        @Override
        public void run() {
            // delete zk master node
            removeMaster();
            // stop jobs scheduler
            JobScheduler.shutdownJobs();
            // stop job thread pool
            ExecutorService es = JobSubmission.getTaskEs();
            if(es != null) {
                es.shutdown();
            }
        }

        /**
         * delete master node from zookeeper
         */
        private void removeMaster() {
            String masterGroup = GlobalConstants.MASTER_GROUP;
            int masterServerPort = Configuration.getInt(prop, "thales.master.server.port",
                    MasterManager.DEFAULT_MASTER_SERVER_PORT);
            String activeMaster = MetricsUtils.getHostName() + ":" + masterServerPort;

            String quorum = prop.getProperty("thales.zookeeper.quorum");
            int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                    GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
            int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                    GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
            CuratorFramework client = null;
            try {
                LOG.info("remove master path " + masterGroup + "/" + activeMaster + " when shutdown");
                String masterPath = masterGroup + "/" + activeMaster;
                client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
                if(CuratorHelper.nodeExist(client, masterPath)) {
                    CuratorHelper.delete(client, masterPath);
                }
            } catch (Exception e) {
                LOG.error(e);
            } finally {
                CuratorHelper.close(client);
            }
        }
    }
}
