package com.ylpu.thales.scheduler.controller;

import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.RoleRequest;
import com.ylpu.thales.scheduler.response.RoleResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ResponseBody
    @RequestMapping(value = "/addRole", method = RequestMethod.POST)
    public SchedulerResponse<Void> addRole(@RequestBody RoleRequest request) {
        roleService.insertOrUpdate(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/updateRole", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateConnection(@RequestBody RoleRequest request) {
        roleService.insertOrUpdate(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/deleteRole", method = RequestMethod.POST)
    public SchedulerResponse<Void> deleteUser(@RequestParam("id") Integer id) {
        roleService.deleteRole(id);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/findAllRole", method = RequestMethod.GET)
    public SchedulerResponse<List<RoleResponse>> findAllRole() {
        return new SchedulerResponse<List<RoleResponse>>(roleService.findAllRole());
    }

    @ResponseBody
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public SchedulerResponse<PageInfo<RoleResponse>> paging(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "roleName", required = false) String roleName) {
        return new SchedulerResponse<PageInfo<RoleResponse>>(roleService.findAll(roleName, pageNo, pageSize));
    }
}
