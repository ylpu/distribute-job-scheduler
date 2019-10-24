package com.ylpu.thales.scheduler.enums;

public enum JobReleaseState {
    
    OFFLINE(1), ONLINE(0), DELETED(-1);
    
    private int code;
    
    private JobReleaseState(int code) {
        this.code = code;
    }
    
    public static JobReleaseState getTaskType(int code) {
        for(JobReleaseState state : JobReleaseState.values()) {
            if(state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("unsupported job state " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}
