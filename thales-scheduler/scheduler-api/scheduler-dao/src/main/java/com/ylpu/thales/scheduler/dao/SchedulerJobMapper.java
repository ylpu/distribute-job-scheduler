package com.ylpu.thales.scheduler.dao;

import java.util.List;

import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.JobTree;
import com.ylpu.thales.scheduler.entity.SchedulerJob;

public interface SchedulerJobMapper extends BaseDao<SchedulerJob, Integer>{
    public List<SchedulerJob> getJobParentsByIds(List<Integer> ids);
    public JobTree queryTreeById(Integer jobId);
}