package com.ylpu.thales.scheduler.executor.hive;

import java.util.Map;

public class HiveConfig {

    private String fileName;
    private Config config = new Config();
    private Map<String, Object> parameters;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

}
