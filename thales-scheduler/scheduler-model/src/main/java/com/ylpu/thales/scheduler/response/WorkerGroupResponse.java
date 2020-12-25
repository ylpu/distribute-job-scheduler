package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import lombok.Data;

@Data
public class WorkerGroupResponse implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String groupName;

    private String groupStrategy;
}