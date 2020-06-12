package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.dao.SchedulerJobInstanceMapper;
import com.ylpu.thales.scheduler.entity.JobInstanceState;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.entity.TaskElapseChart;
import com.ylpu.thales.scheduler.entity.TaskSummary;
import com.ylpu.thales.scheduler.entity.WorkerSummary;
import com.ylpu.thales.scheduler.enums.JobReleaseState;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.RoleTypes;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.TaskElapseChartResponse;
import com.ylpu.thales.scheduler.response.TaskSummaryResponse;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.response.WorkerSummaryResponse;
import com.ylpu.thales.scheduler.service.JobInstanceService;
import com.ylpu.thales.scheduler.service.JobService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class JobInstanceServiceImpl extends BaseServiceImpl<SchedulerJobInstance, Integer>
        implements JobInstanceService {

    @Autowired
    private SchedulerJobInstanceMapper schedulerJobInstanceMapper;

    @Autowired
    private JobService jobService;

    @Override
    protected BaseDao<SchedulerJobInstance, Integer> getDao() {
        return schedulerJobInstanceMapper;
    }

    @Override
    public Integer addJobInstance(JobInstanceRequest request) {
        SchedulerJobInstance jobInstance = new SchedulerJobInstance();
        if (request != null) {
            BeanUtils.copyProperties(request, jobInstance);
            insertSelective(jobInstance);
        }
        return jobInstance.getId();
    }

    @Override
    public void updateJobInstanceSelective(JobInstanceRequest request) {
        SchedulerJobInstance jobInstance = new SchedulerJobInstance();
        if (request != null) {
            BeanUtils.copyProperties(request, jobInstance);
            updateByPrimaryKeySelective(jobInstance);
        }
    }

    @Override
    public void updateJobInstanceByKey(JobInstanceRequest request) {
        SchedulerJobInstance jobInstance = new SchedulerJobInstance();
        if (request != null) {
            BeanUtils.copyProperties(request, jobInstance);
            updateByPrimaryKey(jobInstance);
        }
    }

    @Override
    public JobInstanceResponse getJobInstanceById(Integer id) {
        SchedulerJobInstance schedulerJobInstance = findOneById(id);
        JobInstanceResponse response = new JobInstanceResponse();
        if (schedulerJobInstance != null) {
            BeanUtils.copyProperties(schedulerJobInstance, response);
            response.setTaskState(TaskState.getTaskStateById(schedulerJobInstance.getTaskState()));
            response.setJobId(schedulerJobInstance.getJobId());
            if (schedulerJobInstance.getScheduleTime() != null) {
                response.setScheduleTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (schedulerJobInstance.getStartTime() != null) {
                response.setStartTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getStartTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (schedulerJobInstance.getEndTime() != null) {
                response.setEndTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getEndTime(), DateUtils.DATE_TIME_FORMAT));
            }
            response.setJobReleasestate(
                    JobReleaseState.getJobReleaseState(schedulerJobInstance.getJobReleasestate()).name());
            JobResponse job = jobService.getJobAndRelationById(schedulerJobInstance.getJobId());
            response.setJobConf(job);
        }
        return response;
    }

    public JobInstanceResponse getInstanceIdByTime(Integer jobId, String scheduleTime) {
        SchedulerJobInstance schedulerJobInstance = schedulerJobInstanceMapper.getInstanceIdByTime(jobId, scheduleTime);
        JobInstanceResponse response = new JobInstanceResponse();
        if (schedulerJobInstance != null) {
            BeanUtils.copyProperties(schedulerJobInstance, response);
            response.setTaskState(TaskState.getTaskStateById(schedulerJobInstance.getTaskState()));
            response.setJobId(schedulerJobInstance.getJobId());
            if (schedulerJobInstance.getScheduleTime() != null) {
                response.setScheduleTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (schedulerJobInstance.getStartTime() != null) {
                response.setStartTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getStartTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (schedulerJobInstance.getEndTime() != null) {
                response.setEndTime(
                        DateUtils.getDateAsString(schedulerJobInstance.getEndTime(), DateUtils.DATE_TIME_FORMAT));
            }
            response.setJobReleasestate(
                    JobReleaseState.getJobReleaseState(schedulerJobInstance.getJobReleasestate()).name());
        }
        return response;
    }

    public List<Map<String, Object>> getRunningJobCount() {
        return schedulerJobInstanceMapper.getRunningJobCount();
    }

    public List<JobInstanceStateResponse> getAllJobStatus() {
        List<JobInstanceStateResponse> responseList = new ArrayList<JobInstanceStateResponse>();
        JobInstanceStateResponse stateResponse = null;

        // Calendar startTime = Calendar.getInstance();
        // startTime.setTime(new Date());
        // startTime.add(Calendar.MONTH, -1);
        //
        // Calendar endTime = Calendar.getInstance();
        // endTime.setTime(new Date());
        // endTime.add(Calendar.MONTH, 1);

        List<JobInstanceState> list = schedulerJobInstanceMapper.getAllJobStatus();
        if (list != null && list.size() > 0) {
            for (JobInstanceState state : list) {
                stateResponse = new JobInstanceStateResponse();
                BeanUtils.copyProperties(state, stateResponse);
                responseList.add(stateResponse);
            }
        }
        return responseList;
    }

    @Override
    public void killJob(ScheduleRequest request, Object object) {
        if (!isJobOwner(request, object)) {
            throw new ThalesRuntimeException("非任务所有人不能杀任务");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.killJob(masterUrl, request);
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to kill job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }

    @Override
    public void rerun(ScheduleRequest request, Object object) {
        if (!isJobOwner(request, object)) {
            throw new ThalesRuntimeException("非任务所有人不能重跑任务");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rerun(masterUrl, request);
            // 204-执行成功，但无内容返回
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to rerun job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }

    @Override
    public void rerunAll(ScheduleRequest request, Object object) {
        if (!isJobOwner(request, object)) {
            throw new ThalesRuntimeException("非任务所有人不能重跑所有任务");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rerunAll(CuratorHelper.getMasterServiceUri(), request);
            // 204-执行成功，但无内容返回
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to rerun all job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }

    @Override
    public void markStatus() {
        schedulerJobInstanceMapper.markStatus(new Date());
    }

    public void updateJobStatus(List<Integer> ids, TaskState status) {
        schedulerJobInstanceMapper.updateJobStatus(ids, status.getCode(), new Date());
    }

    @Override
    public PageInfo<JobInstanceResponse> findAll(Integer taskState, String jobName, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SchedulerJobInstance> jobInstanceList = schedulerJobInstanceMapper.findAll(taskState, jobName);
        JobInstanceResponse jobInstanceResponse = null;
        Page<JobInstanceResponse> page = new Page<JobInstanceResponse>();
        if (jobInstanceList != null && jobInstanceList.size() > 0) {
            for (SchedulerJobInstance jobInstance : jobInstanceList) {
                jobInstanceResponse = new JobInstanceResponse();
                BeanUtils.copyProperties(jobInstance, jobInstanceResponse);
                jobInstanceResponse.setTaskState(TaskState.getTaskStateById(jobInstance.getTaskState()));
                jobInstanceResponse.setJobId(jobInstance.getJobId());
                jobInstanceResponse.setJobType(JobType.getJobType(jobInstance.getJobType()).name());
                if (jobInstance.getScheduleTime() != null) {
                    jobInstanceResponse.setScheduleTime(
                            DateUtils.getDateAsString(jobInstance.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
                }
                if (jobInstance.getStartTime() != null) {
                    jobInstanceResponse.setStartTime(
                            DateUtils.getDateAsString(jobInstance.getStartTime(), DateUtils.DATE_TIME_FORMAT));
                }
                if (jobInstance.getEndTime() != null) {
                    jobInstanceResponse.setEndTime(
                            DateUtils.getDateAsString(jobInstance.getEndTime(), DateUtils.DATE_TIME_FORMAT));
                }
                jobInstanceResponse.setJobReleasestate(
                        JobReleaseState.getJobReleaseState(jobInstance.getJobReleasestate()).name());
                page.add(jobInstanceResponse);
            }
        }
        page.setTotal(schedulerJobInstanceMapper.getInstantCount(taskState, jobName));
        PageInfo<JobInstanceResponse> pageInfo = new PageInfo<JobInstanceResponse>(page);
        return pageInfo;
    }

    public List<TaskSummaryResponse> getTaskSummary() {
        List<TaskSummaryResponse> responses = new ArrayList<TaskSummaryResponse>();
        TaskSummaryResponse response = null;
        List<TaskSummary> list = schedulerJobInstanceMapper.getTaskSummary();
        if (list != null && list.size() > 0) {
            for (TaskSummary taskSummary : list) {
                response = new TaskSummaryResponse();
                response.setTaskState(TaskState.getTaskStateById(taskSummary.getTaskState()).toString());
                response.setTaskCount(taskSummary.getTaskCount());
                responses.add(response);
            }
        }
        return responses;
    }
    
    public List<WorkerSummaryResponse> getWorkerSummary() {
        List<WorkerSummaryResponse> responses = new ArrayList<WorkerSummaryResponse>();
        WorkerSummaryResponse response = null;
        List<WorkerSummary> list = schedulerJobInstanceMapper.getWorkerSummary();
        if (list != null && list.size() > 0) {
            for (WorkerSummary workerSummary : list) {
                response = new WorkerSummaryResponse();
                response.setWorker(workerSummary.getWorkerName());
                response.setTaskCount(workerSummary.getTaskCount());
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    public List<TaskElapseChartResponse> getTaskLineByJobId(Integer id) {
        TaskElapseChartResponse response = null;
        List<TaskElapseChartResponse> responses = new ArrayList<TaskElapseChartResponse>();
        List<TaskElapseChart> list = schedulerJobInstanceMapper.getTaskLineByJobId(id);
        if (list != null && list.size() > 0) {
            for (TaskElapseChart chart : list) {
                response = new TaskElapseChartResponse();
                response.setScheduleTime(
                        DateUtils.getDateAsString(chart.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
                response.setElapseTime(chart.getElapseTime());
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    public void markSuccess(ScheduleRequest request, Object object) {
        if (!isJobOwner(request, object)) {
            throw new ThalesRuntimeException("非任务所有人不能标识成功任务");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.markSuccess(CuratorHelper.getMasterServiceUri(), request);
            // 204-执行成功，但无内容返回
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to mark job " + request.getId() + " as success");
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }

    }

    private boolean isJobOwner(ScheduleRequest request, Object object) {
        if (object == null) {
            throw new ThalesRuntimeException("请重新登陆");
        }
        JobInstanceResponse instanceResponse = getJobInstanceById(request.getId());
        UserResponse user = (UserResponse) object;
        List<String> owners = Arrays.asList(instanceResponse.getJobConf().getOwnerIds().split(","));
        if (owners.contains(user.getUserName()) || user.getRoleNames().contains(RoleTypes.ROLE_ADMIN.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void markFail(ScheduleRequest request, Object object) {
        if (!isJobOwner(request, object)) {
            throw new ThalesRuntimeException("非任务所有人不能标识失败任务");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.markFail(CuratorHelper.getMasterServiceUri(), request);
            // 204-执行成功，但无内容返回
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to mark job " + request.getId() + " as fail");
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }
}
