package com.ylpu.thales.scheduler.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.request.NodeRequest;
import com.ylpu.thales.scheduler.response.MasterUsageResponse;
import com.ylpu.thales.scheduler.response.NodeResponse;
import com.ylpu.thales.scheduler.response.WorkerUsageResponse;

public interface WorkerService {

    public PageInfo<NodeResponse> findAll(String workerGroup, String worker, int pageNo, int pageSize);

    public List<String> getWorkerGroups();
    
    public List<WorkerUsageResponse> getWorkerCpuUsage();
    
    public List<WorkerUsageResponse> getWorkerMemoryUsage();
    
    public void markDown(NodeRequest request, Object object);
    
    public List<MasterUsageResponse> getMasterCpuUsage();
    
    public List<MasterUsageResponse> getMasterMemoryUsage();

}
