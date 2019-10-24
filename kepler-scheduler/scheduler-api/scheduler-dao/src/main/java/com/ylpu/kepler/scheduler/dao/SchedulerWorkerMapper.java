package com.ylpu.kepler.scheduler.dao;

import java.util.List;
import java.util.Map;

import com.ylpu.kepler.scheduler.common.dao.BaseDao;
import com.ylpu.kepler.scheduler.entity.SchedulerWorker;

public interface SchedulerWorkerMapper extends BaseDao<SchedulerWorker, Integer>{
    
    void updateWorkersStatusByGroup(Map<String,Object> map);
    
    List<SchedulerWorker> getWorkersInfoByGroup(Map<String,Object> map);
    
    void updateWorkerByHost(SchedulerWorker worker);
}