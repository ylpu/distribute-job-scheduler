package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class NodeRequest implements Serializable {

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
    
    private String currentWorkerStatus;

    private static final long serialVersionUID = 1L;
}