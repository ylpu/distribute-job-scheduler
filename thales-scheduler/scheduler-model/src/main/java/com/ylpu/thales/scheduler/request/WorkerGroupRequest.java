package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import java.util.List;

import com.ylpu.thales.scheduler.enums.NodeStatus;

import lombok.Data;

@Data
public class WorkerGroupRequest implements Serializable {
    
    private String groupName;
    private List<String> workers;
    private NodeStatus status;
    private static final long serialVersionUID = 1L;
  }