package com.ylpu.kepler.scheduler.service;

import java.util.List;

import com.ylpu.kepler.scheduler.common.service.BaseService;
import com.ylpu.kepler.scheduler.entity.SchedulerWorker;
import com.ylpu.kepler.scheduler.request.WorkerGroupRequest;
import com.ylpu.kepler.scheduler.request.WorkerRequest;
import com.ylpu.kepler.scheduler.response.WorkerResponse;

public interface WorkerService extends BaseService<SchedulerWorker,Integer>{

    public void addWorker(WorkerRequest request);
        
    public void insertOrUpdateWorker(WorkerRequest request);
    
    public void updateWorkersStatusByGroup(WorkerGroupRequest param);
    
    public List<WorkerResponse> getWorkersInfoByGroup(WorkerGroupRequest param);
    
    public void updateWorkerByHost(WorkerRequest request);
    
}
