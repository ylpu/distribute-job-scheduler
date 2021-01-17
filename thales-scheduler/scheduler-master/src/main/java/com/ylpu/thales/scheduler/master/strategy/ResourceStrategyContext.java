package com.ylpu.thales.scheduler.master.strategy;

import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.NodeResponse;

public class ResourceStrategyContext {

    private WorkerSelectStrategy strategy;

    public ResourceStrategyContext(WorkerSelectStrategy strategy) {
        this.strategy = strategy;
    }

    public NodeResponse select(MasterManager rm, String groupName, String... lastFailedHosts) {
        return strategy.getIdleWorker(rm, groupName, lastFailedHosts);
    }
}
