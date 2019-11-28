package com.ylpu.thales.scheduler.dao;

import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerUser;

public interface SchedulerUserMapper extends BaseDao<SchedulerUser, Integer>{
	
    int deleteByPrimaryKey(Integer id);

    int insert(SchedulerUser record);

    int insertSelective(SchedulerUser record);

    SchedulerUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SchedulerUser record);

    int updateByPrimaryKey(SchedulerUser record);
    
    SchedulerUser findByUserName(String userName);
}