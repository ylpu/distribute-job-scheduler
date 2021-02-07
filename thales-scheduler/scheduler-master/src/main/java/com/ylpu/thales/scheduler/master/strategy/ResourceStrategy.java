package com.ylpu.thales.scheduler.master.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceStrategy {

    private static Map<JobStrategy, Class<? extends WorkerSelectStrategy>> strategyMap = new ConcurrentHashMap<JobStrategy, Class<? extends WorkerSelectStrategy>>();
    
    private static Map<String, WorkerSelectStrategy> groupStrategyMap = new ConcurrentHashMap<String, WorkerSelectStrategy>();

    static {
        strategyMap.put(JobStrategy.MEMORY, MemoryFreeStrategy.class);
        strategyMap.put(JobStrategy.CPU, CpuIdleStrategy.class);
        strategyMap.put(JobStrategy.TASK, TaskIdleStrategy.class);
        strategyMap.put(JobStrategy.RANDOM, RandomStrategy.class);
        strategyMap.put(JobStrategy.ROBIN, RoundRobinStrategy.class);
    }

    public static WorkerSelectStrategy getStrategy(JobStrategy jobStrategy,String groupName) throws InstantiationException, IllegalAccessException {
        WorkerSelectStrategy strategy = groupStrategyMap.get(groupName);
        if(strategy == null || strategy.getJobStrategy() != jobStrategy) {
            WorkerSelectStrategy strategyInstance = (WorkerSelectStrategy)strategyMap.get(jobStrategy).newInstance();
            groupStrategyMap.put(groupName, strategyInstance);
        }
        return groupStrategyMap.get(groupName);
    }
}
