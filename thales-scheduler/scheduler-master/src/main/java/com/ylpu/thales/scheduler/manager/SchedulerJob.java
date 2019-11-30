package com.ylpu.thales.scheduler.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.rpc.client.JobStatusCheck;

public class SchedulerJob implements Job{
    
    private static Log LOG = LogFactory.getLog(SchedulerJob.class);
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        Integer id = (Integer) context.getJobDetail().getJobDataMap().get("id");
        JobResponse jobResponse = null;
		try {
			jobResponse = JobManager.getJobById(id);
		} catch (Exception e1) {
			LOG.error(e1);
			throw new RuntimeException("can not get job detail for jobid " + id);
		}
        
        JobInstanceRequest request = new JobInstanceRequest();
        request.setScheduleTime(context.getScheduledFireTime());
        request.setStartTime(context.getFireTime());
        
        JobSubmission.initJobInstance(request,jobResponse);
        int jobInstanceId = 0;
		try {
			jobInstanceId = JobManager.addJobInstance(request);
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("failed to add job instance");
		}
        request.setId(jobInstanceId);
        
        LOG.info("start to schedule job "  + jobInstanceId + " with schedule time " + 
                context.getScheduledFireTime());
        
        JobStatusCheck.addResponse(JobSubmission.buildJobStatus(
                jobResponse, context.getScheduledFireTime(),TaskState.SUBMIT));
        
        JobInstanceRequestRpc rpcRequest = JobSubmission.initJobInstanceRequestRpc(request,
                jobResponse);
        
        JobSubmission.addJob(rpcRequest);
    }
}
