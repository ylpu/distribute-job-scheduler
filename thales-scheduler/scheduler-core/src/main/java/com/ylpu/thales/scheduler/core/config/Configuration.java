package com.ylpu.thales.scheduler.core.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public class Configuration {

    private static Log LOG = LogFactory.getLog(Configuration.class);

    private static Map<String, Properties> configMap = new HashMap<String, Properties>();

    public static Properties getConfig() {
        final String config = System.getProperty("config.file");
        if (StringUtils.isNotBlank(config)) {
            return getConfig(config, () -> {
                try {
                    return new FileInputStream(config);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        return getConfig(GlobalConstants.CONFIG_FILE,
                () -> Configuration.class.getClassLoader().getResourceAsStream(GlobalConstants.CONFIG_FILE));
    }

    public static Properties getConfig(String propFileName) {
        String config = System.getProperty("config.file");
        if(StringUtils.isNoneBlank(config)) {
            propFileName = config;
        }
        return getConfig(propFileName,
                () -> Configuration.class.getClassLoader().getResourceAsStream(GlobalConstants.CONFIG_FILE));
    }

    private static Properties getConfig(String propFileName, Supplier<InputStream> supplier) {
        Properties config = configMap.get(propFileName);
        if (config == null) {
            Properties prop = new Properties();
            try {
                prop.load(supplier.get());
            } catch (IOException e) {
                LOG.error(e);
            }
            config = prop;
            configMap.put(propFileName, prop);
        }
        return config;
    }

    public static Properties getConfigFile(String propFileName) {
        Properties config = configMap.get(propFileName);
        if (config == null) {
            Properties prop = new Properties();
            try {
                prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName));
            } catch (IOException e) {
                LOG.error(e);
            }
            config = prop;
            configMap.put(propFileName, prop);
        }
        return config;
    }

    public static int getInt(Properties prop, String key, int defaultValue) {
        return NumberUtils.toInt(getString(prop, key, String.valueOf(defaultValue)));
    }

    public static String getString(Properties prop, String key, String defaultValue) {
        String value = prop.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    public static Boolean getBoolean(Properties prop, String key, Boolean defaultValue) {
        return Boolean.valueOf(getString(prop, key, String.valueOf(defaultValue)));
    }

    public static Double getDouble(Properties prop, String key, Double defaultValue) {
        return Double.valueOf(getString(prop, key, String.valueOf(defaultValue)));
    }

    public static Long getLong(Properties prop, String key, Long defaultValue) {
        return Long.valueOf(getString(prop, key, String.valueOf(defaultValue)));
    }
}
