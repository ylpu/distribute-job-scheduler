package com.ylpu.kepler.scheduler.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

import com.ylpu.kepler.scheduler.common.dao.BaseDao;
import com.ylpu.kepler.scheduler.entity.JobInstanceState;
import com.ylpu.kepler.scheduler.entity.SchedulerJobInstance;

public interface SchedulerJobInstanceMapper extends BaseDao<SchedulerJobInstance, Integer>{
    
    public List<Map<String,Object>> getRunningJobCount();
    
    public List<JobInstanceState> getAllJobStatus(@Param("startTime")Date startTime,@Param("endTime")Date endTime);
    
    public Integer getInstanceIdByTime(@Param("jobId")Integer jobId,@Param("scheduleTime")String scheduleTime);
        
    public void markAsFailed(SchedulerJobInstance jobInstance);
    
    public void updateJobStatus(@Param("ids")List<Integer> ids, @Param("status")Integer status);
}