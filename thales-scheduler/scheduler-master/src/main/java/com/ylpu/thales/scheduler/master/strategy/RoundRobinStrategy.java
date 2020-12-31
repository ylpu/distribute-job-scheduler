package com.ylpu.thales.scheduler.master.strategy;

import java.util.List;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

/**
 * round robin to choose task from worker group to submit task
 *
 */
public class RoundRobinStrategy implements WorkerSelectStrategy {

    private static Integer pos = 0;

    @Override
    public synchronized WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        if (pos >= servers.size()) {
            pos = 0;
        }
        String server = servers.get(pos);
        pos++;
        return rm.getResourceMap().get(server);
    }
}