package com.ylpu.thales.scheduler.response;

import lombok.Data;

@Data
public class TaskSummaryResponse {

    private String taskState;
    private Integer taskCount;

}
