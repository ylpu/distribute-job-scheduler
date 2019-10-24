package com.ylpu.kepler.scheduler.dao;

import java.util.List;

import com.ylpu.kepler.scheduler.common.dao.BaseDao;
import com.ylpu.kepler.scheduler.entity.JobTree;
import com.ylpu.kepler.scheduler.entity.SchedulerJob;

public interface SchedulerJobMapper extends BaseDao<SchedulerJob, Integer>{
    public List<SchedulerJob> getJobParentsByIds(List<Integer> ids);
    public JobTree queryTreeById(Integer jobId);
}