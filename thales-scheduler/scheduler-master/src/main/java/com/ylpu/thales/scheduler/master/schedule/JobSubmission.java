package com.ylpu.thales.scheduler.master.schedule;

import com.google.common.eventbus.AsyncEventBus;
import com.ylpu.thales.scheduler.alert.EventListener;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.utils.CronUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;
import com.ylpu.thales.scheduler.enums.GrpcType;
import com.ylpu.thales.scheduler.enums.JobCycle;
import com.ylpu.thales.scheduler.enums.JobPriority;
import com.ylpu.thales.scheduler.enums.JobReleaseState;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.context.parameter.ContextParameterProvider;
import com.ylpu.thales.scheduler.master.rpc.client.AbstractJobGrpcClient;
import com.ylpu.thales.scheduler.master.rpc.client.JobGrpcBlockingClient;
import com.ylpu.thales.scheduler.master.rpc.client.JobGrpcNonBlockingClient;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
//import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.NodeResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.StringUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class JobSubmission {

    private static final long RESOURCE_CHECK_INTERVAL = 3000;

    private static Log LOG = LogFactory.getLog(JobSubmission.class);
    
    private static ExecutorService taskEs = null;

    private static Queue<TaskCall> timeoutQueue = new LinkedBlockingQueue<TaskCall>();
    
    private static Map<String,PriorityBlockingQueue<TaskCall>> workerGroupQueue = new ConcurrentHashMap<String,PriorityBlockingQueue<TaskCall>>();
    
    private static Map<String, Boolean> waitingResourceMap = new ConcurrentHashMap<String, Boolean>();
    
    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));
        
    public static Map<String, PriorityBlockingQueue<TaskCall>> getGroupQueue() {
        return workerGroupQueue;
    }
    
    public static PriorityBlockingQueue<TaskCall> getGroupQueue(String groupName) {
        return workerGroupQueue.get(groupName);
    }
    

    public static Map<String, Boolean> getWaitingResourceMap() {
        return waitingResourceMap;
    }

    static {
        init();
    }

    private static void init() {
        taskEs = Executors.newCachedThreadPool();
        Thread timeoutThread = new Thread(new TimeoutThread());
        timeoutThread.start();
        eventBus.register(new EventListener());
    }

    public static ExecutorService getTaskEs() {
        return taskEs;
    }

    public static void addRpcRequest(JobInstanceRequestRpc requestRpc) throws Exception {

        List<JobDependency> dependJobs = new ArrayList<JobDependency>();
        //get job dependency
        if (requestRpc.getJob().getDependenciesList() == null
                || requestRpc.getJob().getDependenciesList().size() == 0) {
            dependJobs.add(new JobDependency(requestRpc.getJob().getId(), GlobalConstants.ROOT_SCHEDULE_TIME));
        } else {
            dependJobs = getLatestJobDepends(requestRpc);
        }
        JobStatusChecker.addDepends(dependJobs, requestRpc.getRequestId());
        JobStatusChecker.addJobInstanceRequest(requestRpc);
    }

    public static List<JobDependency> getLatestJobDepends(JobInstanceRequestRpc request) {
        Date currentJobScheduleTime = DateUtils.getDatetime(request.getScheduleTime());
        return getLatestJobDepends(request,currentJobScheduleTime);
    }
    
    public static List<JobDependency> getLatestJobDepends(JobInstanceRequestRpc request, Date currentJobScheduleTime) {

        List<JobDependency> jobDependencies = new ArrayList<JobDependency>();
        List<JobRequestRpc> dependencies = request.getJob().getDependenciesList();

        Iterator<JobRequestRpc> it = dependencies.iterator();
        JobDependency jobDependency = null;
        while (it.hasNext()) {
            jobDependency = new JobDependency();
            JobRequestRpc job = it.next();
            jobDependency.setJobId(job.getId());
            jobDependency.setScheduleTime(CronUtils.getLatestTriggerTime(job.getScheduleCron(),
                    DateUtils.getTime(currentJobScheduleTime, job.getJobCycle(), -1), currentJobScheduleTime));
            jobDependencies.add(jobDependency);
        }
        return jobDependencies;
    }

    public static void addTimeoutQueue(TaskCall taskCall) {
        timeoutQueue.add(taskCall);
    }
    
    public static synchronized void addWaitingQueue(TaskCall taskCall) {
        String workerGroupName = taskCall.getRpcRequest().getJob().getWorkerGroupname();
        PriorityBlockingQueue<TaskCall> taskQueue = workerGroupQueue.get(workerGroupName);
        if(taskQueue == null) {
            waitingResourceMap.put(workerGroupName, true);
            workerGroupQueue.put(workerGroupName, new PriorityBlockingQueue<TaskCall>(10000));
            workerGroupQueue.get(workerGroupName).add(taskCall);
//            taskEs.submit(new TaskWaitingThread(workerGroupQueue.get(workerGroupName)));
            new Thread(new TaskWaitingThread(workerGroupQueue.get(workerGroupName))).start();
        }else {
            taskQueue.add(taskCall);
        }
    }

    private static class TaskWaitingThread implements Runnable {
                
        private PriorityBlockingQueue<TaskCall> taskQueue;
        
        public TaskWaitingThread(PriorityBlockingQueue<TaskCall> taskQueue) {
            this.taskQueue = taskQueue;
        }
        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = taskQueue.poll();
                if (taskCall != null) {
                    AbstractJobGrpcClient client = null;
                    try {
                        LOG.info("job " + taskCall.getRpcRequest().getId() + " start to get worker at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
                        NodeResponse worker = getAvailableWorker(taskCall.getRpcRequest());
                        if(worker != null) {
                            client = getClient(worker,taskCall.getGrpcType()); 
                            try {
                                LOG.info("job " + taskCall.getRpcRequest().getId() + " start to submit to worker at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
                                client.submitJob(taskCall.getRpcRequest());
                                LOG.info("job " + taskCall.getRpcRequest().getId() + " finish submit to worker at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT)); 
                            }catch(Exception e) {
                                LOG.error("fail to submit job " + taskCall.getRpcRequest().getId() + " with exception " + e);
                            }
                        }
                        // cancel waiting resource, transit task status to fail
                        else {
                            try {
                                transitTaskStatus(taskCall.getRpcRequest(),TaskState.FAIL);
                            } catch (Exception e) {
                                LOG.error("fail to transit task " + taskCall.getRpcRequest().getId() + " to fail with exception " + e);
                            } finally {
                                JobStatusChecker.getJobInstanceRequestMap().remove(taskCall.getRpcRequest().getRequestId());
                            }
                        }
                    }finally {
                        if (taskCall.getGrpcType() == GrpcType.SYNC) {
                            if (client != null) {
                                client.shutdown();
                            }
                        }
                    }
                }
            }
        }
        
        private NodeResponse getAvailableWorker(JobInstanceRequestRpc rpcRequest) {
            NodeResponse worker = null;
            int i = 1;
            while (waitingResourceMap.get(rpcRequest.getJob().getWorkerGroupname())) {
                try {
                    worker = MasterManager.getInstance().getIdleWorker(rpcRequest.getJob().getWorkerGroupname(), "");
                    if(worker.getHost() != null && worker.getPort() != null) {
                        return worker;
                    }
                } catch (Exception e) {
                    LOG.error(Thread.currentThread().getName() + " for group " + rpcRequest.getJob().getWorkerGroupname() + " can not get available resource to execute task " + 
                    rpcRequest.getId() + " with  " + i + " tries, exception is " + e.getMessage());
                }
                //transit job status to waiting resource
                try {
                    transitTaskStatus(rpcRequest,TaskState.WAITING_RESOURCE);
                } catch (Exception e1) {
                    LOG.error("fail to transit task " + rpcRequest.getId() + " to waitting resource");
                }
                try {
                    Thread.sleep(RESOURCE_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
                i++;
            }
            JobSubmission.getWaitingResourceMap().put(rpcRequest.getJob().getWorkerGroupname(), true);
            return null;
        }
        
        private void transitTaskStatus(JobInstanceRequestRpc requestRpc,TaskState taskState) throws Exception{
            JobInstanceRequest request = new JobInstanceRequest();
            request.setId(requestRpc.getId());
            request.setTaskState(taskState.getCode());
            JobManager.transitTaskStatus(request);
        }
        
        private AbstractJobGrpcClient getClient(NodeResponse worker,GrpcType grpcType){
            AbstractJobGrpcClient client = null;
            if (grpcType == GrpcType.SYNC) {
                client = new JobGrpcBlockingClient(worker.getHost(), worker.getPort());
            } else {
                client = new JobGrpcNonBlockingClient(worker.getHost(), worker.getPort());
            }
            return client;
        }
    }
    
    //send alert when task timeout
    private static class TimeoutThread implements Runnable {
        JobInstanceResponse jobInstanceResponse = null;
        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = timeoutQueue.peek();
                if(taskCall != null) {
                    try {
                        jobInstanceResponse = JobManager.getJobInstanceById(taskCall.getRpcRequest().getId());
                        if(StringUtils.isNotBlank(jobInstanceResponse.getJobConf().getAlertUsers())) {
                            Event event = new Event();
                            setAlertEvent(event, jobInstanceResponse);
                            try {
                                eventBus.post(event);
                            }catch(Exception e) {
                                LOG.error("fail to send email for task " + taskCall.getRpcRequest().getId() + " with exception " + e.getMessage());
                            }
//                          avoid duplicate send
                            JobStatusChecker.getMailMap().put(taskCall.getRpcRequest().getRequestId(), 
                                    taskCall.getRpcRequest().getRequestId());
                            timeoutQueue.remove(taskCall);
                        }
                    }catch(Exception e) {
                        LOG.error("failed to get task " + taskCall.getRpcRequest().getId() + " with exception " + e.getMessage());
                    }
                }
            }
        }
        private void setAlertEvent(Event event, JobInstanceResponse jobInstanceResponse) {
            event.setTaskId(jobInstanceResponse.getId());
            event.setAlertType(AlertType.getAlertType(jobInstanceResponse.getJobConf().getAlertTypes()));
            event.setAlertUsers(jobInstanceResponse.getJobConf().getAlertUsers());
            event.setLogUrl(jobInstanceResponse.getLogUrl());
            event.setHostName(jobInstanceResponse.getWorker());
            event.setEventType(EventType.TIMEOUT);
        }
    }
    
    /**
     * 保存任务并开始调度
     * 
     * @param request
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void initJobInstance(JobInstanceRequest request, JobResponse jobResponse) {
        request.setApplicationid("");
        request.setCreatorEmail("");
        request.setCreatorName("");
        request.setEndTime(null);
        request.setElapseTime(0);
        request.setJobId(jobResponse.getId());
        request.setLogPath("");
        request.setLogUrl("");
        request.setPid(-1);
        request.setRetryTimes(0);
        request.setTaskState(TaskState.SUBMIT.getCode());
        request.setElapseTime(0);
        if(StringUtil.isBlank(request.getParameters())){
            String replacedJson = JsonUtils.replaceHolder(jobResponse.getJobConfiguration(),ContextParameterProvider.getContextParameter());
            String parameters = JsonUtils.getParameters(replacedJson);
            request.setParameters(parameters);
        }
    }
    
    public static JobInstanceResponseRpc buildResponse(String rpcId, int taskState) {
        if (taskState == TaskState.FAIL.getCode()) {
            return JobInstanceResponseRpc.newBuilder().setResponseId(rpcId).setErrorCode(500)
                    .setTaskState(taskState).setErrorMsg("failed to execute job").build();
        } else {
            return JobInstanceResponseRpc.newBuilder().setResponseId(rpcId).setErrorCode(200)
                    .setTaskState(taskState).setErrorMsg("").build();
        }
    }

    /**
     * 设置任务实例rpc请求
     * 
     * @param request
     * @param response
     * @return
     */

    public static JobInstanceRequestRpc initJobInstanceRequestRpc(JobInstanceRequest request, JobResponse response) {
        JobInstanceRequestRpc rpcJobInstanceRequest = JobInstanceRequestRpc.newBuilder()
                .setApplicationid(request.getApplicationid()).setCreatorEmail(request.getCreatorEmail())
                .setCreatorName(request.getCreatorName())
                .setScheduleTime(DateUtils.getProtobufTime(request.getScheduleTime()))
                .setStartTime(DateUtils.getProtobufTime(request.getStartTime())).setElapseTime(request.getElapseTime())
                .setLogPath(request.getLogPath()).setLogUrl(request.getLogUrl()).setPid(request.getPid())
                .setRetryTimes(request.getRetryTimes()).setTaskState(request.getTaskState()).setWorker("")
                .setId(request.getId())
                .setRequestId(request.getJobId() + "-"
                        + DateUtils.getDateAsString(request.getScheduleTime(), DateUtils.MINUTE_TIME_FORMAT))
                .setJob(setJobRequest(request.getParameters(),response)).build();
        return rpcJobInstanceRequest;
    }

    /**
     * 设置任务实例对应的任务和任务依赖
     * 
     * @param response
     * @return
     */
    public static JobRequestRpc setJobRequest(String parameters,JobResponse response) {
        List<JobRequestRpc> rpcDependencies = new ArrayList<JobRequestRpc>();
        JobRequestRpc rpcDependency = null;
        List<JobResponse> dependencies = response.getDependencies();
        if (dependencies != null && dependencies.size() > 0) {
            for (JobResponse dependency : dependencies) {
                rpcDependency = JobRequestRpc.newBuilder()
                        .setAlertTypes(dependency.getAlertTypes() == null ? AlertType.SMS.getCode()
                                : AlertType.getAlertType(response.getAlertTypes()).getCode())
                        .setAlertUsers(dependency.getAlertUsers() == null ? "" : dependency.getAlertUsers())
                        .setCreatorId(dependency.getCreatorId() == null ? "" : dependency.getCreatorId())
                        .setDescription(dependency.getDescription() == null ? "" : dependency.getCreatorId())
                        .setExecutionTimeout(
                                dependency.getExecutionTimeout() == null ? 0 : dependency.getExecutionTimeout())
                        .setId(dependency.getId())
                        .setIsSelfdependent(
                                dependency.getIsSelfdependent() == null ? true : dependency.getIsSelfdependent())
                        .setJobConfiguration(
                                dependency.getJobConfiguration() == null ? "" : dependency.getJobConfiguration())
                        .setJobCycle(dependency.getJobCycle() == null ? JobCycle.MINUTE.getCode()
                                : JobCycle.getJobCycle(response.getJobCycle()).getCode())
                        .setJobName(dependency.getJobName() == null ? "" : dependency.getJobName())
                        .setJobPriority(dependency.getJobPriority() == null ? JobPriority.LOW.getPriority()
                                : JobPriority.getJobPriority(dependency.getJobPriority()).getPriority())
                        .setJobReleasestate(dependency.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode()
                                : dependency.getJobReleasestate())
                        .setJobType(dependency.getJobType() == null ? JobType.SHELL.getCode()
                                : JobType.getJobType(response.getJobType()).getCode())
                        .setMaxRetrytimes(dependency.getMaxRetrytimes() == null ? 0 : dependency.getMaxRetrytimes())
                        .setOwnerIds(dependency.getOwnerIds() == null ? "" : dependency.getOwnerIds())
                        .setRetryInterval(dependency.getRetryInterval() == null ? 0 : dependency.getRetryInterval())
                        .setScheduleCron(dependency.getScheduleCron() == null ? "" : dependency.getScheduleCron())
                        .setWorkerGroupname(
                                dependency.getWorkerGroupname() == null ? "" : dependency.getWorkerGroupname())
                        .build();
                rpcDependencies.add(rpcDependency);
            }
        }
        String jobConfiguration = response.getJobConfiguration() == null ? "" : response.getJobConfiguration();
        String replacedJobConfiguration = JsonUtils.replaceParameterValue(jobConfiguration, parameters);
        
        JobRequestRpc rpcJobRequest = JobRequestRpc.newBuilder()
                .setAlertTypes(response.getAlertTypes() == null ? AlertType.SMS.getCode()
                        : AlertType.getAlertType(response.getAlertTypes()).getCode())
                .setAlertUsers(response.getAlertUsers() == null ? "" : response.getAlertUsers())
                .setCreatorId(response.getCreatorId() == null ? "" : response.getCreatorId())
                .setDescription(response.getDescription() == null ? "" : response.getDescription())
                .setExecutionTimeout(response.getExecutionTimeout() == null ? 0 : response.getExecutionTimeout())
                .setId(response.getId())
                .setIsSelfdependent(response.getIsSelfdependent() == null ? true : response.getIsSelfdependent())
                .setJobConfiguration(replacedJobConfiguration)
                .setJobCycle(response.getJobCycle() == null ? JobCycle.MINUTE.getCode()
                        : JobCycle.getJobCycle(response.getJobCycle()).getCode())
                .setJobName(response.getJobName() == null ? "" : response.getJobName())
                .setJobPriority(response.getJobPriority() == null ? JobPriority.LOW.getPriority()
                        : JobPriority.getJobPriority(response.getJobPriority()).getPriority())
                .setJobReleasestate(response.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode()
                        : response.getJobReleasestate())
                .setJobType(response.getJobType() == null ? JobType.SHELL.getCode()
                        : JobType.getJobType(response.getJobType()).getCode())
                .setMaxRetrytimes(response.getMaxRetrytimes() == null ? 0 : response.getMaxRetrytimes())
                .setOwnerIds(response.getOwnerIds() == null ? "" : response.getOwnerIds())
                .setRetryInterval(response.getRetryInterval() == null ? 0 : response.getRetryInterval())
                .setScheduleCron(response.getScheduleCron() == null ? "" : response.getScheduleCron())
                .setWorkerGroupname(response.getWorkerGroupname() == null ? "" : response.getWorkerGroupname())
                .addAllDependencies(rpcDependencies).build();
        return rpcJobRequest;
    }
}
