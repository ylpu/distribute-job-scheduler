package com.ylpu.thales.scheduler.response;

import lombok.Data;

@Data
public class WorkerSummaryResponse {

    private String worker;
    private Integer taskCount;

}
