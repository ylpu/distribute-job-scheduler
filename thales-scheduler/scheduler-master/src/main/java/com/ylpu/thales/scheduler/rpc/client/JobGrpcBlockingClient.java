package com.ylpu.thales.scheduler.rpc.client;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * 同步rpc
 *
 */
public class JobGrpcBlockingClient extends AbstractJobGrpcClient{
    
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
        JobInstanceRequest request  = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc,request);
        try {
             LOG.info("准备提交任务 " + requestRpc.getId() + " 到节点  " + requestRpc.getWorker());
             responseRpc = blockStub.submit(requestRpc);
        }catch(Exception e) {
             LOG.error(e.getMessage());
             responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                     "failed to execute task " + requestRpc.getId());
        }
        try {
            updateTaskStatus(request,responseRpc.getTaskState());
        }catch(Exception e) {
            responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                    "failed to execute task " + requestRpc.getId());
        }
        JobStatusCheck.addResponse(responseRpc);
        LOG.info("任务 " + requestRpc.getId() + " 返回值 " + 
        responseRpc.getErrorCode() + " ,返回消息 " +  responseRpc.getErrorMsg());
        if(responseRpc.getErrorCode() != 200) {
          	rerunIfNeeded(requestRpc);
        }
    }
    
    public void kill(JobInstanceRequestRpc requestRpc) throws Exception{
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request  = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc,request);
        try {
            responseRpc = blockStub.kill(requestRpc);
        }catch(Exception e) {
            LOG.error(e.getMessage());
            responseRpc = buildResponse(requestRpc,TaskState.RUNNING,500,
                    "failed to kill task " + requestRpc.getId());
        }
        
        LOG.info("任务 " + requestRpc.getId() + " 返回值 " + 
        responseRpc.getErrorCode() + " ,返回消息 " +  responseRpc.getErrorMsg());
        try {
            updateTaskStatus(request,responseRpc.getTaskState());
        }catch(Exception e) {
            LOG.error(e.getMessage());
            responseRpc = buildResponse(requestRpc,TaskState.RUNNING,500,
                    "failed to kill task " + requestRpc.getId());
        }
        JobStatusCheck.getJobInstanceRequestMap().remove(requestRpc.getRequestId());
        JobStatusCheck.addResponse(responseRpc);
        
        if(responseRpc.getErrorCode() != 200) {
            throw new RuntimeException("failed to kill task " + requestRpc.getId());
        }
    }
    
    public void shutdown(){
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
    }
}