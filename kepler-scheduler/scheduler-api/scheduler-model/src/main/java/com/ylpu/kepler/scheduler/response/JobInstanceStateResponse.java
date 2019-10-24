package com.ylpu.kepler.scheduler.response;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class JobInstanceStateResponse implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int id;
    private int jobId;
    private Date scheduleTime;
    private int TaskState;
}
