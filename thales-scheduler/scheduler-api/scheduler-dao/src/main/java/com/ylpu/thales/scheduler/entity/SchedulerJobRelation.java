package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class SchedulerJobRelation extends BaseEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer jobId;

    private Integer parentjobId;

}