package com.ylpu.thales.scheduler.service.impl;

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
import com.ylpu.thales.scheduler.dao.GroupStrategyMapper;
import com.ylpu.thales.scheduler.entity.GroupStrategy;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import com.ylpu.thales.scheduler.service.GroupStrategyService;

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
        if(groupStrategyMapper.getGroupStrategy(groupStrategryRequest.getGroupName()) != null) {
            throw new RuntimeException("group 已经存在");
        }
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.insertSelective(record);
    }

    @Override
    public void updateGroupStrategy(GroupStrategyRequest groupStrategryRequest) {
        // TODO Auto-generated method stub
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.updateByPrimaryKeySelective(record);
        
    }

    @Override
    public GroupStrategyResponse getGroupStrategy(String groupName) {
        // TODO Auto-generated method stub
        GroupStrategyResponse response = new GroupStrategyResponse();
        GroupStrategy groupStrategy = groupStrategyMapper.getGroupStrategy(groupName);
        BeanUtils.copyProperties(groupStrategy, response);
        return response;
    }

    @Override
    public PageInfo<GroupStrategyResponse> findAll(String groupName,int pageSize, int pageNo) {
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

}