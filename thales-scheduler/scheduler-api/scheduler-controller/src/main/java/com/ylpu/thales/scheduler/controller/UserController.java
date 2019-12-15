package com.ylpu.thales.scheduler.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.UserRequest;
import com.ylpu.thales.scheduler.request.UserRoleRequest;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.service.UserService;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")

public class UserController {
	
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public SchedulerResponse<UserResponse> login(@RequestParam("userName") String userName,
                          @RequestParam("userPass") String userPass,
                          HttpSession session) {
    	    UserResponse user = userService.findByUserName(userName,userPass);
        session.setAttribute("user", user);
        return new SchedulerResponse<UserResponse>(user);
    }

    @GetMapping("/logout")
    public SchedulerResponse<Void> logout(HttpSession session) {
        session.removeAttribute("user");
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/addUser",method=RequestMethod.POST)
    public SchedulerResponse<Void> addUser(@Validated @RequestBody UserRequest userRequest) {
    	    userService.insertOrUpdate(userRequest);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/setRoles",method=RequestMethod.POST)
    public SchedulerResponse<Void> setRoles(@Validated @RequestBody UserRoleRequest request) {
    	    userService.setRoles(request);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/deleteUser",method=RequestMethod.POST)
    public SchedulerResponse<Void> deleteUser(@RequestParam("id") Integer id) {
    	    userService.deleteUser(id);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/updateUser",method=RequestMethod.POST)
    public SchedulerResponse<Void> updateUser(@Validated @RequestBody UserRequest userRequest) {
    	    userService.insertOrUpdate(userRequest);
        return SchedulerResponse.success();
    }
    
    @ResponseBody
    @RequestMapping(value="/paging",method=RequestMethod.GET)
    public SchedulerResponse<PageInfo<UserResponse>> paging(@RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "userName", required = false) String userName) {
        return new SchedulerResponse<PageInfo<UserResponse>>(userService.findAll(userName,pageNo,pageSize));
    }

    @GetMapping("/current-user")
    public SchedulerResponse<UserResponse> login(HttpSession session) {
      	UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) {
            return new SchedulerResponse(null);
        };
        return new SchedulerResponse<UserResponse>(user);
    }
}
