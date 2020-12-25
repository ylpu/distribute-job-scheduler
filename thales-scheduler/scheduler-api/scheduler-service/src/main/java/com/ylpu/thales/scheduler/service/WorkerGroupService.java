package com.ylpu.thales.scheduler.service;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.GroupStrategy;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.response.WorkerGroupResponse;

public interface WorkerGroupService extends BaseService<GroupStrategy, Integer> {

    public void addGroupStrategy(WorkerGroupRequest groupStrategryRequest);

    public void updateGroupStrategy(WorkerGroupRequest groupStrategryRequest);
    
    public PageInfo<WorkerGroupResponse> findAll(String groupName,int pageSize, int pageNo);


}
