package com.ylpu.thales.scheduler.entity;

import java.util.Date;

import lombok.Data;

@Data
public class TaskElapseChart {
	
	private Date scheduleTime;
	private Integer elapseTime;

}
