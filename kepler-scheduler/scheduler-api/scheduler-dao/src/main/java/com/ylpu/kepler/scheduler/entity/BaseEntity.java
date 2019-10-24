package com.ylpu.kepler.scheduler.entity;

import java.util.Date;
import lombok.Data;

@Data
public class BaseEntity {
    private Date createTime;
    private Date updateTime;
}
