package com.ylpu.thales.scheduler.common.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {
    
    /**
     * http://localhost:8080/list/1/2
     * @param url
     * @param t
     * @return
     */
    public static <T> T getForObject(String url, Class<T> t) {
        RestTemplate restTemplate = new RestTemplate();
        T entity = restTemplate.getForObject(url, t);
        return entity;
    }

    /**
     * 
     * @param url--http://localhost:8080/list?id=1&name=test
     * @param typeRef -- 范型返回
     * @param map
     * @return
     */
    public static <T> T getForObject(String url, ParameterizedTypeReference<T> typeRef,Map<String,Object> map) {
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder variables = new StringBuilder();
        ResponseEntity<T> entity = null;
        if(map != null && map.size() > 0) {
            List<Entry<String,Object>> list = new ArrayList<Entry<String,Object>>(map.entrySet());
            
            for(Iterator<Entry<String, Object>> it = list.iterator();it.hasNext();) {
                Entry<String, Object> entry = it.next();
                variables.append(entry.getKey());
                variables.append("=");
                variables.append(entry.getValue());
                if(it.hasNext()) {
                    variables.append("&");
                }
            }
            entity = restTemplate.exchange(url + "?" + variables.toString(),HttpMethod.GET,null, typeRef);
        }else {
            entity = restTemplate.exchange(url,HttpMethod.GET,null, typeRef);
        }
        return entity.getBody();
    }
    
    public static <T> ResponseEntity<T> post(String url,Object request,Class<T> type) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(request, requestHeaders);
        ResponseEntity<T> response = restTemplate.postForEntity(url, requestEntity, type);
        return response;
    }
    
    public static <T> ResponseEntity<T> post(String url,Object request) {
        return post(url,request,null);
    }
}
