package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.JobTree;
import com.ylpu.thales.scheduler.entity.SchedulerJob;

public interface SchedulerJobMapper extends BaseDao<SchedulerJob, Integer>{
    public List<SchedulerJob> getJobParentsByIds(List<Integer> ids);
    public JobTree queryTreeById(Integer jobId);
    List<SchedulerJob> findAll(@Param("jobType") Integer jobType,
            @Param("jobName") String jobName);
    
    public Integer getJobCountByIds(@Param("ids")List<String> ids);
    
    public Integer getJobCountByName(@Param("jobName")String jobName);
   
}