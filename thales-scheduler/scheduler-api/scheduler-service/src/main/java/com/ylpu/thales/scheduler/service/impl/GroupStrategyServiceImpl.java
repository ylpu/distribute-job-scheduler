package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.transaction.Transactional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
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
    
    private static final Log LOG = LogFactory.getLog(GroupStrategyServiceImpl.class);

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
        if(list != null && list.size() >= 1) {
            throw new RuntimeException("group 已经存在");
        }
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.insertSelective(record);
        
        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        try {
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.ROOT_GROUP, CreateMode.PERSISTENT, null);
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.STRATEGY_GROUP, CreateMode.PERSISTENT, null);
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.STRATEGY_GROUP + "/" + groupStrategryRequest.getGroupName(),
                    CreateMode.PERSISTENT, groupStrategryRequest.getGroupStrategy().getBytes());
            
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            if(client != null) {
                CuratorHelper.close(client);
            }
        }
        
    }

    @Override
    public void updateGroupStrategy(GroupStrategyRequest groupStrategryRequest) {
        // TODO Auto-generated method stub
        GroupStrategy record = new GroupStrategy();
        BeanUtils.copyProperties(groupStrategryRequest, record);
        groupStrategyMapper.updateByPrimaryKeySelective(record);

        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        try {
            CuratorHelper.setData(client, GlobalConstants.STRATEGY_GROUP + "/" + groupStrategryRequest.getGroupName(), 
                    groupStrategryRequest.getGroupStrategy().getBytes());
            
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            if(client != null) {
                CuratorHelper.close(client);
            }
        }
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