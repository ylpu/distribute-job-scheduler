package com.ylpu.thales.scheduler.dao;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerWorker;

public interface SchedulerWorkerMapper extends BaseDao<SchedulerWorker, Integer>{
    
    void updateWorkersStatusByGroup(Map<String,Object> map);
    
    List<SchedulerWorker> getWorkersInfoByGroup(Map<String,Object> map);
    
    void updateWorkerByHost(SchedulerWorker worker);
    
    List<SchedulerWorker> findAll(@Param("nodeGroup") String nodeGroup,
            @Param("worker") String worker, Pagination pagination);
}