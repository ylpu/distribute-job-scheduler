package com.ylpu.kepler.scheduler.common.rest;

import org.springframework.http.ResponseEntity;

import com.ylpu.kepler.scheduler.request.ScheduleRequest;

public class ScheduleManager {
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int scheduleJob(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/schedule",request);
            return response.getStatusCode().value();
        }catch(Exception e) {
            return 500;
        }
    }
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int rescheduleJob(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/reschedule",request);
            return response.getStatusCode().value();
        }catch(Exception e) {
            return 500;
        }
    }
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int downJob(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/down",request);
            return response.getStatusCode().value(); 
        }catch(Exception e) {
            return 500;
        }
    }
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int killJob(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/kill",request);
            return response.getStatusCode().value(); 
        }catch(Exception e) {
            return 500;
        }
    }
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int rerun(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/rerun",request);
            return response.getStatusCode().value(); 
        }catch(Exception e) {
            return 500;
        }
    }
    
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * @param response
     * @return
     */
    public static int rerunAll(String url,ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/rerunAll",request);
            return response.getStatusCode().value();
        }catch(Exception e) {
            return 500;
        }
    }
}
