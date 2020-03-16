package com.ylpu.thales.scheduler.executor.hive;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.ylpu.thales.scheduler.core.config.Configuration;
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
        HiveConfig hiveConfig = JsonUtils.jsonToBean(configFile, HiveConfig.class);
        String fileName = hiveConfig.getFileName();
        if(!FileUtils.exist(new File(fileName)) || !fileName.endsWith(".hql")) {
        	   throw new RuntimeException("请输入合法的hql文件 " + fileName);
        }
        Properties prop = Configuration.getConfig();
        String hive_home = Configuration.getString(prop,"hive.home","");
        if(StringUtils.isBlank(hive_home)) {
        	    sb.append("$HIVE_HOME/bin/" + HIVE_COMMAND);
        }else {
            sb.append(hive_home + "/bin/" + HIVE_COMMAND);
        }
        String fileContent = FileUtils.readFile(fileName);
        fileContent = replaceParameters(hiveConfig.getPlaceHolder(),fileContent);
        sb.append(" -e " + fileContent);
        commands[0] = sb.toString();
        return commands;
    }

	@Override
	public void preExecute() throws Exception {
		
	}

	@Override
	public void postExecute() throws Exception {
		
	}
}
