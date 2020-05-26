package com.ylpu.thales.scheduler.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerJob;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobDependencyResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.response.UserResponse;

public interface JobService extends BaseService<SchedulerJob, Integer> {

    public void addJob(JobRequest job, Object user);

    public void updateJob(JobRequest job, Object user);

    public JobResponse getJobAndRelationById(Integer id);

    public JobTree queryTreeById(Integer id);

    public void scheduleJob(ScheduleRequest request);

    public void rescheduleJob(ScheduleRequest request);

    public void downJob(ScheduleRequest request, UserResponse user);

    public PageInfo<JobResponse> findAll(Integer jobType, String jobName, int pageSize, int pageNo);

    public List<JobDependencyResponse> getAllJobs();

}
