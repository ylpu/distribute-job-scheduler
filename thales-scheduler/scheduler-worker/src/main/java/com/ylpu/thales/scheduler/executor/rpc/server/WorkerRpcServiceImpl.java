package com.ylpu.thales.scheduler.executor.rpc.server;

import com.google.common.eventbus.AsyncEventBus;
import com.ylpu.thales.scheduler.alert.EventListener;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceResponseRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.service.GrpcJobServiceGrpc;
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
import com.ylpu.thales.scheduler.worker.WorkerServer;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;

public class WorkerRpcServiceImpl extends GrpcJobServiceGrpc.GrpcJobServiceImplBase {

    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));
    
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
            try {
                transitTaskStatus(request,TaskState.FAIL);
            } catch (Exception e1) {
                LOG.error(e1);
                throw new RuntimeException(e1);
            }
            builder.setTaskState(TaskState.FAIL.getCode())
            .setErrorCode(500)
            .setErrorMsg("failed to run task" + requestRpc.getId());
            // task fail warning
            Event event = new Event();
            setAlertEvent(event, requestRpc.getJob(), request);
            if(StringUtils.isNotBlank(requestRpc.getJob().getAlertUsers())) {
                eventBus.post(event);
            }
        } finally {
            processResponse(responseObserver,builder);
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
        try {
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
            builder.setTaskState(TaskState.KILL.getCode())
            .setErrorCode(200)
            .setErrorMsg("");
            // decrease task number
            jobMetric.decreaseTask();
        } catch (Exception e) {
            LOG.error(e);
            builder.setTaskState(TaskState.RUNNING.getCode())
            .setErrorCode(500)
            .setErrorMsg("failed to kill task" + requestRpc.getId());
            FileUtils.writeFile("failed to kill job " + requestRpc.getId(), requestRpc.getLogPath());
        } finally {
            processResponse(responseObserver,builder);
        }
    }
    
    public void transitTaskStatus(JobInstanceRequest request, TaskState taskState) throws Exception {
        request.setEndTime(new Date());
        request.setElapseTime(DateUtils.getElapseTime(request.getStartTime(), request.getEndTime()));
        request.setTaskState(taskState.getCode());
        JobManager.updateJobInstanceSelective(request);
    }
    //rpc出现网路异常会导致数据库和master端任务状态不一致，这时可以通过页面标记成功或标记失败
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
        Properties prop = Configuration.getConfig();
        int wokerServerPort = Configuration.getInt(prop, "thales.worker.server.port",
                WorkerServer.DEFAULT_WORKER_SERVER_PORT);
        request.setWorker(MetricsUtils.getHostName() + ":" + wokerServerPort);
        request.setJobId(requestRpc.getJob().getId());
        request.setId(requestRpc.getId());
    }
}