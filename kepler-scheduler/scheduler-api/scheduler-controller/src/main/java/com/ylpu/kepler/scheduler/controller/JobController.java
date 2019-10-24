package com.ylpu.kepler.scheduler.controller;

import javax.ws.rs.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ylpu.kepler.scheduler.request.JobRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.response.JobTree;
import com.ylpu.kepler.scheduler.response.SchedulerResponse;
import com.ylpu.kepler.scheduler.service.JobService;

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