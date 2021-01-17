package com.ylpu.thales.scheduler.controller;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.NodeRequest;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.NodeResponse;
import com.ylpu.thales.scheduler.service.WorkerService;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private WorkerService workerService;

    @ResponseBody
    @RequestMapping(value = "/getWorkerGroups", method = RequestMethod.GET)
    public SchedulerResponse<List<String>> getWorkerGroups(
            @RequestParam(value = "workerStatus", required = false) Integer workerStatus) {
        return new SchedulerResponse<List<String>>(workerService.getWorkerGroups());
    }

    @ResponseBody
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public SchedulerResponse<PageInfo<NodeResponse>> paging(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "workerGroup", required = false) String workerGroup,
            @RequestParam(value = "worker", required = false) String worker) {
        return new SchedulerResponse<PageInfo<NodeResponse>>(
                workerService.findAll(workerGroup, worker, pageNo, pageSize));
    }
     
    @ResponseBody
    @RequestMapping(value = "/markDown", method = RequestMethod.POST)
    public SchedulerResponse<Void> markDown(@Validated @RequestBody NodeRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        workerService.markDown(request, object);
        return SchedulerResponse.success();
    }
}
