package com.ylpu.thales.scheduler.response;

import lombok.Data;

@Data
public class WorkerUsageResponse {

    private String worker;
    private int port;
    private double usage;

}
