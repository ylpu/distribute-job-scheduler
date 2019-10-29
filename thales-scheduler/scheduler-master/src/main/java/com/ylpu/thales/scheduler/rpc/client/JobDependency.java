package com.ylpu.thales.scheduler.rpc.client;

import java.io.Serializable;
import lombok.Data;

@Data
public class JobDependency implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int jobId;
	private String scheduleTime;
	
	@Override
	public String toString() {
		return jobId + "-" + scheduleTime;
	}
}
