package com.ylpu.kepler.scheduler.executor.hive;

import java.util.List;

import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.core.utils.FileUtils;
import com.ylpu.kepler.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.kepler.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;

public class HiveExecutor extends AbstractCommonExecutor{
        
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
    public String buildCommand(String configFile) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
