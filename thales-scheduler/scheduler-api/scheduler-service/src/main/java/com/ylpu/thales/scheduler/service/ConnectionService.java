package com.ylpu.thales.scheduler.service;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerConnection;
import com.ylpu.thales.scheduler.request.ConnectionRequest;
import com.ylpu.thales.scheduler.response.ConnectionResponse;

public interface ConnectionService extends BaseService<SchedulerConnection,Integer>{
	
    public PageInfo<ConnectionResponse> findAll(String connectionId,int pageNo,int pageSize);
    
    public void addConnection(ConnectionRequest request);
    
    public void updateConnection(ConnectionRequest request);
    
    public ConnectionResponse getConnection(String connectionId);

}
