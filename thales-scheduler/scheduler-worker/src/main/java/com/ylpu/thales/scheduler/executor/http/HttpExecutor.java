package com.ylpu.thales.scheduler.executor.http;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rest.RestClient;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.executor.log.LogServer;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.enums.HttpMethod;
import com.ylpu.thales.scheduler.enums.TaskState;

public class HttpExecutor extends AbstractCommonExecutor {

    private JobInstanceRequestRpc requestRpc;
    private JobInstanceRequest request;

    public HttpExecutor() {
        super();
    }

    public HttpExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        super(requestRpc, request);
        this.requestRpc = requestRpc;
        this.request = request;
    }

    @Override
    public void execute() throws Exception {
        Properties prop = Configuration.getConfig();

        String logDir = Configuration.getString(prop, "thales.worker.log.path", DEFAULT_LOG_DIR);
        String logPath = logDir + File.separator + requestRpc.getJob().getId() + "-" + requestRpc.getId() + "-"
                + DateUtils.getDateAsString(request.getStartTime(), DateUtils.TIME_FORMAT);
        String logOutPath = logPath + ".out";
        request.setLogPath(logOutPath);
        request.setLogUrl("http://" + MetricsUtils.getHostName() + ":" + LogServer.logServerPort + "/api/log/viewLog/"
                + requestRpc.getId());
        request.setTaskState(TaskState.RUNNING.getCode());
        try {
            JobManager.transitTaskStatus(request);
        }catch(Exception e) {
            throw new RuntimeException("fail to transit task " + requestRpc.getId() +  
                    " to running with exception " + e.getMessage());
        }
        try {
            String jobConfig = requestRpc.getJob().getJobConfiguration();
            HttpParameters httpParameters = JsonUtils.jsonToBean(jobConfig, HttpParameters.class);
            HttpMethod httpMethod = HttpMethod.getMethodByName(httpParameters.getMethod());
            switch (httpMethod) {
            case GET:
                executeGetCommand(httpParameters, logOutPath);
                break;
            case POST:
                executePostCommand(jobConfig, httpParameters, logOutPath);
                break;
            case PUT:
            case DELETE:
            default:
            }     
        }catch(Exception e) {
            throw new RuntimeException("failed to execute task " + request.getId() + 
                    " with exception " + e.getMessage());
        }
    }

    private void executeGetCommand(HttpParameters httpParameters, String logOutPath) throws Exception {
        if (httpParameters.getParameters() != null) {
            ParameterizedTypeReference<Object> typeRef = new ParameterizedTypeReference<Object>() {
            };
            Object jobResponse = RestClient.getForObject(httpParameters.getUrl(), typeRef,
                    httpParameters.getParameters(), httpParameters.getHeaders());
            FileUtils.writeFile(JsonUtils.objToJson(jobResponse), logOutPath);
        }
    }

    private void executePostCommand(String jsonConfig, HttpParameters httpParameters, String logOutPath)
            throws Exception {
        // Object object = getObjectFromJson(jsonConfig,httpParameters.getParameters());
        Map<String, Object> parameters = httpParameters.getParameters();
        if (parameters != null) {
            String jsonStr = JsonUtils.objToJson(parameters);
            ResponseEntity<Object> response = RestClient.postForEntity(httpParameters.getUrl(), jsonStr, Object.class,
                    httpParameters.getHeaders());
            FileUtils.writeFile(JsonUtils.objToJson(response), logOutPath);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterValues(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map<String, Object> parameterValues = new HashMap<String, Object>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase("dynaValues")) {
                parameterValues = (Map<String, Object>) field.get(obj);
            }
        }
        return parameterValues;
    }

    @SuppressWarnings("unchecked")
    public Object getObjectFromJson(String json, Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map<String, Object> parameterTypes = new HashMap<String, Object>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase("dynaClass")) {
                Field[] classeFields = field.get(obj).getClass().getDeclaredFields();
                for (Field cf : classeFields) {
                    if (cf.getName().equals("attributes")) {
                        cf.setAccessible(true);
                        parameterTypes = (Map<String, Object>) cf.get(field.get(obj));
                    }
                }
            }
        }
        HashMap<String, Class<?>> propertyMap = new HashMap<String, Class<?>>();
        for (java.util.Map.Entry<String, Object> entry : parameterTypes.entrySet()) {
            String className = entry.getValue().toString().substring("class ".length());
            propertyMap.put(entry.getKey(), Class.forName(className));
        }
        Object object = generateObject(propertyMap);
        BeanMap beanMap = BeanMap.create(object);
        Map<String, Object> parameterValues = getParameterValues(obj);
        for (java.util.Map.Entry<String, Object> entry : parameterValues.entrySet()) {
            beanMap.put(entry.getKey(), entry.getValue());
        }
        return object;
    }

    @SuppressWarnings("rawtypes")
    private Object generateObject(Map properties) {
        BeanGenerator generator = new BeanGenerator();
        Set keySet = properties.keySet();
        for (Iterator i = keySet.iterator(); i.hasNext();) {
            String key = (String) i.next();
            generator.addProperty(key, (Class) properties.get(key));
        }
        return generator.create();
    }

    @Override
    public void kill() throws Exception {

    }

    @Override
    public String[] buildCommand(String configFile) throws Exception {
        return null;
    }

    @Override
    public void preExecute() throws Exception {

    }

    @Override
    public void postExecute() throws Exception {

    }
}
