package com.ylpu.thales.scheduler.rpc.client;

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.rest.service.SchedulerService;

public abstract class AbstractJobGrpcClient {

    private static Log LOG = LogFactory.getLog(AbstractJobGrpcClient.class);

    public abstract void submitJob(JobInstanceRequestRpc requestRpc) throws Exception;

    public abstract void shutdown();

    public abstract void kill(JobInstanceRequestRpc requestRpc) throws Exception;

    public void setJobInstanceRequest(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        request.setId(requestRpc.getId());
        request.setStartTime(DateUtils.getDatetime(requestRpc.getStartTime()));
        request.setScheduleTime(DateUtils.getDatetime(requestRpc.getScheduleTime()));
    }

    public void updateTaskStatus(JobInstanceRequest request, int code) throws Exception {
        request.setTaskState(code);
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        JobManager.updateJobInstanceSelective(request);
    }

    public JobInstanceResponseRpc buildResponse(JobInstanceRequestRpc requestRpc, TaskState taskState, int errorCode,
            String errorMsg) {
        return JobInstanceResponseRpc.newBuilder().setResponseId(requestRpc.getRequestId()).setErrorCode(errorCode)
                .setTaskState(taskState.getCode()).setErrorMsg(errorMsg).build();
    }

    public void rerunIfNeeded(JobInstanceRequestRpc requestRpc) {
        Properties prop = Configuration.getConfig();
        int retryInterval = Configuration.getInt(prop, "thales.scheduler.job.retry.interval", 1);
        try {
            JobInstanceResponse jobInstance = JobManager.getJobInstanceById(requestRpc.getId());
            if (jobInstance.getRetryTimes() < jobInstance.getJobConf().getMaxRetrytimes()) {
                Thread.sleep(retryInterval * 1000 * 60);
                new SchedulerService().rerun(requestRpc.getId());
            }
        } catch (Exception e) {
            LOG.error(e);
            try {
                Thread.sleep(retryInterval * 1000 * 60);
            } catch (InterruptedException e1) {
                LOG.error(e1);
            }
            rerunIfNeeded(requestRpc);
        }
    }
}