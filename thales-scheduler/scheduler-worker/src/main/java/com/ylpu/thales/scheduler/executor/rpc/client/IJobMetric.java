package com.ylpu.thales.scheduler.executor.rpc.client;

public interface IJobMetric {

    public void increaseTask(String master);

    public void decreaseTask(String master);
}
