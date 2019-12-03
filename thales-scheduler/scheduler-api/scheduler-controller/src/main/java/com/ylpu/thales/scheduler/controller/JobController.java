package com.ylpu.thales.scheduler.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.JobRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobResponse;
import com.ylpu.thales.scheduler.response.JobTree;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.UserResponse;
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
    public SchedulerResponse<Void> addJob(@Validated @RequestBody JobRequest job,HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        jobService.addJob(job,user);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/updateJob",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateJob(@Validated @RequestBody JobRequest job,HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        jobService.updateJob(job,user);
        return SchedulerResponse.success();
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
    public SchedulerResponse<Void> scheduleJob(@Validated @RequestBody ScheduleRequest request) {
       jobService.scheduleJob(request);
       return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/paging",method=RequestMethod.GET)
    public SchedulerResponse<PageInfo<JobResponse>> paging(@RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "jobType", required = false) Integer jobType,
                                         @RequestParam(value = "jobName", required = false) String jobName) {
        return new SchedulerResponse<PageInfo<JobResponse>>(jobService.findAll(jobType, jobName, pageSize, pageNo));
    }
    
    @ResponseBody
    @RequestMapping(value="/rescheduleJob",method=RequestMethod.POST)
    public SchedulerResponse<Void> rescheduleJob(@Validated @RequestBody ScheduleRequest request) {
       jobService.rescheduleJob(request);
       return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/downJob",method=RequestMethod.POST)
    public SchedulerResponse<Void> downJob(@Validated @RequestBody ScheduleRequest request,HttpSession session) {
    	   UserResponse user = (UserResponse) session.getAttribute("user");
       jobService.downJob(request,user);
       return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/getAllJobIds",method=RequestMethod.GET)
    public SchedulerResponse<List<Integer>> getAllJobIds(){
       return new SchedulerResponse<List<Integer>>(jobService.getAllJobIds());
    }
}