package com.ylpu.thales.scheduler.master.rpc.server;

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
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
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
        }finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted(); 
        }
    }

    /**
     * increase task number of worker
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
        }finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted(); 
        }
    }

    /**
     * decrease task number of worker
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
        }finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted(); 
        }
    }

    /**
     * udpate job status
     */
    public void updateJobStatus(JobStatusRequestRpc request, StreamObserver<WorkerResponseRpc> responseObserver) {
        WorkerResponseRpc.Builder builder = WorkerResponseRpc.newBuilder();
        try {
            Object obj = ByteUtils.byteArrayToObject(request.getData().toByteArray());
            if (obj instanceof JobInstanceRequest) {
                
                JobInstanceRequest jobInstanceRequest = (JobInstanceRequest) obj;
                JobManager.transitTaskStatus(jobInstanceRequest);
                JobInstanceResponseRpc responseRpc = JobInstanceResponseRpc.newBuilder()
                        .setResponseId(request.getRequestId())
                        .setErrorCode(200)
                        .setTaskState(jobInstanceRequest.getTaskState())
                        .setErrorMsg("").build();
                JobStatusChecker.addResponse(responseRpc);
                builder.setErrorCode(200);
                builder.setErrorMsg("");
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
}
