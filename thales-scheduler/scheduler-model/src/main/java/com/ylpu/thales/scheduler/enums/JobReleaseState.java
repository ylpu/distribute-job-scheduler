package com.ylpu.thales.scheduler.enums;

public enum JobReleaseState {
    
    OFFLINE(-1), ONLINE(0);
    
    private int code;
    
    private JobReleaseState(int code) {
        this.code = code;
    }
    
    public static JobReleaseState getJobReleaseState(int code) {
        for(JobReleaseState state : JobReleaseState.values()) {
            if(state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("unsupported job state " + code);
    }
    
    public static JobReleaseState getJobReleaseStateByName(String name) {
        for(JobReleaseState jobReleaseState : JobReleaseState.values()) {
            if(jobReleaseState.toString().equalsIgnoreCase(name)) {
                return jobReleaseState;
            }
        }
        throw new IllegalArgumentException("unsupported job release state " + name);
    }
    
    public int getCode() {
        return this.code;
    }
}
