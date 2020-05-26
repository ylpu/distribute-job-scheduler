package com.ylpu.thales.scheduler.core.rpc.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.24.0-SNAPSHOT)", comments = "Source: job.proto")
public final class GrpcJobServiceGrpc {

    private GrpcJobServiceGrpc() {
    }

    public static final String SERVICE_NAME = "GrpcJobService";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getSubmitMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "submit", requestType = com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc.class, responseType = com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getSubmitMethod() {
        io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getSubmitMethod;
        if ((getSubmitMethod = GrpcJobServiceGrpc.getSubmitMethod) == null) {
            synchronized (GrpcJobServiceGrpc.class) {
                if ((getSubmitMethod = GrpcJobServiceGrpc.getSubmitMethod) == null) {
                    GrpcJobServiceGrpc.getSubmitMethod = getSubmitMethod = io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>newBuilder()
                            .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName(generateFullMethodName(SERVICE_NAME, "submit"))
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(io.grpc.protobuf.ProtoUtils
                                    .marshaller(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc
                                            .getDefaultInstance()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils
                                    .marshaller(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc
                                            .getDefaultInstance()))
                            .setSchemaDescriptor(new GrpcJobServiceMethodDescriptorSupplier("submit")).build();
                }
            }
        }
        return getSubmitMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getKillMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "kill", requestType = com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc.class, responseType = com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getKillMethod() {
        io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> getKillMethod;
        if ((getKillMethod = GrpcJobServiceGrpc.getKillMethod) == null) {
            synchronized (GrpcJobServiceGrpc.class) {
                if ((getKillMethod = GrpcJobServiceGrpc.getKillMethod) == null) {
                    GrpcJobServiceGrpc.getKillMethod = getKillMethod = io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>newBuilder()
                            .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName(generateFullMethodName(SERVICE_NAME, "kill"))
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(io.grpc.protobuf.ProtoUtils
                                    .marshaller(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc
                                            .getDefaultInstance()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils
                                    .marshaller(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc
                                            .getDefaultInstance()))
                            .setSchemaDescriptor(new GrpcJobServiceMethodDescriptorSupplier("kill")).build();
                }
            }
        }
        return getKillMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static GrpcJobServiceStub newStub(io.grpc.Channel channel) {
        return new GrpcJobServiceStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output
     * calls on the service
     */
    public static GrpcJobServiceBlockingStub newBlockingStub(io.grpc.Channel channel) {
        return new GrpcJobServiceBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the
     * service
     */
    public static GrpcJobServiceFutureStub newFutureStub(io.grpc.Channel channel) {
        return new GrpcJobServiceFutureStub(channel);
    }

    /**
     */
    public static abstract class GrpcJobServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         * 一对一服务请求
         * </pre>
         */
        public void submit(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request,
                io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> responseObserver) {
            asyncUnimplementedUnaryCall(getSubmitMethod(), responseObserver);
        }

        /**
         */
        public void kill(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request,
                io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> responseObserver) {
            asyncUnimplementedUnaryCall(getKillMethod(), responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(getSubmitMethod(), asyncUnaryCall(
                            new MethodHandlers<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>(
                                    this, METHODID_SUBMIT)))
                    .addMethod(getKillMethod(), asyncUnaryCall(
                            new MethodHandlers<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>(
                                    this, METHODID_KILL)))
                    .build();
        }
    }

    /**
     */
    public static final class GrpcJobServiceStub extends io.grpc.stub.AbstractStub<GrpcJobServiceStub> {
        private GrpcJobServiceStub(io.grpc.Channel channel) {
            super(channel);
        }

        private GrpcJobServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GrpcJobServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GrpcJobServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         * 一对一服务请求
         * </pre>
         */
        public void submit(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request,
                io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> responseObserver) {
            asyncUnaryCall(getChannel().newCall(getSubmitMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void kill(com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request,
                io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> responseObserver) {
            asyncUnaryCall(getChannel().newCall(getKillMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     */
    public static final class GrpcJobServiceBlockingStub extends io.grpc.stub.AbstractStub<GrpcJobServiceBlockingStub> {
        private GrpcJobServiceBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private GrpcJobServiceBlockingStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GrpcJobServiceBlockingStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GrpcJobServiceBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         * 一对一服务请求
         * </pre>
         */
        public com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc submit(
                com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request) {
            return blockingUnaryCall(getChannel(), getSubmitMethod(), getCallOptions(), request);
        }

        /**
         */
        public com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc kill(
                com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request) {
            return blockingUnaryCall(getChannel(), getKillMethod(), getCallOptions(), request);
        }
    }

    /**
     */
    public static final class GrpcJobServiceFutureStub extends io.grpc.stub.AbstractStub<GrpcJobServiceFutureStub> {
        private GrpcJobServiceFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private GrpcJobServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GrpcJobServiceFutureStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GrpcJobServiceFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         * 一对一服务请求
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> submit(
                com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request) {
            return futureUnaryCall(getChannel().newCall(getSubmitMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc> kill(
                com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc request) {
            return futureUnaryCall(getChannel().newCall(getKillMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_SUBMIT = 0;
    private static final int METHODID_KILL = 1;

    private static final class MethodHandlers<Req, Resp> implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GrpcJobServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GrpcJobServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
            case METHODID_SUBMIT:
                serviceImpl.submit((com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc) request,
                        (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>) responseObserver);
                break;
            case METHODID_KILL:
                serviceImpl.kill((com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc) request,
                        (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc>) responseObserver);
                break;
            default:
                throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
            default:
                throw new AssertionError();
            }
        }
    }

    private static abstract class GrpcJobServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        GrpcJobServiceBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return com.ylpu.thales.scheduler.core.rpc.entity.JobGrpc.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("GrpcJobService");
        }
    }

    private static final class GrpcJobServiceFileDescriptorSupplier extends GrpcJobServiceBaseDescriptorSupplier {
        GrpcJobServiceFileDescriptorSupplier() {
        }
    }

    private static final class GrpcJobServiceMethodDescriptorSupplier extends GrpcJobServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        GrpcJobServiceMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GrpcJobServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new GrpcJobServiceFileDescriptorSupplier())
                            .addMethod(getSubmitMethod()).addMethod(getKillMethod()).build();
                }
            }
        }
        return result;
    }
}
