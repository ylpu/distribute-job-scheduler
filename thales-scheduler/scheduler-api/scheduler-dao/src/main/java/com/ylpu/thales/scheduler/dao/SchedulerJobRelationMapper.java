package com.ylpu.thales.scheduler.dao;

import java.util.List;

import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerJobRelation;

public interface SchedulerJobRelationMapper extends BaseDao<SchedulerJobRelation, Integer> {

    void deleteByJobId(Integer id);
    
    List<SchedulerJobRelation> findAll();
}