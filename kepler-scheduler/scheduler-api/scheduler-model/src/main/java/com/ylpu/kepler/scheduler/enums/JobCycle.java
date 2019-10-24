package com.ylpu.kepler.scheduler.enums;

public enum JobCycle {
    
    MINUTE(1), HOUR(2), DAY(3), WEEK(4), MONTH(5), YEAR(6);
    
    private int code;
    
    private JobCycle(int code) {
        this.code = code;
    }
    public static JobCycle getJobCycle(int code) {
        for(JobCycle jobCycle : JobCycle.values()) {
           if(jobCycle.code == code) {
               return jobCycle;
           }
        }
        throw new IllegalArgumentException("unsupported job cycle " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}