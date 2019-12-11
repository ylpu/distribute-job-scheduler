package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class SchedulerConnection extends BaseEntity implements Serializable{
    private Integer id;

    private String connectionId;

    private String connectionType;

    private String hostname;
    
    private Integer port;
    
    private String dbSchema;

    private String username;

    private String password;
}