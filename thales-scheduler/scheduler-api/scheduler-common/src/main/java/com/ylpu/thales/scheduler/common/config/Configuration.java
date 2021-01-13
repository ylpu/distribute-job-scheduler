package com.ylpu.thales.scheduler.common.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

public class Configuration {

    private static Log LOG = LogFactory.getLog(Configuration.class);

    public static Properties getConfig() {
        final String config = System.getProperty("config.file");
        if (StringUtils.isNotBlank(config)) {
            return getConfig(() -> {
                try {
                    return new FileInputStream(config);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        return getConfig(() -> Configuration.class.getClassLoader().getResourceAsStream(GlobalConstants.CONFIG_FILE));
    }

    private static Properties getConfig(Supplier<InputStream> supplier) {
        Properties prop = new Properties();
        try {
            prop.load(supplier.get());
        } catch (IOException e) {
            LOG.error(e);
        }
        return prop;
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
