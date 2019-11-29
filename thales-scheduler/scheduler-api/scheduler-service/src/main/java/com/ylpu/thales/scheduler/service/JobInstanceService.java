package com.ylpu.thales.scheduler.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.entity.TaskElapseChart;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.TaskElapseChartResponse;
import com.ylpu.thales.scheduler.response.TaskSummaryResponse;

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
        
    public void markStatus();
    
    public void markSuccess(ScheduleRequest request);
    
    public void markFail(ScheduleRequest request);
    
    public void updateJobStatus(List<Integer> ids,TaskState status);
    
    public PageInfo<JobInstanceResponse> findAll(Integer taskState, String jobName,int pageNo,int pageSize);
    
    public List<TaskSummaryResponse> getTaskSummary();
    
    public List<TaskElapseChartResponse> getTaskLineByJobId(Integer id);

}
