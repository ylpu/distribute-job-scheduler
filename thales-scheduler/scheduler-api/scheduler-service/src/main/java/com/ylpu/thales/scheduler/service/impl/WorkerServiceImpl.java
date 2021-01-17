package com.ylpu.thales.scheduler.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.ByteUtils;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.entity.BaseEntity;
import com.ylpu.thales.scheduler.enums.NodeType;
import com.ylpu.thales.scheduler.enums.RoleTypes;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.request.NodeRequest;
import com.ylpu.thales.scheduler.response.MasterUsageResponse;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.response.NodeResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;
import com.ylpu.thales.scheduler.service.WorkerService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class WorkerServiceImpl extends BaseServiceImpl<BaseEntity, Serializable> implements WorkerService {
    
    private static final Log LOG = LogFactory.getLog(WorkerServiceImpl.class);

    @Override
    public PageInfo<NodeResponse> findAll(String workerGroup, String worker, int pageNo, int pageSize) {

        Page<NodeResponse> page = new Page<NodeResponse>();
        List<NodeRequest> allWorkerList = getAllWorkers();
        if(StringUtils.isNoneBlank(workerGroup)) {
            allWorkerList = allWorkerList.stream().filter(request -> request.getWorkerGroup().equalsIgnoreCase(workerGroup)).collect(Collectors.toList());
        }
        if(StringUtils.isNoneBlank(worker)) {
            allWorkerList = allWorkerList.stream().filter(request -> request.getHost().equalsIgnoreCase(worker)).collect(Collectors.toList());
        }
        List<NodeRequest> masters = getMasters();
        if(masters != null & masters.size() > 0) {
        	allWorkerList.addAll(masters);
        }
        
        int total = allWorkerList.size();
        if (allWorkerList != null && allWorkerList.size() > 0) {
            List<NodeRequest> pageWorkerList = new ArrayList<NodeRequest>();
            for(int i = (pageNo-1) * pageSize; i< getEndIndex(pageNo,pageSize,total); i++) {
                pageWorkerList.add(allWorkerList.get(i));
            }
            NodeResponse nodeResponse = null;
            for (NodeRequest nodeRequest : pageWorkerList) {
                nodeResponse = new NodeResponse();
                BeanUtils.copyProperties(nodeRequest, nodeResponse);
                nodeResponse.setHost(nodeRequest.getHost());
                nodeResponse.setWorkerType(NodeType.getNodeType(nodeRequest.getWorkerType()).toString());
                nodeResponse.setWorkerStatus(WorkerStatus.getWorkerStatus(nodeRequest.getWorkerStatus()));
                nodeResponse.setLastHeartbeatTime(
                        DateUtils.getDateAsString(nodeRequest.getLastHeartbeatTime(), DateUtils.DATE_TIME_FORMAT));
                page.add(nodeResponse);
            }
        }
        page.setTotal(allWorkerList.size());
        PageInfo<NodeResponse> pageInfo = new PageInfo<NodeResponse>(page);
        return pageInfo;
    }
    
    private int getEndIndex(int pageNo, int pageSize, int total) {
        int end = pageNo * pageSize;
        if(end < total) {
            return end;
        }
        return total;
    }
    
    
    public List<NodeRequest> getAllWorkers(){
        List<NodeRequest> list = new ArrayList<NodeRequest>();
        Properties prop = Configuration.getConfig();
        String quorum =  Configuration.getString(prop, "thales.zookeeper.quorum", "");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> groupList;
        try {
            groupList = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP);
            if(groupList != null && groupList.size() > 0) {
                for(String group : groupList) {
                    List<String> workerList = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP + "/" + group);
                    if(workerList != null && workerList.size() > 0) {
                        for(String worker : workerList) {
                            byte[] bytes = CuratorHelper.getData(client, GlobalConstants.WORKER_GROUP + "/" + group + "/" + worker);
                            NodeRequest request = (NodeRequest) ByteUtils.byteArrayToObject(bytes);
                            list.add(request);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return list;
    }
    
    public List<NodeRequest> getMasters(){
        List<NodeRequest> list = new ArrayList<NodeRequest>();
        Properties prop = Configuration.getConfig();
        String quorum =  Configuration.getString(prop, "thales.zookeeper.quorum", "");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> masters;
        try {
        	masters = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
            if(masters != null && masters.size() > 0) {
                for(String master : masters) {
                    byte[] bytes = CuratorHelper.getData(client, GlobalConstants.MASTER_GROUP + "/" + master);
                    NodeRequest request = (NodeRequest) ByteUtils.byteArrayToObject(bytes);
                    list.add(request);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return list;
    }

    @Override
    public List<String> getWorkerGroups() {
        
        Properties prop = Configuration.getConfig();
        String quorum =  Configuration.getString(prop, "thales.zookeeper.quorum", "");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> groupList = null;
        try {
            groupList = CuratorHelper.getChildren(client, GlobalConstants.WORKER_GROUP);
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return groupList;
    }

    @Override
    public List<WorkerUsageResponse> getWorkerCpuUsage() {
        List<WorkerUsageResponse> responses = new ArrayList<WorkerUsageResponse>();
        WorkerUsageResponse response = null;
        List<NodeRequest> list = getAllWorkers();
        
        if (list != null && list.size() > 0) {
            for (NodeRequest workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getHost() + ":" + workerUsage.getPort());
                response.setUsage(workerUsage.getCpuUsage());
                responses.add(response);
            }
        }
        return responses;
    }
    
    @Override
    public List<WorkerUsageResponse> getWorkerMemoryUsage() {
        List<WorkerUsageResponse> responses = new ArrayList<WorkerUsageResponse>();
        WorkerUsageResponse response = null;
        List<NodeRequest> list = getAllWorkers();
        if (list != null && list.size() > 0) {
            for (NodeRequest workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getHost() + ":" + workerUsage.getPort());
                response.setUsage(workerUsage.getMemoryUsage());
                responses.add(response);
            }
        }
        return responses;
    }
    
    @Override
    public void markDown(NodeRequest request, Object object) {
        if (!isAdmin(object)) {
            throw new ThalesRuntimeException("none admin can not down executor");
        }
        if(request.getCurrentWorkerStatus().equalsIgnoreCase(WorkerStatus.REMOVED.toString())) {
            throw new ThalesRuntimeException("worker" + request.getHost() + " has down");
        }
        String masterUrl = getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.markDown(getMasterServiceUri(), request);
            // 204-success
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to down executor " + request.getHost() + ":" + request.getPort());
            }
        } else {
            throw new ThalesRuntimeException("schedule service is not available");
        }
    }
    
    private boolean isAdmin(Object object) {
        UserResponse user = (UserResponse) object;
        if (user.getRoleNames().contains(RoleTypes.ROLE_ADMIN.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected BaseDao<BaseEntity, Serializable> getDao() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MasterUsageResponse> getMasterCpuUsage() {
        List<MasterUsageResponse> metricList = new ArrayList<MasterUsageResponse>();
        Properties prop = Configuration.getConfig();
        String quorum =  Configuration.getString(prop, "thales.zookeeper.quorum", "");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> locks = null;
        try {
            locks = CuratorHelper.getChildren(client, GlobalConstants.MASTER_LOCK);
            if(locks != null && locks.size() > 0) {
                List<String> masters = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
                if(masters != null && masters.size() > 0) {
                    MasterUsageResponse cpu = new MasterUsageResponse();
                    byte[] bytes = CuratorHelper.getData(client, GlobalConstants.MASTER_GROUP + "/" + masters.get(0));
                    NodeRequest request = (NodeRequest) ByteUtils.byteArrayToObject(bytes);
                    cpu.setHostName(request.getHost() + ":" + request.getPort());
                    cpu.setValue(request.getCpuUsage());
                    metricList.add(cpu);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return metricList;
    }
    

    @Override
    public List<MasterUsageResponse> getMasterMemoryUsage() {
        List<MasterUsageResponse> metricList = new ArrayList<MasterUsageResponse>();
        Properties prop = Configuration.getConfig();
        String quorum =  Configuration.getString(prop, "thales.zookeeper.quorum", "");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        CuratorFramework client  = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
        List<String> locks = null;
        try {
            locks = CuratorHelper.getChildren(client, GlobalConstants.MASTER_LOCK);
            if(locks != null && locks.size() > 0) {
                List<String> masters = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
                if(masters != null && masters.size() > 0) {
                    MasterUsageResponse memory = new MasterUsageResponse();
                    byte[] bytes = CuratorHelper.getData(client, GlobalConstants.MASTER_GROUP + "/" + masters.get(0));
                    NodeRequest request = (NodeRequest) ByteUtils.byteArrayToObject(bytes);
                    
                    memory.setHostName(request.getHost() + ":" + request.getPort());
                    memory.setValue(request.getMemoryUsage());

                    metricList.add(memory);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }finally {
            CuratorHelper.close(client);
        }
        return metricList;
    }
}
