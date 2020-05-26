package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SchedulerJob extends BaseEntity implements Serializable {

    private Integer id;

    private String jobName;

    private Integer jobType;

    private Integer jobPriority;

    private String creatorId;

    private String ownerIds;

    private String alertUsers;

    private Integer alertTypes;

    private String scheduleCron;

    private Boolean isSelfdependent;

    private Integer jobCycle;

    private Integer maxRetrytimes;

    private Integer retryInterval;

    private Integer executionTimeout;

    private String workerGroupname;

    private Integer jobReleasestate;

    private String description;

    private String jobConfiguration;

    private List<SchedulerJobRelation> relations;

    private static final long serialVersionUID = 1L;

}