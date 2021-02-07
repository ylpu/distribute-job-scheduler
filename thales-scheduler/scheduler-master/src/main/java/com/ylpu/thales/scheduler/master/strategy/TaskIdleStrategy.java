package com.ylpu.thales.scheduler.master.strategy;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.master.server.MasterManager;
import com.ylpu.thales.scheduler.response.NodeResponse;

/**
 * choose most task idle worker from worker group to submit task
 *
 */
public class TaskIdleStrategy implements WorkerSelectStrategy {
    
    private static String TASK_LIMTI = "thales.schedule.{0}.task.limit";

    @Override
    public synchronized NodeResponse getIdleWorker(MasterManager rm, String groupName, String... lastFailedHosts) {

        List<String> poolServers = rm.getGroups().get(GlobalConstants.WORKER_GROUP + "/" + groupName);
        Map<String, Integer> poolServerTasks = new HashMap<String, Integer>();

        Map<String, NodeResponse> resourceMap = rm.getResourceMap();
        Map<String, Integer> taskMap = rm.getTaskMap();

        if (poolServers != null && poolServers.size() > 0) {
            for (String server : poolServers) {
                poolServerTasks.put(server, taskMap.get(server));
            }
        }

        List<Entry<String, Integer>> taskSortlist = new ArrayList<Entry<String, Integer>>(poolServerTasks.entrySet());

        Collections.sort(taskSortlist, new Comparator<Map.Entry<String, Integer>>() {
            // 升序排序
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        if (taskSortlist != null && taskSortlist.size() > 0) {

            List<Entry<String, Integer>> runningServers = taskSortlist.stream()
                    .filter(entry -> !Arrays.asList(lastFailedHosts).contains(entry.getKey()))
                    .collect(Collectors.toList());
            if (runningServers != null && runningServers.size() > 0) {
                Properties prop = Configuration.getConfig();
                String key = MessageFormat.format(TASK_LIMTI, groupName.toLowerCase());
                int taskLimit = Configuration.getInt(prop, key, 200);
                if(runningServers.get(0).getValue() > taskLimit) {
                    throw new RuntimeException("worker " + runningServers.get(0).getKey() + " running task number exceed " + taskLimit + ", "
                            + "can not get avalilable resource");
                }
                return resourceMap.get(runningServers.get(0).getKey());
            } else {
                throw new RuntimeException("can not get avalilable resource");
            }
        }
        throw new RuntimeException("can not get avalilable resource");
    }

    @Override
    public JobStrategy getJobStrategy() {
        // TODO Auto-generated method stub
        return JobStrategy.TASK;
    }
}
