package com.ylpu.thales.scheduler.master.schedule;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.GrpcType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.schedule.JobSubmission;
import com.ylpu.thales.scheduler.master.schedule.TaskCall;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    // key是任务id,value是待执行的任务
    private static Map<String, JobInstanceRequestRpc> jobInstanceRequestMap = new ConcurrentHashMap<String, JobInstanceRequestRpc>();
//    lru以防内存溢出
    private static LRU<String, JobInstanceResponseRpc> responses = new LRU<String, JobInstanceResponseRpc>(5000000);
    // key是所依赖的任务,value是任务id
    private static Map<List<JobDependency>, String> dependsMap = new ConcurrentHashMap<List<JobDependency>, String>();
    
    private static Map<String, String> jobDependStatusMap = new ConcurrentHashMap<String,String>();
    
    private static LRU<String, String> mailLRU = new LRU<String,String>(1000000);

    private static final long JOB_DEPENDENCY_CHECK_INTERVAL = 1;
    
    private static final long TIMEOUT_CHECK_INTERVAL = 60;
    
    private static ExecutorService queueThreadPool = null;
    
    private static ExecutorService dependencyThreadPool = null;

    public static void init() {
        
        int queueThreadPoolCount = Configuration.getInt(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                "thales.master.status.queue.checker.threadpool", 1);
        int dependencyThreadPoolCount = Configuration.getInt(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                "thales.master.status.dependency.checker.threadpool", 1);
        queueThreadPool = Executors.newFixedThreadPool(queueThreadPoolCount);
        dependencyThreadPool = Executors.newFixedThreadPool(dependencyThreadPoolCount);
        
        ExecutorService es = Executors.newFixedThreadPool(2);
        es.execute(new JobStatusCheckThread());
        es.execute(new TimeoutThread());
    }

    public static void addResponse(JobInstanceResponseRpc response) {
        responses.put(response.getResponseId(), response);
    }

    public static JobInstanceResponseRpc getResponse(String id) {
        return responses.get(id);
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

    public static LRU<String, JobInstanceResponseRpc> getResponses() {
        return responses;
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
     * 检查依赖任务是否执行成功，只有依赖的任务id在任务的返回列表里并且依赖任务的状态为成功,当前任务才会被执行
     *
     */
    private static class JobStatusCheckThread extends Thread {
        public void run() {
            long interval = Configuration.getLong(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                    "thales.scheduler.job.check.interval", JOB_DEPENDENCY_CHECK_INTERVAL);
            while (!stop) {
                for (Entry<List<JobDependency>, String> entry : dependsMap.entrySet()) {
                    int successfulJobs = 0;
                    List<JobDependency> list = entry.getKey();
                    for (JobDependency jobDependency : list) {
                        if (responses.get().containsKey(jobDependency.toString())) {
                            JobInstanceResponseRpc response = responses.get(jobDependency.toString());
                            if (response.getTaskState() == TaskState.SUCCESS.getCode()) {
                                successfulJobs++;
                            }
                        }
                    }
                    String requestId = dependsMap.get(entry.getKey());
                    JobInstanceRequestRpc rpcRequest = jobInstanceRequestMap.get(requestId);
                    if (successfulJobs == list.size() || isRootJob(list)) {
                         dependsMap.remove(entry.getKey());
//                         rpcRequest = jobInstanceRequestMap.remove(requestId);
                         LOG.info("parent job " + entry.getKey() + " has finished, will add job " + requestId + " to queue");
//                       transit task status to queue，bad performance if there are too many jobs running at some time
                         queueThreadPool.execute(new Runnable() {
                             @Override
                             public void run() {
                                 try {
                                    transitTaskStatus(rpcRequest.getId(),TaskState.QUEUED);
                                    jobDependStatusMap.remove(requestId);
                                    JobSubmission.addWaitingQueue(new TaskCall(rpcRequest, GrpcType.ASYNC));
                                } catch (Exception e) {
                                    LOG.error("failed to transit task " + rpcRequest.getId() + 
                                            " to " + TaskState.QUEUED.toString() + " with exception "+ e.getMessage());
                                }
                             }
                         });
                    }else {
                        LOG.info("job " + requestId + " is waiting for dependency jobs to finish" + entry.getKey());
//                        transit task status to waiting dependency,bad performance if there are too many long waiting dependency jobs
                        dependencyThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                if(!jobDependStatusMap.containsKey(requestId)) {
                                    try {
                                        transitTaskStatus(rpcRequest.getId(),TaskState.WAITING_DEPENDENCY);
                                        jobDependStatusMap.put(requestId, requestId);
                                    } catch (Exception e) {
                                        LOG.error("failed to transit task " + rpcRequest.getId() + 
                                                " to " +  TaskState.WAITING_DEPENDENCY.toString() + " with exception " + e.getMessage());
                                    } 
                                }
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
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
            long interval = Configuration.getLong(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                    "thales.scheduler.timeout.check.interval", TIMEOUT_CHECK_INTERVAL);
            while (true) {
                for (Entry<String, JobInstanceRequestRpc> entry : jobInstanceRequestMap.entrySet()) {
                    if (entry.getValue() != null) {
//                      防止重复发送
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