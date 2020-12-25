package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.GroupStrategyMapper;
import com.ylpu.thales.scheduler.entity.GroupStrategy;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.response.WorkerGroupResponse;
import com.ylpu.thales.scheduler.service.WorkerGroupService;

@Service
@Transactional
public class WorkerGroupServiceImpl extends BaseServiceImpl<GroupStrategy, Integer> implements WorkerGroupService {
    
    private static final Log LOG = LogFactory.getLog(WorkerGroupServiceImpl.class);

    @Autowired
    private GroupStrategyMapper groupStrategyMapper;

    @Override
    protected BaseDao<GroupStrategy, Integer> getDao() {
        return groupStrategyMapper;
    }

    @Override
    public void addGroupStrategy(WorkerGroupRequest groupStrategryRequest) {

        List<WorkerGroupResponse> groupList = getAllGroups();
        if(StringUtils.isNoneBlank(groupStrategryRequest.getGroupName())) {
            groupList = groupList.stream().filter(request -> request.getGroupName().equalsIgnoreCase(groupStrategryRequest.getGroupName())).collect(Collectors.toList());
        }
        if(groupList != null && groupList.size() >= 1) {
            throw new RuntimeException("group 已经存在");
        }
        
        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        try {
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.ROOT_GROUP, CreateMode.PERSISTENT, null);
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.WORKER_GROUP, CreateMode.PERSISTENT, null);
            CuratorHelper.createNodeIfNotExist(client, GlobalConstants.WORKER_GROUP + "/" + groupStrategryRequest.getGroupName(),
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
    public void updateGroupStrategy(WorkerGroupRequest groupStrategryRequest) {

        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        try {
            CuratorHelper.setData(client, GlobalConstants.WORKER_GROUP + "/" + groupStrategryRequest.getGroupName(), 
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
    public PageInfo<WorkerGroupResponse> findAll(String groupName,int pageNo, int pageSize) {

        Page<WorkerGroupResponse> page = new Page<WorkerGroupResponse>();
        List<WorkerGroupResponse> groupList = getAllGroups();
        if(StringUtils.isNoneBlank(groupName)) {
            groupList = groupList.stream().filter(request -> request.getGroupName().equalsIgnoreCase(groupName)).collect(Collectors.toList());
        }
        int total = groupList.size();
        if (groupList != null && groupList.size() > 0) {
            for(int i = (pageNo-1) * pageSize; i< getEndIndex(pageNo,pageSize,total); i++) {
                page.add(groupList.get(i));
            }
        }
        page.setTotal(groupList.size());
        PageInfo<WorkerGroupResponse> pageInfo = new PageInfo<WorkerGroupResponse>(page);
        return pageInfo;
    }
    
    private int getEndIndex(int pageNo, int pageSize, int total) {
        int end = pageNo * pageSize;
        if(end < total) {
            return end;
        }
        return total;
    }
    
    private List<WorkerGroupResponse> getAllGroups(){
        WorkerGroupResponse groupStrategyResponse = new WorkerGroupResponse();
        List<WorkerGroupResponse> list = new ArrayList<WorkerGroupResponse>();
        Properties prop = Configuration.getConfig(GlobalConstants.CONFIG_FILE);
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> groupList;
        try {
            groupList = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP);
            if(groupList != null && groupList.size() > 0) {
                int id = 1;
                for(String group : groupList) {
                    groupStrategyResponse = new WorkerGroupResponse();
                    byte[] bytes = CuratorHelper.getData(client, GlobalConstants.WORKER_GROUP + "/" + group);
                    groupStrategyResponse.setId(id);
                    groupStrategyResponse.setGroupName(group);
                    groupStrategyResponse.setGroupStrategy(new String(bytes));
                    list.add(groupStrategyResponse);
                    id++;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return list;
    }
}