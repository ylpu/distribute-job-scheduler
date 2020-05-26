package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class JobResponse implements Serializable {

    private Integer id;

    private String jobName;

    private String jobType;

    private String jobPriority;

    private String creatorId;

    private String ownerIds;

    private String alertUsers;

    private String alertTypes;

    private String scheduleCron;

    private Boolean isSelfdependent;

    private String jobCycle;

    private Integer maxRetrytimes;

    private Integer retryInterval;

    private Integer executionTimeout;

    private String workerGroupname;

    private Integer jobReleasestate;

    private String description;

    private String jobConfiguration;

    private List<JobResponse> dependencies;

    private List<Integer> dependIds;

    private static final long serialVersionUID = 1L;

}