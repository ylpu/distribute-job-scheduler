package com.ylpu.thales.scheduler.core.alert.entity;

import java.io.Serializable;

import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;

public class Event implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int taskId;
    
    private AlertType alertType;
    
    private EventType eventType;
    
    private String alertUsers;
    
    private String logUrl;
    
    private String hostName;
    
    private String exception;
    
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getAlertUsers() {
        return alertUsers;
    }

    public void setAlertUsers(String alertUsers) {
        this.alertUsers = alertUsers;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
