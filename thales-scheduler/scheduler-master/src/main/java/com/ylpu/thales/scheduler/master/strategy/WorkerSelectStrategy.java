package com.ylpu.thales.scheduler.master.strategy;

import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.NodeResponse;

public interface WorkerSelectStrategy {

    public NodeResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts);
    
    public JobStrategy getJobStrategy();

}