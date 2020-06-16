package com.ylpu.thales.scheduler.strategy;

import java.util.List;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.manager.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

/**
 * round robin的方式选择一台机器提交任务
 *
 */
public class RoundRobinStrategy implements WorkerSelectStrategy {

    private static Integer pos = 0;

    @Override
    public WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        String server = null;
        synchronized (pos) {
            if (pos >= servers.size()) {
                pos = 0;
            }
            server = servers.get(pos);
            pos++;
        }
        return rm.getResourceMap().get(server);
    }
}