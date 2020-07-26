package com.ylpu.thales.scheduler.master.rpc.client;

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.master.api.service.SchedulerService;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;

public abstract class AbstractJobGrpcClient {

    private static Log LOG = LogFactory.getLog(AbstractJobGrpcClient.class);

    public abstract void submitJob(JobInstanceRequestRpc requestRpc) throws Exception;

    public abstract void shutdown();

    public abstract void kill(JobInstanceRequestRpc requestRpc) throws Exception;

    public void transitTaskStatus(JobInstanceRequestRpc requestRpc, int code) throws Exception {
        JobInstanceRequest request = new JobInstanceRequest();
        request.setId(requestRpc.getId());
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(DateUtils.getDatetime(requestRpc.getStartTime()), request.getEndTime()));
        request.setTaskState(code);
        JobManager.updateJobInstanceSelective(request);
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