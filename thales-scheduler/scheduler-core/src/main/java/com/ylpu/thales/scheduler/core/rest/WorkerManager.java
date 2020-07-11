package com.ylpu.thales.scheduler.core.rest;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;

import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerManager {

    public static List<Map<String, Object>> getTaskCountByWorker() throws Exception {
        ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>>() {
        };
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        SchedulerResponse<List<Map<String, Object>>> schedulerResponse = RestClient
                .getForObject(apiUrl + "jobInstance/getRunningJobCount", typeRef, null);
        return schedulerResponse.getData();
    }

    public static void updateWorkersStatusByGroup(WorkerGroupRequest param) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        RestClient.post(apiUrl + "worker/updateWorkersStatusByGroup", param, SchedulerResponse.class);
    }

    public static void updateWorkersStatus(WorkerGroupRequest param) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        RestClient.post(apiUrl + "worker/updateWorkersStatus", param, SchedulerResponse.class);
    }

    public static void insertOrUpdateWorker(WorkerRequest workerRequest) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        RestClient.post(apiUrl + "worker/insertOrUpdateWorker", workerRequest, SchedulerResponse.class);
    }
    
    /**
     * 根据groupName查看策略
     * 
     * @param id
     * @return
     */
    public static GroupStrategyResponse getGroupStrategy(String groupName) throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("groupName", groupName);
        ParameterizedTypeReference<SchedulerResponse<GroupStrategyResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<GroupStrategyResponse>>() {
        };
        SchedulerResponse<GroupStrategyResponse> jobResponse = RestClient.getForObject(apiUrl + "groupStrategy/getGroupStrategy", typeRef, map);
        return jobResponse.getData();
    }
}
