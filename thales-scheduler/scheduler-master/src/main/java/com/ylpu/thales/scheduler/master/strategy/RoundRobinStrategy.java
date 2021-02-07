package com.ylpu.thales.scheduler.master.strategy;

import java.util.List;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.NodeResponse;

/**
 * round robin to choose task from worker group to submit task
 *
 */
public class RoundRobinStrategy implements WorkerSelectStrategy {

    private static Integer pos = 0;

    @Override
    public synchronized NodeResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        if(servers == null || servers.size() == 0) {
            throw new RuntimeException("can not get available resource for group " + groupName);
        }
        if (pos >= servers.size()) {
            pos = 0;
        }
        String server = servers.get(pos);
        pos++;
        return rm.getResourceMap().get(server);
    }

    @Override
    public JobStrategy getJobStrategy() {
        // TODO Auto-generated method stub
        return JobStrategy.ROBIN;
    }
}