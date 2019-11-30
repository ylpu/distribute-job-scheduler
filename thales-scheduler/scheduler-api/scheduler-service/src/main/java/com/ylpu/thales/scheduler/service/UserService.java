package com.ylpu.thales.scheduler.service;

import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerUser;
import com.ylpu.thales.scheduler.request.UserRequest;
import com.ylpu.thales.scheduler.response.UserResponse;

/**
 * <pre>
 *     用户业务逻辑接口
 * </pre>
 *
 */
public interface UserService extends BaseService<SchedulerUser,Integer>{

    /**
     * 新增/修改用户
     *
     * @param user user
     * @return Role
     */
    void saveByUser(UserRequest user);

    /**
     * 根据用户名获得用户
     *
     * @param userName 用户名
     * @return 用户
     */
    UserResponse findByUserName(String userName,String password);


    /**
     * 根据用户Id获得用户
     *
     * @param userId 用户名
     * @return 用户
     */
    UserResponse findByUserId(Integer userId);
    
    /**
     * 根据用户名获得用户
     * @param userName
     * @return
     */
    UserResponse findByUserName(String userName);
}
