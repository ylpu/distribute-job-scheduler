package com.ylpu.thales.scheduler.executor;
import java.util.HashMap;
import java.util.Map;

import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.executor.command.CommandExecutor;
import com.ylpu.thales.scheduler.executor.hive.HiveExecutor;
import com.ylpu.thales.scheduler.executor.shell.ShellExecutor;
import com.ylpu.thales.scheduler.executor.spark.*;
import com.ylpu.thales.scheduler.executor.http.*;
import com.ylpu.thales.scheduler.executor.python.PythonExecutor;

public class ExecutorManager {
    
    private static Map<JobType,Class<? extends AbstractCommonExecutor>> executors = new HashMap<JobType,Class<? extends AbstractCommonExecutor>>();
    
    static {
        executors.put(JobType.SHELL, ShellExecutor.class);
        executors.put(JobType.HQL, HiveExecutor.class);
        executors.put(JobType.SPARK, SparkExecutor.class);
        executors.put(JobType.COMMAND, CommandExecutor.class);
        executors.put(JobType.HTTP, HttpExecutor.class);
        executors.put(JobType.PYTHON, PythonExecutor.class);
    }
    public static Class<? extends AbstractCommonExecutor> getExecutor(JobType jobType){
        if(jobType == null || executors.get(jobType) == null) {
            return ShellExecutor.class;
        }
        return executors.get(jobType);
    }
}
