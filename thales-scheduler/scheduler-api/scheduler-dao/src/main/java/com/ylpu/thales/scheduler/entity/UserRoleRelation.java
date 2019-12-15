package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserRoleRelation extends BaseEntity implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Integer roleId;

    private String roleName;

}