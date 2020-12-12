package com.ylpu.thales.scheduler.core.rest;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.response.*;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobManager {

    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param request
     * @return
     */
    public static int transitTaskStatus(JobInstanceRequest request) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        @SuppressWarnings("rawtypes")
        ResponseEntity<SchedulerResponse> response = RestClient.post(apiUrl + "jobInstance/updateJobInstanceSelective",
                request, SchedulerResponse.class);
        @SuppressWarnings("unchecked")
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        return schedulerResponse.getErrorCode();
    }

    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param request
     * @return
     */
    public static int updateJobInstanceByKey(JobInstanceRequest request) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        @SuppressWarnings("rawtypes")
        ResponseEntity<SchedulerResponse> response = RestClient.post(apiUrl + "jobInstance/updateJobInstanceByKey",
                request, SchedulerResponse.class);
        @SuppressWarnings("unchecked")
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        return schedulerResponse.getErrorCode();
    }

    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param request
     * @return
     */
    public static int updateJobStatus(JobStatusRequest request) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        @SuppressWarnings("rawtypes")
        ResponseEntity<SchedulerResponse> response = RestClient.post(apiUrl + "jobInstance/updateJobStatus", request,
                SchedulerResponse.class);
        @SuppressWarnings("unchecked")
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        return schedulerResponse.getErrorCode();
    }

    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param request
     * @return
     */
    public static int markStatus() throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        @SuppressWarnings("rawtypes")
        ResponseEntity<SchedulerResponse> response = RestClient.post(apiUrl + "jobInstance/markStatus",
                SchedulerResponse.class);
        @SuppressWarnings("unchecked")
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        return schedulerResponse.getErrorCode();
    }

    /**
     * 返回添加任务实例的id
     * 
     * @param request
     * @return
     */
    public static Integer addJobInstance(JobInstanceRequest request) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        @SuppressWarnings("rawtypes")
        ResponseEntity<SchedulerResponse> response = RestClient.post(apiUrl + "jobInstance/addJobInstance", request,
                SchedulerResponse.class);
        SchedulerResponse<?> schedulerResponse = response.getBody();
        String instanceId = String.valueOf(schedulerResponse.getData());
        return NumberUtils.toInt(instanceId);
    }

    /**
     * 获取所有任务实例的状态，用于master恢复。
     * 
     * @return
     */
    public static List<JobInstanceStateResponse> getAllJobStatus() throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>>() {
        };
        SchedulerResponse<List<JobInstanceStateResponse>> response = RestClient
                .getForObject(apiUrl + "jobInstance/getAllJobStatus", typeRef, null);
        return response.getData();
    }

    /**
     * 根据id查看任务实例
     * 
     * @param id
     * @return
     */
    public static JobInstanceResponse getJobInstanceById(Integer id) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {
        };
        SchedulerResponse<JobInstanceResponse> jobInstanceResponse = RestClient
                .getForObject(apiUrl + "jobInstance/getJobInstanceById", typeRef, map);
        return jobInstanceResponse.getData();
    }

    /**
     * 根据id查看任务
     * 
     * @param id
     * @return
     */
    public static JobResponse getJobById(Integer id) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        ParameterizedTypeReference<SchedulerResponse<JobResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobResponse>>() {
        };
        SchedulerResponse<JobResponse> jobResponse = RestClient.getForObject(apiUrl + "job/getJobById", typeRef, map);
        return jobResponse.getData();
    }

    /**
     * 根据connectionId查看连接
     * 
     * @param connectionId
     * @return
     */
    public static ConnectionResponse getConnection(String connectionId) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("connectionId", connectionId);
        ParameterizedTypeReference<SchedulerResponse<ConnectionResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<ConnectionResponse>>() {
        };
        SchedulerResponse<ConnectionResponse> jobResponse = RestClient.getForObject(apiUrl + "connection/getConnection",
                typeRef, map);
        return jobResponse.getData();
    }

    /**
     * 根据任务id查看任务树状结构
     * 
     * @param id
     * @return
     */
    public static JobTree queryTreeById(Integer id) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        ParameterizedTypeReference<SchedulerResponse<JobTree>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobTree>>() {
        };
        SchedulerResponse<JobTree> jobResponse = RestClient.getForObject(apiUrl + "job/queryTreeById", typeRef, map);
        return jobResponse.getData();
    }

    /**
     * 根据schedule时间和任务id查看任务实例id
     * 
     * @param jobId
     * @param scheduleTime
     * @return
     */
    public static JobInstanceResponse getJobInstanceByTime(Integer jobId, String scheduleTime) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", jobId);
        map.put("scheduleTime", scheduleTime);
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {
        };
        SchedulerResponse<JobInstanceResponse> jobResponse = RestClient
                .getForObject(apiUrl + "jobInstance/getJobInstanceByTime", typeRef, map);
        return jobResponse.getData();
    }
    
    public static List<Map<String, Object>> getTaskCountByWorker() throws Exception {
        ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>>() {
        };
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        SchedulerResponse<List<Map<String, Object>>> schedulerResponse = RestClient
                .getForObject(apiUrl + "jobInstance/getRunningJobCount", typeRef, null);
        return schedulerResponse.getData();
    }
}
