package com.ylpu.kepler.scheduler.service;

import com.ylpu.kepler.scheduler.common.service.BaseService;
import com.ylpu.kepler.scheduler.entity.SchedulerJob;
import com.ylpu.kepler.scheduler.request.JobRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.response.JobTree;

public interface JobService extends BaseService<SchedulerJob,Integer>{

    public void addJob(JobRequest job);
        
    public void updateJob(JobRequest job);

    public JobResponse getJobAndRelationById(Integer id);
    
    public JobTree queryTreeById(Integer id);
    
    public void scheduleJob(ScheduleRequest request);
    
    public void rescheduleJob(ScheduleRequest request);
    
    public void downJob(ScheduleRequest request);
    
}
