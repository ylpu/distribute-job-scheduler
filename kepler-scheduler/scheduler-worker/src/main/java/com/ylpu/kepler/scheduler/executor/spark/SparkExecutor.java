package com.ylpu.kepler.scheduler.executor.spark;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.core.utils.FileUtils;
import com.ylpu.kepler.scheduler.core.utils.JsonUtils;
import com.ylpu.kepler.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.kepler.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;

public class SparkExecutor extends AbstractCommonExecutor{
    
    private static final String SPARK_COMMAND = "spark-submit";
    
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
     * spark-submit --class org.apache.spark.examples.SparkPi \
       --master yarn \
       --deploy-mode cluster \
       --driver-memory 1g \
       --executor-memory 1g \
       --executor-cores 1 \
       --queue thequeue \
       examples/target/scala-2.11/jars/spark-examples*.jar 10
     * @return
     */
    public String buildCommand(String configFile) throws Exception {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("$SPARK_HOME/bin/" + SPARK_COMMAND);
        
        Map<String,Object> map = JsonUtils.jsonToMap(configFile);
        String parameters = String.valueOf(map.get("parameters"));
        if(StringUtils.isBlank(parameters)) {
            throw new RuntimeException("spark任务参数不能为空");
        }
        SparkParameters sparkParameters = JsonUtils.jsonToBean(parameters, SparkParameters.class);
        
        commandBuilder.append(" --class " + sparkParameters.getMainClass() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --master yarn \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --deploy-mode " + sparkParameters.getDeployMode() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --driver-memory " + sparkParameters.getDriverMemory() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --executor-memory " + sparkParameters.getExecutorMemory() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --executor-cores " + sparkParameters.getExecutorCores() + " \\");
        commandBuilder.append("\n");
        
        commandBuilder.append(" --queue " + sparkParameters.getQueue() + " \\");
        commandBuilder.append("\n");
        
        if(StringUtils.isNotBlank(sparkParameters.getOthers())){
            commandBuilder.append(" " + sparkParameters.getOthers() + " \\");
            commandBuilder.append("\n");
        }
        commandBuilder.append(sparkParameters.getJars());
        if(StringUtils.isNotBlank(sparkParameters.getMainArgs())) {
            commandBuilder.append(" ");
            commandBuilder.append(sparkParameters.getMainArgs());
        }
        return commandBuilder.toString();
    }
}
