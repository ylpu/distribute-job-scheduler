package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class SchedulerWorker extends BaseEntity implements Serializable {

    private Integer id;

    private Integer workerType;

    private String host;

    private Integer port;

    private String workerGroup;

    private String zkdirectory;

    private double cpuUsage;

    private double memoryUsage;

    private Date lastHeartbeatTime;

    private Integer workerStatus;

    private static final long serialVersionUID = 1L;
}