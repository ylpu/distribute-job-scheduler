package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.dao.SchedulerJobInstanceMapper;
import com.ylpu.thales.scheduler.entity.JobInstanceState;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.service.JobInstanceService;
import com.ylpu.thales.scheduler.service.JobService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class JobInstanceServiceImpl extends BaseServiceImpl<SchedulerJobInstance,Integer> implements JobInstanceService {

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
		if(request != null) {
			BeanUtils.copyProperties(request, jobInstance);
			insertSelective(jobInstance);
		}
		return jobInstance.getId();
	}

	@Override
	public void updateJobInstanceSelective(JobInstanceRequest request) {
	    SchedulerJobInstance jobInstance = new SchedulerJobInstance();
		if(request != null) {
			BeanUtils.copyProperties(request, jobInstance);
			updateByPrimaryKeySelective(jobInstance);
		}
	}
	
    @Override
    public void updateJobInstanceByKey(JobInstanceRequest request) {
        SchedulerJobInstance jobInstance = new SchedulerJobInstance();
        if(request != null) {
            BeanUtils.copyProperties(request, jobInstance);
            updateByPrimaryKey(jobInstance);
        }
    }
    
    @Override
    public JobInstanceResponse getJobInstanceById(Integer id) {
        SchedulerJobInstance schedulerJobInstance = findOneById(id);
        JobInstanceResponse response = new JobInstanceResponse();
        if(schedulerJobInstance != null) {
            BeanUtils.copyProperties(schedulerJobInstance, response);
            response.setTaskState(TaskState.getTaskStateById(schedulerJobInstance.getTaskState()));
			if(schedulerJobInstance.getScheduleTime() != null) {
				response.setScheduleTime(DateUtils.getDateAsString(schedulerJobInstance.getScheduleTime(),DateUtils.DATE_TIME_FORMAT));
			}
			if(schedulerJobInstance.getStartTime() != null) {
				response.setStartTime(DateUtils.getDateAsString(schedulerJobInstance.getStartTime(),DateUtils.DATE_TIME_FORMAT));
			}
			if(schedulerJobInstance.getEndTime() != null) {
				response.setEndTime(DateUtils.getDateAsString(schedulerJobInstance.getEndTime(),DateUtils.DATE_TIME_FORMAT));
			}
            JobResponse job = jobService.getJobAndRelationById(schedulerJobInstance.getJobId());
            response.setJobConf(job); 
        }
        return response;
    }	
    
    public Integer getInstanceIdByTime(Integer jobId,String scheduleTime) {
        return schedulerJobInstanceMapper.getInstanceIdByTime(jobId, scheduleTime);
    }

    
    public List<Map<String,Object>> getRunningJobCount(){
        return schedulerJobInstanceMapper.getRunningJobCount();
    }
    
    public List<JobInstanceStateResponse> getAllJobStatus(){
        List<JobInstanceStateResponse> responseList = new ArrayList<JobInstanceStateResponse>();
        JobInstanceStateResponse stateResponse = null;
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        
        List<JobInstanceState> list = schedulerJobInstanceMapper.getAllJobStatus(calendar.getTime(),new Date());
        if(list != null && list.size() > 0 ) {
            for(JobInstanceState state : list) {
                stateResponse = new JobInstanceStateResponse();
                BeanUtils.copyProperties(state, stateResponse); 
                responseList.add(stateResponse);
            }
        }
        return responseList;
    }

    @Override
    public void killJob(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.killJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to kill job " + request.getId());
            }
        }else {
            throw new RuntimeException("can not find master for job " + request.getId());
        }
    }
    
    @Override
    public void rerun(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rerun(masterUrl, request);
            //204-执行成功，但无内容返回
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to rerun job " + request.getId());
            }
        }else {
            throw new RuntimeException("can not find master for job " + request.getId());
        }
    }
    
    @Override
    public void rerunAll(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rerunAll(getMasterServiceUri(request.getId()), request);
            //204-执行成功，但无内容返回
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to rerun all job " + request.getId());
            }
        }else {
            throw new RuntimeException("can not find master for job " + request.getId());
        }
    }
    
    @Override
    public void markAsFailed(List<JobInstanceRequest> list) {
        for(JobInstanceRequest request : list) {
            if(request != null) {
                SchedulerJobInstance jobInstance = new SchedulerJobInstance();
                BeanUtils.copyProperties(request, jobInstance);
                schedulerJobInstanceMapper.markAsFailed(jobInstance);
            }
        }
    }
    
    public void updateJobStatus(List<Integer> ids,TaskState status) {
        schedulerJobInstanceMapper.updateJobStatus(ids, status.getCode());
    }

	@Override
	public PageInfo<JobInstanceResponse> findAll(Integer taskState, String worker,int pageNo,int pageSize) {
		PageHelper.startPage(pageNo,pageSize);
		List<SchedulerJobInstance> jobInstanceList = schedulerJobInstanceMapper.findAll(taskState, worker);
		JobInstanceResponse jobInstanceResponse = null;
		Page<JobInstanceResponse> page = new Page<JobInstanceResponse>();
		if(jobInstanceList != null && jobInstanceList.size() > 0) {
			for(SchedulerJobInstance jobInstance : jobInstanceList) {
				jobInstanceResponse = new JobInstanceResponse();
				BeanUtils.copyProperties(jobInstance, jobInstanceResponse);
				jobInstanceResponse.setTaskState(TaskState.getTaskStateById(jobInstance.getTaskState()));
				if(jobInstance.getScheduleTime() != null) {
					jobInstanceResponse.setScheduleTime(DateUtils.getDateAsString(jobInstance.getScheduleTime(),DateUtils.DATE_TIME_FORMAT));
				}
				if(jobInstance.getStartTime() != null) {
					jobInstanceResponse.setStartTime(DateUtils.getDateAsString(jobInstance.getStartTime(),DateUtils.DATE_TIME_FORMAT));
				}
				if(jobInstance.getEndTime() != null) {
					jobInstanceResponse.setEndTime(DateUtils.getDateAsString(jobInstance.getEndTime(),DateUtils.DATE_TIME_FORMAT));
				}
				page.add(jobInstanceResponse);
			}
		}
		page.setTotal(schedulerJobInstanceMapper.getInstantCount());
		PageInfo<JobInstanceResponse> pageInfo = new PageInfo<JobInstanceResponse>(page);
        return pageInfo;
	}
}
