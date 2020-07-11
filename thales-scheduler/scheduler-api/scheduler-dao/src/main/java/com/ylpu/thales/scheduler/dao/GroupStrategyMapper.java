package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.GroupStrategy;

public interface GroupStrategyMapper extends BaseDao<GroupStrategy, Integer>{
    
    GroupStrategy getGroupStrategy(@Param("groupName") String groupName);
    
    List<GroupStrategy> findAll(@Param("groupName") String groupName);
    
    Integer getGroupCount(@Param("groupName") String groupName);
    
}