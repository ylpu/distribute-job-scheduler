package com.ylpu.thales.scheduler.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ylpu.thales.scheduler.common.rest.RestClient;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;

public class JobInstanceControllerTest {

    private static final String API_URI = "http://localhost:8085/api/";

    // @Test
    public void getRunningJobCount() {
        ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<Map<String, Object>>>>() {
        };
        SchedulerResponse<List<Map<String, Object>>> schedulerResponse = RestClient
                .getForObject(API_URI + "jobInstance/getRunningJobCount", typeRef, null);
        System.out.println(schedulerResponse.getData().size());
    }

    // @Test
    public void getAllJobStatus() {
        ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>> typeRef = new ParameterizedTypeReference<SchedulerResponse<List<JobInstanceStateResponse>>>() {
        };
        SchedulerResponse<List<JobInstanceStateResponse>> schedulerResponse = RestClient
                .getForObject(API_URI + "jobInstance/getAllJobStatus", typeRef, null);
        System.out.println(schedulerResponse.getData().size());
    }

//    @Test
    public void addJobInstance() {
        String scheduleTime = "2020-1-1 20:00:00";
        for (int i = 0; i < 400000; i++) {
            JobInstanceRequest jr = new JobInstanceRequest();
            jr.setJobId(58);
            jr.setLogUrl("");
            Date scheduleDateTime = DateUtils.addDays(com.ylpu.thales.scheduler.common.utils.DateUtils
                    .getDateFromString(scheduleTime, com.ylpu.thales.scheduler.common.utils.DateUtils.DATE_TIME_FORMAT),
                    i);
            jr.setScheduleTime(scheduleDateTime);
            jr.setStartTime(new Date());
            jr.setEndTime(new Date());
            jr.setTaskState(TaskState.SUCCESS.getCode());
            ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/addJobInstance", jr,
                    SchedulerResponse.class);
            // SchedulerResponse schedulerResponse = response.getBody();
            // System.out.println(schedulerResponse.getData());
        }
    }

    // @Test
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
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/updateJobInstance", jr,
                SchedulerResponse.class);
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        System.out.println(schedulerResponse.getErrorCode());
    }

    // @Test
    public void markAsFailed() {
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/markAsFailed",
                SchedulerResponse.class);
        SchedulerResponse<Void> schedulerResponse = response.getBody();
        System.out.println(schedulerResponse.getErrorCode());
    }

    // @Test
    public void testGetJobInstance() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 110);
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {
        };
        SchedulerResponse<JobInstanceResponse> response = RestClient
                .getForObject(API_URI + "jobInstance/getJobInstanceById", typeRef, map);
        System.out.println(response.getData().getId());
    }

    // @Test
    public void viewLog() {
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>() {
        };
        String str = RestClient.getForObject("http://localhost:10001/api/log/viewLog/225", typeRef, null);
        System.out.println(str);
    }

    // @Test
    public void rerun() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(243);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/rerun", request);
        System.out.println(response.getStatusCodeValue());
    }

    // @Test
    public void rerunAll() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(243);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/rerunAll", request);
        System.out.println(response.getStatusCodeValue());
    }

    // @Test
    public void kill() {
        ScheduleRequest request = new ScheduleRequest();
        request.setId(234);
        ResponseEntity<SchedulerResponse> response = RestClient.post(API_URI + "jobInstance/killJob", request);
        System.out.println(response.getStatusCodeValue());
    }

    // @Test
    public void viewLog(String logUrl) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse();
        ServletOutputStream outputStream = null;
        InputStream decryptInputStream = null;
        try {
            decryptInputStream = getInputStream(logUrl);
            outputStream = response.getOutputStream();
            // 在http响应中输出流
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = decryptInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (decryptInputStream != null) {
                try {
                    decryptInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
     @Test
    public void testGetJobInstanceByTime() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", 32);
        map.put("scheduleTime", "2020-06-14 13:10:00");
        ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>> typeRef = new ParameterizedTypeReference<SchedulerResponse<JobInstanceResponse>>() {
        };
        SchedulerResponse<JobInstanceResponse> response = RestClient
                .getForObject(API_URI + "jobInstance/getJobInstanceByTime", typeRef, map);
        System.out.println(response.getData().getId());
    }

    public static InputStream getInputStream(String logUrl) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(logUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            inputStream = httpURLConnection.getInputStream();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inputStream;
    }
}
