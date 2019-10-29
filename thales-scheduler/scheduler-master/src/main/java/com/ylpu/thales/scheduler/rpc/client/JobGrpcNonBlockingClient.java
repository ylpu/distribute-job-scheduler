package com.ylpu.thales.scheduler.rpc.client;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * 异步rpc
 *
 */
public class JobGrpcNonBlockingClient extends AbstractJobGrpcClient{
    
    private static ListeningExecutorService executorService = null;
    
    private String host;
    private int port;
    
    static {
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()/2));
    }
    
    private static Log LOG = LogFactory.getLog(JobGrpcNonBlockingClient.class);
    
    private final ManagedChannel channel;
    private final GrpcJobServiceGrpc.GrpcJobServiceFutureStub futureStub;
    
    public JobGrpcNonBlockingClient(String host, int port) {
        this.host = host;
        this.port = port;
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        futureStub = GrpcJobServiceGrpc.newFutureStub(channel);
    }
    
    public void shutdown(){
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
    }

    public void submit(JobInstanceRequestRpc requestRpc){
        JobInstanceResponseRpc responseRpc = null;
        JobInstanceRequest request  = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc,request);
        try {
            //无依赖直接执行
            if(requestRpc.getJob().getDependenciesList() == null 
                    || requestRpc.getJob().getDependenciesList().size() == 0) {
                submitJob(requestRpc);
            }
            //有依赖的话加入等待
            else {
                JobCallBackScan.putCallback(requestRpc);
                List<JobDependency> dependJobs = getLatestJobDepends(requestRpc);
                JobCallBackScan.addDepends(dependJobs, requestRpc.getRequestId());
                
                request.setTaskState(TaskState.WAITING.getCode());
                JobManager.updateJobInstanceSelective(request);
            }
        }catch(Exception e) {
             LOG.error("任务 " + requestRpc.getId() + " 执行失败,异常" + e.getMessage());
             updateTaskStatus(request,TaskState.FAIL.getCode());
             responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                     "failed to execute task " + requestRpc.getId());
             JobCallBackScan.addResponse(responseRpc);
             shutdown();
        }
    }
    
    public void submitJob(JobInstanceRequestRpc requestRpc){
        LOG.info("准备提交任务 " + requestRpc.getId() + " 到节点  " + host + ":" + port);
        JobInstanceRequest request  = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc,request);
        try {
            ListenableFuture<JobInstanceResponseRpc> future = futureStub.submit(requestRpc);
            //异步回调
            addCallBack(future,executorService,requestRpc,request);
        }catch(Exception e) {
             LOG.error("任务 " + requestRpc.getId() + " 执行失败,异常" + e.getMessage());
             updateTaskStatus(request,TaskState.FAIL.getCode());
             JobInstanceResponseRpc responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                     "failed to execute task " + requestRpc.getId());
             JobCallBackScan.addResponse(responseRpc);
             shutdown();
        }
    }
    
    private void addCallBack(ListenableFuture<JobInstanceResponseRpc> future,ListeningExecutorService executorService,
            JobInstanceRequestRpc requestRpc,JobInstanceRequest request) {
        Futures.addCallback(
                future,
                new FutureCallback<JobInstanceResponseRpc>() {
                    @Override
                    public void onSuccess(JobInstanceResponseRpc result) {
                        LOG.info("任务" + requestRpc.getId() + "执行完成");
                        updateTaskStatus(request,result.getTaskState());
                        JobCallBackScan.addResponse(result);
                        shutdown();
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        LOG.error("任务" + requestRpc.getId() + "执行失败,异常" + t.getMessage());
                        updateTaskStatus(request,TaskState.FAIL.getCode());
                        JobInstanceResponseRpc responseRpc = buildResponse(requestRpc,TaskState.FAIL,500,
                                "failed to execute task " + requestRpc.getId());;
                        JobCallBackScan.addResponse(responseRpc);
                        shutdown();
                    }
                },executorService);
    }

    @Override
    public void kill(JobInstanceRequestRpc requestRpc) {
        
    }
}