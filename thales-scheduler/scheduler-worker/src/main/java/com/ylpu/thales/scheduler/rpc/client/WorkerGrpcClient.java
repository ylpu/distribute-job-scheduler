package com.ylpu.thales.scheduler.rpc.client;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcWorkerServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class WorkerGrpcClient {
    
    private static Log LOG = LogFactory.getLog(WorkerGrpcClient.class);
    private final ManagedChannel channel;
    private final GrpcWorkerServiceGrpc.GrpcWorkerServiceBlockingStub blockStub;
    
    public WorkerGrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockStub = GrpcWorkerServiceGrpc.newBlockingStub(channel);
    }
    public void shutdown() throws InterruptedException{
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    
    public void updateResource(WorkerRequestRpc request){
        WorkerResponseRpc response = blockStub.updateResource(request);
        LOG.info("update worker :" + request.getHost() + " with response code " + response.getErrorCode() 
        + " and message " + response.getErrorMsg());
    }
    
    public void insertOrUpdateGroup(WorkerRequestRpc request){
        WorkerResponseRpc response = blockStub.insertOrUpdateGroup(request);
        LOG.info("update Group :" + request.getNodeGroup() + " with response code " + response.getErrorCode() 
        + " and message " + response.getErrorMsg());
    }
    
    public int updateJobStatus(JobStatusRequestRpc request) throws Exception{
		WorkerResponseRpc response = blockStub.updateJobStatus(request);
        LOG.info("update job status :" + request.getRequestId() + " with response code " + response.getErrorCode() 
        + " and message " + response.getErrorMsg());
        return response.getErrorCode();
    }
    
    public void incTask(WorkerParameter parameter) {
        WorkerResponseRpc response = blockStub.incTask(parameter); 
        LOG.info("increase task for :" + parameter.getHostname() + " with response code " + response.getErrorCode() 
        + " and message " + response.getErrorMsg());
    }
    
    public void decTask(WorkerParameter parameter) {
        WorkerResponseRpc response = blockStub.decTask(parameter);
        LOG.info("decrease task for :" + parameter.getHostname() + " with response code " + response.getErrorCode() 
        + " and message " + response.getErrorMsg());
    }
}