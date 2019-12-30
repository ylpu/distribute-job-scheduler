package com.ylpu.thales.scheduler.manager;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.utils.CronUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.GrpcType;
import com.ylpu.thales.scheduler.enums.JobCycle;
import com.ylpu.thales.scheduler.enums.JobPriority;
import com.ylpu.thales.scheduler.enums.JobReleaseState;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.rpc.client.AbstractJobGrpcClient;
import com.ylpu.thales.scheduler.rpc.client.JobStatusCheck;
import com.ylpu.thales.scheduler.rpc.client.JobDependency;
import com.ylpu.thales.scheduler.rpc.client.JobGrpcBlockingClient;
import com.ylpu.thales.scheduler.rpc.client.JobGrpcNonBlockingClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class JobSubmission {
    
    private static final long RESOURCE_CHECK_INTERVAL = 3000;
    
    private static final int POOL_SIZE = 2;
    
    private static Log LOG = LogFactory.getLog(JobSubmission.class);
    
    private static ExecutorService es = null;
    
    private static Queue<TaskCall> timeoutQueue = new LinkedBlockingQueue<TaskCall>();

    private static PriorityBlockingQueue<TaskCall> waitingQueue = new PriorityBlockingQueue<TaskCall>();

    public static PriorityBlockingQueue<TaskCall> getWaitingQueue() {
		return waitingQueue;
	}

	static {
        init();
    }
    
    private static void init() {
        int poolSize = Configuration.getInt(Configuration.getConfig("config.properties"), 
                "thales.scheduler.pool.size", POOL_SIZE);
        es = Executors.newFixedThreadPool(poolSize);
        es.execute(new TaskWaitingThread());
        es.execute(new TimeoutThread());
    }
    
    public static void updateJobStatus(JobInstanceRequestRpc requestRpc) throws Exception {
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request  = new JobInstanceRequest();
        request.setId(requestRpc.getId());
        request.setStartTime(DateUtils.getDatetime(requestRpc.getStartTime()));
        request.setScheduleTime(DateUtils.getDatetime(requestRpc.getScheduleTime()));
        try {
            List<JobDependency> dependJobs = new ArrayList<JobDependency>();
            if(requestRpc.getJob().getDependenciesList() == null 
                    || requestRpc.getJob().getDependenciesList().size() == 0) {
                dependJobs.add(new JobDependency(requestRpc.getJob().getId(),"root"));
            }
            else {
                dependJobs = getLatestJobDepends(requestRpc);
            }
            JobStatusCheck.addJobInstanceRequest(requestRpc);
            
            JobStatusCheck.addDepends(dependJobs, requestRpc.getRequestId());
            
            request.setTaskState(TaskState.WAITING.getCode());
            JobManager.updateJobInstanceSelective(request);
            
            responseRpc = buildResponse(requestRpc,TaskState.WAITING,200,"");
            JobStatusCheck.addResponse(responseRpc);
        }catch(Exception e) {
            LOG.error("fail to update job "  + requestRpc.getId() +  " to waiting status with exception " + e.getMessage());
            request.setTaskState(TaskState.FAIL.getCode());
            request.setEndTime(new Date());
            request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(),request.getEndTime()));
            JobManager.updateJobInstanceSelective(request);
            responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                    "fail to update job " + requestRpc.getId() + " to fail status");
            JobStatusCheck.addResponse(responseRpc);
        }
    }
    
    private static List<JobDependency> getLatestJobDepends(JobInstanceRequestRpc request) {
    	
        List<JobDependency> jobDependencies = new ArrayList<JobDependency>();
        Date currentJobScheduleTime = DateUtils.getDatetime(request.getScheduleTime());
        List<JobRequestRpc> dependencies = request.getJob().getDependenciesList();
        
        Iterator<JobRequestRpc> it = dependencies.iterator();
        JobDependency jobDependency = null;
        while(it.hasNext()) {
        	jobDependency = new JobDependency();
            JobRequestRpc job = it.next();
            jobDependency.setJobId(job.getId());
            jobDependency.setScheduleTime(CronUtils.getLatestTriggerTime(job.getScheduleCron(),
                    DateUtils.getTime(currentJobScheduleTime, job.getJobCycle(),-1),currentJobScheduleTime));
            jobDependencies.add(jobDependency);
        }
        return jobDependencies;
    }
    
    private static JobInstanceResponseRpc buildResponse(JobInstanceRequestRpc requestRpc,
            TaskState taskState,int errorCode,String errorMsg) {
        return JobInstanceResponseRpc.newBuilder()
        .setResponseId(requestRpc.getRequestId())
        .setErrorCode(errorCode)
        .setTaskState(taskState.getCode())
        .setErrorMsg(errorMsg)
        .build();
    }
    
    
    public static void addWaitingTask(TaskCall taskCall) {
        waitingQueue.add(taskCall);
    }
    
    public static void addTimeoutTask(TaskCall taskCall) {
        timeoutQueue.add(taskCall);
    }
    
    private static class TaskWaitingThread implements Runnable{
        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = waitingQueue.poll();
                if(taskCall != null) {
                    AbstractJobGrpcClient client = null;
                    try {
                        try {
                             client = getClient(taskCall.getRpcRequest(),taskCall.getGrpcType());
                             client.submitJob(taskCall.getRpcRequest());
                        } catch (Exception e) {
                             LOG.error(e);
                             processException(taskCall.getRpcRequest());
                        }
                    }finally {
                        //同步rpc直接关闭，异步rpc需要内部关闭
                        if(taskCall.getGrpcType() == GrpcType.SYNC) {
                            if(client != null) {
                                client.shutdown();
                            }
                        }
                    }
                }
            }
        }
        private void processException(JobInstanceRequestRpc request) {
		    JobStatusRequest jr = new JobStatusRequest();
		    jr.setIds(Arrays.asList(request.getId()));
		    jr.setStatus(TaskState.FAIL);
            try {
				JobManager.updateJobStatus(jr);
    	            String responseId = request.getJob().getId() + "-" + DateUtils.getDateAsString(DateUtils.getDatetime(request.getScheduleTime()),DateUtils.TIME_FORMAT);
    	            JobInstanceResponseRpc response = JobInstanceResponseRpc.newBuilder()
    	                .setResponseId(responseId)
    	                .setErrorCode(500)
    	                .setTaskState(TaskState.FAIL.getCode())
    	                .setErrorMsg("failed to execute job")
    	                .build();
    	            JobStatusCheck.addResponse(response);
			} catch (Exception e) {
				LOG.error(e);
			}
        }
    }
    
    private static class TimeoutThread implements Runnable{
        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = timeoutQueue.poll();
                if(taskCall != null) {
                    AbstractJobGrpcClient client = null;
                    try {
                        client = getClient(taskCall.getRpcRequest(),taskCall.getGrpcType());
                        try {
                             client.kill(taskCall.getRpcRequest());
                        } catch (Exception e) {
                             LOG.error(e);
                        }
                    }finally {
                        //同步rpc直接关闭，异步rpc需要内部关闭
                        if(taskCall.getGrpcType() == GrpcType.SYNC) {
                            if(client != null) {
                                client.shutdown();
                            }
                        }
                    }
                }
            }
        }
    }

    private static AbstractJobGrpcClient getClient(JobInstanceRequestRpc rpcRequest,GrpcType grpcType) {
        AbstractJobGrpcClient client = null;
        WorkerResponse worker = getAvailableWorker(rpcRequest.getJob().getWorkerGroupname(),rpcRequest.getId());
        if(grpcType == GrpcType.SYNC) {
            client = new JobGrpcBlockingClient(worker.getHost(),worker.getPort()); 
        }else {
            client = new JobGrpcNonBlockingClient(worker.getHost(),worker.getPort()); 
        }
        return client;
    }
    
    private static WorkerResponse getAvailableWorker(String workerGroup,int jobId) {
        WorkerResponse worker = null;
        int i = 1 ;
        while(i <= 3 ) {
            try {      
                worker = MasterManager.getInstance().getIdleWorker(
                        workerGroup, "");
                return worker;
            }catch(Exception e) {
                LOG.error("can not get available resource to execute job " + jobId + " with  " + i + " tries");
            }
            try {
                Thread.sleep(RESOURCE_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
            i++;
        }
        throw new RuntimeException("can not get available resource to execute job " + jobId);
    }

        
    /**
     * 保存任务并开始调度
     * @param request
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     */
    public static void initJobInstance(JobInstanceRequest request,JobResponse jobResponse) {
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
    }
    
    public static JobInstanceResponseRpc buildJobStatus(JobResponse jobResponse,Date scheduleTime,TaskState state) {
    	    String responseId = jobResponse.getId() + "-" + DateUtils.getDateAsString(scheduleTime,DateUtils.TIME_FORMAT);
    	    if(state == TaskState.FAIL) {
    	        return JobInstanceResponseRpc.newBuilder()
    	                .setResponseId(responseId)
    	                .setErrorCode(500)
    	                .setTaskState(state.getCode())
    	                .setErrorMsg("failed to execute job")
    	                .build();
    	    }else {
    	        return JobInstanceResponseRpc.newBuilder()
    	                .setResponseId(responseId)
    	                .setErrorCode(200)
    	                .setTaskState(state.getCode())
    	                .setErrorMsg("")
    	                .build();
    	    }
    }
    
    /**
     * 设置任务实例rpc请求
     * @param request
     * @param response
     * @return
     */
    
    public static JobInstanceRequestRpc initJobInstanceRequestRpc(JobInstanceRequest request,JobResponse response) {
        JobInstanceRequestRpc rpcJobInstanceRequest = JobInstanceRequestRpc.newBuilder()
                .setApplicationid(request.getApplicationid())
                .setCreatorEmail(request.getCreatorEmail())
                .setCreatorName(request.getCreatorName())
                .setScheduleTime(DateUtils.getProtobufTime(request.getScheduleTime()))
                .setStartTime(DateUtils.getProtobufTime(request.getStartTime()))
                .setElapseTime(request.getElapseTime())
                .setLogPath(request.getLogPath())
                .setLogUrl(request.getLogUrl())
                .setPid(request.getPid())
                .setRetryTimes(request.getRetryTimes())
                .setTaskState(TaskState.SUBMIT.getCode())
                .setWorker("")
                .setId(request.getId())
                .setRequestId(request.getJobId() + "-" + DateUtils.getDateAsString(
                        request.getScheduleTime(),DateUtils.TIME_FORMAT))
                .setJob(setJobRequest(response))
                .build();
        return rpcJobInstanceRequest;
    }
    
    /**
     * 设置任务实例对应的任务和任务依赖
     * @param response
     * @return
     */
    public static JobRequestRpc setJobRequest(JobResponse response) {
        List<JobRequestRpc> rpcDependencies  = new ArrayList<JobRequestRpc>();
        JobRequestRpc rpcDependency = null;
        List<JobResponse> dependencies = response.getDependencies();
        if(dependencies != null && dependencies.size() > 0) {
            for(JobResponse dependency : dependencies) {
                rpcDependency  = JobRequestRpc.newBuilder()
                .setAlertTypes(dependency.getAlertTypes() == null ? AlertType.SMS.getCode() : AlertType.getAlertType(response.getAlertTypes()).getCode())
                .setAlertUsers(dependency.getAlertUsers() == null ? "" : dependency.getAlertUsers())
                .setCreatorId(dependency.getCreatorId() == null ? "" : dependency.getCreatorId() )
                .setDescription(dependency.getDescription() == null ? "" : dependency.getCreatorId() )
                .setExecutionTimeout(dependency.getExecutionTimeout() == null ? 0: dependency.getExecutionTimeout())
                .setId(dependency.getId())
                .setIsSelfdependent(dependency.getIsSelfdependent() == null ? true : dependency.getIsSelfdependent())
                .setJobConfiguration(dependency.getJobConfiguration() == null ? "" : dependency.getJobConfiguration())
                .setJobCycle(dependency.getJobCycle() == null ? JobCycle.MINUTE.getCode(): JobCycle.getJobCycle(response.getJobCycle()).getCode())
                .setJobName(dependency.getJobName() == null ? "" : dependency.getJobName())
                .setJobPriority(dependency.getJobPriority() == null ? JobPriority.LOW.getPriority(): JobPriority.getJobPriority(dependency.getJobPriority()).getPriority())
                .setJobReleasestate(dependency.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode() : dependency.getJobReleasestate())
                .setJobType(dependency.getJobType() == null ? JobType.SHELL.getCode(): JobType.getJobType(response.getJobType()).getCode())
                .setMaxRetrytimes(dependency.getMaxRetrytimes() == null ? 0 : dependency.getMaxRetrytimes())
                .setOwnerIds(dependency.getOwnerIds() == null ? "" : dependency.getOwnerIds())
                .setRetryInterval(dependency.getRetryInterval() == null ? 0 : dependency.getRetryInterval())
                .setScheduleCron(dependency.getScheduleCron() == null ? "" : dependency.getScheduleCron())
                .setWorkerGroupname(dependency.getWorkerGroupname() == null ? "" : dependency.getWorkerGroupname())
                .build();
                rpcDependencies.add(rpcDependency);
            }
        }
        JobRequestRpc rpcJobRequest = JobRequestRpc.newBuilder()
                .setAlertTypes(response.getAlertTypes() == null ? AlertType.SMS.getCode() : AlertType.getAlertType(response.getAlertTypes()).getCode())
                .setAlertUsers(response.getAlertUsers() == null ? "" : response.getAlertUsers())
                .setCreatorId(response.getCreatorId() == null ? "" : response.getCreatorId())
                .setDescription(response.getDescription() == null ? "" : response.getDescription())
                .setExecutionTimeout(response.getExecutionTimeout() == null ? 0: response.getExecutionTimeout())
                .setId(response.getId())
                .setIsSelfdependent(response.getIsSelfdependent() == null ? true : response.getIsSelfdependent())
                .setJobConfiguration(response.getJobConfiguration() == null ? "" : response.getJobConfiguration())
                .setJobCycle(response.getJobCycle() == null ? JobCycle.MINUTE.getCode(): JobCycle.getJobCycle(response.getJobCycle()).getCode())
                .setJobName(response.getJobName() == null ? "" : response.getJobName())
                .setJobPriority(response.getJobPriority() == null ? JobPriority.LOW.getPriority(): JobPriority.getJobPriority(response.getJobPriority()).getPriority())
                .setJobReleasestate(response.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode() : response.getJobReleasestate())
                .setJobType(response.getJobType() == null ? JobType.SHELL.getCode(): JobType.getJobType(response.getJobType()).getCode())
                .setMaxRetrytimes(response.getMaxRetrytimes() == null ? 0 : response.getMaxRetrytimes())
                .setOwnerIds(response.getOwnerIds() == null ? "" : response.getOwnerIds())
                .setRetryInterval(response.getRetryInterval() == null ? 0 : response.getRetryInterval())
                .setScheduleCron(response.getScheduleCron() == null ? "" : response.getScheduleCron())
                .setWorkerGroupname(response.getWorkerGroupname() == null ? "" : response.getWorkerGroupname())
                .addAllDependencies(rpcDependencies)
               .build();
        return rpcJobRequest;
    }
}
