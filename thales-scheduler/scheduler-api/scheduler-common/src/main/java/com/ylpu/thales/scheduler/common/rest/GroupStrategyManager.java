package com.ylpu.thales.scheduler.common.rest;

import org.springframework.http.ResponseEntity;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;

public class GroupStrategyManager {

    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param response
     * @return
     */
    public static int addGroupStrategy(String url, GroupStrategyRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "group/addGroupStrategy", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }
    /**
     * 返回更新成功与否标识,200表示成功，500表示失败
     * 
     * @param response
     * @return
     */
    public static int updateGroupStrategy(String url, GroupStrategyRequest request) {
        try {
            ResponseEntity<Object> response = RestClient.post(url + "group/updateGroupStrategy", request);
            return response.getStatusCode().value();
        } catch (Exception e) {
            return 500;
        }
    }

}
