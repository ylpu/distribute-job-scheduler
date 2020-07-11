package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class GroupStrategy extends BaseEntity implements Serializable {
    
    private Integer id;

    private String groupName;

    private String groupStrategy;
}