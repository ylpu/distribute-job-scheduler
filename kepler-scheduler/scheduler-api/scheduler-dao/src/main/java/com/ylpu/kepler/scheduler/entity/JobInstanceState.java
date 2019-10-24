package com.ylpu.kepler.scheduler.entity;

import java.util.Date;
import lombok.Data;

@Data
public class JobInstanceState {
    
    private int id;
    private int jobId;
    private Date scheduleTime;
    private int TaskState;
}
