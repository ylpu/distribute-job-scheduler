package com.ylpu.thales.scheduler.jmx;

import com.ylpu.thales.scheduler.core.utils.StringUtils;
import com.ylpu.thales.scheduler.master.MasterManager;
import com.ylpu.thales.scheduler.schedule.JobSubmission;
import com.ylpu.thales.scheduler.schedule.TaskCall;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class MasterMetric implements MasterMetricMBean {

    @Override
    public String getWaitingTask() {
        StringBuilder queueGroupBuilder = new StringBuilder();
        Map<String,PriorityBlockingQueue<TaskCall>> queues = JobSubmission.getGroupQueue();
        for(Map.Entry<String,PriorityBlockingQueue<TaskCall>> entry  : queues.entrySet()) {
            PriorityBlockingQueue<TaskCall> queue = entry.getValue();
            Iterator<TaskCall> it = queue.iterator();
            StringBuilder queueBuilder = new StringBuilder();
            while (it.hasNext()) {
                TaskCall task = it.next();
                queueBuilder.append(task.getRpcRequest().getId());
                if (it.hasNext()) {
                    queueBuilder.append(",");
                }
            }
            queueGroupBuilder.append("[" + entry.getKey() + ":" + queueBuilder.toString() + "]");
        }
        return queueGroupBuilder.toString();
    }

    @Override
    public String getTaskMap() {
        return StringUtils.getMapAsString(MasterManager.getInstance().getTaskMap());
    }

    @Override
    public String getActiveMaster() {
        return MasterManager.getInstance().getActiveMaster();
    }
}