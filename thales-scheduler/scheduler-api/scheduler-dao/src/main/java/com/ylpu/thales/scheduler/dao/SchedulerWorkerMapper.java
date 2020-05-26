package com.ylpu.thales.scheduler.dao;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.SchedulerWorker;

public interface SchedulerWorkerMapper extends BaseDao<SchedulerWorker, Integer> {

    void updateWorkersStatusByGroup(Map<String, Object> map);

    void updateWorkersStatus(Map<String, Object> map);

    List<SchedulerWorker> getWorkersInfoByGroup(Map<String, Object> map);

    Integer getAllWorkers(@Param("workerGroup") String workerGroup, @Param("worker") String worker);

    void updateWorkerByHost(SchedulerWorker worker);

    List<SchedulerWorker> findAll(@Param("workerGroup") String workerGroup, @Param("worker") String worker);

    List<String> getWorkerGroups(@Param("workerStatus") Integer workerStatus);
}