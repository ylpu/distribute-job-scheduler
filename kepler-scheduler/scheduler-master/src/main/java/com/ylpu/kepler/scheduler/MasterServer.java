package com.ylpu.kepler.scheduler;

import com.ylpu.kepler.scheduler.core.config.Configuration;
import com.ylpu.kepler.scheduler.core.constants.GlobalConstants;
import com.ylpu.kepler.scheduler.core.rest.JobManager;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.kepler.scheduler.core.utils.DateUtils;
import com.ylpu.kepler.scheduler.core.utils.MetricsUtils;
import com.ylpu.kepler.scheduler.core.zk.ZKHelper;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.manager.JobScheduler;
import com.ylpu.kepler.scheduler.manager.MasterManager;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.rpc.client.JobCallBackScan;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
            while(true) {}
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
            //删除zk临时节点
            removeZkPath();
            //更新数据库任务状态为失败
            markAsFailed();
            //关掉任务调度
            JobScheduler.shutdownJobs();
        }
        
        /**
         *  master在意外退出时删除zk临时节点
         */
        private void removeZkPath() {
            String masterGroup = GlobalConstants.MASTER_GROUP;
            int masterServerPort = Configuration.getInt(prop,"kepler.master.server.port",MasterManager.DEFAULT_MASTER_SERVER_PORT);
            String activeMaster = MetricsUtils.getHostName() + ":" + masterServerPort;
            
            String quorum = prop.getProperty("kepler.zookeeper.quorum");
            int sessionTimeout = Configuration.getInt(prop, "kepler.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
            int connectionTimeout = Configuration.getInt(prop, "kepler.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
            ZkClient zkClient = null;
            try {
                LOG.info("remove master path " +  masterGroup + "/" + activeMaster + " when shutdown");
                zkClient = ZKHelper.getClient(quorum,sessionTimeout,connectionTimeout);
                ZKHelper.delete(zkClient, masterGroup + "/" + activeMaster); 
            }finally{
                if(zkClient != null) {
                    zkClient.close();
                }
            }
        }
        /**
         *  master在意外退出时设置任务状态为失败
         */
        private void markAsFailed() {
            Map<String, JobInstanceResponseRpc> responses = JobCallBackScan.getResponses();
            List<JobInstanceRequest> list = new ArrayList<JobInstanceRequest>();
            JobInstanceRequest request = null;
            for(Entry<String, JobInstanceResponseRpc> entry : responses.entrySet()) {
                String key = entry.getKey();
                if(StringUtils.isNotBlank(key) && (entry.getValue().getTaskState() == TaskState.SUBMIT.getCode()
                        || entry.getValue().getTaskState() == TaskState.PENDING.getCode()
                        || entry.getValue().getTaskState() == TaskState.WAITING.getCode()
                        || entry.getValue().getTaskState() == TaskState.RUNNING.getCode())) {
                    request = new JobInstanceRequest();
                    request.setJobId(NumberUtils.toInt(key.split("-")[0]));
                    request.setScheduleTime(DateUtils.getDateFromString(key.split("-")[1], DateUtils.TIME_FORMAT));
                    request.setTaskState(TaskState.FAIL.getCode());
                    list.add(request);
                }
            }
            JobManager.markAsFailed(list);
        }
    }
}
