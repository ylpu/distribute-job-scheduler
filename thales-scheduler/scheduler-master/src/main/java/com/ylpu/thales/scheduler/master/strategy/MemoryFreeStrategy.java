package com.ylpu.thales.scheduler.master.strategy;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

import java.util.Comparator;

/**
 * choose most memory free worker from worker group to submit task
 *
 */
public class MemoryFreeStrategy implements WorkerSelectStrategy {
    
    private static String MEMORY_LIMTI = "thales.schedule.{0}.memory.limit";

    @Override
    public synchronized WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        List<WorkerResponse> sortedServers = new ArrayList<WorkerResponse>();
        if (servers != null && servers.size() > 0) {
            for (String server : servers) {
                if (rm.getResourceMap().get(server) != null) {
                    sortedServers.add(rm.getResourceMap().get(server));
                }
            }
            Collections.sort(sortedServers, new Comparator<WorkerResponse>() {
                @Override
                public int compare(WorkerResponse n1, WorkerResponse n2) {
                    if (n1.getMemoryUsage() > n2.getMemoryUsage()) {
                        return 1;
                    } else if (n1.getMemoryUsage() < n2.getMemoryUsage()) {
                        return -1;
                    } else {
                        return 0; // 相等为0
                    }
                }
            });
            if (sortedServers != null && sortedServers.size() > 0) {
                if (lastFailedHosts == null || lastFailedHosts.length == 0) {
                    return sortedServers.get(0);
                } else {
                    List<WorkerResponse> runningServers = sortedServers.stream()
                            .filter(hostInfo -> !Arrays.asList(lastFailedHosts).contains(hostInfo.getHost()))
                            .collect(Collectors.toList());
                    if (runningServers != null && runningServers.size() > 0) {
                        WorkerResponse worker = runningServers.get(0);
                        Properties prop = Configuration.getConfig();
                        String key = MessageFormat.format(MEMORY_LIMTI, groupName.toLowerCase());
                        int memoryUsageLimit = Configuration.getInt(prop, key, 95);
                        if (worker.getMemoryUsage() > memoryUsageLimit) {
                            throw new RuntimeException(
                                    "worker " + worker.getHost() + " memory usage exceed " + memoryUsageLimit + "，number is " + worker.getMemoryUsage());
                        }
                        return runningServers.get(0);
                    } else {
                        throw new RuntimeException("can not get avalilable resource");
                    }
                }

            }
        }
        throw new RuntimeException("can not get avalilable resource");
    }
}