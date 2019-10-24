package com.ylpu.thales.scheduler.manager.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceStrategy {
    
    private static Map<JobStrategy,WorkerSelectStrategy> strategyMap = new ConcurrentHashMap<JobStrategy,WorkerSelectStrategy>();
    
    static{
        strategyMap.put(JobStrategy.MEMORY, new MemoryFreeStrategy());
        strategyMap.put(JobStrategy.CPU, new CpuIdleStrategy());
        strategyMap.put(JobStrategy.TASK, new TaskIdleStrategy());
        strategyMap.put(JobStrategy.RANDOM, new RandomStrategy());
        strategyMap.put(JobStrategy.ROBIN, new RoundRobinStrategy());
    }
    
    public static void addStrategy(JobStrategy key ,WorkerSelectStrategy serverSelectStrategy){
        strategyMap.put(key, serverSelectStrategy);
    }
    
    public static WorkerSelectStrategy getStrategy(JobStrategy taskStrategy){  
        WorkerSelectStrategy strategy = strategyMap.get(taskStrategy);
        if(strategy == null){
            strategy = new MemoryFreeStrategy();
        }
        return strategy;
    }
}
