package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import lombok.Data;

@Data
public class WorkerResponse implements Serializable {

    private Integer id;

    private Integer workerType;

    private String host;

    private Integer port;

    private String workerGroup;

    private String zkdirectory;

    private double cpuUsage;

    private double memoryUsage;

    private String lastHeartbeatTime;

    private WorkerStatus workerStatus;

    private static final long serialVersionUID = 1L;
}