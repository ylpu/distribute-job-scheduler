package com.ylpu.thales.scheduler.core.utils;

import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class StringUtils {

    private StringUtils() {

    }

    public static String getRootPath(URL url) {
        String fileUrl = url.getFile();
        int pos = fileUrl.indexOf('!');
        if (-1 == pos) {
            return fileUrl;
        }
        return fileUrl.substring(5, pos);
    }

    public static String dotToSplash(String name) {
        return name.replaceAll("\\.", "/");
    }

    public static String trimExtension(String name) {
        int pos = name.indexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }
        return name;
    }

    public static String trimURI(String uri) {
        String trimmed = uri.substring(1);
        int splashIndex = trimmed.indexOf('/');
        return trimmed.substring(splashIndex);
    }

    public static <K, V> String getMapAsString(Map<K, V> map) {
        StringBuilder builder = new StringBuilder();
        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Iterator<Entry<K, V>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            builder.append("[" + entry.getKey() + "]" + " : ");
            builder.append(entry.getValue().toString());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
    
    
    public static String getValue(String value,String defaultValue) {
        
        if (org.apache.commons.lang3.StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value;
    }
    
    public static boolean isBlank(String value) {
        
        if (org.apache.commons.lang3.StringUtils.isBlank(value)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}