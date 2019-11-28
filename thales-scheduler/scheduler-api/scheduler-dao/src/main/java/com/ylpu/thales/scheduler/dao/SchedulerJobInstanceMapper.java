package com.ylpu.thales.scheduler.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.JobInstanceState;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.entity.TaskElapseChart;
import com.ylpu.thales.scheduler.entity.TaskSummary;

public interface SchedulerJobInstanceMapper extends BaseDao<SchedulerJobInstance, Integer>{
    
    public List<Map<String,Object>> getRunningJobCount();
    
    public List<JobInstanceState> getAllJobStatus(@Param("startTime")Date startTime,@Param("endTime")Date endTime);
    
    public Integer getInstanceIdByTime(@Param("jobId")Integer jobId,@Param("scheduleTime")String scheduleTime);
        
    public void markStatus(SchedulerJobInstance jobInstance);
    
    public void updateJobStatus(@Param("ids")List<Integer> ids, @Param("status")Integer status);
    
    List<SchedulerJobInstance> findAll(@Param("taskState") Integer taskState,
            @Param("jobName") String jobName);
    
    public Integer getInstantCount();
    
    public List<TaskSummary> getTaskSummary();
    
    public List<TaskElapseChart> getTaskLineByJobId(Integer id);

}