package com.ylpu.thales.scheduler.rest.service;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.CronUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.rpc.client.AbstractJobGrpcClient;
import com.ylpu.thales.scheduler.rpc.client.JobGrpcBlockingClient;
import com.ylpu.thales.scheduler.schedule.JobChecker;
import com.ylpu.thales.scheduler.schedule.JobDependency;
import com.ylpu.thales.scheduler.schedule.JobScheduleInfo;
import com.ylpu.thales.scheduler.schedule.JobScheduler;
import com.ylpu.thales.scheduler.schedule.JobSubmission;
import com.ylpu.thales.scheduler.schedule.SchedulerJob;
import com.ylpu.thales.scheduler.schedule.TaskCall;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SchedulerService {

    private static Log LOG = LogFactory.getLog(SchedulerService.class);

    /**
     * 杀任务
     * 
     * @param response
     * @throws Exception
     */
    public void killJob(ScheduleRequest request) throws Exception {
        killJob(request.getId());
    }
    
    public void killJob(Integer id) throws Exception{
        try {
            JobInstanceResponse response = JobManager.getJobInstanceById(id);
            if (response.getTaskState() != TaskState.RUNNING) {
                throw new RuntimeException("can not kill job " + id + " because job is not running ");
            }
            String worker = response.getWorker();
            if (StringUtils.isNotBlank(worker)) {
                JobInstanceRequestRpc rpcJobInstanceRequest = setRequest(response);
                String[] hostAndPort = worker.split(":");
                AbstractJobGrpcClient client = null;
                try {
                    client = new JobGrpcBlockingClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
                    client.kill(rpcJobInstanceRequest);
                } finally {
                    if (client != null) {
                        client.shutdown();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
    }

    public void markStatus(ScheduleRequest scheduleRequest, TaskState toState) throws Exception {
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(scheduleRequest.getId());
//        JobInstanceResponseRpc responseRpc = JobChecker.getResponse(jobRequestId);
        String responseId = jobInstanceResponse.getJobConf().getId() + "-" + 
        DateUtils.getDateAsString(DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(), 
                DateUtils.DATE_TIME_FORMAT), DateUtils.MINUTE_TIME_FORMAT);
        
        if(jobInstanceResponse != null) {
            try {
                cleanExistingTask(jobInstanceResponse,responseId);
                JobStatusRequest jr = new JobStatusRequest();
                jr.setIds(Arrays.asList(scheduleRequest.getId()));
                jr.setStatus(toState);
                JobManager.updateJobStatus(jr);
                
                JobChecker.addResponse(JobSubmission.buildResponse(responseId,toState.getCode()));
            } catch (Exception e) {
                LOG.error(e);
                throw e;
            }
        }
    }
    
    private void cleanExistingTask(JobInstanceResponse jobInstanceResponse,String responseId) throws Exception {
        
        if (jobInstanceResponse.getTaskState() == TaskState.RUNNING) {
            killJob(jobInstanceResponse.getId());
        }
        //remove rpc request from request map
        else if(jobInstanceResponse.getTaskState() == TaskState.WAITING_DEPENDENCY) {
            removeRequestRpc(jobInstanceResponse,responseId);

        }else if(jobInstanceResponse.getTaskState() == TaskState.QUEUED) {
            JobInstanceRequestRpc request = JobChecker.getJobInstanceRequestMap().remove(responseId);
            JobSubmission.getGroupQueue(request.getJob().getWorkerGroupname()).remove(new TaskCall(request));
                
        }else if(jobInstanceResponse.getTaskState() == TaskState.WAITING_RESOURCE) {
            JobChecker.getJobInstanceRequestMap().remove(responseId);
//          cancel waiting
//          JobSubmission.setNeed_waiting(false);
        }
    }
    
    private JobInstanceRequestRpc removeRequestRpc(JobInstanceResponse jobInstanceResponse,String jobRequestId) {
        JobInstanceRequestRpc requestRpc = null;
        Map<List<JobDependency>, String> dependsMap = JobChecker.getDepends();
        for (Entry<List<JobDependency>, String> entry : dependsMap.entrySet()) {
             String dependenciesAsString = getDependenciesAsString(entry.getKey());
             if(getDependenciesAsString(jobInstanceResponse).equalsIgnoreCase(dependenciesAsString) &&
                     entry.getValue().equalsIgnoreCase(jobRequestId)) {
                 String requestId = JobChecker.getDepends().remove(entry.getKey());
                 requestRpc = JobChecker.getJobInstanceRequestMap().remove(requestId);
             }
        }
        return requestRpc;
    }
    
    private String getDependenciesAsString(JobInstanceResponse jobInstanceResponse) {
        JobInstanceRequest request = new JobInstanceRequest();
        JobSubmission.initJobInstance(request, request.getJobId());
        request.setScheduleTime(DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(),DateUtils.DATE_TIME_FORMAT));
        request.setStartTime(DateUtils.getDateFromString(jobInstanceResponse.getStartTime(),DateUtils.DATE_TIME_FORMAT));
        request.setId(jobInstanceResponse.getId());
        JobInstanceRequestRpc rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobInstanceResponse.getJobConf());
        
        List<JobDependency> dependJobs = new ArrayList<JobDependency>();
        //get job dependency
        if (rpcRequest.getJob().getDependenciesList() == null
                || rpcRequest.getJob().getDependenciesList().size() == 0) {
            dependJobs.add(new JobDependency(rpcRequest.getJob().getId(), GlobalConstants.ROOT_SCHEDULE_TIME));
        } else {
            dependJobs = JobSubmission.getLatestJobDepends(rpcRequest,
                    DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(),DateUtils.DATE_TIME_FORMAT));
        }
        return getDependenciesAsString(dependJobs);
    }
    
    private String getDependenciesAsString(List<JobDependency> taskDependencies) {
        StringBuilder sb = new StringBuilder();
        Iterator<JobDependency> it = taskDependencies.iterator();
        while(it.hasNext()) {
            sb.append(it.next().toString());
            if(it.hasNext()) {
                sb.append("_");
            }
        }
        return sb.toString();
    }

    private JobInstanceRequestRpc setRequest(JobInstanceResponse response) {
        JobInstanceRequestRpc newRpcJobInstanceRequest = JobInstanceRequestRpc.newBuilder()
                .setApplicationid(response.getApplicationid()).setPid(response.getPid()).setId(response.getId())
                .setRequestId(
                        response.getJobConf().getId() + "-"
                                + DateUtils.getDateAsString(DateUtils.getDateFromString(response.getScheduleTime(),
                                        DateUtils.DATE_TIME_FORMAT), DateUtils.MINUTE_TIME_FORMAT))
                .setJob(JobSubmission.setJobRequest(response.getJobConf())).setCreatorEmail(response.getCreatorEmail())
                .setCreatorName(response.getCreatorName())
                .setScheduleTime(DateUtils.getProtobufTime(
                        DateUtils.getDateFromString(response.getScheduleTime(), DateUtils.DATE_TIME_FORMAT)))
                .setStartTime(DateUtils.getProtobufTime(
                        DateUtils.getDateFromString(response.getStartTime(), DateUtils.DATE_TIME_FORMAT)))
                .setLogPath(response.getLogPath()).setLogUrl(response.getLogUrl())
                .setRetryTimes(response.getRetryTimes()).setTaskState(response.getTaskState().getCode())
                .setWorker(response.getWorker()).build();
        return newRpcJobInstanceRequest;
    }

    /**
     * 任务调度
     * 
     * @param response
     * @throws Exception
     */
    public void scheduleJob(ScheduleRequest request) throws Exception {
        try {
            JobResponse jobResponse = JobManager.getJobById(request.getId());
            
            JobScheduleInfo scheduleInfo = new JobScheduleInfo();
            setScheduleInfo(jobResponse, scheduleInfo);
            if(!JobScheduler.jobExists(scheduleInfo)) {
                JobScheduler.addJob(scheduleInfo, SchedulerJob.class);
            }else {
                throw new RuntimeException("job " + jobResponse.getJobName() + " has scheduled");
            }
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
    }

    /**
     * 任务时间修改后重新调度
     * 
     * @param response
     * @throws Exception
     */
    public void rescheduleJob(ScheduleRequest request) throws Exception {
        try {
            JobScheduleInfo scheduleInfo = new JobScheduleInfo();
            JobResponse response = JobManager.getJobById(request.getId());
            setScheduleInfo(response, scheduleInfo);
            JobScheduler.modifyJobTime(scheduleInfo);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }
    }

    /**
     * 任务下线
     * 
     * @param response
     * @throws Exception
     */
    public void downJob(ScheduleRequest request) throws Exception {
        try {
            JobResponse response = JobManager.getJobById(request.getId());
            JobScheduleInfo scheduleInfo = new JobScheduleInfo();
            setScheduleInfo(response, scheduleInfo);
            JobScheduler.removeJob(scheduleInfo);
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        }

    }

    /**
     * 重跑当前任务
     * 
     * @param response
     * @throws Exception
     */
    public void rerun(Integer id) throws Exception {
        JobInstanceRequestRpc rpcRequest = null;
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(id);
        if (jobInstanceResponse.getJobConf() == null) {
            LOG.warn("job does not exist or has already down " + id);
        }else {
            
            String responseId = jobInstanceResponse.getJobConf().getId() + "-" + 
            DateUtils.getDateAsString(DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(), 
                    DateUtils.DATE_TIME_FORMAT), DateUtils.MINUTE_TIME_FORMAT);
            
            if (jobInstanceResponse.getTaskState() == TaskState.SUBMIT
                    || jobInstanceResponse.getTaskState() == TaskState.SCHEDULED
                    || jobInstanceResponse.getTaskState() == TaskState.WAITING_DEPENDENCY
                    || jobInstanceResponse.getTaskState() == TaskState.QUEUED
                    || jobInstanceResponse.getTaskState() == TaskState.WAITING_RESOURCE
                    || jobInstanceResponse.getTaskState() == TaskState.RUNNING) {
                LOG.warn("job " + id +  " has already running,will clean the existing job");
                cleanExistingTask(jobInstanceResponse,responseId);
            } 
            JobInstanceRequest request = new JobInstanceRequest();
            try {
                // 初始化任务
                JobSubmission.initJobInstance(request, jobInstanceResponse.getJobId());
                request.setId(jobInstanceResponse.getId());
                request.setRetryTimes(jobInstanceResponse.getRetryTimes() + 1);
                request.setScheduleTime(
                        DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
                request.setStartTime(new Date());
                request.setCreateTime(new Date());
                //transit task status to submit
                JobManager.updateJobInstanceByKey(request);

                JobChecker.addResponse(JobSubmission.buildResponse(responseId,TaskState.SUBMIT.getCode()));

            }catch(Exception e) {
                LOG.error("failed to submit task " + request.getId() , e);
            }
            try {
                rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobInstanceResponse.getJobConf());
//                caculate dependency and add to request
                JobSubmission.addRpcRequest(rpcRequest);
            } catch (Exception e) {
                LOG.error("fail to update job " + rpcRequest.getId() + " status with exception " + e.getMessage());
                scheduleFailed(request);
                JobInstanceResponseRpc responseRpc = JobSubmission.buildResponse(rpcRequest.getRequestId(), TaskState.FAIL.getCode());
                JobChecker.addResponse(responseRpc);
            }
        } 
    }
    
    private void scheduleFailed(JobInstanceRequest request) {
        request.setTaskState(TaskState.FAIL.getCode());
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        try {
            JobManager.updateJobInstanceSelective(request);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * 重跑当前任务以及下游所有任务
     * 
     * @param scheduleRequest
     * @throws Exception
     */
    public void rerunAll(Integer id) throws Exception {
        try {
            rerun(id);
            JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(id);
            if (jobInstanceResponse != null) {
                JobTree jobTree = JobManager.queryTreeById(jobInstanceResponse.getJobConf().getId());
                if (jobTree != null && jobTree.getChildren() != null && jobTree.getChildren().size() > 0) {
                    for (JobTree child : jobTree.getChildren()) {
                        rerunChild(DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(),
                                DateUtils.DATE_TIME_FORMAT), child);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * 重跑子任务
     * 
     * @param startScheduleTime
     * @param jobTree
     * @throws Exception
     */
    private void rerunChild(Date startScheduleTime, JobTree jobTree) throws Exception {
        try {
            String scheduleTime = caculateJobScheduleTime(startScheduleTime, jobTree.getScheduleCron());
            JobInstanceResponse response = JobManager.getJobInstanceByTime(jobTree.getJobId(), scheduleTime);
            if (response != null && response.getId() != null) {
                rerun(response.getId());
            }
            if (jobTree.getChildren() != null) {
                for (JobTree child : jobTree.getChildren()) {
                    rerunChild(startScheduleTime, child);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * 计算子任务的schedule time
     * 
     * @param startScheduleTime
     * @param currentScheduleCron
     * @return
     */
    private String caculateJobScheduleTime(Date startScheduleTime, String currentScheduleCron) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 1);
        return CronUtils.getNextTriggerTimeAsString(currentScheduleCron, startScheduleTime, calendar.getTime());
    }

    private void setScheduleInfo(JobResponse response, JobScheduleInfo scheduleInfo) {
        scheduleInfo.setCron(response.getScheduleCron());
        scheduleInfo.setJobName(response.getJobName());
        scheduleInfo.setJobGroupName(GlobalConstants.DEFAULT_SCHEDULER_JOB_GROUP);
        scheduleInfo.setTriggerName(response.getJobName());
        scheduleInfo.setTriggerGroupName(GlobalConstants.DEFAULT_SCHEDULER_TRIGGER_GROUP);
//        scheduleInfo.setData(response);
        scheduleInfo.setId(response.getId());
    }
}
