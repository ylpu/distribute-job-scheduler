package com.ylpu.thales.scheduler.schedule;

import java.io.Serializable;

public class JobDependency implements Serializable {

    private static final long serialVersionUID = 1L;

    private int jobId;
    private String scheduleTime;

    public JobDependency() {

    }

    public JobDependency(int jobId, String scheduleTime) {
        this.jobId = jobId;
        this.scheduleTime = scheduleTime;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    @Override
    public String toString() {
        return jobId + "-" + scheduleTime;
    }
}
