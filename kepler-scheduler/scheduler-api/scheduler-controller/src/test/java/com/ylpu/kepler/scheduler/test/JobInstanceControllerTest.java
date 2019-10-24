package com.ylpu.kepler.scheduler.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import com.ylpu.kepler.scheduler.core.rest.RestClient;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.request.ScheduleRequest;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;
import com.ylpu.kepler.scheduler.response.JobInstanceStateResponse;
import com.ylpu.kepler.scheduler.response.SchedulerResponse;

public class JobInstanceControllerTest {
    
    private static final String API_URI = "http://localhost:8080/api/";
    
    @Test
    public void getRunningJobCount() {
        ParameterizedTypeReference<SchedulerResponse<List<Map<String,Object>>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<Map<String,Object>>>>() {};
        SchedulerResponse<List<Map<String,Object>>> schedulerResponse = RestClient.getForObject(API_URI + "jobInstance/getRunningJobCount",typeRef,null);
        System.out.println(schedulerResponse.getData().size());
    }
    
    @Test
    public void getAllJobStatus() {
        ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>>() {};
        SchedulerResponse<List<JobInstanceStateResponse>> schedulerResponse = RestClient.getForObject(API_URI + "jobInstance/getAllJobStatus",typeRef,null);
        System.out.println(schedulerResponse.getData().size());
    }
    
    @Test
    public void addJobInstance() {
        JobInstanceRequest jr = new JobInstanceRequest();
        jr.setJobId(29);
        jr.setLogUrl("");
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/addJobInstance",jr,SchedulerResponse.class);
        SchedulerResponse schedulerResponse = response.getBody();
        System.out.println(schedulerResponse.getData());
    }
    
    @Test
    public void updateJobInstance() {
        JobInstanceRequest jr = new JobInstanceRequest();
        jr.setId(114);
        jr.setTaskState(3);
        jr.setLogPath(null);
        jr.setWorker(null);
        jr.setJobId(null);
        jr.setLogUrl(null);
        jr.setCreatorName(null);
        jr.setCreatorEmail(null);
        jr.setCreatorEmail(null);
        jr.setRetryTimes(0);
        jr.setPid(null);
        jr.setElapseTime(53);
        jr.setEndTime(new Date());
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/updateJobInstance",jr,SchedulerResponse.class);
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        System.out.println(schedulerResponse.getErrorCode());
    }
    
    @Test
    public void markAsFailed() {
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/markAsFailed",SchedulerResponse.class);
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        System.out.println(schedulerResponse.getErrorCode());
    }
    
    @Test
    public void testGetJobInstance() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", 110);
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {};
        SchedulerResponse<JobInstanceResponse> response = RestClient.getForObject(API_URI + "jobInstance/getJobInstanceById",typeRef, map);
        System.out.println(response.getData().getId());
    }
    
    @Test
    public void viewLog() {
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>() {};
        String str = RestClient.getForObject("http://localhost:10001/api/log/viewLog/225",typeRef,null);
        System.out.println(str);
    }
    
    @Test
    public void rerun() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(243);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/rerun",request);
        System.out.println(response.getStatusCodeValue());
    }
    
    @Test
    public void rerunAll() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(243);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/rerunAll",request);
        System.out.println(response.getStatusCodeValue());
    }
    
    @Test
    public void kill() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(234);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/killJob",request);
        System.out.println(response.getStatusCodeValue());
    }
}
