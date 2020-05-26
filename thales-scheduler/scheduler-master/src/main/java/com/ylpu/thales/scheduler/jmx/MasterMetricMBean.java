package com.ylpu.thales.scheduler.jmx;

public interface MasterMetricMBean {

    public String getWaitingTask();

    public String getTaskMap();

    public String getActiveMaster();
}