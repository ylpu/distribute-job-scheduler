package com.ylpu.thales.scheduler.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;

public interface WorkerService {

    public PageInfo<WorkerResponse> findAll(String workerGroup, String worker, int pageNo, int pageSize);

    public List<String> getWorkerGroups();
    
    public List<WorkerUsageResponse> getWorkerCpuUsage();
    
    public List<WorkerUsageResponse> getWorkerMemoryUsage();
    
    public void markDown(WorkerRequest request, Object object);

}
