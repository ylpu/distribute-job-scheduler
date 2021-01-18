package com.ylpu.thales.scheduler.master.api.service;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
//import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.utils.CronUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.rpc.client.AbstractJobGrpcClient;
import com.ylpu.thales.scheduler.master.rpc.client.JobGrpcBlockingClient;
import com.ylpu.thales.scheduler.master.schedule.JobStatusChecker;
import com.ylpu.thales.scheduler.master.schedule.JobDependency;
import com.ylpu.thales.scheduler.master.schedule.JobScheduleInfo;
import com.ylpu.thales.scheduler.master.schedule.JobScheduler;
import com.ylpu.thales.scheduler.master.schedule.JobSubmission;
import com.ylpu.thales.scheduler.master.schedule.SchedulerJob;
import com.ylpu.thales.scheduler.master.schedule.TaskCall;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;

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
     * kill task
     * 
     * @param response
     * @throws Exception
     */
    public void killJob(ScheduleRequest request) throws Exception {
        killJob(request.getId());
    }
    
    public void killJob(Integer id) throws Exception{
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
            }catch(Exception e) {
                LOG.error("faile to kill task " + id + " with exception " + e.getMessage());
                throw e;
            }finally {
                if (client != null) {
                    client.shutdown();
                }
            }
        }
    }

    public void markStatus(ScheduleRequest scheduleRequest, TaskState toState) throws Exception {
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(scheduleRequest.getId());
        if(jobInstanceResponse != null) {
            try {
                String responseId = jobInstanceResponse.getJobConf().getId() + "-" + 
                        DateUtils.getDateAsString(DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(), 
                                DateUtils.DATE_TIME_FORMAT), DateUtils.MINUTE_TIME_FORMAT);
                
                cleanExistingTask(jobInstanceResponse,responseId);
                JobStatusRequest jr = new JobStatusRequest();
                jr.setIds(Arrays.asList(scheduleRequest.getId()));
                jr.setStatus(toState);
                JobManager.updateJobStatus(jr);
                JobStatusChecker.addResponse(JobSubmission.buildResponse(responseId,toState.getCode()));
            } catch (Exception e) {
                LOG.error("failed to mark task " + scheduleRequest.getId() + " to " + toState.toString() , e);
                throw e;
            }
        }
    }
    
    private void cleanExistingTask(JobInstanceResponse jobInstanceResponse,String responseId) throws Exception {

        if(jobInstanceResponse.getTaskState() == TaskState.SCHEDULED || 
                jobInstanceResponse.getTaskState() == TaskState.SUBMIT) {
            JobStatusChecker.getJobInstanceRequestMap().remove(responseId);
            removeJobDependency(jobInstanceResponse,responseId);
        }else if(jobInstanceResponse.getTaskState() == TaskState.WAITING_DEPENDENCY) {
            JobStatusChecker.getJobInstanceRequestMap().remove(responseId);
            removeJobDependency(jobInstanceResponse,responseId);
            JobStatusChecker.getJobDependStatusMap().remove(responseId);
        }else if(jobInstanceResponse.getTaskState() == TaskState.QUEUED) {
            JobInstanceRequestRpc request = JobStatusChecker.getJobInstanceRequestMap().remove(responseId);
            if(request != null) {
                JobSubmission.getGroupQueue(request.getJob().getWorkerGroupname()).remove(new TaskCall(request));
            }
        }
        else if(jobInstanceResponse.getTaskState() == TaskState.WAITING_RESOURCE) {
            JobStatusChecker.getJobInstanceRequestMap().remove(responseId);
            JobSubmission.getWaitingResourceMap().put(jobInstanceResponse.getJobConf().getWorkerGroupname(), false);
        }else if (jobInstanceResponse.getTaskState() == TaskState.RUNNING) {
            killJob(jobInstanceResponse.getId());
        }else if(jobInstanceResponse.getTaskState() == TaskState.FAIL || jobInstanceResponse.getTaskState() == TaskState.SUCCESS
                || jobInstanceResponse.getTaskState() == TaskState.KILL) {
            JobStatusChecker.getResponses().remove(responseId);
        }
    }
    
    private void removeJobDependency(JobInstanceResponse jobInstanceResponse,String jobRequestId) {
        Map<List<JobDependency>, String> dependsMap = JobStatusChecker.getDepends();
        if(dependsMap != null && dependsMap.size() > 0) {
            for (Entry<List<JobDependency>, String> entry : dependsMap.entrySet()) {
                String dependenciesAsString = getDependenciesAsString(entry.getKey());
                if(getDependenciesAsString(jobInstanceResponse).equalsIgnoreCase(dependenciesAsString) &&
                        entry.getValue().equalsIgnoreCase(jobRequestId)) {
                    JobStatusChecker.getDepends().remove(entry.getKey());
//                    requestRpc = JobStatusChecker.getJobInstanceRequestMap().remove(requestId);
                }
           }
        }
    }
    
    private String getDependenciesAsString(JobInstanceResponse jobInstanceResponse) {
        JobInstanceRequest request = new JobInstanceRequest();
        JobSubmission.initJobInstance(request, jobInstanceResponse.getJobConf());
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
                .setApplicationid(response.getApplicationid())
                .setPid(response.getPid())
                .setId(response.getId())
                .setRequestId(
                        response.getJobConf().getId() + "-"
                                + DateUtils.getDateAsString(DateUtils.getDateFromString(response.getScheduleTime(),
                                        DateUtils.DATE_TIME_FORMAT), DateUtils.MINUTE_TIME_FORMAT))
                .setJob(JobSubmission.setJobRequest(response.getParameters(),response.getJobConf()))
                .setCreatorEmail(response.getCreatorEmail())
                .setCreatorName(response.getCreatorName())
                .setScheduleTime(DateUtils.getProtobufTime(
                        DateUtils.getDateFromString(response.getScheduleTime(), DateUtils.DATE_TIME_FORMAT)))
                .setStartTime(DateUtils.getProtobufTime(
                        DateUtils.getDateFromString(response.getStartTime(), DateUtils.DATE_TIME_FORMAT)))
                .setLogPath(response.getLogPath())
                .setLogUrl(response.getLogUrl())
                .setRetryTimes(response.getRetryTimes())
                .setTaskState(response.getTaskState().getCode())
                .setWorker(response.getWorker()).build();
        return newRpcJobInstanceRequest;
    }

    /**
     * schedule job
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
            LOG.error("failed to schedule job " + request.getId() , e);
            throw e;
        }
    }

    /**
     * reschedule job
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
            LOG.error("failed to reschedule job " + request.getId() , e);
            throw e;
        }
    }

    /**
     * down job
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
            LOG.error("failed to down job " + request.getId() , e);
            throw e;
        }

    }

    /**
     * return job
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
            
            LOG.warn("job " + id +  " has already exist,will clean it at first");
            cleanExistingTask(jobInstanceResponse,responseId);
            
            JobInstanceRequest request = new JobInstanceRequest();
            request.setParameters(jobInstanceResponse.getParameters());
            try {
                JobSubmission.initJobInstance(request, jobInstanceResponse.getJobConf());
                request.setId(jobInstanceResponse.getId());
                request.setRetryTimes(jobInstanceResponse.getRetryTimes() + 1);
                request.setScheduleTime(
                        DateUtils.getDateFromString(jobInstanceResponse.getScheduleTime(), DateUtils.DATE_TIME_FORMAT));
                request.setStartTime(new Date());
                request.setCreateTime(new Date());
                //transit task status to submit
                JobManager.updateJobInstanceByKey(request);                
                rpcRequest = JobSubmission.initJobInstanceRequestRpc(request, jobInstanceResponse.getJobConf());
//              caculate dependency and add to request
                JobSubmission.addRpcRequest(rpcRequest);
                LOG.info("job " + jobInstanceResponse.getId() + " submit at " + DateUtils.getDateAsString(new Date(),DateUtils.TIME_FORMAT));
            }catch(Exception e) {
                LOG.error("failed to rerun task " + request.getId() , e);
                throw new RuntimeException(e);
            }
        } 
    }

    /**
     * rerun current job and child jobs
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
     * rerun child job
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
     * caculate task schedule time
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
