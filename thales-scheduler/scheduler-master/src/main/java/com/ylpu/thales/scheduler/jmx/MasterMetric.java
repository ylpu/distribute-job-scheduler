package com.ylpu.thales.scheduler.jmx;

import com.ylpu.thales.scheduler.core.utils.StringUtils;
import com.ylpu.thales.scheduler.manager.JobSubmission;
import com.ylpu.thales.scheduler.manager.MasterManager;
import com.ylpu.thales.scheduler.manager.TaskCall;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class MasterMetric implements MasterMetricMBean {

    @Override
    public String getWaitingTask() {
    	    StringBuilder sb = new StringBuilder();
    	    PriorityBlockingQueue<TaskCall> queue = JobSubmission.getWaitingQueue();
    	    Iterator<TaskCall> it = queue.iterator();
    	    while(it.hasNext()) {
    	       TaskCall task = it.next();
    	    	   sb.append(task.getRpcRequest().getId());
    	    	   if(it.hasNext()) {
    	    		   sb.append(",");
    	    	   }
    	    }
        return sb.toString();
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