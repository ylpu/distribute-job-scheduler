package com.ylpu.kepler.scheduler.executor.listener;

import java.io.Serializable;

import com.ylpu.kepler.scheduler.enums.AlertType;

public class Event implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int taskId;
    
    private AlertType alertType;
    
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
