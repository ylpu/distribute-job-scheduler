package com.ylpu.thales.scheduler.rpc.client;

import java.util.Date;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public abstract class AbstractJobGrpcClient {
    
    public abstract void submitJob(JobInstanceRequestRpc requestRpc);
    
    public abstract void shutdown();
    
    public abstract void kill(JobInstanceRequestRpc requestRpc);
    
    public void setJobInstanceRequest(JobInstanceRequestRpc requestRpc,JobInstanceRequest request) {
        request.setId(requestRpc.getId());
        request.setStartTime(DateUtils.getDatetime(requestRpc.getStartTime()));
        request.setScheduleTime(DateUtils.getDatetime(requestRpc.getScheduleTime()));
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