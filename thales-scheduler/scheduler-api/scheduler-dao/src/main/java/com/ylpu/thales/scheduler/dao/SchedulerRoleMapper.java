package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerRole;

public interface SchedulerRoleMapper extends BaseDao<SchedulerRole, Integer>{
    
    Integer getRoleCount(@Param("roleName") String roleName);
    
    List<SchedulerRole> findAll(@Param("roleName") String roleName);
    
}