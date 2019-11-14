package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import java.util.Date;

import com.ylpu.thales.scheduler.enums.NodeStatus;

import lombok.Data;

@Data
public class WorkerResponse implements Serializable {
    
    private Integer id;

    private Integer nodeType;

    private String host;

    private Integer port;

    private String nodeGroup;

    private String zkdirectory;

    private double cpuUsage;
    
    private double memoryUsage;

    private String lastHeartbeatTime;

    private NodeStatus nodeStatus;

    private static final long serialVersionUID = 1L;
  }