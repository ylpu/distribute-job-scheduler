package com.ylpu.kepler.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ylpu.kepler.scheduler.common.dao.BaseDao;
import com.ylpu.kepler.scheduler.common.rest.ScheduleManager;
import com.ylpu.kepler.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.kepler.scheduler.dao.SchedulerJobInstanceMapper;
import com.ylpu.kepler.scheduler.entity.JobInstanceState;
import com.ylpu.kepler.scheduler.entity.SchedulerJobInstance;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;
import com.ylpu.kepler.scheduler.response.JobInstanceStateResponse;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.service.JobInstanceService;
import com.ylpu.kepler.scheduler.service.JobService;
import com.ylpu.kepler.scheduler.service.exception.KeplerRuntimeException;

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
        int status = ScheduleManager.killJob(getMasterServiceUri(request.getId()), request);
        if(status != HttpStatus.NO_CONTENT.value()) {
            throw new KeplerRuntimeException("error occurs,can not kill job " + request.getId());
        }

    }
    
    @Override
    public void rerun(ScheduleRequest request) {
        int status = ScheduleManager.rerun(getMasterServiceUri(request.getId()), request);
        //204-执行成功，但无内容返回
        if(status != HttpStatus.NO_CONTENT.value()) {
            throw new KeplerRuntimeException("error occurs,can not rerun job " + request.getId());
        }
    }
    
    @Override
    public void rerunAll(ScheduleRequest request) {
        int status = ScheduleManager.rerunAll(getMasterServiceUri(request.getId()), request);
        //204-执行成功，但无内容返回
        if(status != HttpStatus.NO_CONTENT.value()) {
            throw new KeplerRuntimeException("error occurs,can not rerun all job " + request.getId());
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
}
