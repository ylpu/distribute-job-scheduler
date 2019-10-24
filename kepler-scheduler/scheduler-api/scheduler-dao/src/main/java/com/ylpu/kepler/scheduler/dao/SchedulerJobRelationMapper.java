package com.ylpu.kepler.scheduler.dao;

import com.ylpu.kepler.scheduler.common.dao.BaseDao;
import com.ylpu.kepler.scheduler.entity.SchedulerJobRelation;

public interface SchedulerJobRelationMapper extends BaseDao<SchedulerJobRelation, Integer>{
    
    void deleteByJobId(Integer id);
}