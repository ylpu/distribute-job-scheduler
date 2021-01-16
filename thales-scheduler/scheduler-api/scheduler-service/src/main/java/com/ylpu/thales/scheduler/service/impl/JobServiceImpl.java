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
public class JobServiceImpl extends BaseServiceImpl<SchedulerJob, Integer> implements JobService {

    @Autowired
    private SchedulerJobMapper schedulerJobMapper;

    @Autowired
    private SchedulerJobRelationMapper schedulerJobRelationMapper;

    @Override
    protected BaseDao<SchedulerJob, Integer> getDao() {
        return schedulerJobMapper;
    }

    @Override
    public void addJob(JobRequest job, Object object) {
        List<Integer> depencies = new ArrayList<Integer>();
        if (schedulerJobMapper.getJobCountByName(job.getJobName()) >= 1) {
            throw new ThalesRuntimeException("job name has exist");
        }
        if (job.getDependIds() == null || job.getDependIds().size() == 0) {
            depencies = Arrays.asList(-1);
        } else {
            depencies = job.getDependIds();
        }
        SchedulerJob schedulerJob = new SchedulerJob();
        if (job != null) {
            if (object != null) {
                UserResponse user = (UserResponse) object;
                job.setCreatorId(user.getUserName());
            }
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob, job);
            insertSelective(schedulerJob);
        }
        SchedulerJobRelation sr = null;
        if (depencies != null && depencies.size() > 0) {
            for (Integer parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(schedulerJob.getId());
                sr.setParentjobId(parentJobId);
                schedulerJobRelationMapper.insertSelective(sr);
            }
        }
    }

    @Override
    public void updateJob(JobRequest job, Object object) {
        
        if (!isJobOwner(job.getOwnerIds(), object)) {
            throw new ThalesRuntimeException("not job owner can not change job");
        }
        List<Integer> depencies = new ArrayList<Integer>();
        if (job.getDependIds() == null || job.getDependIds().size() == 0) {
            depencies = Arrays.asList(-1);
        } else {
            depencies = job.getDependIds();
        }
        if(job.getDependIds() != null && job.getDependIds().size() > 0 && 
        		job.getDependIds().contains(job.getId())) {
            throw new ThalesRuntimeException("job " + job.getId() + " can not depend itself");
        }
        if (isCycleReference(job)) {
            throw new ThalesRuntimeException("job " + job.getId() + " has cycle reference");
        }

        if (job != null) {
            SchedulerJob schedulerJob = new SchedulerJob();
            BeanUtils.copyProperties(job, schedulerJob);
            setJobRequest(schedulerJob, job);
            updateByPrimaryKeySelective(schedulerJob);
        }
        if (depencies != null && depencies.size() > 0) {
            schedulerJobRelationMapper.deleteByJobId(job.getId());
            SchedulerJobRelation sr = null;
            for (Integer parentJobId : depencies) {
                sr = new SchedulerJobRelation();
                sr.setJobId(job.getId());
                sr.setParentjobId(parentJobId);
                schedulerJobRelationMapper.insertSelective(sr);
            }
        }
    }

    public List<JobDependencyResponse> getAllJobs() {
        List<JobDependencyResponse> responses = new ArrayList<JobDependencyResponse>();
        JobDependencyResponse response = null;
        List<JobDependency> list = schedulerJobMapper.getAllJobs();
        if (list != null && list.size() > 0) {
            for (JobDependency job : list) {
                response = new JobDependencyResponse();
                BeanUtils.copyProperties(job, response);
                responses.add(response);
            }
        }
        return responses;
    }

    private boolean isJobOwner(String ownerId, Object object) {
        UserResponse user = (UserResponse) object;
        List<String> owners = Arrays.asList(ownerId.split(","));
        if (owners.contains(user.getUserName()) || user.getRoleNames().contains(RoleTypes.ROLE_ADMIN.toString())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCycleReference(JobRequest job) {
        JobTree jobTree = queryTreeById(job.getId());
        List<Integer> children = new ArrayList<Integer>();
        listChildren(jobTree, children);
        if (job.getDependIds() != null && job.getDependIds().size() > 0) {
            for (Integer id : job.getDependIds()) {
                if (children.contains(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void listChildren(JobTree jobTree, List<Integer> children) {
        if (jobTree.getChildren() == null) {
            return;
        } else {
            for (JobTree job : jobTree.getChildren()) {
                children.add(job.getJobId());
                listChildren(job, children);
            }
        }
    }
//    public JobTree queryTreeById(Integer id) {
//        JobTree targetTree = new JobTree();
//        List<com.ylpu.thales.scheduler.entity.JobTree> treeList = schedulerJobMapper.queryTreeById(id);
//        if (treeList != null && treeList.size() > 0) {
//            if (treeList.get(0) != null) {
//                setResponse(treeList.get(0), targetTree);
//            }
//        }
//
//        return targetTree;
//    }
    
    public JobTree queryTreeById(Integer id) {
        JobTree rootTree = new JobTree();
        List<SchedulerJob> allJobList = schedulerJobMapper.findAll(null, "", "");
        List<SchedulerJobRelation> allRelationList = schedulerJobRelationMapper.findAll();
        SchedulerJob schedulerJob = allJobList.stream().filter(job -> job.getId() == id).collect(Collectors.toList()).get(0);
        setJobTree(schedulerJob,rootTree);
        setChildren(rootTree,allJobList,allRelationList);
        return rootTree;
    }
    
    private void setChildren(JobTree jobTree,List<SchedulerJob> allJobList,List<SchedulerJobRelation> allRelationList) {
        
        List<SchedulerJobRelation> relationJobList = new ArrayList<SchedulerJobRelation>();
        if(allRelationList != null && allRelationList.size() > 0) {
            for(SchedulerJobRelation jobRelation : allRelationList) {
                if(jobRelation.getParentjobId() == jobTree.getJobId()) {
                    relationJobList.add(jobRelation);
                }
            }
        }
        List<JobTree> children = null;
        List<Integer> relationIdList = relationJobList.stream().map(relation -> relation.getJobId()).collect(Collectors.toList());
        if(relationIdList != null && relationIdList.size() > 0) {
            children = new ArrayList<JobTree>();
            JobTree tree = null;
            for(Integer relationId : relationIdList) {
                for(SchedulerJob childJob : allJobList) {
                    if(childJob.getId() == relationId) {
                        tree = new JobTree();
                        setJobTree(childJob,tree);
                        children.add(tree);
                        break;
                    }
                }
            }
        }
        if(children == null || children.size() == 0) {
            return;
        }else {
            jobTree.setChildren(children);
            for(JobTree child : children) {
                setChildren(child,allJobList,allRelationList);
            }
        }
    }
    
    private void setJobTree(SchedulerJob schedulerJob, JobTree jobTree) {
        jobTree.setJobCycle(schedulerJob.getJobCycle());
        jobTree.setScheduleCron(schedulerJob.getScheduleCron());
        jobTree.setJobId(schedulerJob.getId());
        jobTree.setJobName(schedulerJob.getJobName());
        
    }

//    private void setResponse(com.ylpu.thales.scheduler.entity.JobTree sourceTree, JobTree targetTree) {
//        List<JobTree> targetList = new ArrayList<JobTree>();
//        setTarget(sourceTree, targetTree);
//        targetTree.setChildren(targetList);
//
//        List<com.ylpu.thales.scheduler.entity.JobTree> sourceList = sourceTree.getChildren();
//        for (com.ylpu.thales.scheduler.entity.JobTree source : sourceList) {
//            JobTree target = new JobTree();
//            setTarget(source, target);
//            targetList.add(target);
//            setResponse(source, target);
//        }
//    }

//    private void setTarget(com.ylpu.thales.scheduler.entity.JobTree source, JobTree target) {
//        target.setJobCycle(source.getJobCycle());
//        target.setScheduleCron(source.getScheduleCron());
//        target.setJobId(source.getJobId());
//        target.setParentJobId(source.getParentJobId());
//        target.setJobName(source.getJobName());
//    }

    @Override
    public JobResponse getJobAndRelationById(Integer id) {
        JobResponse response = new JobResponse();
        List<SchedulerJob> jobList = schedulerJobMapper.getJobParentsByIds(Arrays.asList(id));
        if (jobList == null || jobList.size() == 0) {
            return null;
        }
        SchedulerJob schedulerJob = jobList.get(0);
        if (schedulerJob != null) {
            BeanUtils.copyProperties(schedulerJob, response);
            setJobResponse(schedulerJob, response);
            List<SchedulerJobRelation> dependencies = schedulerJob.getRelations();
            List<Integer> ids = dependencies.stream().map(t -> t.getParentjobId()).collect(Collectors.toList());
            List<JobResponse> list = new ArrayList<JobResponse>();
            if (ids != null && ids.size() > 0) {
                List<SchedulerJob> jobs = schedulerJobMapper.getJobParentsByIds(ids);
                if (jobs != null && jobs.size() > 0) {
                    JobResponse dependency = null;
                    for (SchedulerJob job : jobs) {
                        dependency = new JobResponse();
                        BeanUtils.copyProperties(job, dependency);
                        setJobResponse(job, dependency);
                        list.add(dependency);
                    }
                }
            }
            response.setDependencies(list);
        }
        return response;
    }

    private void setJobResponse(SchedulerJob schedulerJob, JobResponse response) {
        response.setJobCycle(JobCycle.getJobCycle(schedulerJob.getJobCycle()).name());
        response.setJobPriority(JobPriority.getJobPriority(schedulerJob.getJobPriority()).name());
        response.setJobType(JobType.getJobType(schedulerJob.getJobType()).name());
        response.setAlertTypes(AlertType.getAlertType(schedulerJob.getAlertTypes()).name());
        List<Integer> collect = schedulerJob.getRelations().stream().map(p -> p.getParentjobId())
                .collect(Collectors.toList());
        if (collect.size() == 1 && collect.get(0).equals(-1)) {
            response.setDependIds(null);
        } else {
            response.setDependIds(collect);
        }
    }

    private void setJobRequest(SchedulerJob schedulerJob, JobRequest job) {
        schedulerJob.setJobCycle(JobCycle.getJobCycle(job.getJobCycle()).getCode());
        schedulerJob.setJobPriority(JobPriority.getJobPriority(job.getJobPriority()).getPriority());
        schedulerJob.setJobType(JobType.getJobType(job.getJobType()).getCode());
        schedulerJob.setAlertTypes(AlertType.getAlertType(job.getAlertTypes()).getCode());
        schedulerJob.setJobReleasestate(JobReleaseState.ONLINE.getCode());
    }

    @Override
    public void scheduleJob(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.scheduleJob(masterUrl, request);
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to schedule job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("schedule service is not available");
        }

    }

    @Override
    public void rescheduleJob(ScheduleRequest request) {
        String masterUrl = getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.rescheduleJob(masterUrl, request);
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to reschedule job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("scheduler service is not available");
        }
    }

    @Override
    public void downJob(ScheduleRequest request, UserResponse user) {
        JobResponse jobResponse = getJobAndRelationById(request.getId());
        if (!isJobOwner(jobResponse.getOwnerIds(), user)) {
            throw new ThalesRuntimeException("not job owner can not down job");
        }
        SchedulerJob schedulerJob = new SchedulerJob();
        schedulerJob.setId(request.getId());
        schedulerJob.setJobReleasestate(JobReleaseState.OFFLINE.getCode());
        schedulerJob.setUpdateTime(new Date());
        updateByPrimaryKeySelective(schedulerJob);
        String masterUrl = getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.downJob(masterUrl, request);
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to down job " + request.getId());
            }
        } else {
            throw new ThalesRuntimeException("scheduler service is not available");
        }

    }

    @Override
    public PageInfo<JobResponse> findAll(Integer jobType, String jobName, int pageSize, int pageNo, String userName) {
        PageHelper.startPage(pageNo, pageSize);
        List<SchedulerJob> jobList = schedulerJobMapper.findAll(jobType, jobName, userName);
        JobResponse jobResponse = null;
        Page<JobResponse> page = new Page<JobResponse>();
        if (jobList != null && jobList.size() > 0) {
            for (SchedulerJob job : jobList) {
                jobResponse = new JobResponse();
                List<SchedulerJobRelation> jobRelations = schedulerJobMapper.findParentJobsById(job.getId());
                job.setRelations(jobRelations);
                BeanUtils.copyProperties(job, jobResponse);
                setJobResponse(job, jobResponse);
                page.add(jobResponse);
            }
        }
        page.setTotal(schedulerJobMapper.getJobCount(jobType, jobName, userName));
        PageInfo<JobResponse> pageInfo = new PageInfo<JobResponse>(page);
        return pageInfo;
    }
}