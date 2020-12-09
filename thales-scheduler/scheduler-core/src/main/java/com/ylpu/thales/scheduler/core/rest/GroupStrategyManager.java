package com.ylpu.thales.scheduler.core.rest;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;
import com.ylpu.thales.scheduler.response.GroupStrategyResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import org.springframework.core.ParameterizedTypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupStrategyManager {
    
    public static List<GroupStrategyResponse> getAllGroupStrategy() throws Exception {
        String apiUrl = Configuration.getString(Configuration.getConfig(), "thales.api.url",
                GlobalConstants.DEFAULT_API_URL);
        ParameterizedTypeReference<SchedulerResponse<List<GroupStrategyResponse>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<GroupStrategyResponse>>>() {
        };
        SchedulerResponse<List<GroupStrategyResponse>> jobResponse = RestClient.getForObject(apiUrl + "groupStrategy/getAllGroupStrategy", typeRef, null);
        return jobResponse.getData();
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
