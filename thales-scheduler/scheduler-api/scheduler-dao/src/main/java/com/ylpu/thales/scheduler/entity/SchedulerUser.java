package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class SchedulerUser extends BaseEntity implements Serializable {
	
    private Integer id;

    private String userName;

    private String password;

}