package com.ylpu.thales.scheduler.common.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Configuration {
	
    private static Log LOG = LogFactory.getLog(Configuration.class);
	
    private static Map<String,Properties> configMap = new HashMap<String,Properties>();
    
    public static Properties getConfig(String propFileName) {
    	    Properties config = configMap.get(propFileName);
    	    if(config == null){
            Properties prop = new Properties();
            try {
                prop.load(Configuration.class.getClassLoader().getResourceAsStream(propFileName));
            } catch (IOException e) {
            	    LOG.error(e);
            }
            config = prop;
            configMap.put(propFileName, prop);
    	    }
        return config;
    }
    
    public static int getInt(Properties prop,String key,int defaultValue) {
        return NumberUtils.toInt(getString(prop,key,String.valueOf(defaultValue)));
    }
    
    public static String getString(Properties prop,String key,String defaultValue) {
        String value = prop.getProperty(key);
        if(StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }
    
    public static Boolean getBoolean(Properties prop,String key,Boolean defaultValue) {
        return Boolean.valueOf(getString(prop,key,String.valueOf(defaultValue)));
    }
    
    public static Double getDouble(Properties prop,String key,Double defaultValue) {
        return Double.valueOf(getString(prop,key,String.valueOf(defaultValue)));       
    }
    
    public static Long getLong(Properties prop,String key,Long defaultValue) {
        return Long.valueOf(getString(prop,key,String.valueOf(defaultValue)));       
    }
}
