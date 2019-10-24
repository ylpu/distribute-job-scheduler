package com.ylpu.kepler.scheduler.manager.strategy;

import com.ylpu.kepler.scheduler.manager.MasterManager;
import com.ylpu.kepler.scheduler.response.WorkerResponse;

public interface WorkerSelectStrategy {
    
    public WorkerResponse getIdleWorker(MasterManager rm,String groupName,String... lastFailedHosts);

}