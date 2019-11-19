package com.ylpu.thales.scheduler.enums;

public enum JobType {
    
    SHELL(1), HQL(2), SPARK(3);
    
    private int code;
    
    private JobType(int code) {
        this.code = code;
    }
    
    public static JobType getJobType(int code) {
        for(JobType jobType : JobType.values()) {
           if(jobType.code == code) {
               return jobType;
           }
        }
        throw new IllegalArgumentException("unsupported job type " + code);
    }
    
    public static JobType getJobType(String name) {
        for(JobType jobType : JobType.values()) {
           if(jobType.toString().equalsIgnoreCase(name)) {
               return jobType;
           }
        }
        throw new IllegalArgumentException("unsupported job name " + name);
    }
    
    public int getCode() {
        return this.code;
    }
}