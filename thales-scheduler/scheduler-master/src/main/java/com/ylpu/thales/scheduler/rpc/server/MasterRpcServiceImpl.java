package com.ylpu.thales.scheduler.rpc.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcWorkerServiceGrpc;
import com.ylpu.thales.scheduler.core.utils.ByteUtils;
import com.ylpu.thales.scheduler.master.MasterManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.schedule.JobChecker;

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
            }
            JobInstanceResponseRpc responseRpc = setJobStatus(request);
            JobChecker.addResponse(responseRpc);
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

    private JobInstanceResponseRpc setJobStatus(JobStatusRequestRpc requestRpc) {
        return JobInstanceResponseRpc.newBuilder().setResponseId(requestRpc.getRequestId()).setErrorCode(200)
                .setTaskState(requestRpc.getTaskState()).setErrorMsg("").build();
    }
}
