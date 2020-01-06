package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
import com.ylpu.thales.scheduler.dao.SchedulerJobMapper;
import com.ylpu.thales.scheduler.dao.SchedulerJobRelationMapper;
import com.ylpu.thales.scheduler.entity.JobDependency;
import com.ylpu.thales.scheduler.entity.SchedulerJob;
import com.ylpu.thales.scheduler.entity.SchedulerJobRelation;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.JobCycle;
import com.ylpu.thales.scheduler.enums.JobPriority;
import com.ylpu.thales.scheduler.enums.JobReleaseState;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.RoleTypes;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobDependencyResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.response.UserResponse;
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
    public void addJob(JobRequest job,Object object) {	
		List<Integer> depencies = new ArrayList<Integer>();
		if(schedulerJobMapper.getJobCountByName(job.getJobName()) >=1) {
			throw new ThalesRuntimeException("任务名称已经存在");
		}
		if(job.getDependIds() == null || job.getDependIds().size() == 0){
			depencies = Arrays.asList(-1);
		}else {
	        depencies = job.getDependIds();
		}
        SchedulerJob schedulerJob = new SchedulerJob();
        if(job != null) {
            if(object != null) {
              	UserResponse user = (UserResponse)object;
                job.setCreatorId(user.getUserName());
            }
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob,job);
            insertSelective(schedulerJob);
        }
        SchedulerJobRelation sr = null; 
        if(depencies != null && depencies.size() > 0) {
           for(Integer parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(schedulerJob.getId());
                sr.setParentjobId(parentJobId);
                schedulerJobRelationMapper.insertSelective(sr);
           }
       }
    }
	
    @Override
    public void updateJob(JobRequest job,Object object) {  
		List<Integer> depencies = new ArrayList<Integer>();
		if(job.getDependIds() == null || job.getDependIds().size() == 0){
			depencies = Arrays.asList(-1);
		}else {
	        depencies = job.getDependIds();
		}
        if(isCycleReference(job)) {
     	   throw new ThalesRuntimeException("任务 " + job.getId() + " 存在环形依赖");
        }
        if(!isJobOwner(job.getOwnerIds(),object)) {
          	throw new ThalesRuntimeException("非任务owner不能修改任务");
        }
        if(job != null) {
            SchedulerJob schedulerJob = new SchedulerJob();
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob,job);
            updateByPrimaryKeySelective(schedulerJob);
        }
        if(depencies != null && depencies.size() > 0) {
            schedulerJobRelationMapper.deleteByJobId(job.getId());
            SchedulerJobRelation sr = null;
            for(Integer parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(job.getId());
                sr.setParentjobId(parentJobId);
                schedulerJobRelationMapper.insertSelective(sr);
            }
        }
    }	
    
    public List<JobDependencyResponse> getAllJobs(){
    	   List<JobDependencyResponse> responses = new ArrayList<JobDependencyResponse>();
    	   JobDependencyResponse response = null;
    	   List<JobDependency> list = schedulerJobMapper.getAllJobs();
    	   if(list != null && list.size() > 0 ) {
    		   for(JobDependency job : list) {
    			   response = new JobDependencyResponse();
    			   BeanUtils.copyProperties(job, response);
    			   responses.add(response);
    		   }
    	   }
    	   return responses;
    }
    
    private boolean isJobOwner(String ownerId,Object object) {
		if(object == null) {
			throw new ThalesRuntimeException("请重新登陆");
		}
 		UserResponse user = (UserResponse)object;
 		List<String> owners = Arrays.asList(ownerId.split(","));
 	    if(owners.contains(user.getUserName()) || user.getRoleNames().contains(RoleTypes.ROLE_ADMIN.toString())) {
 	        return true;
 	    }else {
            return false;
 	    }
    }
    
    private boolean isCycleReference(JobRequest job) {
    	    JobTree jobTree = queryTreeById(job.getId());
    	    List<Integer> children = new ArrayList<Integer>();
    	    listChildren(jobTree,children);
    	    if(job.getDependIds() != null && job.getDependIds().size() > 0){
    	    	   for(Integer id : job.getDependIds()) {
    	    		   if(children.contains(id)) {
    	    			   return true;
    	    		   }
    	    	   }
    	    }
    	    return false;
    }
    
	private static void listChildren(JobTree jobTree,List<Integer> children){
		if(jobTree.getChildren() == null) {
			return;
		}else {
			for(JobTree job : jobTree.getChildren()) {
				children.add(job.getJobId());
				listChildren(job,children);
			}
		}
	}
    
    public JobTree queryTreeById(Integer id) {
        JobTree targetTree = new JobTree();
        List<com.ylpu.thales.scheduler.entity.JobTree> treeList = schedulerJobMapper.queryTreeById(id);
        if(treeList != null && treeList.size() > 0) {
            if(treeList.get(0) != null) {
                setResponse(treeList.get(0),targetTree); 
            }
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
        target.setJobName(source.getJobName());
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
	        if(collect.size() ==1 && collect.get(0).equals(-1)) {
	  	      response.setDependIds(null);
	        }else {
	  	      response.setDependIds(collect);
	        }
	}
	
	private void setJobRequest(SchedulerJob schedulerJob,JobRequest job) {
        schedulerJob.setJobCycle(JobCycle.getJobCycle(job.getJobCycle()).getCode());
        schedulerJob.setJobPriority(JobPriority.getJobPriority(job.getJobPriority()).getPriority());
        schedulerJob.setJobType(JobType.getJobType(job.getJobType()).getCode());
        schedulerJob.setAlertTypes(AlertType.getAlertType(job.getAlertTypes()).getCode());
        schedulerJob.setJobReleasestate(JobReleaseState.ONLINE.getCode());
	}

    @Override
    public void scheduleJob(ScheduleRequest request) {
        String masterUrl = CuratorHelper.getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.scheduleJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to schedule job " + request.getId());
            }
        }else {
            throw new ThalesRuntimeException("调度服务不可用");
        }

    }
    
    @Override
    public void rescheduleJob(ScheduleRequest request) {
        String masterUrl = CuratorHelper.getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rescheduleJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to reschedule job " + request.getId());
            }
        }else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }
    
    @Override
    public void downJob(ScheduleRequest request,UserResponse user) {
    	    JobResponse jobResponse = getJobAndRelationById(request.getId());
        if(!isJobOwner(jobResponse.getOwnerIds(),user)) {
           throw new ThalesRuntimeException("非任务owner不能修改任务");
        }
        SchedulerJob schedulerJob = new SchedulerJob();
        schedulerJob.setId(request.getId());
        schedulerJob.setJobReleasestate(JobReleaseState.OFFLINE.getCode());
        schedulerJob.setUpdateTime(new Date());
        updateByPrimaryKeySelective(schedulerJob);
        String masterUrl = CuratorHelper.getMasterServiceUri(request.getId());
        if(StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.downJob(masterUrl, request);
            if(status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to down job " + request.getId());
            }
        }else {
        	    throw new ThalesRuntimeException("调度服务不可用");
        }

    }

	@Override
	public PageInfo<JobResponse> findAll(Integer jobType, String jobName, int pageSize, int pageNo) {
		PageHelper.startPage(pageNo,pageSize);
		List<SchedulerJob> jobList = schedulerJobMapper.findAll(jobType, jobName);
		JobResponse jobResponse = null;
		Page<JobResponse> page = new Page<JobResponse>();
		if(jobList != null && jobList.size() > 0) {
			for(SchedulerJob job : jobList) {
				jobResponse = new JobResponse();
				BeanUtils.copyProperties(job, jobResponse);
  		        setJobResponse(job,jobResponse);
  		        page.add(jobResponse);
			}
		}
		page.setTotal(schedulerJobMapper.getJobCount(jobType,jobName));
		PageInfo<JobResponse> pageInfo = new PageInfo<JobResponse>(page);
        return pageInfo;
	}
}