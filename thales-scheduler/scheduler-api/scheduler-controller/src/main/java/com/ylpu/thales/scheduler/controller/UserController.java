package com.ylpu.thales.scheduler.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/current-user")
    public SchedulerResponse<UserResponse> login(HttpSession session) {
      	UserResponse user = (UserResponse) session.getAttribute("user");
        if (user == null) {
            return new SchedulerResponse(500,"用户未登陆");
        };
        return new SchedulerResponse<UserResponse>(user);
    }
}
