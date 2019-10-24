package com.ylpu.thales.scheduler.jmx;

public interface MasterMetricMBean {
	
    public String getGroupServers();	

    public String getResourceMap();
    
    public String getTaskMap(); 
    
    public String getActiveMaster();
}