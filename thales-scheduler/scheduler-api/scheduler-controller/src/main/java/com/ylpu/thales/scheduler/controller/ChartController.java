package com.ylpu.thales.scheduler.controller;

import com.ylpu.thales.scheduler.response.JobTypeSummaryResponse;
import com.ylpu.thales.scheduler.response.MasterUsageResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.TaskSummaryResponse;
import com.ylpu.thales.scheduler.response.WorkerSummaryResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;
import com.ylpu.thales.scheduler.service.JobInstanceService;
import com.ylpu.thales.scheduler.service.WorkerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/chart")
public class ChartController {

    @Autowired
    private WorkerService workerService;
    
    @Autowired
    private JobInstanceService jobInstanceService;

    @ResponseBody
    @RequestMapping(value = "/getWorkerCpuUsage", method = RequestMethod.GET)
    public SchedulerResponse<List<WorkerUsageResponse>> getWorkerCpuUsage() {
        return new SchedulerResponse<List<WorkerUsageResponse>>(workerService.getWorkerCpuUsage());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getWorkerMemoryUsage", method = RequestMethod.GET)
    public SchedulerResponse<List<WorkerUsageResponse>> getWorkerMemoryUsage() {
        return new SchedulerResponse<List<WorkerUsageResponse>>(workerService.getWorkerMemoryUsage());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getTaskSummary", method = RequestMethod.GET)
    public SchedulerResponse<List<TaskSummaryResponse>> getTaskSummary() {
        return new SchedulerResponse<List<TaskSummaryResponse>>(jobInstanceService.getTaskSummary());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getMasterCpuUsage", method = RequestMethod.GET)
    public SchedulerResponse<List<MasterUsageResponse>> getMasterCpuUsage() {
        return new SchedulerResponse<List<MasterUsageResponse>>(workerService.getMasterCpuUsage());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getMasterMemoryUsage", method = RequestMethod.GET)
    public SchedulerResponse<List<MasterUsageResponse>> getMasterMemoryUsage() {
        return new SchedulerResponse<List<MasterUsageResponse>>(workerService.getMasterMemoryUsage());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getWorkerSummary", method = RequestMethod.GET)
    public SchedulerResponse<List<WorkerSummaryResponse>> getWorkerSummary() {
        return new SchedulerResponse<List<WorkerSummaryResponse>>(jobInstanceService.getWorkerSummary());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getJobTypeSummary", method = RequestMethod.GET)
    public SchedulerResponse<List<JobTypeSummaryResponse>> getJobTypeSummary() {
        return new SchedulerResponse<List<JobTypeSummaryResponse>>(jobInstanceService.getJobTypeSummary());
    }
}
