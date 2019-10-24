package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import java.util.List;

import com.ylpu.thales.scheduler.enums.TaskState;

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