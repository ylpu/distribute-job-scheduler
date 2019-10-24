package com.ylpu.kepler.scheduler.rpc.client;

import com.ylpu.kepler.scheduler.core.config.Configuration;
import com.ylpu.kepler.scheduler.core.constants.GlobalConstants;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.kepler.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.kepler.scheduler.enums.GrpcType;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.manager.JobSubmission;
import com.ylpu.kepler.scheduler.manager.TaskCall;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JobCallBackScan {
    
    private static Log LOG = LogFactory.getLog(JobCallBackScan.class);
    
    private static volatile boolean stop = false;
    //key是任务id,value是待执行的任务
    private static Map<String, JobInstanceRequestRpc> callBack = new ConcurrentHashMap<String, JobInstanceRequestRpc>();
    //任务的返回列表
    private static Map<String,JobInstanceResponseRpc> responses = new WeakHashMap<String,JobInstanceResponseRpc>();
    //key是所依赖的任务id,value是任务id
    private static Map<String,String> depends = new ConcurrentHashMap<String,String>();
    
    private static final long CHECK_INTERVAL = 2000;
        
    public static void start(){
        CallBackScan scan = new CallBackScan();
        scan.setDaemon(true);
        scan.start();
    }
    
    public static void addResponse(JobInstanceResponseRpc response) {
        responses.put(response.getResponseId(), response);
    }
    
    public static JobInstanceResponseRpc getResponse(String id) {
        return responses.get(id);
    }
    
    public static void addDepends(String key,String value) {
        depends.put(key,value);
    }
    
    public static void putCallback(JobInstanceRequestRpc callback) {
        callBack.put(callback.getRequestId(), callback);
    }
    
    public static JobInstanceRequestRpc getCallBack(String id) {
        return callBack.get(id);
    }
    
    public static Map<String, JobInstanceRequestRpc> getCallBack() {
        return callBack;
    }

    public static Map<String, JobInstanceResponseRpc> getResponses() {
        return responses;
    }

    public static Map<String, String> getDepends() {
        return depends;
    }

    /**
     * 检查依赖任务是否执行成功，只有依赖的任务id在任务的返回列表里并且依赖任务的状态为成功,当前任务才会被执行
     *
     */
    private static class CallBackScan extends Thread{
        public void run() {
            long interval = Configuration.getLong(Configuration.getConfig(GlobalConstants.CONFIG_FILE), 
                    "kepler.scheduler.job.check.interval", CHECK_INTERVAL);
            while(!stop) {
                for(Entry<String,String> depend : depends.entrySet()) {
                    int ids = 0;
                    String dependIds[] = depend.getKey().split(",");
                    for(String dependId : dependIds) {
                        if(responses.containsKey(dependId)) {
                            JobInstanceResponseRpc response = responses.get(dependId);
                            if(response.getTaskState() == TaskState.SUCCESS.getCode()) {
                                ids++;
                            }
                        }
                    }
                    if(ids == dependIds.length) {
                        String requestId = depends.remove(depend.getKey());
                        JobInstanceRequestRpc request = callBack.remove(requestId);
                        JobSubmission.addWaitingTask(new TaskCall(request,GrpcType.ASYNC));
                    }
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }  
        }
    }
}