package com.ylpu.thales.scheduler.worker.test;

import java.util.Map;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import com.ylpu.thales.scheduler.core.rest.RestClient;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.executor.http.HttpExecutor;
import com.ylpu.thales.scheduler.executor.http.HttpParameters;

public class HttpExecutorTest {

    @Test
    public void testGet() throws Exception {
        HttpExecutor httpExecutor = new HttpExecutor();
        String getJson = "{\"url\":\"http://localhost:8085/api/job/getJobById\",\n" + "\"method\": \"get\",\n"
                + "\"parameters\":{\n" + "  \"id\":45\n" + "}\n" + "}";
        try {
            HttpParameters getMap = JsonUtils.jsonToBean(getJson, HttpParameters.class);
            Map<String, Object> parameterValues = httpExecutor.getParameterValues(getMap.getParameters());
            ParameterizedTypeReference<Object> typeRef = new ParameterizedTypeReference<Object>() {
            };
            Object jobResponse = RestClient.getForObject(getMap.getUrl(), typeRef, parameterValues);
            System.out.println(JsonUtils.objToJson(jobResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testPost() throws Exception {
        HttpExecutor httpExecutor = new HttpExecutor();
        Object response = null;
        String postJson = "{\"url\":\"http://localhost:8085\",\n" + "\"method\": \"post\",\n" + "\"parameters\":{\n"
                + "  \"alertTypes\": \"email\",\n" + "  \"alertUsers\": \"string\",\n"
                + "  \"creatorId\": \"string\",\n" + "  \"dependIds\": \"\",\n" + "  \"description\": \"string\",\n"
                + "  \"executionTimeout\": 0,\n" + "  \"isSelfdependent\": true,\n"
                + "  \"jobConfiguration\": \"string\",\n" + "  \"jobCycle\": \"DAY\",\n"
                + "  \"jobName\": \"rest-test\",\n" + "  \"jobPriority\": \"HIGH\",\n" + "  \"jobReleasestate\": 0,\n"
                + "  \"jobType\": \"shell\",\n" + "  \"maxRetrytimes\": 0,\n" + "  \"ownerIds\": \"test\",\n"
                + "  \"retryInterval\": 0,\n" + "  \"scheduleCron\": \"0 3 15 * * ?\",\n"
                + "  \"workerGroupname\": \"hive\"\n" + "}\n" + "}";
        try {
            HttpParameters map = JsonUtils.jsonToBean(postJson, HttpParameters.class);
            System.out.println(map.getParameters());
            Object object = httpExecutor.getObjectFromJson(postJson, map.getParameters());
            response = RestClient.post("http://localhost:8085/api/job/addJob", object, Object.class);
            System.out.println(JsonUtils.objToJson(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testPost1() throws Exception {
        HttpExecutor httpExecutor = new HttpExecutor();
        Object response = null;
        String postJson = "{\"url\":\"http://localhost:8085\",\n" + "\"method\": \"post\",\n" + "\"parameters\":{\n"
                + "  \"alertTypes\": \"email\",\n" + "  \"alertUsers\": \"string\",\n"
                + "  \"creatorId\": \"string\",\n" + "  \"dependIds\": \"\",\n" + "  \"description\": \"string\",\n"
                + "  \"executionTimeout\": 0,\n" + "  \"isSelfdependent\": true,\n"
                + "  \"jobConfiguration\": \"string\",\n" + "  \"jobCycle\": \"DAY\",\n"
                + "  \"jobName\": \"rest-test\",\n" + "  \"jobPriority\": \"HIGH\",\n" + "  \"jobReleasestate\": 0,\n"
                + "  \"jobType\": \"shell\",\n" + "  \"maxRetrytimes\": 0,\n" + "  \"ownerIds\": \"test\",\n"
                + "  \"retryInterval\": 0,\n" + "  \"scheduleCron\": \"0 3 15 * * ?\",\n"
                + "  \"workerGroupname\": \"hive\"\n" + "}\n" + "}";
        try {
            HttpParameters map = JsonUtils.jsonToBean(postJson, HttpParameters.class);
            System.out.println(map.getParameters());
            String jsonStr = JsonUtils.objToJson(map.getParameters());
            Object obj = JsonUtils.jsonToBean(jsonStr, Object.class);
            // Object object = httpExecutor.getObjectFromJson(postJson,map.getParameters());
            response = RestClient.post("http://localhost:8085/api/job/addJob", obj, Object.class);
            System.out.println(JsonUtils.objToJson(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
