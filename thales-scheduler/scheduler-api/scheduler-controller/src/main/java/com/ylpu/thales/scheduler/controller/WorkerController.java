package com.ylpu.thales.scheduler.controller;

import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.service.WorkerService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/worker")
public class WorkerController {
    
    @Autowired
    private WorkerService workerService;
    
    
    @ResponseBody
    @RequestMapping(value="/addWorker",method=RequestMethod.POST)
    public void addWorker(@RequestBody WorkerRequest request) {
        workerService.addWorker(request);
    } 
    
    @ResponseBody
    @RequestMapping(value="/insertOrUpdateWorker",method=RequestMethod.POST)
    public void insertOrUpdateWorker(@RequestBody WorkerRequest request) {
        workerService.insertOrUpdateWorker(request);
    }  
    
    @ResponseBody
    @RequestMapping(value="/updateWorkersStatusByGroup",method=RequestMethod.POST)
    public void updateWorkersStatusByGroup(@RequestBody WorkerGroupRequest request) {
        workerService.updateWorkersStatusByGroup(request);
    } 
    
    @ResponseBody
    @RequestMapping(value="/getWorkersInfoByGroup",method=RequestMethod.POST)
    public SchedulerResponse<List<WorkerResponse>> getWorkersInfoByGroup(@RequestBody WorkerGroupRequest request) {
        return new SchedulerResponse<List<WorkerResponse>>(workerService.getWorkersInfoByGroup(request));
    } 
}
