package com.ylpu.kepler.scheduler.executor.shell;

import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.core.utils.FileUtils;
import com.ylpu.kepler.scheduler.core.utils.JsonUtils;
import com.ylpu.kepler.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.kepler.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;

import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Map;

public class ShellExecutor extends AbstractCommonExecutor{ 
            
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
    
    /**
     * {"fileName" : "","parameters" : ""}
     * @param configFile
     * @return
     */
    @Override
    public String buildCommand(String configFile) throws Exception {
        Map<String,Object> map = JsonUtils.jsonToMap(configFile);
        String fileName = String.valueOf(map.get("fileName"));
        String parameters = String.valueOf(map.get("parameters"));
        StringBuilder sb = new StringBuilder("sh " + fileName);
        if(StringUtils.isNotBlank(parameters)) {
            sb.append(" ");
            sb.append(parameters);
        }
        return sb.toString();
    }
}
