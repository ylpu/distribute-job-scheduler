package com.ylpu.thales.scheduler.rpc.client;

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
import com.ylpu.thales.scheduler.manager.JobChecker;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
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

    public void submitJob(JobInstanceRequestRpc requestRpc) throws Exception {
        LOG.info("prepare to submit task " + requestRpc.getRequestId() + " to host  " + host + ":" + port);
        JobInstanceRequest request = new JobInstanceRequest();
        setJobInstanceRequest(requestRpc, request);
        try {
            ListenableFuture<JobInstanceResponseRpc> future = futureStub.submit(requestRpc);
            // async callback
            addCallBack(future, executorService, requestRpc, request);
        } catch (Exception e) {
            LOG.error("failed to submit task " + requestRpc.getRequestId() + " to " + host, e);
            try {
                updateTaskStatus(request, TaskState.FAIL.getCode());
                JobInstanceResponseRpc responseRpc = buildResponse(requestRpc, TaskState.FAIL, 500,
                        "failed to execute task " + requestRpc.getRequestId());
                JobChecker.addResponse(responseRpc);
            } catch (Exception e1) {
                LOG.error(e1);
            }
            shutdown();
            rerunIfNeeded(requestRpc);
        }
    }

    private void addCallBack(ListenableFuture<JobInstanceResponseRpc> future, ListeningExecutorService executorService,
            JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        Futures.addCallback(future, new FutureCallback<JobInstanceResponseRpc>() {
            @Override
            public void onSuccess(JobInstanceResponseRpc result) {
                LOG.info("task " + requestRpc.getRequestId() + " execute successful");
                try {
                    updateTaskStatus(request, result.getTaskState());
                    JobChecker.addResponse(result);
                } catch (Exception e) {
                    LOG.error("failed to update task " + requestRpc.getId() +  " status to successful after callback",e);
                }
                shutdown();
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("failed to execute task " + requestRpc.getRequestId(),t);
                try {
                    updateTaskStatus(request, TaskState.FAIL.getCode());
                    JobInstanceResponseRpc responseRpc = buildResponse(requestRpc, TaskState.FAIL, 500,
                            "failed to execute task " + requestRpc.getRequestId());
                    JobChecker.addResponse(responseRpc);
                } catch (Exception e) {
                    LOG.error("failed to update task " + requestRpc.getRequestId() + " status to fail after callback",e);
                }
                shutdown();
                rerunIfNeeded(requestRpc);
            }
        }, executorService);
    }

    @Override
    public void kill(JobInstanceRequestRpc requestRpc) {

    }
}