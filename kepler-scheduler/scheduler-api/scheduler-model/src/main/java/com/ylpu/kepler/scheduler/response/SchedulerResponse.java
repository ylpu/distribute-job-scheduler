package com.ylpu.kepler.scheduler.response;

import java.io.Serializable;

public class SchedulerResponse<T> implements Serializable{
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int errorCode = 200;
	private String errorMsg;
	private T data;
	
    public SchedulerResponse() {
        
    }
    
    public static SchedulerResponse<Void> success() {
        return new SchedulerResponse<Void>(200);
    }
    
	public SchedulerResponse(T data) {
		this.data = data;
	}
	
	public SchedulerResponse(int errorCode) {
	    this.errorCode = errorCode;
	}
	
	public SchedulerResponse(int errorCode,String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
