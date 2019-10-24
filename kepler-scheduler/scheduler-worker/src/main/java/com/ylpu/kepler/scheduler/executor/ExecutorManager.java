package com.ylpu.kepler.scheduler.executor;
import java.util.HashMap;
import java.util.Map;

import com.ylpu.kepler.scheduler.enums.JobType;
import com.ylpu.kepler.scheduler.executor.hive.HiveExecutor;
import com.ylpu.kepler.scheduler.executor.shell.ShellExecutor;
import com.ylpu.kepler.scheduler.executor.spark.*;

public class ExecutorManager {
    
    private static Map<JobType,Class<? extends AbstractCommonExecutor>> executors = new HashMap<JobType,Class<? extends AbstractCommonExecutor>>();
    
    static {
        executors.put(JobType.SHELL, ShellExecutor.class);
        executors.put(JobType.HQL, HiveExecutor.class);
        executors.put(JobType.SPARK, SparkExecutor.class);
    }
    public static Class<? extends AbstractCommonExecutor> getExecutor(JobType jobType){
        if(jobType == null || executors.get(jobType) == null) {
            return ShellExecutor.class;
        }
        return executors.get(jobType);
    }
}
