package com.ylpu.kepler.scheduler.test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import com.ylpu.kepler.scheduler.core.rest.JobManager;
import com.ylpu.kepler.scheduler.core.rest.RestClient;
import com.ylpu.kepler.scheduler.enums.TaskState;
import com.ylpu.kepler.scheduler.request.JobInstanceRequest;
import com.ylpu.kepler.scheduler.request.JobStatusRequest;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;
import com.ylpu.kepler.scheduler.response.JobInstanceStateResponse;
import com.ylpu.kepler.scheduler.response.SchedulerResponse;

public class RestTest {
    
    private static final String API_URI = "http://localhost:8080/api/";
    
    @Test
    public void testGetJobInstance() {
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {};
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", 129);
        SchedulerResponse<JobInstanceResponse> jobInstanceResponse = RestClient.getForObject(API_URI + "jobInstance/getJobInstanceById",typeRef,map);
        System.out.println(jobInstanceResponse.getData().getLogUrl());
    }
    
    @Test
    public void updateJobInstance() {
        JobInstanceRequest jr = new JobInstanceRequest();
        jr.setId(114);
        jr.setTaskState(7);
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
  public void addJobInstance() {
      JobInstanceRequest jr = new JobInstanceRequest();
      jr.setTaskState(7);
      jr.setLogPath(null);
      jr.setWorker(null);
      jr.setJobId(2);
      jr.setLogUrl(null);
      jr.setCreatorName(null);
      jr.setCreatorEmail(null);
      jr.setCreatorEmail(null);
      jr.setRetryTimes(0);
      jr.setPid(null);
      jr.setElapseTime(53);
      jr.setEndTime(new Date());
      System.out.println(JobManager.addJobInstance(jr));
  }
    
    @Test
    public void testUpdateJobStatus() {
        JobStatusRequest request = new JobStatusRequest();
        request.setIds(Arrays.asList(130));
        request.setStatus(TaskState.FAIL);
        System.out.println(JobManager.updateJobStatus(request));
    }
    
    @Test
    public void testgetAllJobStatus() {
        List<JobInstanceStateResponse> responses = JobManager.getAllJobStatus();
        for(JobInstanceStateResponse response : responses) {
            System.out.println(response.getTaskState());
        }
    }
    
    @Test
    public void testJobInstanceById() {
        System.out.println(JobManager.getJobInstanceById(140).getLogPath());
    }
}
