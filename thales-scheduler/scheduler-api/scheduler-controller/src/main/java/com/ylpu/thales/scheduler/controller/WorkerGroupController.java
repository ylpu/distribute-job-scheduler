package com.ylpu.thales.scheduler.controller;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.GroupStrategyService;

import java.util.List;

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
    private GroupStrategyService groupStategyService;

    @ResponseBody
    @RequestMapping(value = "/addGroupStrategy", method = RequestMethod.POST)
    public SchedulerResponse<Void> addGroupStrategy(@RequestBody GroupStrategyRequest request) {
        groupStategyService.addGroupStrategy(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/updateGroupStrategy", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateGroupStrategy(@RequestBody GroupStrategyRequest request) {
        groupStategyService.updateGroupStrategy(request);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value = "/getAllGroupStrategy", method = RequestMethod.GET)
    public SchedulerResponse<List<GroupStrategyResponse>> getAllGroupStrategy() {
        return new SchedulerResponse<List<GroupStrategyResponse>>(groupStategyService.getAllGroupStrategy());
    }

    @ResponseBody
    @RequestMapping(value = "/getGroupStrategy", method = RequestMethod.GET)
    public SchedulerResponse<GroupStrategyResponse> getGroupStrategy(@RequestParam("groupName") String groupName) {
        return new SchedulerResponse<GroupStrategyResponse>(groupStategyService.getGroupStrategy(groupName));
    }

    @ResponseBody
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public SchedulerResponse<PageInfo<GroupStrategyResponse>> paging(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "groupName", required = false) String groupName) {
        return new SchedulerResponse<PageInfo<GroupStrategyResponse>>(
                groupStategyService.findAll(groupName, pageNo, pageSize));
    }
}
