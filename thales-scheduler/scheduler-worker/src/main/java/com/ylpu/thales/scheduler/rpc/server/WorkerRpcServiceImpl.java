package com.ylpu.thales.scheduler.rpc.server;

import com.google.common.eventbus.AsyncEventBus;
import com.google.protobuf.ByteString;
import com.ylpu.thales.scheduler.WorkerServer;
import com.ylpu.thales.scheduler.alert.EventListener;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.curator.CuratorHelper;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.core.utils.ByteUtils;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.executor.ExecutorManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.rpc.client.WorkerGrpcClient;

import io.grpc.stub.StreamObserver;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;

public class WorkerRpcServiceImpl extends GrpcJobServiceGrpc.GrpcJobServiceImplBase {

    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));
    
    private static long MASTER_SWITCH_WAIT_INTERVAL = 10000L;

    static {
        eventBus.register(new EventListener());
    }

    private static Log LOG = LogFactory.getLog(WorkerRpcServiceImpl.class);
    private IJobMetric jobMetric;

    public IJobMetric getJobMetric() {
        return jobMetric;
    }

    public void setJobMetric(IJobMetric jobMetric) {
        this.jobMetric = jobMetric;
    }

    public void submit(JobInstanceRequestRpc requestRpc, StreamObserver<JobInstanceResponseRpc> responseObserver) {

        JobInstanceResponseRpc.Builder builder = JobInstanceResponseRpc.newBuilder();
        builder.setResponseId(requestRpc.getRequestId());
        JobInstanceRequest request = new JobInstanceRequest();
        initJobInstanceRequest(requestRpc, request);
        
        JobStatusRequestRpc jobStatusRequestRpc = null;
        String oldMaster = "";    
        try {
            oldMaster = CuratorHelper.getActiveMaster();
            // increase task number
            jobMetric.increaseTask();
            // run task
            LOG.info("start to execute task " + requestRpc.getId());
            AbstractCommonExecutor executor = getExecutor(requestRpc, request);
            executor.preExecute();
            executor.execute();
            executor.postExecute();
            transitTaskStatus(request,TaskState.SUCCESS);
            // set task response status
            builder.setTaskState(TaskState.SUCCESS.getCode()).setErrorCode(200).setErrorMsg("");
            // job status when master failover
            jobStatusRequestRpc = buildJobStatusRequestRpc(requestRpc, TaskState.SUCCESS,
                    request);
            // decrease task number
            jobMetric.decreaseTask();
        } catch (Exception e) {
            LOG.error(e);
            try {
                transitTaskStatus(request,TaskState.FAIL);
            } catch (Exception e1) {
                LOG.error(e1);
                throw new RuntimeException(e1);
            }
            builder.setTaskState(TaskState.FAIL.getCode()).setErrorCode(500).setErrorMsg("failed to run task" + requestRpc.getId());
            jobStatusRequestRpc = buildJobStatusRequestRpc(requestRpc, TaskState.FAIL,
                    request);
            // task fail warning
            Event event = new Event();
            setAlertEvent(event, requestRpc.getJob(), request);
            if(StringUtils.isNotBlank(requestRpc.getJob().getAlertUsers())) {
                eventBus.post(event);
            }
        } finally {
            processResponse(responseObserver,builder,jobStatusRequestRpc,oldMaster);
        }
    }
    
    private void setAlertEvent(Event event, JobRequestRpc requestRpc, JobInstanceRequest request) {
        event.setTaskId(requestRpc.getId());
        event.setAlertType(AlertType.getAlertType(requestRpc.getAlertTypes()));
        event.setAlertUsers(requestRpc.getAlertUsers());
        event.setLogUrl(request.getLogUrl());
        event.setHostName(request.getWorker());
        event.setEventType(EventType.FAIL);
    }

    //sync kill
    public void kill(JobInstanceRequestRpc requestRpc, StreamObserver<JobInstanceResponseRpc> responseObserver) {
        JobInstanceResponseRpc.Builder builder = JobInstanceResponseRpc.newBuilder();
        builder.setResponseId(requestRpc.getRequestId());
        JobInstanceRequest request = new JobInstanceRequest();
        initJobInstanceRequest(requestRpc, request);
        LOG.info("start to kill task " + requestRpc.getId());
        JobStatusRequestRpc jobStatusRequestRpc = null;
        String oldMaster = ""; 
        try {
            oldMaster = CuratorHelper.getActiveMaster();
            AbstractCommonExecutor executor = getExecutor(requestRpc, request);
            executor.kill();
            // wait for task to fail
            int i = 0;
            while (i < 3) {
                JobInstanceResponse instanceResponse = JobManager.getJobInstanceById(requestRpc.getId());
                if (instanceResponse.getTaskState() == TaskState.FAIL) {
                    break;
                }
                Thread.sleep(1000);
                i++;
            }
            transitTaskStatus(request,TaskState.KILL);
            FileUtils.writeFile("sucessful kill job " + requestRpc.getId(), requestRpc.getLogPath());
            builder.setTaskState(TaskState.KILL.getCode()).setErrorCode(200).setErrorMsg("");
            // job status when master failover
            jobStatusRequestRpc = buildJobStatusRequestRpc(requestRpc, TaskState.KILL,
                    request);
            // decrease task number
            jobMetric.decreaseTask();
        } catch (Exception e) {
            LOG.error(e);
            builder.setTaskState(TaskState.RUNNING.getCode()).setErrorCode(500).setErrorMsg("failed to kill task" + requestRpc.getId());
            jobStatusRequestRpc = buildJobStatusRequestRpc(requestRpc, TaskState.RUNNING,
                    request);
            FileUtils.writeFile("failed to kill job " + requestRpc.getId(), requestRpc.getLogPath());
        } finally {
            processResponse(responseObserver,builder,jobStatusRequestRpc,oldMaster);
        }
    }
    
    public void transitTaskStatus(JobInstanceRequest request, TaskState taskState) throws Exception {
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        request.setTaskState(taskState.getCode());
        JobManager.updateJobInstanceSelective(request);
    }
    
    public JobStatusRequestRpc buildJobStatusRequestRpc(JobInstanceRequestRpc requestRpc, TaskState taskState, JobInstanceRequest request) {
        JobStatusRequestRpc.Builder builder = JobStatusRequestRpc.newBuilder();
        builder.setRequestId(requestRpc.getRequestId());
        builder.setTaskState(taskState.getCode());
        builder.setData(ByteString.copyFrom(ByteUtils.objectToByteArray(request)));
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(DateUtils.getDatetime(requestRpc.getStartTime()), request.getEndTime()));
        return builder.build();
    }
    
    private void processResponse(StreamObserver<JobInstanceResponseRpc> responseObserver,
            JobInstanceResponseRpc.Builder builder,JobStatusRequestRpc jobStatusRequestRpc,String oldMaster) {
        String currentMaster = "";
        try {
            currentMaster = CuratorHelper.getActiveMaster();
        } catch (Exception e) {
            LOG.error(e);
        }
        //master does not failover
        if(currentMaster.equalsIgnoreCase(oldMaster)) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
        //master failover，rebuild rpc and update job status
        else {
            updateJobStatus(jobStatusRequestRpc,currentMaster);
        }
    }
    
    public void updateJobStatus(JobStatusRequestRpc request,String currentMaster) {
        //master还在切换中，等待master切换完成
        if(StringUtils.isBlank(currentMaster)) {
            LOG.warn("waiting for node " + currentMaster + " become new master");
            tryToUpdateJobStatus(request);
        }else {
            LOG.warn("update task status to new master " +  currentMaster);
            tryToUpdateJobStatus(request,currentMaster);
        }
    }
    
    private void tryToUpdateJobStatus(JobStatusRequestRpc request) {
        WorkerGrpcClient client = null;
        while(true) {
            try {
                String currentMaster = CuratorHelper.getActiveMaster(); 
                if(StringUtils.isNotBlank(currentMaster)) {
                    String[] hostAndPort = currentMaster.split(":");
                    client = new WorkerGrpcClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
                    client.updateJobStatus(request);
                    break;
                }
            }catch (Exception e) {
                LOG.error(e);
            }finally {
                if (client != null) {
                    try {
                        client.shutdown();
                    } catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
            }
            try {
                Thread.sleep(MASTER_SWITCH_WAIT_INTERVAL);
            } catch (InterruptedException e1) {
                LOG.error(e1);
            }
        }
    }
    
    private void tryToUpdateJobStatus(JobStatusRequestRpc request, String currentMaster) {
        WorkerGrpcClient client = null;
        try {
            String[] hostAndPort = currentMaster.split(":");
            client = new WorkerGrpcClient(hostAndPort[0], NumberUtils.toInt(hostAndPort[1]));
            client.updateJobStatus(request);
        }catch (Exception e) {
            LOG.error(e);
        }finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }
    }

    private AbstractCommonExecutor getExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request)
            throws Exception {
        Class<?> cls = ExecutorManager.getExecutor(JobType.getJobType(requestRpc.getJob().getJobType()));
        AbstractCommonExecutor executor = (AbstractCommonExecutor) cls
                .getConstructor(JobInstanceRequestRpc.class, JobInstanceRequest.class).newInstance(requestRpc, request);
        return executor;
    }

    private void initJobInstanceRequest(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        request.setApplicationid(requestRpc.getApplicationid());
        request.setCreatorEmail(requestRpc.getCreatorEmail());
        request.setCreatorName(requestRpc.getCreatorName());
        request.setScheduleTime(DateUtils.getDatetime(requestRpc.getScheduleTime()));
        request.setStartTime(DateUtils.getDatetime(requestRpc.getStartTime()));
        request.setElapseTime(requestRpc.getElapseTime());
        request.setLogPath(requestRpc.getLogPath());
        request.setLogUrl(requestRpc.getLogUrl());
        request.setPid(requestRpc.getPid());
        request.setRetryTimes(requestRpc.getRetryTimes());
        request.setTaskState(requestRpc.getTaskState());
        Properties prop = Configuration.getConfig();
        int wokerServerPort = Configuration.getInt(prop, "thales.worker.server.port",
                WorkerServer.DEFAULT_WORKER_SERVER_PORT);
        request.setWorker(MetricsUtils.getHostName() + ":" + wokerServerPort);
        request.setJobId(requestRpc.getJob().getId());
        request.setId(requestRpc.getId());
    }
}