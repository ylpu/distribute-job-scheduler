package com.ylpu.thales.scheduler.executor.command;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class CommandExecutor extends AbstractCommonExecutor{
	        
    private JobInstanceRequestRpc requestRpc;
                
    public CommandExecutor (JobInstanceRequestRpc requestRpc,JobInstanceRequest request){
        super(requestRpc,request);
        this.requestRpc = requestRpc;
    }
    
    /**
     * 从日志中获取相关applicationid
     */
    @Override
    public void kill() throws Exception{
        Integer pid = requestRpc.getPid();
        if(pid != null) {
            TaskProcessUtils.execCommand("./src/script/kill.sh", 
                    "/tmp/pid/" + pid + ".out", "/tmp/pid/" + pid + ".error", pid);
        }
        //脚本中如果有hql,杀掉相关的任务
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
     * {"commandLine" : ""}
     * @param configFile
     * @return
     */
    public String[] buildCommand(String configFile) throws Exception {

        Map<String,Object> map = JsonUtils.jsonToMap(configFile);
        String commandLine = String.valueOf(map.get("commandLine"));
        if(StringUtils.isBlank(commandLine)) {
        	   throw new RuntimeException("commandLine can not empty");
        }
	    String[] strs = new String[3];
	    strs[0] = "/bin/bash";
	    strs[1] = "-c";
	    strs[2] = commandLine;
        return strs;
    }

	@Override
	public void preExecute() throws Exception {
		
	}

	@Override
	public void postExecute() throws Exception {
		
	}
}
