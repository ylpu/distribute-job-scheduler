package com.ylpu.kepler.scheduler.enums;

public enum JobPriority {
    
    LOW(1), MIDDLE(2), HIGH(3);
    
    private int priority;
    
    private JobPriority(int priority) {
        this.priority = priority;
    }
    
    public static JobPriority getNodeStatus(int priority) {
        for(JobPriority jobPriority : JobPriority.values()) {
            if(jobPriority.priority == priority) {
                return jobPriority;
            }
        }
        throw new IllegalArgumentException("unsupported node status " + priority);
    }
    
    public int getPriority() {
        return this.priority;
    }
}
