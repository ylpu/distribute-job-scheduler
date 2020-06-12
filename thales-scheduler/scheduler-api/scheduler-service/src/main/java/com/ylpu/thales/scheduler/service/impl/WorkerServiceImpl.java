package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.dao.SchedulerWorkerMapper;
import com.ylpu.thales.scheduler.entity.SchedulerWorker;
import com.ylpu.thales.scheduler.entity.WorkerUsage;
import com.ylpu.thales.scheduler.enums.RoleTypes;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;
import com.ylpu.thales.scheduler.service.WorkerService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class WorkerServiceImpl extends BaseServiceImpl<SchedulerWorker, Integer> implements WorkerService {

    @Autowired
    private SchedulerWorkerMapper schedulerWorkerMapper;

    @Override
    protected BaseDao<SchedulerWorker, Integer> getDao() {
        return schedulerWorkerMapper;
    }

    @Override
    public void addWorker(WorkerRequest request) {
        SchedulerWorker worker = new SchedulerWorker();
        if (request != null) {
            BeanUtils.copyProperties(request, worker);
            insertSelective(worker);
        }
    }

    @Override
    public void updateWorkerByHost(WorkerRequest request) {
        SchedulerWorker worker = new SchedulerWorker();
        if (request != null) {
            BeanUtils.copyProperties(request, worker);
            worker.setUpdateTime(new Date());
            schedulerWorkerMapper.updateWorkerByHost(worker);
        }
    }

    @Override
    public void updateWorkersStatusByGroup(WorkerGroupRequest param) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("groupName", param.getGroupName());
        map.put("workers", param.getWorkers());
        map.put("status", param.getStatus().getCode());
        schedulerWorkerMapper.updateWorkersStatusByGroup(map);
    }

    @Override
    public List<WorkerResponse> getWorkersInfoByGroup(WorkerGroupRequest param) {
        List<WorkerResponse> list = new ArrayList<WorkerResponse>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("groupName", param.getGroupName());
        map.put("workers", param.getWorkers());
        List<SchedulerWorker> workers = schedulerWorkerMapper.getWorkersInfoByGroup(map);
        WorkerResponse response = null;
        if (workers != null && workers.size() > 0) {
            for (SchedulerWorker worker : workers) {
                response = new WorkerResponse();
                BeanUtils.copyProperties(worker, response);
                list.add(response);
            }
        }
        return list;
    }

    @Override
    public void insertOrUpdateWorker(WorkerRequest request) {
        WorkerGroupRequest param = new WorkerGroupRequest();
        param.setGroupName(request.getWorkerGroup());
        param.setWorkers(Arrays.asList(request.getHost()));
        List<WorkerResponse> list = getWorkersInfoByGroup(param);
        if (list == null || list.size() == 0) {
            addWorker(request);
        } else {
            updateWorkerByHost(request);
        }
    }

    @Override
    public PageInfo<WorkerResponse> findAll(String workerGroup, String worker, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);

        List<SchedulerWorker> workerList = schedulerWorkerMapper.findAll(workerGroup, worker);
        WorkerResponse workerResponse = null;
        Page<WorkerResponse> page = new Page<WorkerResponse>();
        if (workerList != null && workerList.size() > 0) {
            for (SchedulerWorker schedulerWorker : workerList) {
                workerResponse = new WorkerResponse();
                BeanUtils.copyProperties(schedulerWorker, workerResponse);
                workerResponse.setWorkerStatus(WorkerStatus.getWorkerStatus(schedulerWorker.getWorkerStatus()));
                workerResponse.setLastHeartbeatTime(
                        DateUtils.getDateAsString(schedulerWorker.getLastHeartbeatTime(), DateUtils.DATE_TIME_FORMAT));
                page.add(workerResponse);
            }
        }
        page.setTotal(schedulerWorkerMapper.getAllWorkers(workerGroup, worker));
        PageInfo<WorkerResponse> pageInfo = new PageInfo<WorkerResponse>(page);
        return pageInfo;
    }

    @Override
    public List<String> getWorkerGroups(Integer workerStatus) {
        return schedulerWorkerMapper.getWorkerGroups(workerStatus);
    }

    @Override
    public void updateWorkersStatus(WorkerGroupRequest param) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", param.getStatus().getCode());
        schedulerWorkerMapper.updateWorkersStatus(map);
    }

    @Override
    public List<WorkerUsageResponse> getWorkerCpuUsage() {
        List<WorkerUsageResponse> responses = new ArrayList<WorkerUsageResponse>();
        WorkerUsageResponse response = null;
        List<WorkerUsage> list = schedulerWorkerMapper.getWorkerCpuUsage();
        if (list != null && list.size() > 0) {
            for (WorkerUsage workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getWorkerName());
                response.setUsage(workerUsage.getUsage());
                responses.add(response);
            }
        }
        return responses;
    }
    
    @Override
    public List<WorkerUsageResponse> getWorkerMemoryUsage() {
        List<WorkerUsageResponse> responses = new ArrayList<WorkerUsageResponse>();
        WorkerUsageResponse response = null;
        List<WorkerUsage> list = schedulerWorkerMapper.getWorkerMemoryUsage();
        if (list != null && list.size() > 0) {
            for (WorkerUsage workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getWorkerName());
                response.setUsage(workerUsage.getUsage());
                responses.add(response);
            }
        }
        return responses;
    }
    
    @Override
    public void markDown(WorkerRequest request, Object object) {
        if (!isAdmin(object)) {
            throw new ThalesRuntimeException("非admin不能下线executor");
        }
        if(request.getCurrentWorkerStatus().equalsIgnoreCase(WorkerStatus.REMOVED.toString())) {
            throw new ThalesRuntimeException("节点 " + request.getHost() + " 已经下线");
        }
        String masterUrl = CuratorHelper.getMasterServiceUri();
        if (StringUtils.isNotBlank(masterUrl)) {
            int status = ScheduleManager.markDown(CuratorHelper.getMasterServiceUri(), request);
            // 204-执行成功，但无内容返回
            if (status != HttpStatus.NO_CONTENT.value()) {
                throw new ThalesRuntimeException("failed to down executor " + request.getHost() + ":" + request.getPort());
            }
        } else {
            throw new ThalesRuntimeException("调度服务不可用");
        }
    }
    
    private boolean isAdmin(Object object) {
        if (object == null) {
            throw new ThalesRuntimeException("请重新登陆");
        }
        UserResponse user = (UserResponse) object;
        if (user.getRoleNames().contains(RoleTypes.ROLE_ADMIN.toString())) {
            return true;
        } else {
            return false;
        }
    }
}
