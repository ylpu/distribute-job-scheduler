package com.ylpu.thales.scheduler.controller;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.response.WorkerGroupResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.WorkerGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/groupStrategy")
public class WorkerGroupController {

    @Autowired
    private WorkerGroupService groupStategyService;

    @ResponseBody
    @RequestMapping(value = "/addGroupStrategy", method = RequestMethod.POST)
    public SchedulerResponse<Void> addGroupStrategy(@RequestBody WorkerGroupRequest request) {
        groupStategyService.addGroupStrategy(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/updateGroupStrategy", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateGroupStrategy(@RequestBody WorkerGroupRequest request) {
        groupStategyService.updateGroupStrategy(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public SchedulerResponse<PageInfo<WorkerGroupResponse>> paging(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "groupName", required = false) String groupName) {
        return new SchedulerResponse<PageInfo<WorkerGroupResponse>>(
                groupStategyService.findAll(groupName, pageNo, pageSize));
    }
}
