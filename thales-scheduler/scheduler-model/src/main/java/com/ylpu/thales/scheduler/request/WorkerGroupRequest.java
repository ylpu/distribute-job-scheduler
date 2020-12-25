package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class WorkerGroupRequest implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String groupName;

    private String groupStrategy;
}