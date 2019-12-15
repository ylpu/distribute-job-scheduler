package com.ylpu.thales.scheduler.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerRole;
import com.ylpu.thales.scheduler.request.RoleRequest;
import com.ylpu.thales.scheduler.response.RoleResponse;

public interface RoleService extends BaseService<SchedulerRole,Integer>{
	
    public PageInfo<RoleResponse> findAll(String roleName,int pageNo,int pageSize);
    
    public void insertOrUpdate(RoleRequest request);
    
    public void deleteRole(Integer id);
    
    public List<RoleResponse> findAllRole();

}
