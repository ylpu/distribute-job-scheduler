package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.plugins.Page;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.SchedulerJobMapper;
import com.ylpu.thales.scheduler.dao.SchedulerJobRelationMapper;
import com.ylpu.thales.scheduler.entity.SchedulerJob;
import com.ylpu.thales.scheduler.entity.SchedulerJobRelation;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.JobCycle;
import com.ylpu.thales.scheduler.enums.JobPriority;
import com.ylpu.thales.scheduler.enums.JobReleaseState;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.service.JobService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class JobServiceImpl extends BaseServiceImpl<SchedulerJob,Integer> implements JobService {

    @Autowired
    private SchedulerJobMapper schedulerJobMapper;
    
    @Autowired
    private SchedulerJobRelationMapper schedulerJobRelationMapper;

    @Override
    protected BaseDao<SchedulerJob, Integer> getDao() {
        return schedulerJobMapper;
    }

	@Override
    public void addJob(JobRequest job) {	
		if(!isValidJobDependIds(job.getDependIds())) {
			throw new ThalesRuntimeException("任务依赖不存在");
		}
        SchedulerJob schedulerJob = new SchedulerJob();
        if(job != null) {
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob,job);
            insertSelective(schedulerJob);
        }
        SchedulerJobRelation sr = null; 
        List<String> depencies = Arrays.asList(job.getDependIds().split(","));
        if(depencies != null && depencies.size() > 0) {
           for(String parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(schedulerJob.getId());
                sr.setParentjobId(NumberUtils.toInt(parentJobId));
                schedulerJobRelationMapper.insertSelective(sr);
           }
       }
    }
	
	private boolean isValidJobDependIds(String ids) {
		String[] jobIds = ids.split(",");
		if(!(jobIds.length == 1 && jobIds[0].equals("-1"))) {
			Integer count = schedulerJobMapper.getJobCountByIds(Arrays.asList(ids));
			if(count != jobIds.length) {
				return false;
			}
		}
		return true;
	}
	
    @Override
    public void updateJob(JobRequest job) {  
		if(!isValidJobDependIds(job.getDependIds())) {
			throw new ThalesRuntimeException("任务依赖不存在");
		}
        SchedulerJob schedulerJob = new SchedulerJob();
        if(job != null) {
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob,job);
            updateByPrimaryKeySelective(schedulerJob);
        }
        List<String> depencies = Arrays.asList(job.getDependIds().split(","));
        if(depencies != null && depencies.size() > 0) {
            schedulerJobRelationMapper.deleteByJobId(job.getId());
            SchedulerJobRelation sr = null;
            for(String parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(job.getId());
                sr.setParentjobId(NumberUtils.toInt(parentJobId));
                schedulerJobRelationMapper.insertSelective(sr);
            }
        }
    }	
    
    public JobTree queryTreeById(Integer id) {
        JobTree targetTree = new JobTree();
        com.ylpu.thales.scheduler.entity.JobTree sourceTree =  schedulerJobMapper.queryTreeById(id);
        if(sourceTree != null) {
            setResponse(sourceTree,targetTree); 
        }
        return targetTree;
    }
    
    private void setResponse(com.ylpu.thales.scheduler.entity.JobTree sourceTree,JobTree targetTree) {
        List<JobTree> targetList = new ArrayList<JobTree>();
        setTarget(sourceTree,targetTree);
        targetTree.setChildren(targetList);
        
        List<com.ylpu.thales.scheduler.entity.JobTree> sourceList = sourceTree.getChildren();
        for(com.ylpu.thales.scheduler.entity.JobTree source : sourceList) {
            JobTree target = new JobTree();
            setTarget(source,target);
            targetList.add(target);
            setResponse(source,target);
        }
    }
    
    private void setTarget(com.ylpu.thales.scheduler.entity.JobTree source,JobTree target) {
        target.setJobCycle(source.getJobCycle());
        target.setScheduleCron(source.getScheduleCron());
        target.setJobId(source.getJobId());
        target.setParentJobId(source.getParentJobId());
    }

	@Override
	public JobResponse getJobAndRelationById(Integer id) {
	    JobResponse response = new JobResponse();
	    List<SchedulerJob> jobList = schedulerJobMapper.getJobParentsByIds(Arrays.asList(id));
	    if(jobList == null || jobList.size() == 0) {
	       return null;
	    }
		SchedulerJob schedulerJob = jobList.get(0);
		if(schedulerJob != null) {
		        BeanUtils.copyProperties(schedulerJob, response);
		        setJobResponse(schedulerJob,response);
		        List<SchedulerJobRelation> dependencies = schedulerJob.getRelations();
		        List<Integer> ids = dependencies.stream().map(t -> t.getParentjobId()).collect(Collectors.toList());
		        List<JobResponse> list = new ArrayList<JobResponse>();
		        if(ids != null && ids.size() > 0) {
		            List<SchedulerJob> jobs = schedulerJobMapper.getJobParentsByIds(ids);
		            if(jobs != null && jobs.size() > 0) {
		                JobResponse dependency = null;
		                for(SchedulerJob job  : jobs) {
		                    dependency = new JobResponse();
		                    BeanUtils.copyProperties(job, dependency);
		      		        setJobResponse(job,dependency);
		                    list.add(dependency);
		                }
		            }  
		        }
		        response.setDependencies(list); 
		}
		return response;
	}
	
	private void setJobResponse(SchedulerJob schedulerJob,JobResponse response) {
	      response.setJobCycle(JobCycle.getJobCycle(schedulerJob.getJobCycle()).name());
	      response.setJobPriority(JobPriority.getJobPriority(schedulerJob.getJobPriority()).name());
	      response.setJobType(JobType.getJobType(schedulerJob.getJobType()).name());
	      response.setAlertTypes(AlertType.getAlertType(schedulerJob.getAlertTypes()).name());
	        List<Integer> collect = schedulerJob.getRelations()
	                .stream()
	                .map(p->p.getParentjobId())
	                .collect(Collectors.toList());
	      response.setDependIds(com.ylpu.thales.scheduler.common.utils.StringUtils.convertListAsString(collect));
	}
	
	private void setJobRequest(SchedulerJob schedulerJob,JobRequest job) {
        schedulerJob.setJobCycle(JobCycle.getJobCycle(job.getJobCycle()).getCode());
        schedulerJob.setJobPriority(JobPriority.getJobPriority(job.getJobPriority()).getPriority());
        schedulerJob.setJobType(JobType.getJobType(job.getJobType()).getCode());
        schedulerJob.setAlertTypes(AlertType.getAlertType(job.getAlertTypes()).getCode());
	}

    @Override
    public void scheduleJob(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.scheduleJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to schedule job " + request.getId());
            }
        }else {
            throw new RuntimeException("can not find master url for job " + request.getId());
        }

    }
    
    @Override
    public void rescheduleJob(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rescheduleJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to reschedule job " + request.getId());
            }
        }else {
            throw new RuntimeException("can not find master url for job " + request.getId());
        }
    }
    
    @Override
    public void downJob(ScheduleRequest request) {
        
        SchedulerJob schedulerJob = new SchedulerJob();
        schedulerJob.setId(request.getId());
        schedulerJob.setJobReleasestate(JobReleaseState.OFFLINE.getCode());
        updateByPrimaryKeySelective(schedulerJob);
        String masterUrl = getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.downJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to down job " + request.getId());
            }
        }else {
           throw new RuntimeException("can not find master url for job " + request.getId());
        }

    }

	@Override
	public Page<JobResponse> findAll(Integer jobType, String jobName, Page<JobResponse> page) {
		List<SchedulerJob> jobList = schedulerJobMapper.findAll(jobType, jobName, page);
		JobResponse jobResponse = null;
		List<JobResponse> response = new ArrayList<JobResponse>();
		if(jobList != null && jobList.size() > 0) {
			for(SchedulerJob job : jobList) {
				jobResponse = new JobResponse();
				BeanUtils.copyProperties(job, jobResponse);
  		        setJobResponse(job,jobResponse);
				response.add(jobResponse);
			}
		}
        return page.setRecords(response);
	}
}