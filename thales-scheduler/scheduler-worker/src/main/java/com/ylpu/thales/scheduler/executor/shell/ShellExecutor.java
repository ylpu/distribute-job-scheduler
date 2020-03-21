package com.ylpu.thales.scheduler.executor.shell;

import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.executor.python.PythonConfig;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ShellExecutor extends AbstractCommonExecutor{ 
	
	private static final String SHELL_COMMAND = "/bin/bash";
            
    private JobInstanceRequestRpc requestRpc;
            
    public ShellExecutor (JobInstanceRequestRpc requestRpc,JobInstanceRequest request){
        super(requestRpc,request);
        this.requestRpc = requestRpc;
    }

    /**
     * 
     */
    @Override
    public void kill() throws Exception{
        Integer pid = requestRpc.getPid();
        if(pid != null) {
            TaskProcessUtils.execCommand("./src/script/killProcess.sh", 
                    "/tmp/pid/" + pid + ".out", "/tmp/pid/" + pid + ".error", pid);
        }
    }
    
    /**
     * {"fileName" : "","parameters" : ""}
     * @param configFile
     * @return
     */
    @Override
    public String[] buildCommand(String configFile) throws Exception {
	    List<String> commands = new ArrayList<String>();
        PythonConfig config = JsonUtils.jsonToBean(configFile, PythonConfig.class);
        String fileName = config.getFileName();
        if(!FileUtils.exist(new File(fileName))) {
    	        throw new RuntimeException("file does not exist " + fileName);
        }
        commands.add(SHELL_COMMAND);
        commands.add(fileName);
        Map<String,Object> parameters = config.getParameters();
        if(parameters != null && parameters.size() > 0) {
    	       for(Entry<String, Object> entry : parameters.entrySet()) {
    		      commands.add(String.valueOf(entry.getValue()));
    	       }
        }
        String[] strings = new String[commands.size()];
        commands.toArray(strings);
        return strings;
    }

	@Override
	public void preExecute() throws Exception {
		
	}

	@Override
	public void postExecute() throws Exception {
		
	}
}
