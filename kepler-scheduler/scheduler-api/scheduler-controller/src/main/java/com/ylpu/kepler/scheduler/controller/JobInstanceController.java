package com.ylpu.kepler.scheduler.controller;

import java.util.List;
import java.util.Map;

import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.request.JobStatusRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;
import com.ylpu.kepler.scheduler.response.JobInstanceStateResponse;
import com.ylpu.kepler.scheduler.response.SchedulerResponse;
import com.ylpu.kepler.scheduler.service.JobInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/jobInstance")
public class JobInstanceController {
	
    @Autowired
    private JobInstanceService jobInstanceService;
    
    @ResponseBody
    @RequestMapping(value="/addJobInstance",method=RequestMethod.POST)
    public SchedulerResponse<Integer> addJobInstance(@RequestBody JobInstanceRequest request) {
        return new SchedulerResponse<Integer>(jobInstanceService.addJobInstance(request));
    }
    
    @ResponseBody
    @RequestMapping(value="/updateJobInstanceSelective",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateJobInstanceSelective(@RequestBody JobInstanceRequest request) {
        jobInstanceService.updateJobInstanceSelective(request);
        return SchedulerResponse.success();
    }  
    
    @ResponseBody
    @RequestMapping(value="/updateJobInstanceByKey",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateJobInstanceByKey(@RequestBody JobInstanceRequest request) {
        jobInstanceService.updateJobInstanceByKey(request);
        return SchedulerResponse.success();
    } 
    
    @ResponseBody
    @RequestMapping(value="/getJobInstanceById",method=RequestMethod.GET)
    public SchedulerResponse<JobInstanceResponse> getJobInstanceById(@RequestParam("id") Integer id) {
       return new SchedulerResponse<JobInstanceResponse>(jobInstanceService.getJobInstanceById(id));
    }
    
    @ResponseBody
    @RequestMapping(value="/getInstanceIdByTime",method=RequestMethod.GET)
    public SchedulerResponse<Integer> getInstanceIdByTime(@RequestParam("jobId") Integer jobId,@RequestParam("scheduleTime") String scheduleTime) {
       return new SchedulerResponse<Integer>(jobInstanceService.getInstanceIdByTime(jobId,scheduleTime));
    }
    
    /**
     * master启动时获取每台机器运行中的任务个数，供监控使用
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/getRunningJobCount",method=RequestMethod.GET)
    public SchedulerResponse<List<Map<String,Object>>> getRunningJobCount(){
        return new SchedulerResponse<List<Map<String,Object>>>(jobInstanceService.getRunningJobCount());
    }
    /**
     * master启动时加载最近一个月任务实例状态(可以根据实际情况调整)
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/getAllJobStatus",method=RequestMethod.GET)
    public SchedulerResponse<List<JobInstanceStateResponse>> getAllJobStatus(){
        return new SchedulerResponse<List<JobInstanceStateResponse>>(jobInstanceService.getAllJobStatus());
    }
    
    /**
     * 杀任务
     * @param request
     */
    @ResponseBody
    @RequestMapping(value="/killJob",method=RequestMethod.POST)
    public void killJob(@Validated @RequestBody ScheduleRequest request) {
        jobInstanceService.killJob(request);
    }
    
    /**
     * 重跑当前任务实例
     * @param request
     */
    @ResponseBody
    @RequestMapping(value="/rerun",method=RequestMethod.POST)
    public void rerun(@Validated @RequestBody ScheduleRequest request) {
        jobInstanceService.rerun(request);
    }
    
    /**
     * 重跑当前及下游所有任务实例 
     * @param request
     */
    @ResponseBody
    @RequestMapping(value="/rerunAll",method=RequestMethod.POST)
    public void rerunAll(@Validated @RequestBody ScheduleRequest request) {
        jobInstanceService.rerunAll(request);
    }
    
    /**
     * master意外down掉时使用
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/markAsFailed",method=RequestMethod.POST)
    public SchedulerResponse<Void> markAsFailed(@RequestBody List<JobInstanceRequest> list) {
        jobInstanceService.markAsFailed(list);
       return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/updateJobStatus",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateJobStatus(@Validated @RequestBody JobStatusRequest request) {
        jobInstanceService.updateJobStatus(request.getIds(), request.getStatus());
       return SchedulerResponse.success();
    }
}