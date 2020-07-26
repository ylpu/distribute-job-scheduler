package com.ylpu.thales.scheduler.master.strategy;

import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

public interface WorkerSelectStrategy {

    public WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts);

}