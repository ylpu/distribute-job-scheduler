package com.ylpu.thales.scheduler.manager.strategy;

import com.ylpu.thales.scheduler.manager.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

public interface WorkerSelectStrategy {
    
    public WorkerResponse getIdleWorker(MasterManager rm,String groupName,String... lastFailedHosts);

}