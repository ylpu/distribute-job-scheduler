package com.ylpu.kepler.scheduler.manager;

import com.ylpu.kepler.scheduler.core.config.Configuration;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.kepler.scheduler.core.utils.DateUtils;
import com.ylpu.kepler.scheduler.enums.AlertType;
import com.ylpu.kepler.scheduler.enums.GrpcType;
import com.ylpu.kepler.scheduler.enums.JobCycle;
import com.ylpu.kepler.scheduler.enums.JobPriority;
import com.ylpu.kepler.scheduler.enums.JobReleaseState;
import com.ylpu.kepler.scheduler.enums.JobType;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.response.WorkerResponse;
import com.ylpu.kepler.scheduler.rpc.client.AbstractJobGrpcClient;
import com.ylpu.kepler.scheduler.rpc.client.JobGrpcBlockingClient;
import com.ylpu.kepler.scheduler.rpc.client.JobGrpcNonBlockingClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class JobSubmission {
    
    private static final long RESOURCE_CHECK_INTERVAL = 3000;
    
    private static final int POOL_SIZE = 2;
    
    private static Log LOG = LogFactory.getLog(JobSubmission.class);
    
    private static ExecutorService es = null;
    
    private static PriorityBlockingQueue<TaskCall> queue = new PriorityBlockingQueue<TaskCall>();

    private static PriorityBlockingQueue<TaskCall> waitingQueue = new PriorityBlockingQueue<TaskCall>();

    static {
        init();
    }
    
    private static void init() {
        int poolSize = Configuration.getInt(Configuration.getConfig("config.properties"), 
                "kepler.scheduler.pool.size", POOL_SIZE);
        es = Executors.newFixedThreadPool(poolSize);
        es.execute(new TaskThread());
        es.execute(new TaskWaitingThread());
    }
    
    public static void addTask(TaskCall taskCall) {
        queue.add(taskCall);
    }
    
    public static void addWaitingTask(TaskCall taskCall) {
        waitingQueue.add(taskCall);
    }
    
    private static class TaskThread implements Runnable{

        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = queue.poll();
                if(taskCall != null) {
                    AbstractJobGrpcClient client = null;
                    try {
                        client = getClient(taskCall.getRpcRequest(),taskCall.getGrpcType());
                        client.submit(taskCall.getRpcRequest());
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
    
    private static class TaskWaitingThread implements Runnable{
        @Override
        public void run() {
            while (true) {
                TaskCall taskCall = waitingQueue.poll();
                if(taskCall != null) {
                    AbstractJobGrpcClient client = null;
                    try {
                        client = getClient(taskCall.getRpcRequest(),taskCall.getGrpcType());
                        client.submitJob(taskCall.getRpcRequest());
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
        while(true) {
            try {      
                worker = MasterManager.getInstance().getIdleWorker(
                        workerGroup, "");
                return worker;
            }catch(Exception e) {
                LOG.error("无法执行任务" + jobId + "因为" + e.getMessage());
            }
            try {
                Thread.sleep(RESOURCE_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
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
    
    public static JobInstanceResponseRpc buildJobStatus(int jobId,Date scheduleTime,TaskState state) {
        return JobInstanceResponseRpc.newBuilder()
        .setResponseId(jobId + "-" + DateUtils.getDateAsString(scheduleTime,DateUtils.TIME_FORMAT))
        .setErrorCode(200)
        .setTaskState(state.getCode())
        .setErrorMsg("")
        .build();
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
                .setAlertTypes(dependency.getAlertTypes() == null ? AlertType.SMS.getCode() : response.getAlertTypes())
                .setAlertUsers(dependency.getAlertUsers() == null ? "" : dependency.getAlertUsers())
                .setCreatorId(dependency.getCreatorId() == null ? "" : dependency.getCreatorId() )
                .setDescription(dependency.getDescription() == null ? "" : dependency.getCreatorId() )
                .setExecutionTimeout(dependency.getExecutionTimeout() == null ? 0: response.getExecutionTimeout())
                .setId(dependency.getId())
                .setIsSelfdependent(dependency.getIsSelfdependent() == null ? true : response.getIsSelfdependent())
                .setJobConfiguration(dependency.getJobConfiguration() == null ? "" : dependency.getJobConfiguration())
                .setJobCycle(dependency.getJobCycle() == null ? JobCycle.MINUTE.getCode(): response.getJobCycle())
                .setJobName(dependency.getJobName() == null ? "" : dependency.getJobName())
                .setJobPriority(dependency.getJobPriority() == null ? JobPriority.LOW.getPriority(): response.getJobPriority())
                .setJobReleasestate(dependency.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode() : response.getJobReleasestate())
                .setJobType(dependency.getJobType() == null ? JobType.SHELL.getCode(): response.getJobType())
                .setMaxRetrytimes(dependency.getMaxRetrytimes() == null ? 0 : response.getMaxRetrytimes())
                .setOwnerIds(dependency.getOwnerIds() == null ? "" : dependency.getOwnerIds())
                .setRetryInterval(dependency.getRetryInterval() == null ? 0 : response.getRetryInterval())
                .setScheduleCron(dependency.getScheduleCron() == null ? "" : dependency.getScheduleCron())
                .setWorkerGroupname(dependency.getWorkerGroupname() == null ? "" : dependency.getWorkerGroupname())
                .build();
                rpcDependencies.add(rpcDependency);
            }
        }
        JobRequestRpc rpcJobRequest = JobRequestRpc.newBuilder()
                .setAlertTypes(response.getAlertTypes() == null ? AlertType.SMS.getCode() : response.getAlertTypes())
                .setAlertUsers(response.getAlertUsers() == null ? "" : response.getAlertUsers())
                .setCreatorId(response.getCreatorId() == null ? "" : response.getCreatorId())
                .setDescription(response.getDescription() == null ? "" : response.getDescription())
                .setExecutionTimeout(response.getExecutionTimeout() == null ? 0: response.getExecutionTimeout())
                .setId(response.getId())
                .setIsSelfdependent(response.getIsSelfdependent() == null ? true : response.getIsSelfdependent())
                .setJobConfiguration(response.getJobConfiguration() == null ? "" : response.getJobConfiguration())
                .setJobCycle(response.getJobCycle() == null ? JobCycle.MINUTE.getCode(): response.getJobCycle())
                .setJobName(response.getJobName() == null ? "" : response.getJobName())
                .setJobPriority(response.getJobPriority() == null ? JobPriority.LOW.getPriority(): response.getJobPriority())
                .setJobReleasestate(response.getJobReleasestate() == null ? JobReleaseState.ONLINE.getCode() : response.getJobReleasestate())
                .setJobType(response.getJobType() == null ? JobType.SHELL.getCode(): response.getJobType())
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
