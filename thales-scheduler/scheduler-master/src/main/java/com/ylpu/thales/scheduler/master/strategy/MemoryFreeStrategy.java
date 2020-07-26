package com.ylpu.thales.scheduler.master.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.WorkerResponse;

import java.util.Comparator;

/**
 * 从group中选择内存使用最小的一台机器提交任务
 *
 */
public class MemoryFreeStrategy implements WorkerSelectStrategy {

    @Override
    public WorkerResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {
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
                    // 任务重试会选择没有失败并且资源最多的server,如果没有可用server就抛出异常
                    List<WorkerResponse> runningServers = sortedServers.stream()
                            .filter(hostInfo -> !Arrays.asList(lastFailedHosts).contains(hostInfo.getHost()))
                            .collect(Collectors.toList());
                    if (runningServers != null && runningServers.size() > 0) {
                        WorkerResponse worker = runningServers.get(0);
                        if (worker.getMemoryUsage() > 95) {
                            throw new RuntimeException(
                                    "worker " + worker.getHost() + " 内存使用率大于95%，为 " + worker.getMemoryUsage());
                        }
                        return runningServers.get(0);
                    } else {
                        throw new RuntimeException("找不到可用的worker执行任务");
                    }
                }

            }
        }
        throw new RuntimeException("找不到可用的worker执行任务 ");
    }
}