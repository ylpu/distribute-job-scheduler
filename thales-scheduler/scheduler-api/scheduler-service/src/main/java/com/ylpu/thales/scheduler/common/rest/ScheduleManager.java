package com.ylpu.thales.scheduler.common.rest;

import org.springframework.http.ResponseEntity;

import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.request.NodeRequest;

public class ScheduleManager {

    public static int scheduleJob(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/schedule", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int rescheduleJob(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/reschedule", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int downJob(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/down", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int killJob(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/kill", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int rerun(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/rerun", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int rerunAll(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/rerunAll", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int markSuccess(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/markSuccess", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

    public static int markFail(String url, ScheduleRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "job/markFail", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }
    
    public static int markDown(String url,NodeRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "worker/down", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }
}
