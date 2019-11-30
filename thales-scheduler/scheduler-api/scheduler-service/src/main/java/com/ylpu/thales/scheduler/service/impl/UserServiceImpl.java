package com.ylpu.thales.scheduler.service.impl;

import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.SchedulerUserMapper;
import com.ylpu.thales.scheduler.entity.SchedulerUser;
import com.ylpu.thales.scheduler.request.UserRequest;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.service.UserService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;


@Service
public class UserServiceImpl extends BaseServiceImpl<SchedulerUser,Integer> implements UserService {

    @Autowired(required = false)
    private SchedulerUserMapper userMapper;

    @Override
    public void saveByUser(UserRequest userRequest) {
    	    SchedulerUser user = new SchedulerUser();
        if (userRequest != null && userRequest.getId() != null) {
        	    BeanUtils.copyProperties(userRequest, user);
            userMapper.updateByPrimaryKeySelective(user);
        } else {
            userMapper.insert(user);
        }
    }
    
    @Override
    public UserResponse findByUserName(String userName) {
        SchedulerUser user = userMapper.findByUserName(userName);
        if(user != null) {
            UserResponse response = new UserResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        }
        return null;
    }

    @Override
    public UserResponse findByUserName(String userName,String password) {
    	    SchedulerUser user = userMapper.findByUserName(userName);
        if (user == null) {
        	   throw new ThalesRuntimeException("用户名不存在！");
        }
        if (!Objects.equals(user.getPassword(), password)) {
     	   throw new ThalesRuntimeException("密码不正确!");
        }
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public UserResponse findByUserId(Integer userId) {
        SchedulerUser user = userMapper.selectByPrimaryKey(userId);
        if(user != null) {
            UserResponse response = new UserResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        }
        return null;
    }

	@Override
	protected BaseDao<SchedulerUser, Integer> getDao() {
		return userMapper;
	}
}
