package com.ylpu.thales.scheduler.rpc.client;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ylpu.thales.scheduler.core.rest.JobManager;
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
    
    private String host;
    private int port;
    
    public JobGrpcBlockingClient(String host, int port) {
        this.host = host;
        this.port = port;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockStub = GrpcJobServiceGrpc.newBlockingStub(channel);
    }

    public void submit(JobInstanceRequestRpc requestRpc){
        LOG.info("准备提交任务 " + requestRpc.getId() + " 到节点  " + host + ":" + port);
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request  = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc,request);
        try {
            //无依赖直接执行
            if(requestRpc.getJob().getDependenciesList() == null 
                    || requestRpc.getJob().getDependenciesList().size() == 0) {
                submitJob(requestRpc);
            }
            //有依赖的话需要父任务执行完才能执行
            else {
                synchronized(requestRpc) {
                    JobCallBackScan.putCallback(requestRpc);
                    String parentJobs = getLatestJobDepends(requestRpc);
                    JobCallBackScan.addDepends(parentJobs, requestRpc.getRequestId());
                    
                    request.setTaskState(TaskState.WAITING.getCode());
                    JobManager.updateJobInstanceSelective(request);
                }
            }
        }catch(Exception e) {
             LOG.error(e.getMessage());
             updateTaskStatus(request,TaskState.FAIL.getCode());
             responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                     "failed to execute task " + requestRpc.getId());
        }
        JobCallBackScan.addResponse(responseRpc);
        LOG.info("任务 " + requestRpc.getId() + " 返回值 " + 
        responseRpc.getErrorCode() + " ,返回消息 " +  responseRpc.getErrorMsg()); 
    }

    @Override
    public void submitJob(JobInstanceRequestRpc requestRpc) {
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
        updateTaskStatus(request,responseRpc.getTaskState());
        JobCallBackScan.addResponse(responseRpc);
        LOG.info("任务 " + requestRpc.getId() + " 返回值 " + 
        responseRpc.getErrorCode() + " ,返回消息 " +  responseRpc.getErrorMsg());
    }
    
    public void kill(JobInstanceRequestRpc requestRpc){
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
        
        updateTaskStatus(request,responseRpc.getTaskState());
        
        JobCallBackScan.addResponse(responseRpc);
        if(responseRpc.getErrorCode() == 500) {
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