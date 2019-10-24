package com.ylpu.thales.scheduler.manager.strategy;

public enum JobStrategy {
    
    TASK(1),
    MEMORY(2),
    CPU(3),
    RANDOM(4),
    ROBIN(5);
    
    private int code;
    
    private JobStrategy(int code) {
        this.code = code;
    }
    
    public static JobStrategy getJobStrategyById(int code) {
        for(JobStrategy js : JobStrategy.values()) {
            if(js.code == code) {
                return js;
            }
        }
        return JobStrategy.TASK;
    }
    
    public static JobStrategy getJobStrategyByName(String name) {
        for(JobStrategy js : JobStrategy.values()) {
            if(js.toString().equalsIgnoreCase(name)) {
                return js;
            }
        }
        return JobStrategy.TASK;
    }
}