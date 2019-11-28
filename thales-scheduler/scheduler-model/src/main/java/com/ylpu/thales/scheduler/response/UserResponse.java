package com.ylpu.thales.scheduler.response;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String userName;
	private String password;

}
