package com.ylpu.thales.scheduler.master.strategy;

import java.util.List;
import java.util.Random;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

/**
 * 从group中随机选一台机器去提交任务
 *
 */
public class RandomStrategy implements WorkerSelectStrategy {
    @Override
    public synchronized WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        WorkerResponse idleServer = null;
        if (servers != null && servers.size() > 0) {
            String[] keys = servers.toArray(new String[0]);
            Random random = new Random();
            String randomServer = keys[random.nextInt(keys.length)];
            idleServer = rm.getResourceMap().get(randomServer);
        }
        if (idleServer != null) {
            return idleServer;
        }
        throw new RuntimeException("can not get avalilable resource");
    }
}