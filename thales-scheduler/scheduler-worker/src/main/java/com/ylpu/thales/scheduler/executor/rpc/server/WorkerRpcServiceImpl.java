package com.ylpu.thales.scheduler.executor.rpc.server;

import com.google.common.eventbus.AsyncEventBus;
import com.ylpu.thales.scheduler.alert.EventListener;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;
import com.ylpu.thales.scheduler.enums.JobType;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.executor.ExecutorManager;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.worker.WorkerServer;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class WorkerRpcServiceImpl extends GrpcJobServiceGrpc.GrpcJobServiceImplBase {

    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));
    private static Map<String,TaskState> statusMap = new HashMap<String,TaskState>();
    
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
        
        try {
            // increase task number
            jobMetric.increaseTask();
            // run task
            AbstractCommonExecutor executor = getExecutor(requestRpc, request);
            LOG.info("start to pre execute task " + requestRpc.getId());
            executor.preExecute();
            LOG.info("start execute task " + requestRpc.getId());
            executor.execute();
            LOG.info("start to post execute task " + requestRpc.getId());
            executor.postExecute();
            transitTaskStatus(request,TaskState.SUCCESS);
            // set task response status
            builder.setTaskState(TaskState.SUCCESS.getCode())
            .setErrorCode(200)
            .setErrorMsg("");
            // decrease task number
            jobMetric.decreaseTask();
        } catch (Exception e) {
            LOG.error(e);
            if(statusMap.get(requestRpc.getRequestId()) != TaskState.KILL) {
                try {
                    transitTaskStatus(request,TaskState.FAIL);
                } catch (Exception e1) {
                    LOG.error("fail to transit task " + request.getId() + " to fail with exception " + e1.getMessage());
                }
                builder.setTaskState(TaskState.FAIL.getCode())
                .setErrorCode(500)
                .setErrorMsg("failed to run task" + requestRpc.getId());
                // task fail warning
                Event event = new Event();
                setAlertEvent(event, requestRpc.getJob(), request);
                if(StringUtils.isNotBlank(requestRpc.getJob().getAlertUsers())) {
                    try {
                        eventBus.post(event);
                    }catch(Exception e1) {
                        LOG.error("failed to send email for task " + requestRpc.getId() + " with exception " + e1.getMessage());
                    }
                }
            }

        } finally {
            if(statusMap.get(requestRpc.getRequestId()) != TaskState.KILL) {
                processResponse(responseObserver,builder);
            }
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

//    sync kill
    public void kill(JobInstanceRequestRpc requestRpc, StreamObserver<JobInstanceResponseRpc> responseObserver) {
        statusMap.put(requestRpc.getRequestId(), TaskState.KILL);
        JobInstanceResponseRpc.Builder builder = JobInstanceResponseRpc.newBuilder();
        builder.setResponseId(requestRpc.getRequestId());
        JobInstanceRequest request = new JobInstanceRequest();
        initJobInstanceRequest(requestRpc, request);
        LOG.info("start to kill task " + requestRpc.getId());
        try {
            AbstractCommonExecutor executor = getExecutor(requestRpc, request);
            executor.kill();
            transitTaskStatus(request,TaskState.KILL);
            builder.setTaskState(TaskState.KILL.getCode())
            .setErrorCode(200)
            .setErrorMsg("");
            // decrease task number
            jobMetric.decreaseTask();
        } catch (Exception e) {
            LOG.error("fail to transit task " + requestRpc.getId() + " to kill with exception "+ e);
            builder.setTaskState(TaskState.RUNNING.getCode())
            .setErrorCode(500)
            .setErrorMsg("");
        } finally {
            statusMap.remove(requestRpc.getRequestId());
            processResponse(responseObserver,builder);
        }
    }
    
    public void transitTaskStatus(JobInstanceRequest request, TaskState taskState) throws Exception {
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        request.setTaskState(taskState.getCode());
        JobManager.transitTaskStatus(request);
    }

    private void processResponse(StreamObserver<JobInstanceResponseRpc> responseObserver,
            JobInstanceResponseRpc.Builder builder) {
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
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
        request.setWorker(MetricsUtils.getHostName() + ":" + WorkerServer.workerServerPort);
        request.setJobId(requestRpc.getJob().getId());
        request.setId(requestRpc.getId());
    }
}