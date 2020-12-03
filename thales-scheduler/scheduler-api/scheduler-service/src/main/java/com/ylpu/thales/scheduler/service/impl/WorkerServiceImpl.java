package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.rest.ScheduleManager;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.enums.RoleTypes;
import com.ylpu.thales.scheduler.enums.WorkerStatus;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.UserResponse;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;
import com.ylpu.thales.scheduler.service.WorkerService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class WorkerServiceImpl implements WorkerService {

    @Override
    public PageInfo<WorkerResponse> findAll(String workerGroup, String worker, int pageNo, int pageSize) {

        Page<WorkerResponse> page = new Page<WorkerResponse>();
        List<WorkerRequest> allWorkerList = CuratorHelper.getAllWorkers();
        if(StringUtils.isNoneBlank(workerGroup)) {
            allWorkerList = allWorkerList.stream().filter(request -> request.getWorkerGroup().equalsIgnoreCase(workerGroup)).collect(Collectors.toList());
        }
        if(StringUtils.isNoneBlank(worker)) {
            allWorkerList = allWorkerList.stream().filter(request -> request.getHost().equalsIgnoreCase(worker)).collect(Collectors.toList());
        }
        int total = allWorkerList.size();
        if (allWorkerList != null && allWorkerList.size() > 0) {
            List<WorkerRequest> pageWorkerList = new ArrayList<WorkerRequest>();
            for(int i = (pageNo-1) * pageSize; i< getEndIndex(pageNo,pageSize,total); i++) {
                pageWorkerList.add(allWorkerList.get(i));
            }
            WorkerResponse workerResponse = null;
            for (WorkerRequest wokerRequest : pageWorkerList) {
                workerResponse = new WorkerResponse();
                BeanUtils.copyProperties(wokerRequest, workerResponse);
                workerResponse.setWorkerStatus(WorkerStatus.getWorkerStatus(wokerRequest.getWorkerStatus()));
                workerResponse.setLastHeartbeatTime(
                        DateUtils.getDateAsString(wokerRequest.getLastHeartbeatTime(), DateUtils.DATE_TIME_FORMAT));
                page.add(workerResponse);
            }
        }
        page.setTotal(allWorkerList.size());
        PageInfo<WorkerResponse> pageInfo = new PageInfo<WorkerResponse>(page);
        return pageInfo;
    }
    
    private int getEndIndex(int pageNo, int pageSize, int total) {
        int end = pageNo * pageSize;
        if(end < total) {
            return end;
        }
        return total;
    }

    @Override
    public List<String> getWorkerGroups() {
        return CuratorHelper.getWorkerGroups();
    }

    @Override
    public List<WorkerUsageResponse> getWorkerCpuUsage() {
        List<WorkerUsageResponse> responses = new ArrayList<WorkerUsageResponse>();
        WorkerUsageResponse response = null;
        List<WorkerRequest> list = CuratorHelper.getAllWorkers();
        
        if (list != null && list.size() > 0) {
            for (WorkerRequest workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getHost());
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
        List<WorkerRequest> list = CuratorHelper.getAllWorkers();
        if (list != null && list.size() > 0) {
            for (WorkerRequest workerUsage : list) {
                response = new WorkerUsageResponse();
                response.setWorker(workerUsage.getHost());
                response.setUsage(workerUsage.getMemoryUsage());
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
            throw new ThalesRuntimeException("服务不可用");
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
