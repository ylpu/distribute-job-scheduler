package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class SchedulerJobInstance extends BaseEntity implements Serializable {
    
    private Integer id;

    private Integer jobId;
    
    private String jobName;
    
    private Integer jobType;
    
    private Integer taskState;

    private String logUrl;

    private String logPath;
    
    private String worker;

    private String creatorName;

    private String creatorEmail;

    private Integer retryTimes;

    private Integer pid;

    private String applicationid;

    private Integer elapseTime;
    
    private Date scheduleTime;
    
    private Date startTime;
    
    private Date endTime;

    private static final long serialVersionUID = 1L;
    
}