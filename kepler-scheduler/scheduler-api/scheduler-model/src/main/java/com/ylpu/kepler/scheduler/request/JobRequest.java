package com.ylpu.kepler.scheduler.request;

import java.io.Serializable;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import lombok.Data;

@Data
public class JobRequest implements Serializable {
    
    private Integer id;
    
    @Length(max = 50, message = "The length of job name can not exceed 50")
    @NotEmpty(message = "job name can not be null")
    private String jobName;

    private Integer jobType;

    private Integer jobPriority;

    private String creatorId;

    private String ownerIds;

    private String alertUsers;

    private Integer alertTypes;
    
    @NotEmpty(message = "cron expression can not be null")
    private String scheduleCron;

    private Boolean isSelfdependent;

    private Integer jobCycle;

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
    private List<Integer> dependencies;

    private static final long serialVersionUID = 1L;

}