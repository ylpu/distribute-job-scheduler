package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SchedulerUser extends BaseEntity implements Serializable {
	
    private Integer id;

    private String userName;

    private String password;
    
    private String confirmPass;
    
    private String email;
    
    private List<UserRoleRelation> relations;

}