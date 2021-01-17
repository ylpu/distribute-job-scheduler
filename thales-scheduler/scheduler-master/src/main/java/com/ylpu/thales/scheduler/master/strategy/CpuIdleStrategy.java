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
import com.ylpu.thales.scheduler.response.NodeResponse;

import java.util.Comparator;

/**
 * choose most cpu idle worker to submit task
 *
 */
public class CpuIdleStrategy implements WorkerSelectStrategy {
    
    private static String CPU_LIMTI = "thales.schedule.{0}.cpu.limit";
    
    @Override
    public synchronized NodeResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
        List<String> servers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        List<NodeResponse> sortedServers = new ArrayList<NodeResponse>();
        if (servers != null && servers.size() > 0) {
            for (String server : servers) {
                if (rm.getResourceMap().get(server) != null) {
                    sortedServers.add(rm.getResourceMap().get(server));
                }
            }
            Collections.sort(sortedServers, new Comparator<NodeResponse>() {
                @Override
                public int compare(NodeResponse n1, NodeResponse n2) {
                    if (n1.getCpuUsage() > n2.getCpuUsage()) {
                        return 1;
                    } else if (n1.getCpuUsage() < n2.getCpuUsage()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            if (sortedServers != null && sortedServers.size() > 0) {
                if (lastFailedHosts == null || lastFailedHosts.length == 0) {
                    return sortedServers.get(0);
                } else {
                    List<NodeResponse> runningServers = sortedServers.stream()
                            .filter(hostInfo -> !Arrays.asList(lastFailedHosts).contains(hostInfo.getHost()))
                            .collect(Collectors.toList());
                    if (runningServers != null && runningServers.size() > 0) {
                        NodeResponse worker = runningServers.get(0);
                        Properties prop = Configuration.getConfig();
                        String key = MessageFormat.format(CPU_LIMTI, groupName.toLowerCase());
                        int cpuUsageLimit = Configuration.getInt(prop, key, 95);
                        if (worker.getCpuUsage() > cpuUsageLimit) {
                            throw new RuntimeException(
                                    "worker " + worker.getHost() + " cpu usage exceed " + cpuUsageLimit + "，the number is " + worker.getCpuUsage());
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