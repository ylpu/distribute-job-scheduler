package com.ylpu.kepler.scheduler.service;

import java.util.List;
import java.util.Map;

import com.ylpu.kepler.scheduler.common.service.BaseService;
import com.ylpu.kepler.scheduler.entity.SchedulerJobInstance;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;
import com.ylpu.kepler.scheduler.response.JobInstanceStateResponse;

public interface JobInstanceService extends BaseService<SchedulerJobInstance,Integer>{

    public Integer addJobInstance(JobInstanceRequest request);
    
    public void updateJobInstanceSelective(JobInstanceRequest request);
    
    public void updateJobInstanceByKey(JobInstanceRequest request);
    
    public Integer getInstanceIdByTime(Integer jobId,String scheduleTime);
    
    public JobInstanceResponse getJobInstanceById(Integer id);
    
    public List<Map<String,Object>> getRunningJobCount();
    
    public List<JobInstanceStateResponse> getAllJobStatus();
    
    public void killJob(ScheduleRequest request);
    
    public void rerun(ScheduleRequest request);
    
    public void rerunAll(ScheduleRequest request);
        
    public void markAsFailed(List<JobInstanceRequest> list);
    
    public void updateJobStatus(List<Integer> ids,TaskState status);
}
