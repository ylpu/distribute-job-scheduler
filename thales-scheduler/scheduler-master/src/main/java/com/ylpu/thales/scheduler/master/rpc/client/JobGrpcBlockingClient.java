package com.ylpu.thales.scheduler.master.rpc.client;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
//import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
//import com.ylpu.thales.scheduler.master.schedule.JobSubmission;
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
    public void submitJob(JobInstanceRequestRpc rpcRequest){

    }

    public void kill(JobInstanceRequestRpc rpcRequest){
        JobInstanceResponseRpc rpcResponse =  blockStub.kill(rpcRequest);
        LOG.info("task " + rpcRequest.getRequestId() + " return code is " + rpcResponse.getErrorCode() + " ,return message "
                + rpcResponse.getErrorMsg());
        JobStatusChecker.addResponse(rpcResponse);
        //remove request after kill
        JobStatusChecker.getJobInstanceRequestMap().remove(rpcRequest.getRequestId());
        if (rpcResponse.getErrorCode() != 200) {
            throw new RuntimeException("failed to kill task " + rpcRequest.getRequestId());
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