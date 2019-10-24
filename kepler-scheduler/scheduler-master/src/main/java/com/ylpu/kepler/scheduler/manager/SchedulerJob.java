package com.ylpu.kepler.scheduler.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ylpu.kepler.scheduler.core.rest.JobManager;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.enums.GrpcType;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.rpc.client.JobCallBackScan;

public class SchedulerJob implements Job{
    
    private static Log LOG = LogFactory.getLog(SchedulerJob.class);
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        JobResponse jobResponse = (JobResponse) context.getJobDetail().getJobDataMap().get("data");
        
        JobInstanceRequest request = new JobInstanceRequest();
        request.setScheduleTime(context.getScheduledFireTime());
        request.setStartTime(context.getFireTime());
        
        JobSubmission.initJobInstance(request,jobResponse);
        int jobInstanceId = JobManager.addJobInstance(request);
        request.setId(jobInstanceId);
        
        LOG.info("start to schedule job "  + jobInstanceId + " with schedule time " + 
                context.getScheduledFireTime() + ",next fire time is " + context.getNextFireTime());
        
        JobCallBackScan.addResponse(JobSubmission.buildJobStatus(
                jobResponse.getId(), context.getScheduledFireTime(),TaskState.SUBMIT));
        
        JobInstanceRequestRpc rpcRequest = JobSubmission.initJobInstanceRequestRpc(request,
                jobResponse);
        
        JobSubmission.addTask(new TaskCall(rpcRequest,GrpcType.ASYNC));
    }
}
