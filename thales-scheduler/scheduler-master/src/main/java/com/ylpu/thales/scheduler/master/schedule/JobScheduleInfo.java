package com.ylpu.thales.scheduler.master.schedule;

import lombok.Data;

@Data
public class JobScheduleInfo {
    private String jobName;
    private String jobGroupName;
    private String triggerName;
    private String triggerGroupName;
    private String cron;
    private int jobPriority;
    private Integer id;
    private Object data;
}
