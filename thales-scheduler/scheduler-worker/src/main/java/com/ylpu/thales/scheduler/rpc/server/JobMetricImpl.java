package com.ylpu.thales.scheduler.rpc.server;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.rpc.entity.WorkerParameter;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.core.zk.CuratorHelper;
import com.ylpu.thales.scheduler.rpc.client.WorkerGrpcClient;
/**
 * 增加或减少任务个数，用于监控
 *
 */
public class JobMetricImpl implements IJobMetric{
    
    private static Log LOG = LogFactory.getLog(JobMetricImpl.class);
    
    public void increaseTask() {
        WorkerGrpcClient client = null;
        try {
            String master = CuratorHelper.getActiveMaster();
            if(StringUtils.isNoneBlank(master)) {
               String[] hostAndPort =  master.split(":");
               WorkerParameter parameter = WorkerParameter.newBuilder().setHostname(
                       MetricsUtils.getHostIpAddress()).build();
               client = new WorkerGrpcClient(hostAndPort[0],NumberUtils.toInt(hostAndPort[1]));
               client.incTask(parameter);
            }  
         }catch(Exception e) {
             LOG.error(e);
         }finally {
             if(client != null) {
                 try {
                     client.shutdown();
                 } catch (InterruptedException e) {
                     LOG.error(e);
                 }
             }
         }
    }
    
    public void decreaseTask() {
        WorkerGrpcClient client = null;
        try {
            String master = CuratorHelper.getActiveMaster();
            if(StringUtils.isNoneBlank(master)) {
               String[] hostAndPort =  master.split(":");
               WorkerParameter parameter = WorkerParameter.newBuilder().setHostname(
                       MetricsUtils.getHostIpAddress()).build();
               client = new WorkerGrpcClient(hostAndPort[0],NumberUtils.toInt(hostAndPort[1]));
               client.decTask(parameter);
             }   
         }catch(Exception e) {
             LOG.error(e);
         }finally {
             if(client != null) {
                 try {
                    client.shutdown();
                 } catch (InterruptedException e) {
                    LOG.error(e);
                 }
             }
         }
    }
}
