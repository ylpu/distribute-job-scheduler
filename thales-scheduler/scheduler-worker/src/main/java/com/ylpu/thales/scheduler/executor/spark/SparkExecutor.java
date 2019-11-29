package com.ylpu.thales.scheduler.executor.spark;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class SparkExecutor extends AbstractCommonExecutor{
    
    private static final String SPARK_COMMAND = "spark-sql -f ";
    
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
        
        Map<String,Object> map = JsonUtils.jsonToMap(configFile);
        
        String fileName = String.valueOf(map.get("fileName"));
        if(StringUtils.isBlank(fileName) || !fileName.endsWith(".sql")) {
            throw new RuntimeException("请输入sql");
        }
        
        String parameters = String.valueOf(map.get("parameters"));
        if(StringUtils.isBlank(parameters)) {
            throw new RuntimeException("spark任务参数不能为空");
        }
        
        commandBuilder.append(fileName + " \\");
        commandBuilder.append("\n");
        
        SparkParameters sparkParameters = JsonUtils.jsonToBean(parameters, SparkParameters.class);
        
        commandBuilder.append("--master " + sparkParameters.getMasterUrl() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append("--executor-memory " + sparkParameters.getExecutorMemory() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append("--executor-cores " + sparkParameters.getExecutorCores() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append("--total-executor-cores " + sparkParameters.getTotalExecutorCores());
        String[] commands = new String[1];
        commands[0] = commandBuilder.toString();
        return commands;
    }
}
