package com.ylpu.thales.scheduler.core.rest;

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
    
    public static <T> T getForObject(String url, ParameterizedTypeReference<T> typeRef,Map<String,Object> map) {
    	   return getForObject(url,typeRef,map,null);
    }

    
    /**
     * ttp://localhost:8080/list?id=1&name=test
     * @param url
     * @param t
     * @param map
     * @return
     */
    public static <T> T getForObject(String url, ParameterizedTypeReference<T> typeRef,Map<String,Object> map,Map<String,Object> headers) {
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder variables = new StringBuilder();
        
        ResponseEntity<T> entity = null;
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("needAuthorize", "false");
        HttpEntity<String> httpEntity = null;
        if(headers != null && headers.size() > 0) {
        	   for(Entry<String,Object> entry : headers.entrySet()) {
        		   requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
        	   }
        	   httpEntity = new HttpEntity<String>(requestHeaders);
        }
        
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
            entity = restTemplate.exchange(url + "?" + variables.toString(),HttpMethod.GET,httpEntity, typeRef);
        }else {
            entity = restTemplate.exchange(url,HttpMethod.GET,httpEntity, typeRef);
        }
        return entity.getBody();
    }
    
    public static <T> ResponseEntity<T> post(String url,Object request,Class<T> type) {
    	    return post(url,request,type,null);
    }
    
    public static <T> ResponseEntity<T> postForEntity(String url,Object request,Class<T> type,Map<String,Object> headers) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.add("needAuthorize", "false");
        HttpEntity<Object> httpEntity = null;
        if(headers != null && headers.size() > 0) {
     	   for(Entry<String,Object> entry : headers.entrySet()) {
     		   requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
     	   }
        }
  	    httpEntity = new HttpEntity<Object>(request,requestHeaders);
        ResponseEntity<T> response = restTemplate.postForEntity(url, httpEntity, type);
        return response;
    }
    
    public static <T> ResponseEntity<T> post(String url,Object request,Class<T> type,Map<String,Object> headers) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity<Object> httpEntity = null;
        if(headers != null && headers.size() > 0) {
        	   for(Entry<String,Object> entry : headers.entrySet()) {
        		   requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
        	   }
        	   httpEntity = new HttpEntity<Object>(request,requestHeaders);
        }else {
           httpEntity = new HttpEntity<Object>(request,null);
        }
        ResponseEntity<T> response = restTemplate.postForEntity(url, httpEntity, type);
        return response;
    }
    
    public static <T> ResponseEntity<T> post(String url,Object request) {
        return post(url,request,null,null);
    }
    
    public static <T> ResponseEntity<T> post(String url,Class<T> type) {
        return post(url,null,type,null);
    }
}
