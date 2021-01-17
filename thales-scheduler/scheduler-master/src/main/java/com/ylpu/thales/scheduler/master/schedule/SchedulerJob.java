package com.ylpu.thales.scheduler.master.schedule;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
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
                JobSubmission.initJobInstance(request, jobResponse);
                LOG.info("job " + jobResponse.getId() + " schedule at " + context.getScheduledFireTime());
                LOG.info("job " + jobResponse.getId() + " start to run at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
                request.setScheduleTime(context.getScheduledFireTime());
                request.setStartTime(new Date());
                request.setTaskState(TaskState.SCHEDULED.getCode());
                request.setId(addJobInstance(request,jobId));
                
                rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobResponse);
              //caculate dependency and add to request
                JobSubmission.addRpcRequest(rpcRequest);
            } catch (Exception e) {
                LOG.error("fail to schedule job " + rpcRequest.getId(),e);
            } 
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
