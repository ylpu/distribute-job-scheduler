package com.ylpu.thales.scheduler.service;

import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerJob;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;

public interface JobService extends BaseService<SchedulerJob,Integer>{

    public void addJob(JobRequest job);
        
    public void updateJob(JobRequest job);

    public JobResponse getJobAndRelationById(Integer id);
    
    public JobTree queryTreeById(Integer id);
    
    public void scheduleJob(ScheduleRequest request);
    
    public void rescheduleJob(ScheduleRequest request);
    
    public void downJob(ScheduleRequest request);
    
}
