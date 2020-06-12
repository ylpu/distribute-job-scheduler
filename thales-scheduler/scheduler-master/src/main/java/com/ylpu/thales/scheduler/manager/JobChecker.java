package com.ylpu.thales.scheduler.manager;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.GrpcType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.manager.JobSubmission;
import com.ylpu.thales.scheduler.manager.TaskCall;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.rpc.client.JobDependency;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JobChecker {

    private static Log LOG = LogFactory.getLog(JobChecker.class);

    private static volatile boolean stop = false;
    // key是任务id,value是待执行的任务
    private static Map<String, JobInstanceRequestRpc> jobInstanceRequestMap = new ConcurrentHashMap<String, JobInstanceRequestRpc>();
    // 任务的返回列表
    private static Map<String, JobInstanceResponseRpc> responses = new WeakHashMap<String, JobInstanceResponseRpc>();
    // key是所依赖的任务,value是任务id
    private static Map<List<JobDependency>, String> dependsMap = new ConcurrentHashMap<List<JobDependency>, String>();

    private static final long CHECK_INTERVAL = 1;

    public static void start() {
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

    public static Map<String, JobInstanceResponseRpc> getResponses() {
        return responses;
    }

    public static Map<List<JobDependency>, String> getDepends() {
        return dependsMap;
    }

    /**
     * 检查依赖任务是否执行成功，只有依赖的任务id在任务的返回列表里并且依赖任务的状态为成功,当前任务才会被执行
     *
     */
    private static class JobStatusCheckThread extends Thread {
        public void run() {
            long interval = Configuration.getLong(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                    "thales.scheduler.job.check.interval", CHECK_INTERVAL);
            while (!stop) {
                for (Entry<List<JobDependency>, String> entry : dependsMap.entrySet()) {
                    int ids = 0;
                    List<JobDependency> list = entry.getKey();
                    for (JobDependency jobDependency : list) {
                        if (responses.containsKey(jobDependency.toString())) {
                            JobInstanceResponseRpc response = responses.get(jobDependency.toString());
                            if (response.getTaskState() == TaskState.SUCCESS.getCode()) {
                                ids++;
                            }
                        }
                    }
                    String requestId = dependsMap.get(entry.getKey());
                    JobInstanceRequestRpc request = jobInstanceRequestMap.get(requestId);
                    if (ids == list.size() || isRootJob(list)) {
                         requestId = dependsMap.remove(entry.getKey());
                         request = jobInstanceRequestMap.remove(requestId);
                         LOG.info("parent job " + entry.getKey() + " has finished, will add job " + requestId + " to queue");
                         transitTaskStatusToQueue(request.getId());
                         JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(requestId, TaskState.QUEUED, 200,"");
                         JobChecker.addResponse(responseRpc);
                         JobSubmission.addWaitingTask(new TaskCall(request, GrpcType.ASYNC));
                    }else {
                        LOG.info("job " + requestId + " is waiting for dependency jobs to finish" + entry.getKey());
                        JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(requestId, TaskState.WAITING_DEPENDENCY, 200,"");
                        JobChecker.addResponse(responseRpc);
                        transitTaskStatusToWaitingDependency(request.getId());
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
                if (dependency.toString().split("-")[1].equals("root")) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static void transitTaskStatusToQueue(Integer taskId) {
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(taskId);
        request.setTaskState(TaskState.QUEUED.getCode());
        try {
            JobManager.updateJobInstanceSelective(request);
        } catch (Exception e) {
            LOG.error(e);
        }
    }
    
    private static void transitTaskStatusToWaitingDependency(Integer taskId) {
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(taskId);
        request.setTaskState(TaskState.WAITING_DEPENDENCY.getCode());
        try {
            JobManager.updateJobInstanceSelective(request);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private static class TimeoutThread extends Thread {
        public void run() {
            long interval = Configuration.getLong(Configuration.getConfig(GlobalConstants.CONFIG_FILE),
                    "thales.scheduler.timeout.check.interval", CHECK_INTERVAL);
            while (true) {
                for (Entry<String, JobInstanceRequestRpc> entry : jobInstanceRequestMap.entrySet()) {
                    if (entry.getValue() != null) {
                        Date startTime = DateUtils.getDatetime(entry.getValue().getStartTime());
                        int elapseTime = DateUtils.getElapseTime(startTime, new Date());
                        if (entry.getValue().getJob().getExecutionTimeout() > 0) {
                            if (elapseTime > entry.getValue().getJob().getExecutionTimeout()) {
                                JobSubmission.addTimeoutTask(new TaskCall(entry.getValue(), GrpcType.SYNC));
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(interval * 1000 * 60);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }
    }
}