package com.ylpu.thales.scheduler.master.schedule;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.GrpcType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JobStatusChecker {

    private static Log LOG = LogFactory.getLog(JobStatusChecker.class);

    private static volatile boolean stop = false;
    // key is task id,value task information
    private static Map<String, JobInstanceRequestRpc> jobInstanceRequestMap = new ConcurrentHashMap<String, JobInstanceRequestRpc>();
    // lru avoid out of memory
    private static LRU<String, JobInstanceResponseRpc> responseMap = new LRU<String, JobInstanceResponseRpc>(5000000);
    // key is dependency task list,value is task id
    private static Map<List<JobDependency>, String> dependsMap = new ConcurrentHashMap<List<JobDependency>, String>();
    
    private static Map<String, String> jobDependStatusMap = new ConcurrentHashMap<String,String>();
    
    private static LRU<String, String> mailLRU = new LRU<String,String>(1000000);

    private static final long JOB_DEPENDENCY_CHECK_INTERVAL = 1;
    
    private static final long TIMEOUT_CHECK_INTERVAL = 60;
    
    private static ExecutorService queueThreadPool = null;
    
    private static ExecutorService dependencyThreadPool = null;

    public static void init() {
        
        Properties prop = Configuration.getConfig();
        int queueThreadPoolCount = Configuration.getInt(prop,
                "thales.master.status.queue.checker.threadpool", 1);
        int dependencyThreadPoolCount = Configuration.getInt(prop,
                "thales.master.status.dependency.checker.threadpool", 1);
        queueThreadPool = Executors.newFixedThreadPool(queueThreadPoolCount);
        dependencyThreadPool = Executors.newFixedThreadPool(dependencyThreadPoolCount);
        
        ExecutorService es = Executors.newFixedThreadPool(2);
        es.execute(new JobStatusCheckThread());
        es.execute(new TimeoutThread());
    }

    public static void addResponse(JobInstanceResponseRpc response) {
        responseMap.put(response.getResponseId(), response);
    }

    public static JobInstanceResponseRpc getResponse(String id) {
        return responseMap.get(id);
    }

    public static void addDepends(List<JobDependency> key, String value) {
        dependsMap.put(key, value);
    }

    public static void addJobInstanceRequest(JobInstanceRequestRpc jobInstanceRequestRpc) {
        jobInstanceRequestMap.put(jobInstanceRequestRpc.getRequestId(), jobInstanceRequestRpc);
    }

    public static JobInstanceRequestRpc getJobInstanceRequest(String id) {
        return jobInstanceRequestMap.get(id);
    }

    public static Map<String, JobInstanceRequestRpc> getJobInstanceRequestMap() {
        return jobInstanceRequestMap;
    }

    public static LRU<String, JobInstanceResponseRpc> getResponseMap() {
        return responseMap;
    }

    public static Map<List<JobDependency>, String> getDepends() {
        return dependsMap;
    }

    public static Map<String, String> getJobDependStatusMap() {
        return jobDependStatusMap;
    }
    
    public static LRU<String,String> getMailMap() {
        return mailLRU;
    }

    /**
     * check dependency job are successful or not, if dependency jobs are successful, then the downstream job can be execute.
     */
    private static class JobStatusCheckThread extends Thread {
        public void run() {
            Properties prop = Configuration.getConfig();
            long interval = Configuration.getLong(prop,
                    "thales.scheduler.job.check.interval", JOB_DEPENDENCY_CHECK_INTERVAL);
            while (!stop) {
                for (Entry<List<JobDependency>, String> entry : dependsMap.entrySet()) {
                    int successfulJobs = 0;
                    List<JobDependency> list = entry.getKey();
                    for (JobDependency jobDependency : list) {
                        if (responseMap.get().containsKey(jobDependency.toString())) {
                            JobInstanceResponseRpc response = responseMap.get(jobDependency.toString());
                            if (response.getTaskState() == TaskState.SUCCESS.getCode()) {
                                successfulJobs++;
                            }
                        }
                    }
                    String requestId = dependsMap.get(entry.getKey());
                    JobInstanceRequestRpc rpcRequest = jobInstanceRequestMap.get(requestId);
                    if (successfulJobs == list.size() || isRootJob(list)) {
                         dependsMap.remove(entry.getKey());
                         LOG.info("parent job " + entry.getKey() + " has finished, will add job " + requestId + " to queue");
//                       transit task status to queue
                         transitToQueue(rpcRequest,requestId);
                    }else {
                        LOG.info("job " + requestId + " is waiting for dependency jobs to finish" + entry.getKey());
//                        transit task status to waiting dependency
                        transitToWaitingDependency(rpcRequest,requestId, entry.getKey());
                    }
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }
        
        private void transitToQueue(JobInstanceRequestRpc rpcRequest,String requestId) {
        	 queueThreadPool.execute(new Runnable() {
                 @Override
                 public void run() {
                     try {
                        transitTaskStatus(rpcRequest.getId(),TaskState.QUEUED);
                        jobDependStatusMap.remove(requestId);
                        JobSubmission.addWaitingQueue(new TaskCall(rpcRequest, GrpcType.ASYNC));
                        LOG.info("job " + rpcRequest.getId() + " queue at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
                    } catch (Exception e) {
                        LOG.error("failed to transit task " + rpcRequest.getId() + 
                                " to " + TaskState.QUEUED.toString() + " with exception "+ e.getMessage());
                        jobInstanceRequestMap.remove(requestId);
                    }
                 }
             });
        }
        
        private void transitToWaitingDependency(JobInstanceRequestRpc rpcRequest,String requestId, List<JobDependency> depends) {
            dependencyThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //avoid duplicate transit task status to waiting dependency
                    if(!jobDependStatusMap.containsKey(requestId)) {
                        try {
                            transitTaskStatus(rpcRequest.getId(),TaskState.WAITING_DEPENDENCY);
                            jobDependStatusMap.put(requestId, requestId);
                            LOG.info("job " + rpcRequest.getId() + " waiting dependency at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
                        } catch (Exception e) {
                            LOG.error("failed to transit task " + rpcRequest.getId() + 
                                    " to " +  TaskState.WAITING_DEPENDENCY.toString() + " with exception " + e.getMessage());
                            dependsMap.remove(depends);
                            jobInstanceRequestMap.remove(requestId);
                        } 
                    }
                }
            });
        }

        private boolean isRootJob(List<JobDependency> list) {
            if (list != null && list.size() == 1) {
                JobDependency dependency = list.get(0);
                if (dependency.toString().split("-")[1].equals(GlobalConstants.ROOT_SCHEDULE_TIME)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static void transitTaskStatus(Integer taskId,TaskState taskState) throws Exception {
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(taskId);
        request.setTaskState(taskState.getCode());
        JobManager.transitTaskStatus(request);
    }

    private static class TimeoutThread extends Thread {
        public void run() {
            Properties prop = Configuration.getConfig();
            long interval = Configuration.getLong(prop,
                    "thales.scheduler.timeout.check.interval", TIMEOUT_CHECK_INTERVAL);
            while (true) {
                for (Entry<String, JobInstanceRequestRpc> entry : jobInstanceRequestMap.entrySet()) {
                    if (entry.getValue() != null) {
//                      avoid duplicate send
                        if(StringUtils.isBlank(mailLRU.get(entry.getKey()))) {
                            Date startTime = DateUtils.getDatetime(entry.getValue().getStartTime());
                            int elapseTime = DateUtils.getElapseTime(startTime, new Date());
                            if (entry.getValue().getJob().getExecutionTimeout() > 0) {
                                if (elapseTime / 60 > entry.getValue().getJob().getExecutionTimeout()) {
                                    JobSubmission.addTimeoutQueue(new TaskCall(entry.getValue(), GrpcType.SYNC));
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }
    }
}