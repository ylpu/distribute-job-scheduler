package com.ylpu.thales.scheduler.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.entity.JobInstanceState;
import com.ylpu.thales.scheduler.entity.SchedulerJobInstance;
import com.ylpu.thales.scheduler.entity.TaskElapseChart;
import com.ylpu.thales.scheduler.entity.JobTypeSummary;
import com.ylpu.thales.scheduler.entity.TaskSummary;
import com.ylpu.thales.scheduler.entity.WorkerSummary;

public interface SchedulerJobInstanceMapper extends BaseDao<SchedulerJobInstance, Integer> {

    public List<Map<String, Object>> getRunningJobCount();

    public List<JobInstanceState> getAllJobStatus();

    public SchedulerJobInstance getJobInstanceByTime(@Param("jobId") Integer jobId,
            @Param("scheduleTime") String scheduleTime);

    public void markStatus(@Param("date") Date date);

    public void updateJobStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status,
            @Param("date") Date date);

    List<SchedulerJobInstance> findAll(@Param("taskState") Integer taskState, @Param("jobName") String jobName, @Param("userName") String userName);

    public Integer getInstantCount(@Param("taskState") Integer taskState, @Param("jobName") String jobName, @Param("userName") String userName);

    public List<TaskSummary> getTaskSummary();
    
    public List<WorkerSummary> getWorkerSummary();
    
    public List<JobTypeSummary> getJobTypeSummary();

    public List<TaskElapseChart> getTaskLineByJobId(Integer id);

}