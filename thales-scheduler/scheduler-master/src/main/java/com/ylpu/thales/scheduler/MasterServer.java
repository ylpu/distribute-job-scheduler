package com.ylpu.thales.scheduler;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.core.zk.CuratorHelper;
import com.ylpu.thales.scheduler.manager.JobScheduler;
import com.ylpu.thales.scheduler.manager.MasterManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import java.util.Properties;

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
            for(;;);
        } catch (Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    private static class ShutDownHookThread extends Thread{
        
        private Properties prop;
        
        public ShutDownHookThread(Properties prop) {
            this.prop = prop;
        }
        @Override
        public void run() {
            //删除zk master节点
            removeZkPath();
            //关掉任务调度
            JobScheduler.shutdownJobs();
        }
        
        /**
         *  master在意外退出时删除zk临时节点
         */
        private void removeZkPath() {
            String masterGroup = GlobalConstants.MASTER_GROUP;
            int masterServerPort = Configuration.getInt(prop,"thales.master.server.port",MasterManager.DEFAULT_MASTER_SERVER_PORT);
            String activeMaster = MetricsUtils.getHostName() + ":" + masterServerPort;
            
            String quorum = prop.getProperty("thales.zookeeper.quorum");
            int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
            int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
            CuratorFramework client = null;
            try {
                LOG.info("remove master path " +  masterGroup + "/" + activeMaster + " when shutdown");
                client = CuratorHelper.getCuratorClient(quorum,sessionTimeout,connectionTimeout);
                CuratorHelper.delete(client, masterGroup + "/" + activeMaster); 
            }catch(Exception e) {
            	LOG.error(e);
            }finally{
            	CuratorHelper.close(client);
            }
        }
    }
}
