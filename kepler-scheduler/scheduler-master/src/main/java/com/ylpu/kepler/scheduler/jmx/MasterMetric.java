package com.ylpu.kepler.scheduler.jmx;

import com.ylpu.kepler.scheduler.core.utils.StringUtils;
import com.ylpu.kepler.scheduler.manager.MasterManager;
import com.ylpu.kepler.scheduler.response.WorkerResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MasterMetric implements MasterMetricMBean {

    @Override
    public String getGroupServers() {
        return StringUtils.getMapAsString(MasterManager.getInstance().getGroups());
    }

    @Override
    public String getResourceMap() {
        return getResourceMapAsString(MasterManager.getInstance().getResourceMap());
    }
	
    @Override
    public String getTaskMap() {
        return StringUtils.getMapAsString(MasterManager.getInstance().getTaskMap());
    }
    
    @Override
    public String getActiveMaster() {
        return MasterManager.getInstance().getActiveMaster();
    }
    
    private String getResourceMapAsString(Map<String, WorkerResponse> map) {
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<String, WorkerResponse>> list = new ArrayList<Map.Entry<String, WorkerResponse>>(map.entrySet());
        Iterator<Entry<String, WorkerResponse>> iterator = list.iterator();
        while(iterator.hasNext()) {
            Entry<String, WorkerResponse> entry = iterator.next();
            builder.append(entry.getKey() + " : [");
            builder.append("cpuUsage : " + entry.getValue().getCpuUsage() + ", ");
            builder.append("memoryUsage : " + entry.getValue().getMemoryUsage());
            builder.append("]");
            if(iterator.hasNext()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    } 
}