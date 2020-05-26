package com.ylpu.thales.scheduler.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerUser;
import com.ylpu.thales.scheduler.entity.UserRole;

public interface SchedulerUserMapper extends BaseDao<SchedulerUser, Integer> {

    SchedulerUser findByUserName(@Param("userName") String userName);

    List<SchedulerUser> findAll(@Param("userName") String userName);

    Integer getUserCount(@Param("userName") String userName);

    void deleteUserRole(Integer userId);

    void insertUserRole(UserRole userRole);
}