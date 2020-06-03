package com.ylpu.thales.scheduler.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;

public class SchedulerJob implements Job {

    private static Log LOG = LogFactory.getLog(SchedulerJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        //get job instance details by job instance id
        Integer id = (Integer) context.getJobDetail().getJobDataMap().get("id");
        JobInstanceResponse jobInstanceResponse = null;
        try {
            jobInstanceResponse = JobManager.getJobInstanceById(id);
        } catch (Exception e1) {
            LOG.error("can not get job instance detail " + id);
            throw new RuntimeException(e1);
        }
        //init rpc request
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(id);
        request.setScheduleTime(context.getScheduledFireTime());
        request.setStartTime(context.getFireTime());
        JobSubmission.initJobInstance(request, jobInstanceResponse.getJobConf());
        JobInstanceRequestRpc rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobInstanceResponse.getJobConf());
        
        try {
            JobSubmission.scheduleJob(rpcRequest);
        } catch (Exception e) {
            LOG.error("fail to update job " + rpcRequest.getId() + " to waiting status with exception " + e.getMessage());
            JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(rpcRequest, TaskState.FAIL, 500,
                    "fail to update job " + rpcRequest.getId() + " to fail status");
            JobChecker.addResponse(responseRpc);
        }
    }
}
