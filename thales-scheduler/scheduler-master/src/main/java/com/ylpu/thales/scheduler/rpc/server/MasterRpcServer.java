package com.ylpu.thales.scheduler.rpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MasterRpcServer {

    private static Log LOG = LogFactory.getLog(MasterRpcServer.class);

    private int port;

    public MasterRpcServer(int port) {
        this.port = port;
    }

    private Server server;

    public void start(){
        try {
            server = ServerBuilder.forPort(port).addService(new MasterRpcServiceImpl()).build().start();
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread()); 
        }catch(Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    private class ShutdownHookThread extends Thread {
        @Override
        public void run() {
            LOG.error("*** shutting down rpc server since JVM is shutting down");
            MasterRpcServer.this.stop();
            LOG.error("*** server shut down");
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}