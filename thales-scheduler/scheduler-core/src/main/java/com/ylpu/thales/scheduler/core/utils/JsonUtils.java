package com.ylpu.thales.scheduler.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtils {
    /**
     * convert object to json
     * 
     */
    public static String objToJson(Object obj) {
        JSONObject jSONObject = JSONObject.fromObject(obj);
        String jsonString = JSON.toJSONString(jSONObject, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
        return jsonString;
    }

    /**
     * convert object to json
     * 
     * @param jsonString
     */
    public static <T> T jsonToBean(String jsonString, Class<T> type) {
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return jsonToBean(jsonObject, type);
    }

    /**
     * convert jsonObject to javabean
     * 
     * @param jsonObject
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T jsonToBean(JSONObject jsonObject, Class<T> type) {
        return (T) JSONObject.toBean(jsonObject, type);
    }

    /**
     * json 转换为 beanList
     * 
     * @param jsonArray
     * @param type
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List jsonToBeanList(JSONArray jsonArray, Class type) {
        List<Object> list = new ArrayList<Object>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray) {
                list.add(jsonToBeanList((JSONArray) obj, type));
            } else if (obj instanceof JSONObject) {
                list.add(jsonToBean((JSONObject) obj, type));
            } else {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * convert jsonarry to list
     * 
     * @param jsonArr
     * @return
     */
    public static List<Object> jsonToMapList(JSONArray jsonArr) {
        List<Object> list = new ArrayList<Object>();
        for (Object obj : jsonArr) {
            if (obj instanceof JSONArray) {
                list.add(jsonToMapList((JSONArray) obj));
            } else if (obj instanceof JSONObject) {
                list.add(jsonToMap((JSONObject) obj));
            } else {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * convert json to map
     * 
     * @param json
     * @return
     */
    public static Map<String, Object> jsonToMap(String json) {
        JSONObject obj = JSONObject.fromObject(json);
        return jsonToMap(obj);
    }

    /**
     * convert JSONObject to map
     * 
     * @param json
     * @return
     */
    public static Map<String, Object> jsonToMap(JSONObject obj) {
        Set<?> set = obj.keySet();
        Map<String, Object> map = new HashMap<String, Object>(set.size());
        for (Object key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof JSONArray) {
                map.put(key.toString(), jsonToMapList((JSONArray) value));
            } else if (value instanceof JSONObject) {
                map.put(key.toString(), jsonToMap((JSONObject) value));
            } else {
                map.put(key.toString(), obj.get(key));
            }
        }
        return map;
    }
    
    public static String replaceHolder(String json, Map<String,Object> placeHolder) {
        if(placeHolder != null && placeHolder.size() > 0) {
            Gson gson = new Gson();
            Map<String , Object> map = gson.fromJson(json,Map.class);
            Map<String, Object> parameters = (Map<String, Object>) map.get("parameters");
            if(parameters != null && parameters.size() > 0) {
                for(Entry<String,Object> entry : parameters.entrySet()) {
                    String placeHolderKey = entry.getValue().toString().replace("&", "").toLowerCase();
                    if(placeHolder.containsKey(placeHolderKey)) {
                        json = json.replace(entry.getValue().toString(), placeHolder.get(placeHolderKey).toString());
                    }
                }
            }
        }
        return json;
    }
    
    public static String getParameters(String json) {
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        Map<String , Object> map = gson.fromJson(json,Map.class);
        Map<String, Object> parameters = (Map<String, Object>) map.get("parameters");
        if(parameters != null && parameters.size() > 0) {
            Set<Entry<String,Object>> entrySet = parameters.entrySet();
            Iterator<Entry<String,Object>> it = entrySet.iterator();
            while(it.hasNext()) {
                Entry<String,Object> entry = it.next();
                sb.append(entry.getKey() + "=" + entry.getValue());
                if(it.hasNext()) {
                    sb.append(",");
                }
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
}
