package com.ylpu.thales.scheduler.controller;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.ConnectionRequest;
import com.ylpu.thales.scheduler.response.ConnectionResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/connection")
public class ConnectionController {
    
    @Autowired
    private ConnectionService connectionService;
    
    @ResponseBody
    @RequestMapping(value="/addConnection",method=RequestMethod.POST)
    public SchedulerResponse<Void> addConnection(@RequestBody ConnectionRequest request) {
    	   connectionService.addConnection(request);
    	   return SchedulerResponse.success();
    }  
    
    @ResponseBody
    @RequestMapping(value="/updateConnection",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateConnection(@RequestBody ConnectionRequest request) {
    	    connectionService.updateConnection(request);
    	    return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/getConnection",method=RequestMethod.GET)
    public SchedulerResponse<ConnectionResponse> getConnection(@RequestParam("connectionId") String connectionId) {
    	    return new SchedulerResponse<ConnectionResponse>(connectionService.getConnection(connectionId));
    }
    
    @ResponseBody
    @RequestMapping(value="/paging",method=RequestMethod.GET)
    public SchedulerResponse<PageInfo<ConnectionResponse>> paging(@RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "connectionId", required = false) String connectionId) {
        return new SchedulerResponse<PageInfo<ConnectionResponse>>(connectionService.findAll(connectionId, pageNo,pageSize));
    }
}
