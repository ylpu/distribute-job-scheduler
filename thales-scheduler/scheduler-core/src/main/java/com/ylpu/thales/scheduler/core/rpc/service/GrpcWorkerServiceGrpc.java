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
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0-SNAPSHOT)",
    comments = "Source: worker.proto")
public final class GrpcWorkerServiceGrpc {

  private GrpcWorkerServiceGrpc() {}

  public static final String SERVICE_NAME = "GrpcWorkerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getIncTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "incTask",
      requestType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter.class,
      responseType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getIncTaskMethod() {
    io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getIncTaskMethod;
    if ((getIncTaskMethod = GrpcWorkerServiceGrpc.getIncTaskMethod) == null) {
      synchronized (GrpcWorkerServiceGrpc.class) {
        if ((getIncTaskMethod = GrpcWorkerServiceGrpc.getIncTaskMethod) == null) {
          GrpcWorkerServiceGrpc.getIncTaskMethod = getIncTaskMethod =
              io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "incTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcWorkerServiceMethodDescriptorSupplier("incTask"))
              .build();
        }
      }
    }
    return getIncTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getDecTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "decTask",
      requestType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter.class,
      responseType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getDecTaskMethod() {
    io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getDecTaskMethod;
    if ((getDecTaskMethod = GrpcWorkerServiceGrpc.getDecTaskMethod) == null) {
      synchronized (GrpcWorkerServiceGrpc.class) {
        if ((getDecTaskMethod = GrpcWorkerServiceGrpc.getDecTaskMethod) == null) {
          GrpcWorkerServiceGrpc.getDecTaskMethod = getDecTaskMethod =
              io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "decTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcWorkerServiceMethodDescriptorSupplier("decTask"))
              .build();
        }
      }
    }
    return getDecTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateResourceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "updateResource",
      requestType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc.class,
      responseType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateResourceMethod() {
    io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateResourceMethod;
    if ((getUpdateResourceMethod = GrpcWorkerServiceGrpc.getUpdateResourceMethod) == null) {
      synchronized (GrpcWorkerServiceGrpc.class) {
        if ((getUpdateResourceMethod = GrpcWorkerServiceGrpc.getUpdateResourceMethod) == null) {
          GrpcWorkerServiceGrpc.getUpdateResourceMethod = getUpdateResourceMethod =
              io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "updateResource"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcWorkerServiceMethodDescriptorSupplier("updateResource"))
              .build();
        }
      }
    }
    return getUpdateResourceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateJobStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "updateJobStatus",
      requestType = com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc.class,
      responseType = com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc,
      com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateJobStatusMethod() {
    io.grpc.MethodDescriptor<com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> getUpdateJobStatusMethod;
    if ((getUpdateJobStatusMethod = GrpcWorkerServiceGrpc.getUpdateJobStatusMethod) == null) {
      synchronized (GrpcWorkerServiceGrpc.class) {
        if ((getUpdateJobStatusMethod = GrpcWorkerServiceGrpc.getUpdateJobStatusMethod) == null) {
          GrpcWorkerServiceGrpc.getUpdateJobStatusMethod = getUpdateJobStatusMethod =
              io.grpc.MethodDescriptor.<com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc, com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "updateJobStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc.getDefaultInstance()))
              .setSchemaDescriptor(new GrpcWorkerServiceMethodDescriptorSupplier("updateJobStatus"))
              .build();
        }
      }
    }
    return getUpdateJobStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GrpcWorkerServiceStub newStub(io.grpc.Channel channel) {
    return new GrpcWorkerServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GrpcWorkerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new GrpcWorkerServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GrpcWorkerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new GrpcWorkerServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class GrpcWorkerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void incTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnimplementedUnaryCall(getIncTaskMethod(), responseObserver);
    }

    /**
     */
    public void decTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnimplementedUnaryCall(getDecTaskMethod(), responseObserver);
    }

    /**
     */
    public void updateResource(com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateResourceMethod(), responseObserver);
    }

    /**
     */
    public void updateJobStatus(com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateJobStatusMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getIncTaskMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>(
                  this, METHODID_INC_TASK)))
          .addMethod(
            getDecTaskMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter,
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>(
                  this, METHODID_DEC_TASK)))
          .addMethod(
            getUpdateResourceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc,
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>(
                  this, METHODID_UPDATE_RESOURCE)))
          .addMethod(
            getUpdateJobStatusMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc,
                com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>(
                  this, METHODID_UPDATE_JOB_STATUS)))
          .build();
    }
  }

  /**
   */
  public static final class GrpcWorkerServiceStub extends io.grpc.stub.AbstractStub<GrpcWorkerServiceStub> {
    private GrpcWorkerServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcWorkerServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcWorkerServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcWorkerServiceStub(channel, callOptions);
    }

    /**
     */
    public void incTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getIncTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void decTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDecTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateResource(com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateResourceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateJobStatus(com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc request,
        io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateJobStatusMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class GrpcWorkerServiceBlockingStub extends io.grpc.stub.AbstractStub<GrpcWorkerServiceBlockingStub> {
    private GrpcWorkerServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcWorkerServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcWorkerServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcWorkerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc incTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request) {
      return blockingUnaryCall(
          getChannel(), getIncTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc decTask(com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request) {
      return blockingUnaryCall(
          getChannel(), getDecTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc updateResource(com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc request) {
      return blockingUnaryCall(
          getChannel(), getUpdateResourceMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc updateJobStatus(com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc request) {
      return blockingUnaryCall(
          getChannel(), getUpdateJobStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class GrpcWorkerServiceFutureStub extends io.grpc.stub.AbstractStub<GrpcWorkerServiceFutureStub> {
    private GrpcWorkerServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GrpcWorkerServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GrpcWorkerServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GrpcWorkerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> incTask(
        com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request) {
      return futureUnaryCall(
          getChannel().newCall(getIncTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> decTask(
        com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter request) {
      return futureUnaryCall(
          getChannel().newCall(getDecTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> updateResource(
        com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateResourceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc> updateJobStatus(
        com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateJobStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_INC_TASK = 0;
  private static final int METHODID_DEC_TASK = 1;
  private static final int METHODID_UPDATE_RESOURCE = 2;
  private static final int METHODID_UPDATE_JOB_STATUS = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GrpcWorkerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GrpcWorkerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INC_TASK:
          serviceImpl.incTask((com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter) request,
              (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>) responseObserver);
          break;
        case METHODID_DEC_TASK:
          serviceImpl.decTask((com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter) request,
              (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>) responseObserver);
          break;
        case METHODID_UPDATE_RESOURCE:
          serviceImpl.updateResource((com.ylpu.thales.scheduler.core.rpc.entity.WorkerRequestRpc) request,
              (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>) responseObserver);
          break;
        case METHODID_UPDATE_JOB_STATUS:
          serviceImpl.updateJobStatus((com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc) request,
              (io.grpc.stub.StreamObserver<com.ylpu.thales.scheduler.core.rpc.entity.WorkerResponseRpc>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GrpcWorkerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GrpcWorkerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.ylpu.thales.scheduler.core.rpc.entity.WorkerGrpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GrpcWorkerService");
    }
  }

  private static final class GrpcWorkerServiceFileDescriptorSupplier
      extends GrpcWorkerServiceBaseDescriptorSupplier {
    GrpcWorkerServiceFileDescriptorSupplier() {}
  }

  private static final class GrpcWorkerServiceMethodDescriptorSupplier
      extends GrpcWorkerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GrpcWorkerServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (GrpcWorkerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GrpcWorkerServiceFileDescriptorSupplier())
              .addMethod(getIncTaskMethod())
              .addMethod(getDecTaskMethod())
              .addMethod(getUpdateResourceMethod())
              .addMethod(getUpdateJobStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
