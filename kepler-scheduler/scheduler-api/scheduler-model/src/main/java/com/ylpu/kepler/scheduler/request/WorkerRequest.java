package com.ylpu.kepler.scheduler.request;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class WorkerRequest implements Serializable {
    
    private Integer id;

    private Integer nodeType;
 
    private String host;

    private Integer port;

    private String nodeGroup;

    private String zkdirectory;

    private double cpuUsage;
    
    private double memoryUsage;

    private Date lastHeartbeatTime;

    private Integer nodeStatus;

    private static final long serialVersionUID = 1L;
  }