package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.SchedulerRoleMapper;
import com.ylpu.thales.scheduler.entity.SchedulerRole;
import com.ylpu.thales.scheduler.request.RoleRequest;
import com.ylpu.thales.scheduler.response.RoleResponse;
import com.ylpu.thales.scheduler.service.RoleService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class RoleServiceImpl extends BaseServiceImpl<SchedulerRole, Integer> implements RoleService {

    @Autowired
    private SchedulerRoleMapper schedulerRoleMapper;

    @Override
    protected BaseDao<SchedulerRole, Integer> getDao() {
        return schedulerRoleMapper;
    }

    @Override
    public PageInfo<RoleResponse> findAll(String roleName, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SchedulerRole> roleList = schedulerRoleMapper.findAll(roleName);
        RoleResponse roleResponse = null;
        Page<RoleResponse> page = new Page<RoleResponse>();
        if (roleList != null && roleList.size() > 0) {
            for (SchedulerRole role : roleList) {
                roleResponse = new RoleResponse();
                BeanUtils.copyProperties(role, roleResponse);
                page.add(roleResponse);
            }
        }
        page.setTotal(schedulerRoleMapper.getRoleCount(roleName));
        PageInfo<RoleResponse> pageInfo = new PageInfo<RoleResponse>(page);
        return pageInfo;
    }

    public List<RoleResponse> findAllRole() {
        List<SchedulerRole> roleList = schedulerRoleMapper.findAll(null);
        RoleResponse roleResponse = null;
        List<RoleResponse> list = new ArrayList<RoleResponse>();
        if (roleList != null && roleList.size() > 0) {
            for (SchedulerRole role : roleList) {
                roleResponse = new RoleResponse();
                BeanUtils.copyProperties(role, roleResponse);
                list.add(roleResponse);
            }
        }
        return list;
    }

    @Override
    public void deleteRole(Integer id) {
        schedulerRoleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void insertOrUpdate(RoleRequest request) {
        SchedulerRole role = new SchedulerRole();
        if (request != null && request.getId() != null) {
            BeanUtils.copyProperties(request, role);
            schedulerRoleMapper.updateByPrimaryKeySelective(role);
        } else {
            List<SchedulerRole> list = schedulerRoleMapper.findAll(request.getRoleName());
            if (list != null && list.size() >= 1) {
                throw new ThalesRuntimeException("角色已经存在");
            }
            BeanUtils.copyProperties(request, role);
            schedulerRoleMapper.insertSelective(role);
        }
    }
}
