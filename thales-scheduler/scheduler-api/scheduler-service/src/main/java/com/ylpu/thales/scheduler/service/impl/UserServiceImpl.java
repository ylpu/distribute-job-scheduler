package com.ylpu.thales.scheduler.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.StringUtils;
import com.ylpu.thales.scheduler.dao.SchedulerUserMapper;
import com.ylpu.thales.scheduler.entity.SchedulerUser;
import com.ylpu.thales.scheduler.entity.UserRole;
import com.ylpu.thales.scheduler.request.UserRequest;
import com.ylpu.thales.scheduler.request.UserRoleRequest;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.service.UserService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;


@Service
public class UserServiceImpl extends BaseServiceImpl<SchedulerUser,Integer> implements UserService {

    @Autowired(required = false)
    private SchedulerUserMapper userMapper;

    @Override
    public void insertOrUpdate(UserRequest userRequest) {
    	    SchedulerUser user = new SchedulerUser();
    	    if(!userRequest.getPassword().equals(userRequest.getConfirmPass())){
    	      	throw new ThalesRuntimeException("密码与确认密码不一致"); 
    	    }
        if (userRequest.getId() != null) {
        	    BeanUtils.copyProperties(userRequest, user);
            userMapper.updateByPrimaryKeySelective(user);
        } else {
        	    UserResponse reponse = findByUserName(userRequest.getUserName());
        	    if(reponse != null) {
        	    	   throw new ThalesRuntimeException("用户名已经存在");
        	    }else {
               BeanUtils.copyProperties(userRequest, user);
               userMapper.insertSelective(user);
        	    }

        }
    }
    
    @Override
    public UserResponse findByUserName(String userName) {
        SchedulerUser user = userMapper.findByUserName(userName);
        if(user != null) {
            UserResponse response = new UserResponse();
            BeanUtils.copyProperties(user, response);
			setRole(user,response);
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
        setRole(user,response);
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
	public PageInfo<UserResponse> findAll(String userName,int pageNo,int pageSize) {
		PageHelper.startPage(pageNo,pageSize);
		List<SchedulerUser> userList = userMapper.findAll(userName);
		UserResponse userResponse = null;
		Page<UserResponse> page = new Page<UserResponse>();
		if(userList != null && userList.size() > 0) {
			for(SchedulerUser user : userList) {
				userResponse = new UserResponse();
				BeanUtils.copyProperties(user, userResponse);
				setRole(user,userResponse);
				page.add(userResponse);
			}
		}
		page.setTotal(userMapper.getUserCount(userName));
		PageInfo<UserResponse> pageInfo = new PageInfo<UserResponse>(page);
        return pageInfo;
	}
	
	private void setRole(SchedulerUser user,UserResponse userResponse) {
        if(user.getRelations() != null && user.getRelations().size() > 0) {
         	userResponse.setRoleIds(user.getRelations().stream().map(p -> p.getRoleId()).collect(Collectors.toList()));
         	userResponse.setRoleNames(StringUtils.convertListAsString(
            		user.getRelations().stream().map(p -> p.getRoleName()).collect(Collectors.toList())));
        }
	}

	@Override
	protected BaseDao<SchedulerUser, Integer> getDao() {
		return userMapper;
	}

	@Override
	public void deleteUser(Integer id) {
		userMapper.deleteByPrimaryKey(id);
	}

	@Override
	public void setRoles(UserRoleRequest request) {
		if(request != null) {
			if(request.getId() != null) {
				userMapper.deleteUserRole(request.getId());
				UserRole userRole = null;
				if(request.getRoleIds() != null && request.getRoleIds().size() > 0) {
					for(Integer roleId : request.getRoleIds()) {
						userRole = new UserRole();
						userRole.setUserId(request.getId());
						userRole.setRoleId(roleId);
						userRole.setCreateTime(new Date());
						userRole.setUpdateTime(new Date());
						userMapper.insertUserRole(userRole);
					}
				}
			}
		}
	}
}
