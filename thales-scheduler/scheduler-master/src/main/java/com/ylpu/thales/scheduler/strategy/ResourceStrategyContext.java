package com.ylpu.thales.scheduler.strategy;

import com.ylpu.thales.scheduler.manager.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

public class ResourceStrategyContext {

    private WorkerSelectStrategy strategy;

    public ResourceStrategyContext(WorkerSelectStrategy strategy) {
        this.strategy = strategy;
    }

    public WorkerResponse select(MasterManager rm, String groupName, String... lastFailedHosts) {
        return strategy.getIdleWorker(rm, groupName, lastFailedHosts);
    }
}
