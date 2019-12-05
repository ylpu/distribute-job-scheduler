package com.ylpu.thales.scheduler.executor.spark;

import java.io.File;
import java.util.List;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class SparkExecutor extends AbstractCommonExecutor{
    
    private static final String SPARK_COMMAND = "spark-sql -e ";
    
    private JobInstanceRequestRpc requestRpc;
            
    public SparkExecutor (JobInstanceRequestRpc requestRpc,JobInstanceRequest request){
        super(requestRpc,request);
        this.requestRpc = requestRpc;
    }
    
    /**
     * 
     */
    @Override
    public void kill() throws Exception{
        String logPath = requestRpc.getLogPath();
        List<String> applicationList = FileUtils.getSparkAppIds(logPath);
        if(applicationList != null && applicationList.size() > 0) {
            for (String application : applicationList) {
                TaskProcessUtils.killYarnApplication(application);
            }  
        }
    }

    @Override
    /**
     * {"fileName" : "","parameters" : ""}
     * @param configFile
     * @return
     */
    public String[] buildCommand(String configFile) throws Exception {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("$SPARK_HOME/bin/" + SPARK_COMMAND);
        
        SparkConfig sparkConfig = JsonUtils.jsonToBean(configFile, SparkConfig.class);
        
        String fileName = String.valueOf(sparkConfig.getFileName());
        if(!FileUtils.exist(new File(fileName)) || !fileName.endsWith(".sql")) {
           throw new RuntimeException("file does not exist or not end with sql" + fileName);
        }
        
        SparkParameters sparkParameters = sparkConfig.getParameters();
        if(sparkParameters == null) {
            throw new RuntimeException("spark任务参数不能为空");
        }
        
        String fileContent = FileUtils.readFile(fileName);
        fileContent = replaceParameters(sparkConfig.getPlaceHolder(),fileContent);
        
        commandBuilder.append(fileContent);
        commandBuilder.append(" ");
        
        commandBuilder.append("--master " + sparkParameters.getMasterUrl());
        commandBuilder.append(" ");
        
        commandBuilder.append("--executor-memory " + sparkParameters.getExecutorMemory());
        commandBuilder.append(" ");
        
        commandBuilder.append("--executor-cores " + sparkParameters.getExecutorCores());
        commandBuilder.append(" ");
        
        commandBuilder.append("--total-executor-cores " + sparkParameters.getTotalExecutorCores());
        
        String[] commands = new String[1];
        commands[0] = commandBuilder.toString();
        return commands;
    }
}