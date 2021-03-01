package com.ylpu.thales.scheduler.executor.rpc.client;

public interface IJobMetric {

    public void incOrDecTaskNumber(String master, String methodName);
    
}
