package com.ylpu.thales.scheduler.master.jmx;

public interface MasterMetricMBean {

    public String getWaitingTask();

    public String getTaskMap();

    public String getActiveMaster();
}