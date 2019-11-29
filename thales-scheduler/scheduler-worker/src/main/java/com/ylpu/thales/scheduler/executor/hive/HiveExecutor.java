package com.ylpu.thales.scheduler.executor.hive;

import java.io.File;
import java.util.List;
import java.util.Map;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class HiveExecutor extends AbstractCommonExecutor{
	
	private static final String HIVE_COMMAND = "hive";
        
    private JobInstanceRequestRpc requestRpc;
                
    public HiveExecutor (JobInstanceRequestRpc requestRpc,JobInstanceRequest request){
        super(requestRpc,request);
        this.requestRpc = requestRpc;
    }
    
    /**
     * 从日志中获取相关applicationid
     */
    @Override
    public void kill() throws Exception{
        String logPath = requestRpc.getLogPath();
        List<String> applicationList = FileUtils
                .getApplicationIdFromLog(logPath);
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
    	    String[] commands = new String[1];
        StringBuilder sb = new StringBuilder();
        Map<String,Object> map = JsonUtils.jsonToMap(configFile);
        String fileName = String.valueOf(map.get("fileName"));
        if(!FileUtils.exist(new File(fileName))) {
        	    throw new RuntimeException("file does not exist");
        }
        sb.append("$HIVE_HOME/bin/" + HIVE_COMMAND);
        sb.append(" -f " + fileName);
        commands[0] = sb.toString();
        return commands;
    }
}
