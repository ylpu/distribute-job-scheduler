package com.ylpu.thales.scheduler.master.rpc.client;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.schedule.JobSubmission;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * async rpc
 *
 */
public class JobGrpcNonBlockingClient extends AbstractJobGrpcClient {

    private static ListeningExecutorService executorService = null;

    private String host;
    private int port;

    static {
        executorService = MoreExecutors
                .listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2));
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

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
    }

    public void submitJob(JobInstanceRequestRpc rpcRequest){
        try {
            LOG.info("prepare to submit task " + rpcRequest.getRequestId() + " to host  " + host + ":" + port);
            ListenableFuture<JobInstanceResponseRpc> future = futureStub.submit(rpcRequest);
            // async callback
            addCallBack(future, executorService, rpcRequest);
        }catch(Exception e) {
            LOG.error("fail to submit task " + rpcRequest.getId() + " with exception " + e.getMessage());
            try {
                transitTaskStatus(rpcRequest, TaskState.FAIL.getCode());
                JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(rpcRequest.getRequestId(), TaskState.FAIL.getCode());
                JobStatusChecker.addResponse(responseRpc);
            } catch (Exception e1) {
                LOG.error(e); 
            } finally {
                shutdown();
            }
            rerunIfNeeded(rpcRequest);
        }
    }

    private void addCallBack(ListenableFuture<JobInstanceResponseRpc> future, ListeningExecutorService executorService,
            JobInstanceRequestRpc rpcRequest) {
        Futures.addCallback(future, new FutureCallback<JobInstanceResponseRpc>() {
            @Override
            public void onSuccess(JobInstanceResponseRpc result) {
                LOG.info("task " + rpcRequest.getRequestId() + " execute successful");
                try {
                    transitTaskStatus(rpcRequest, result.getTaskState());
                    JobStatusChecker.addResponse(result);
                    //remove request after execute successful
                    JobStatusChecker.getJobInstanceRequestMap().remove(rpcRequest.getRequestId());
                } catch (Exception e) {
                    LOG.error(e);
                } finally {
                    shutdown();
                }
                if(result.getTaskState() == TaskState.FAIL.getCode()) {
                    rerunIfNeeded(rpcRequest);
                }
            }
            //network exception or some unknown exception
            @Override
            public void onFailure(Throwable t) {
                LOG.error("failed to execute task " + rpcRequest.getRequestId(),t);
                try {
                    transitTaskStatus(rpcRequest, TaskState.FAIL.getCode());
                    JobInstanceResponseRpc rpcResponse = JobSubmission.buildResponse(rpcRequest.getRequestId(), TaskState.FAIL.getCode());
                    JobStatusChecker.addResponse(rpcResponse);
                    //remove request after execute fail
                    JobStatusChecker.getJobInstanceRequestMap().remove(rpcRequest.getRequestId());
                } catch (Exception e) {
                    LOG.error("failed to update task " + rpcRequest.getRequestId() + " status to fail after callback",e);
                } finally {
                    shutdown();
                }
                rerunIfNeeded(rpcRequest);
            }
        }, executorService);
    }

    @Override
    public void kill(JobInstanceRequestRpc rpcRequest) {

    }
}