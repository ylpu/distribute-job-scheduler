package com.ylpu.kepler.scheduler.manager;

import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.enums.GrpcType;

public class TaskCall implements Comparable<TaskCall>{
    
    private JobInstanceRequestRpc rpcRequest;
    private GrpcType grpcType;
    
    public TaskCall(JobInstanceRequestRpc rpcRequest,GrpcType grpcType) {
        this.rpcRequest = rpcRequest;
        this.grpcType = grpcType;
    }
    
    public JobInstanceRequestRpc getRpcRequest() {
        return rpcRequest;
    }
    public void setRpcRequest(JobInstanceRequestRpc rpcRequest) {
        this.rpcRequest = rpcRequest;
    }
    public GrpcType getGrpcType() {
        return grpcType;
    }
    public void setGrpcType(GrpcType grpcType) {
        this.grpcType = grpcType;
    }

    @Override
    public int compareTo(TaskCall o) {
        if(this.rpcRequest.getJob().getJobPriority() > o.getRpcRequest().getJob().getJobPriority()) {
            return -1;
        }else if(this.rpcRequest.getJob().getJobPriority() < o.getRpcRequest().getJob().getJobPriority()) {
            return 1;
        }else {
            return 0; 
        }
    }
}
