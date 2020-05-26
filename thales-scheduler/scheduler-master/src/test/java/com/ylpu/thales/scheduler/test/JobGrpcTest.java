package com.ylpu.thales.scheduler.test;

import java.util.concurrent.TimeUnit;

import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class JobGrpcTest {

    private final ManagedChannel channel;// 客户端与服务器的通信channel
    private final GrpcJobServiceGrpc.GrpcJobServiceBlockingStub blockStub;// 阻塞式客户端存根节点

    public JobGrpcTest(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();// 指定grpc服务器地址和端口初始化通信channel
        blockStub = GrpcJobServiceGrpc.newBlockingStub(channel);// 根据通信channel初始化客户端存根节点
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // 客户端方法
    public void submit(String str) {
        // 封装请求参数
        JobInstanceRequestRpc request = JobInstanceRequestRpc.newBuilder().setId(1).setCreatorEmail("tester").build();
        // 客户端存根节点调用grpc服务接口，传递请求参数
        JobInstanceResponseRpc response = blockStub.submit(request);
        System.out.println("client, serviceName:" + response.getErrorCode() + response.getErrorMsg());
    }

    public static void main(String[] args) throws InterruptedException {
        JobGrpcTest client = new JobGrpcTest("127.0.0.1", 8090);
        for (int i = 0; i < 5; i++) {
            client.submit("client word:" + i);
            Thread.sleep(3000);
        }
    }
}