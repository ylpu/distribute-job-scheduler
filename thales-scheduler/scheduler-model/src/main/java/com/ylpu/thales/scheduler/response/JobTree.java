package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class JobTree implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Integer jobId;
    private Integer parentJobId;
    private Integer jobCycle;
    private String scheduleCron;
    private String jobName;
    private List<JobTree> children;
}
