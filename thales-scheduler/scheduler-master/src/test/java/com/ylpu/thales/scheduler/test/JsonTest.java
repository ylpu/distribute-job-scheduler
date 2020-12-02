package com.ylpu.thales.scheduler.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonTest {
    
    public static String replaceHolder(String json, Map<String,Object> placeHolder) {
        if(placeHolder != null && placeHolder.size() > 0) {
            Gson gson = new Gson();
            Map<String , Object> map = gson.fromJson(json,Map.class);
            Map<String, Object> parameters = (Map<String, Object>) map.get("parameters");
            if(parameters != null && parameters.size() > 0) {
                for(Entry<String,Object> entry : parameters.entrySet()) {
                    String placeHolderKey = entry.getValue().toString().replace("&", "").toLowerCase();
                    if(placeHolder.containsKey(placeHolderKey)) {
                        json = json.replace(entry.getValue().toString(), "\"" + placeHolder.get(placeHolderKey) + "\"");
                    }
                }
            }
        }
        return json;
    }
    
    public static String getParameters(String json) {
        Gson gson = new Gson();
        Map<String , Object> map = gson.fromJson(json,Map.class);
        Map<String, Object> parameters = (Map<String, Object>) map.get("parameters");
        Set<Entry<String,Object>> entrySet = parameters.entrySet();
        Iterator<Entry<String,Object>> it = entrySet.iterator();
        StringBuilder sb = new StringBuilder();
        while(it.hasNext()) {
            Entry<String,Object> entry = it.next();
            sb.append(entry.getKey() + "=" + entry.getValue());
            if(it.hasNext()) {
                sb.append(",");
            }
        }     
        return sb.toString();
    }
    
    public static String replaceParameterValue(String content, String parameters) {
        Gson gson = new Gson();
        JsonObject firstObject = null;
        if(StringUtils.isNoneBlank(parameters)) {
            JsonElement element = gson.fromJson(content, JsonElement.class);
            firstObject = element.getAsJsonObject();
            String[] params =  parameters.split(",");
            if(params != null && params.length > 0) {
                for(String param : params) {
                    String[] kv = param.split("=");
                    firstObject.get("parameters").getAsJsonObject().addProperty(kv[0], kv[1]);
                }
                return firstObject.toString();
            }else {
                return content;
            }
        }else {
            return content;
        }
    }
    
    public static void main(String[] args) {
                
        String jsonString = "{\"fileName\" : \"/tmp/shell/test.sh\",\"parameters\" : {\"param1\":&from,\"param2\":&to}}";
        Map<String,Object> placeHolder = new HashMap<String,Object>();
        placeHolder.put("from", "20201201");
        placeHolder.put("to", "20201202");
        String replaceJsonString = replaceHolder(jsonString,placeHolder);
        System.out.println(replaceJsonString);
        
        System.out.println(getParameters(replaceJsonString));
        
        String parameters = "param1=test2,param2=test1";
        System.out.println(replaceParameterValue(replaceJsonString,parameters));
    }
}
