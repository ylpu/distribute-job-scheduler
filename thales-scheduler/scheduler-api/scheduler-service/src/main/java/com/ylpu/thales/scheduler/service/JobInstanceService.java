package com.ylpu.thales.scheduler.service;

import java.util.List;
import java.util.Map;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.TaskElapseChartResponse;
import com.ylpu.thales.scheduler.response.JobTypeSummaryResponse;
import com.ylpu.thales.scheduler.response.TaskSummaryResponse;
import com.ylpu.thales.scheduler.response.WorkerSummaryResponse;

public interface JobInstanceService extends BaseService<SchedulerJobInstance, Integer> {

    public Integer addJobInstance(JobInstanceRequest request);

    public void updateJobInstanceSelective(JobInstanceRequest request);

    public void updateJobInstanceByKey(JobInstanceRequest request);

    public JobInstanceResponse getJobInstanceByTime(Integer jobId, String scheduleTime);

    public JobInstanceResponse getJobInstanceById(Integer id);

    public List<Map<String, Object>> getRunningJobCount();

    public List<JobInstanceStateResponse> getAllJobStatus();

    public void killJob(ScheduleRequest request, Object object);

    public void rerun(ScheduleRequest request, Object object);

    public void rerunAll(ScheduleRequest request, Object object);

    public void markStatus();

    public void markSuccess(ScheduleRequest request, Object object);

    public void markFail(ScheduleRequest request, Object object);

    public void updateJobStatus(List<Integer> ids, TaskState status);

    public PageInfo<JobInstanceResponse> findAll(Integer taskState, String jobName, int pageNo, int pageSize, String userName);

    public List<TaskSummaryResponse> getTaskSummary();
    
    public List<WorkerSummaryResponse> getWorkerSummary();

    public List<TaskElapseChartResponse> getTaskLineByJobId(Integer id);
    
    public List<JobTypeSummaryResponse> getJobTypeSummary();

}
