package com.ylpu.thales.scheduler.response;

import lombok.Data;

@Data
public class JobTypeSummaryResponse {

    private String jobType;
    private Integer taskCount;

}
