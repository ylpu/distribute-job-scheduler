package com.ylpu.thales.scheduler.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.entity.SchedulerWorker;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.WorkerResponse;

public interface WorkerService extends BaseService<SchedulerWorker,Integer>{

    public void addWorker(WorkerRequest request);
        
    public void insertOrUpdateWorker(WorkerRequest request);
    
    public void updateWorkersStatusByGroup(WorkerGroupRequest param);
    
    public List<WorkerResponse> getWorkersInfoByGroup(WorkerGroupRequest param);
    
    public void updateWorkerByHost(WorkerRequest request);
    
    public PageInfo<WorkerResponse> findAll(String nodeGroup, String worker, int pageNo,int pageSize);
    
}
