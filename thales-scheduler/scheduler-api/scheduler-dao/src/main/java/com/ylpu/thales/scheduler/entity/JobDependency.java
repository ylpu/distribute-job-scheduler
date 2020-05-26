package com.ylpu.thales.scheduler.entity;

import lombok.Data;

@Data
public class JobDependency {
    private Integer id;
    private String jobName;
}
