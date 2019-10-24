package com.ylpu.thales.scheduler.enums;

public enum WorkerStatus {
    
    ADDED(1), REMOVED(2), UPDATED(3);
    
    private int code;
    
    private WorkerStatus(int code) {
        this.code = code;
    }
    
    public static WorkerStatus getNodeStatus(int code) {
        for(WorkerStatus nodeStatus : WorkerStatus.values()) {
            if(nodeStatus.code == code) {
                return nodeStatus;
            }
        }
        throw new IllegalArgumentException("unsupported node status " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}
