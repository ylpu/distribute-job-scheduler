package com.ylpu.thales.scheduler.executor.clickHouse;

import java.util.Map;

public class ClickHouseConfig {
    private String dsName;
    private String query;
    private Config config = new Config();
    private Map<String, Object> parameters;
    
    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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