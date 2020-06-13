package com.ylpu.thales.scheduler.rpc.client;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.manager.JobChecker;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * sync rpc
 *
 */
public class JobGrpcBlockingClient extends AbstractJobGrpcClient {

    private static Log LOG = LogFactory.getLog(JobGrpcBlockingClient.class);

    private final ManagedChannel channel;
    private final GrpcJobServiceGrpc.GrpcJobServiceBlockingStub blockStub;

    public JobGrpcBlockingClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockStub = GrpcJobServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void submitJob(JobInstanceRequestRpc requestRpc) throws Exception {
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc, request);
        try {
            LOG.info("submit task  " + requestRpc.getId() + " to host  " + requestRpc.getWorker());
            responseRpc = blockStub.submit(requestRpc);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            responseRpc = buildResponse(requestRpc, TaskState.FAIL, 500,
                    "failed to execute task " + requestRpc.getId());
        }
        LOG.info("task " + requestRpc.getId() + " return code is " + responseRpc.getErrorCode() + " ,return messsage is "
                + responseRpc.getErrorMsg());
        try {
            updateTaskStatus(request, responseRpc.getTaskState());
            JobChecker.addResponse(responseRpc);
        } catch (Exception e) {
            LOG.error("failed to update task status " + requestRpc.getId(), e);
        }
        if (responseRpc.getErrorCode() != 200) {
            rerunIfNeeded(requestRpc);
        }
    }

    public void kill(JobInstanceRequestRpc requestRpc) throws Exception {
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc, request);
        try {
            responseRpc = blockStub.kill(requestRpc);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            responseRpc = buildResponse(requestRpc, TaskState.RUNNING, 500,
                    "failed to kill task " + requestRpc.getId());
        }
        LOG.info("task " + requestRpc.getId() + " return code is " + responseRpc.getErrorCode() + " ,return message "
                + responseRpc.getErrorMsg());
        try {
            updateTaskStatus(request, responseRpc.getTaskState());
            JobChecker.addResponse(responseRpc);
        } catch (Exception e) {
            LOG.error("failed to update task status " + requestRpc.getId(), e);
        }
        if (responseRpc.getErrorCode() != 200) {
            throw new RuntimeException("failed to kill task " + requestRpc.getId());
        }
    }

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
    }
}