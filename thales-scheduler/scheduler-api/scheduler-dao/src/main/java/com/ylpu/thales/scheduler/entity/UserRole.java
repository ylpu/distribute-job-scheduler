package com.ylpu.thales.scheduler.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class UserRole implements Serializable{
	
	private Integer userId;
	private Integer roleId;
	private Date createTime;
	private Date updateTime;

}
