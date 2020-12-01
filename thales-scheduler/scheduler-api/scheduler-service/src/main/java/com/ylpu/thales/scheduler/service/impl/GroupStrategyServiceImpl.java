package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.rest.GroupStrategyManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.GroupStrategyMapper;
import com.ylpu.thales.scheduler.entity.GroupStrategy;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import com.ylpu.thales.scheduler.service.GroupStrategyService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class GroupStrategyServiceImpl extends BaseServiceImpl<GroupStrategy, Integer> implements GroupStrategyService {

    @Autowired
    private GroupStrategyMapper groupStrategyMapper;

    @Override
    protected BaseDao<GroupStrategy, Integer> getDao() {
        return groupStrategyMapper;
    }

    @Override
    public void addGroupStrategy(GroupStrategyRequest groupStrategryRequest) {
        // TODO Auto-generated method stub
        List<GroupStrategy> list = groupStrategyMapper.getGroupStrategy(groupStrategryRequest.getGroupName());
        if(list != null && list.size() > 1) {
            throw new RuntimeException("group 已经存在");
        }
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.insertSelective(record);
//        
//        String masterUrl = CuratorHelper.getMasterServiceUri();
//        if (StringUtils.isNotBlank(masterUrl)) {
//            int status = GroupStrategyManager.addGroupStrategy(masterUrl, groupStrategryRequest);
//            if (status != HttpStatus.NO_CONTENT.value()) {
//                 throw new ThalesRuntimeException("failed to add group strategy job " + groupStrategryRequest.getGroupName());
//            }
//        } else {
//            throw new ThalesRuntimeException("调度服务不可用");
//        }
    }

    @Override
    public void updateGroupStrategy(GroupStrategyRequest groupStrategryRequest) {
        // TODO Auto-generated method stub
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.updateByPrimaryKeySelective(record);
        
//        String masterUrl = CuratorHelper.getMasterServiceUri();
//        if (StringUtils.isNotBlank(masterUrl)) {
//            int status = GroupStrategyManager.updateGroupStrategy(masterUrl, groupStrategryRequest);
//            if (status != HttpStatus.NO_CONTENT.value()) {
//                 throw new ThalesRuntimeException("failed to update group strategy job " + groupStrategryRequest.getGroupName());
//            }
//        } else {
//            throw new ThalesRuntimeException("调度服务不可用");
//        }
    }

    @Override
    public GroupStrategyResponse getGroupStrategy(String groupName) {
        // TODO Auto-generated method stub
        GroupStrategyResponse response = new GroupStrategyResponse();
        List<GroupStrategy> groupStrategies = groupStrategyMapper.getGroupStrategy(groupName);
        if(groupStrategies == null || groupStrategies.size() == 0) {
            return response;
        }
        BeanUtils.copyProperties(groupStrategies.get(0), response);
        return response;
    }

    @Override
    public PageInfo<GroupStrategyResponse> findAll(String groupName,int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<GroupStrategy> groupList = groupStrategyMapper.findAll(groupName);
        GroupStrategyResponse groupStrategyResponse = null;
        Page<GroupStrategyResponse> page = new Page<GroupStrategyResponse>();
        if (groupList != null && groupList.size() > 0) {
            for (GroupStrategy group : groupList) {
                groupStrategyResponse = new GroupStrategyResponse();
                BeanUtils.copyProperties(group, groupStrategyResponse);
                page.add(groupStrategyResponse);
            }
        }
        page.setTotal(groupStrategyMapper.getGroupCount(groupName));
        PageInfo<GroupStrategyResponse> pageInfo = new PageInfo<GroupStrategyResponse>(page);
        return pageInfo;
    }

    @Override
    public List<GroupStrategyResponse> getAllGroupStrategy() {
        List<GroupStrategy> groupList = groupStrategyMapper.findAll("");
        GroupStrategyResponse groupStrategyResponse = null;
        List<GroupStrategyResponse> list = new ArrayList<GroupStrategyResponse>();
        if (groupList != null && groupList.size() > 0) {
            for (GroupStrategy group : groupList) {
                groupStrategyResponse = new GroupStrategyResponse();
                BeanUtils.copyProperties(group, groupStrategyResponse);
                list.add(groupStrategyResponse);
            }
        }
        return list;
    }
}