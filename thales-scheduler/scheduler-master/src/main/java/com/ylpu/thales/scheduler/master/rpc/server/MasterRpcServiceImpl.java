package com.ylpu.thales.scheduler.master.rpc.server;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcWorkerServiceGrpc;
import com.ylpu.thales.scheduler.core.utils.ByteUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.api.service.SchedulerService;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;

import io.grpc.stub.StreamObserver;

public class MasterRpcServiceImpl extends GrpcWorkerServiceGrpc.GrpcWorkerServiceImplBase {

    private static Log LOG = LogFactory.getLog(MasterRpcServiceImpl.class);

    public void insertOrUpdateGroup(WorkerRequestRpc request, StreamObserver<WorkerResponseRpc> responseObserver) {
        WorkerResponseRpc.Builder builder = WorkerResponseRpc.newBuilder();
        try {
            MasterManager.getInstance().insertOrUpdateGroup(request.getWorkerGroup());
            builder.setErrorCode(200);
            builder.setErrorMsg("");
        } catch (Exception e) {
            LOG.error(e);
            builder.setErrorCode(500);
            builder.setErrorMsg(e.getMessage());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 增加worker任务个数
     */
    public void incTask(WorkerParameter parameter, StreamObserver<WorkerResponseRpc> responseObserver) {
        WorkerResponseRpc.Builder builder = WorkerResponseRpc.newBuilder();
        try {
            MasterManager.getInstance().increaseTask(parameter.getHostname());
            builder.setErrorCode(200);
            builder.setErrorMsg("");
        } catch (Exception e) {
            LOG.error(e);
            builder.setErrorCode(500);
            builder.setErrorMsg(e.getMessage());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 减少worker任务个数
     */
    public void decTask(WorkerParameter parameter, StreamObserver<WorkerResponseRpc> responseObserver) {
        WorkerResponseRpc.Builder builder = WorkerResponseRpc.newBuilder();
        try {
            MasterManager.getInstance().decreaseTask(parameter.getHostname());
            builder.setErrorCode(200);
            builder.setErrorMsg("");
        } catch (Exception e) {
            LOG.error(e);
            builder.setErrorCode(500);
            builder.setErrorMsg(e.getMessage());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 更新任务状态
     */
    public void updateJobStatus(JobStatusRequestRpc request, StreamObserver<WorkerResponseRpc> responseObserver) {
        WorkerResponseRpc.Builder builder = WorkerResponseRpc.newBuilder();
        try {
            Object obj = ByteUtils.byteArrayToObject(request.getData().toByteArray());
            if (obj instanceof JobInstanceRequest) {
                
                JobInstanceRequest jobInstanceRequest = (JobInstanceRequest) obj;
                JobManager.updateJobInstanceSelective(jobInstanceRequest);
                JobInstanceResponseRpc responseRpc = setJobStatus(request);
                JobStatusChecker.addResponse(responseRpc);
                builder.setErrorCode(200);
                builder.setErrorMsg("");
                
                if(request.getTaskState() == TaskState.FAIL.getCode()) {
                    rerunIfNeeded(jobInstanceRequest.getId());
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            builder.setErrorCode(500);
            builder.setErrorMsg(e.getMessage());
        } finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }

    private JobInstanceResponseRpc setJobStatus(JobStatusRequestRpc requestRpc) {
        return JobInstanceResponseRpc.newBuilder().setResponseId(requestRpc.getRequestId()).setErrorCode(200)
                .setTaskState(requestRpc.getTaskState()).setErrorMsg("").build();
    }
    
    public void rerunIfNeeded(Integer taskId) {
        Properties prop = Configuration.getConfig();
        int retryInterval = Configuration.getInt(prop, "thales.scheduler.job.retry.interval", 1);
        try {
            JobInstanceResponse jobInstance = JobManager.getJobInstanceById(taskId);
            if (jobInstance.getRetryTimes() < jobInstance.getJobConf().getMaxRetrytimes()) {
                Thread.sleep(retryInterval * 1000 * 60);
                new SchedulerService().rerun(taskId);
            }
        } catch (Exception e) {
            LOG.error(e);
            try {
                Thread.sleep(retryInterval * 1000 * 60);
            } catch (InterruptedException e1) {
                LOG.error(e1);
            }
            rerunIfNeeded(taskId);
        }
    }
}
