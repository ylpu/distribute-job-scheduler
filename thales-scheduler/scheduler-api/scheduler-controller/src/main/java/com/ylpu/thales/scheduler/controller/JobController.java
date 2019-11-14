package com.ylpu.thales.scheduler.controller;

import javax.ws.rs.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.baomidou.mybatisplus.plugins.Page;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.JobService;

@Controller
@RequestMapping("/api/job")
public class JobController {

    @Context
    private UriInfo uriInfo;

    @Autowired
    private JobService jobService;
    
    @ResponseBody
    @RequestMapping(value="/addJob",method=RequestMethod.POST)
    public void addJob(@Validated @RequestBody JobRequest job) {
        jobService.addJob(job);
    }
    
    @ResponseBody
    @RequestMapping(value="/updateJob",method=RequestMethod.POST)
    public void updateJob(@Validated @RequestBody JobRequest job) {
        jobService.updateJob(job);
    }
    
    @ResponseBody
    @RequestMapping(value="/getJobById",method=RequestMethod.GET)
    public SchedulerResponse<JobResponse> getJobById(@RequestParam("id") Integer id) {
       return new SchedulerResponse<JobResponse>(jobService.getJobAndRelationById(id));
    }
    
    @ResponseBody
    @RequestMapping(value="/queryTreeById",method=RequestMethod.GET)
    public SchedulerResponse<JobTree> queryTreeById(@RequestParam("id") Integer id) {
       return new SchedulerResponse<JobTree>(jobService.queryTreeById(id));
    }
    
    @ResponseBody
    @RequestMapping(value="/scheduleJob",method=RequestMethod.POST)
    public void scheduleJob(@Validated @RequestBody ScheduleRequest request) {
       jobService.scheduleJob(request);
    }
    
    @ResponseBody
    @RequestMapping(value="/paging",method=RequestMethod.GET)
    public SchedulerResponse<Page<JobResponse>> paging(@RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "jobType", required = false) Integer jobType,
                                         @RequestParam(value = "jobName", required = false) String jobName) {
        Page<JobResponse> pageable = new Page<JobResponse>(pageNo, pageSize);
        Page<JobResponse> page = jobService.findAll(jobType, jobName, pageable);
        return new SchedulerResponse<Page<JobResponse>>(page);
    }
    
    @ResponseBody
    @RequestMapping(value="/rescheduleJob",method=RequestMethod.POST)
    public void rescheduleJob(@Validated @RequestBody ScheduleRequest request) {
       jobService.rescheduleJob(request);
    }
    
    @ResponseBody
    @RequestMapping(value="/downJob",method=RequestMethod.POST)
    public void downJob(@Validated @RequestBody ScheduleRequest request) {
       jobService.downJob(request);
    }
}