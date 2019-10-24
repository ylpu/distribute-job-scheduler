package com.ylpu.thales.scheduler.rpc.client;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.utils.CronUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public abstract class AbstractJobGrpcClient {

    public abstract void submit(JobInstanceRequestRpc requestRpc);
    
    public abstract void submitJob(JobInstanceRequestRpc requestRpc);
    
    public abstract void shutdown();
    
    public abstract void kill(JobInstanceRequestRpc requestRpc);
    
    public void setJobInstanceRequest(JobInstanceRequestRpc requestRpc,JobInstanceRequest request) {
        request.setId(requestRpc.getId());
        request.setStartTime(DateUtils.getDatetime(requestRpc.getStartTime()));
        request.setScheduleTime(DateUtils.getDatetime(requestRpc.getScheduleTime()));
    }
    
    /**
     * 获取当前任务依赖，得到离当前任务调度时间最近的依赖任务
     * @param request
     * @return
     */
    public String getLatestJobDepends(JobInstanceRequestRpc request) {
        StringBuilder builder = new StringBuilder();
        List<JobRequestRpc> dependencies = request.getJob().getDependenciesList();
        Date currentJobScheduleTime = DateUtils.getDatetime(request.getScheduleTime());
        Iterator<JobRequestRpc> it = dependencies.iterator();
        while(it.hasNext()) {
            JobRequestRpc job = it.next();
            String depend = job.getId() + "-" + CronUtils.getLatestTriggerTime(job.getScheduleCron(),
                    DateUtils.getTime(currentJobScheduleTime, job.getJobCycle(),-1),currentJobScheduleTime);
            builder.append(depend);
            if(it.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
    
    public void updateTaskStatus(JobInstanceRequest request,int code) {
        request.setTaskState(code);
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(),request.getEndTime()));
        JobManager.updateJobInstanceSelective(request);
    }
    
    public JobInstanceResponseRpc buildResponse(JobInstanceRequestRpc requestRpc,
            TaskState taskState,int errorCode,String errorMsg) {
        return JobInstanceResponseRpc.newBuilder()
        .setResponseId(requestRpc.getRequestId())
        .setErrorCode(errorCode)
        .setTaskState(taskState.getCode())
        .setErrorMsg(errorMsg)
        .build();
    }
}