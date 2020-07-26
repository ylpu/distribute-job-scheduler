package com.ylpu.thales.scheduler.master.schedule;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.schedule.JobSubmission;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobResponse;

public class SchedulerJob implements Job {

    private static Log LOG = LogFactory.getLog(SchedulerJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        Integer jobId = (Integer) context.getJobDetail().getJobDataMap().get("id");
        JobResponse jobResponse = getJobResponseById(jobId);
        JobInstanceRequest request = new JobInstanceRequest();
        JobInstanceRequestRpc rpcRequest = null;
        if(jobResponse != null) {
            try {
                JobSubmission.initJobInstance(request, jobResponse.getId());
                request.setScheduleTime(context.getScheduledFireTime());
                request.setStartTime(new Date());
                request.setId(addJobInstance(request,jobId));
                rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobResponse);
              //transit task status to scheduled
                transitTaskStatusToScheduled(rpcRequest.getId(),TaskState.SCHEDULED.getCode());
                JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(rpcRequest.getRequestId(), TaskState.SCHEDULED.getCode());
                JobStatusChecker.addResponse(responseRpc);
              //caculate dependency and add to request
                JobSubmission.addRpcRequest(rpcRequest);
            } catch (Exception e) {
                LOG.error(
                        "fail to update job " + rpcRequest.getId(),e);
                scheduleFailed(request);
                JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(rpcRequest.getRequestId(), TaskState.FAIL.getCode());
                JobStatusChecker.addResponse(responseRpc);
            } 
        }
    }
    
    private void transitTaskStatusToScheduled(Integer taskId, Integer statusCode) throws Exception {
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(taskId);
        request.setTaskState(statusCode);
        JobManager.updateJobInstanceSelective(request);
    }
    
    private void scheduleFailed(JobInstanceRequest request) {
        request.setTaskState(TaskState.FAIL.getCode());
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        try {
            JobManager.updateJobInstanceSelective(request);
        } catch (Exception e1) {
            LOG.error(e1);
            throw new RuntimeException(e1);
        }
    }
    
    private JobResponse getJobResponseById(Integer jobId) {
        JobResponse jobResponse = null;
        try {
            jobResponse = JobManager.getJobById(jobId);
        } catch (Exception e1) {
            LOG.error("can not get job detail " + jobId);
            throw new RuntimeException(e1);
        }
        return jobResponse;
    }
    
    private int addJobInstance(JobInstanceRequest request, int jobId) {
        int jobInstanceId = 0;
        try {
            jobInstanceId = JobManager.addJobInstance(request);
        } catch (Exception e) {
            LOG.error("fail to add instance for job " + jobId);
            throw new RuntimeException(e);
        }
        return jobInstanceId;
    }
}
