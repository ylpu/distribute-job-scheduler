package com.ylpu.thales.scheduler.strategy;

import com.ylpu.thales.scheduler.master.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

public interface WorkerSelectStrategy {

    public WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts);

}