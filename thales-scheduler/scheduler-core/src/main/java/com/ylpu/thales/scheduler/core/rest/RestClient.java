package com.ylpu.thales.scheduler.core.rest;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestClient {
    
    private static final int CONNECT_TIMEOUT = 50000;

    private static final int SOCKET_TIMEOUT = 50000;
    
    private static final int CONNECT_REQUEST_TIMEOUT = 50000;

    private static final int MAX_TOTAL = 200;

    private static final int MAX_PER_ROUTE = 200;
    
    private static HttpComponentsClientHttpRequestFactory factory;
    
    static {
        factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient(poolingHttpClientConnectionManager(), requestConfig()));
    }
    
    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(CONNECT_TIMEOUT);
        return connectionManager;
    }

    private static RequestConfig requestConfig() {
        RequestConfig result = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        return result;
    }

    private static CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, RequestConfig requestConfig) {

        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,int executionCount, HttpContext context) {
                if (executionCount >= 3) { //give up if retry more than 3 times
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// retry if server lost connection
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// do not retry if ssl exception
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// do not retry if interrupted exception
                    return false;
                }
                if (exception instanceof UnknownHostException) {// do not retry if unknow host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// do not retry if connetion timeout
                    return false;
                }
                if (exception instanceof SSLException) {// do not retry if SSL exception 
                    return false;
                }
 
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
              
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient result = HttpClientBuilder
                .create()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(httpRequestRetryHandler)
                .build();
        return result;
    }

    /**
     * http://localhost:8080/list/1/2
     * 
     * @param url
     * @param t
     * @return
     */
    public static <T> T getForObject(String url, Class<T> t) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        T entity = restTemplate.getForObject(url, t);
        return entity;
    }

    public static <T> T getForObject(String url, ParameterizedTypeReference<T> typeRef, Map<String, Object> map) {
        return getForObject(url, typeRef, map, null);
    }

    /**
     * ttp://localhost:8080/list?id=1&name=test
     * 
     * @param url
     * @param t
     * @param map
     * @return
     */
    public static <T> T getForObject(String url, ParameterizedTypeReference<T> typeRef, Map<String, Object> map,
            Map<String, Object> headers) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        StringBuilder variables = new StringBuilder();

        ResponseEntity<T> entity = null;
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("needAuthorize", "false");
        HttpEntity<String> httpEntity = null;
        if (headers != null && headers.size() > 0) {
            for (Entry<String, Object> entry : headers.entrySet()) {
                requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
            httpEntity = new HttpEntity<String>(requestHeaders);
        }

        if (map != null && map.size() > 0) {
            List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>(map.entrySet());

            for (Iterator<Entry<String, Object>> it = list.iterator(); it.hasNext();) {
                Entry<String, Object> entry = it.next();
                variables.append(entry.getKey());
                variables.append("=");
                variables.append(entry.getValue());
                if (it.hasNext()) {
                    variables.append("&");
                }
            }
            entity = restTemplate.exchange(url + "?" + variables.toString(), HttpMethod.GET, httpEntity, typeRef);
        } else {
            entity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, typeRef);
        }
        return entity.getBody();
    }

    public static <T> ResponseEntity<T> post(String url, Object request, Class<T> type) {
        return post(url, request, type, null);
    }

    public static <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> type,
            Map<String, Object> headers) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.add("needAuthorize", "false");
        HttpEntity<Object> httpEntity = null;
        if (headers != null && headers.size() > 0) {
            for (Entry<String, Object> entry : headers.entrySet()) {
                requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        httpEntity = new HttpEntity<Object>(request, requestHeaders);
        ResponseEntity<T> response = restTemplate.postForEntity(url, httpEntity, type);
        return response;
    }

    public static <T> ResponseEntity<T> post(String url, Object request, Class<T> type, Map<String, Object> headers) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity<Object> httpEntity = null;
        if (headers != null && headers.size() > 0) {
            for (Entry<String, Object> entry : headers.entrySet()) {
                requestHeaders.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
            httpEntity = new HttpEntity<Object>(request, requestHeaders);
        } else {
            httpEntity = new HttpEntity<Object>(request, null);
        }
        ResponseEntity<T> response = restTemplate.postForEntity(url, httpEntity, type);
        return response;
    }

    public static <T> ResponseEntity<T> post(String url, Object request) {
        return post(url, request, null, null);
    }

    public static <T> ResponseEntity<T> post(String url, Class<T> type) {
        return post(url, null, type, null);
    } 
}
