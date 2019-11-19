package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import lombok.Data;

@Data
public class JobRequest implements Serializable {
    
    private Integer id;
    
    @Length(max = 50, message = "The length of job name can not exceed 50")
    @NotEmpty(message = "job name can not be null")
    private String jobName;

    private String jobType;

    private String jobPriority;

    private String creatorId;

    private String ownerIds;

    private String alertUsers;

    private String alertTypes;
    
    @NotEmpty(message = "cron expression can not be null")
    private String scheduleCron;

    private Boolean isSelfdependent;

    private String jobCycle;

    private Integer maxRetrytimes;

    private Integer retryInterval;

    private Integer executionTimeout;
    
    @NotEmpty(message = "group name can not be null")
    private String workerGroupname;

    private Integer jobReleasestate;

    private String description;

    private String jobConfiguration;
    
    //如果是root任务，依赖的任务id为-1
    @NotEmpty(message = "dependency can not be null")
    private String dependIds;

    private static final long serialVersionUID = 1L;

}