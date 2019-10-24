package com.ylpu.kepler.scheduler.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import com.ylpu.kepler.scheduler.core.rest.RestClient;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.response.JobTree;
import com.ylpu.kepler.scheduler.response.SchedulerResponse;

public class JobControllerTest {
    
    private static final String API_URI = "http://localhost:8080/api/";
    
    @Test
    public void getJobById() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", 29);
        ParameterizedTypeReference<SchedulerResponse<JobResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobResponse>>() {};
        SchedulerResponse<JobResponse> response = RestClient.getForObject(API_URI +"job/getJobById",typeRef,map);
        System.out.println(response.getData().getId());
    }
    
    @Test
    public void getTreeById() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", 37);
        ParameterizedTypeReference<SchedulerResponse<JobTree>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobTree>>() {};
        SchedulerResponse<JobTree> response = RestClient.getForObject(API_URI +"job/queryTreeById",typeRef,map);
        System.out.println(response.getData());
    }
    
    @Test
    public void scheduleJob() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(36);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "job/scheduleJob",request,SchedulerResponse.class);
        System.out.println(response.getStatusCodeValue());
    }
}
