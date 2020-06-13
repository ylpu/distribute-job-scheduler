package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.JobDependency;
import com.ylpu.thales.scheduler.entity.JobTree;
import com.ylpu.thales.scheduler.entity.SchedulerJob;

public interface SchedulerJobMapper extends BaseDao<SchedulerJob, Integer> {
    public List<SchedulerJob> getJobParentsByIds(List<Integer> ids);

    public List<JobTree> queryTreeById(Integer jobId);

    List<SchedulerJob> findAll(@Param("jobType") Integer jobType, @Param("jobName") String jobName, @Param("userName") String userName);

    public Integer getJobCountByIds(@Param("ids") List<Integer> ids);

    public Integer getJobCount(@Param("jobType") Integer jobType, @Param("jobName") String jobName, @Param("userName") String userName);

    public Integer getJobCountByName(@Param("jobName") String jobName);

    public List<JobDependency> getAllJobs();

}