package com.ylpu.thales.scheduler.executor.rpc.server;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ylpu.thales.scheduler.executor.rpc.client.JobMetricImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class WorkerRpcServer {

    private Server server;

    private int serverPort;

    private static Log LOG = LogFactory.getLog(WorkerRpcServer.class);

    public WorkerRpcServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void startServer() throws IOException {
        WorkerRpcServiceImpl jobService = new WorkerRpcServiceImpl();
        jobService.setJobMetric(new JobMetricImpl());
        server = ServerBuilder.forPort(serverPort).addService(jobService).build().start();
        LOG.info("worker start...");
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void shutdownNow() {
        if (server != null) {
            server.shutdownNow();
        }
    }
}
