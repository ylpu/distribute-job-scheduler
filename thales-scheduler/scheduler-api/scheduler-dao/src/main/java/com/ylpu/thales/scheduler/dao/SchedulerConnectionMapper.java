package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerConnection;

public interface SchedulerConnectionMapper extends BaseDao<SchedulerConnection, Integer>{
   
	List<SchedulerConnection> findAll(@Param("connectionId") String connectionId);
		
    Integer getConnectionCount(@Param("connectionId") String connectionId);
    
}