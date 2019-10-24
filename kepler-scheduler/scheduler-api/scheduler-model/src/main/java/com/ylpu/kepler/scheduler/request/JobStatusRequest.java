package com.ylpu.kepler.scheduler.request;

import java.io.Serializable;
import java.util.List;

import com.ylpu.kepler.scheduler.enums.TaskState;

import lombok.Data;

@Data
public class JobStatusRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private List<Integer> ids;
    private TaskState status;
}