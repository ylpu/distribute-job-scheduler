package com.ylpu.thales.scheduler.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.GroupStrategy;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;

public interface GroupStrategyService extends BaseService<GroupStrategy, Integer> {

    public void addGroupStrategy(GroupStrategyRequest groupStrategryRequest);

    public void updateGroupStrategy(GroupStrategyRequest groupStrategryRequest);
    
    public GroupStrategyResponse getGroupStrategy(String groupName);
    
    public List<GroupStrategyResponse> getAllGroupStrategy();
    
    public PageInfo<GroupStrategyResponse> findAll(String groupName,int pageSize, int pageNo);


}
