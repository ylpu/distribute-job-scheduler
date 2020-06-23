package com.ylpu.thales.scheduler.executor.clickHouse;

import java.util.Map;

public class ClickHouseConfig {
    private String dsName;
    private String query;
    private ClickHouseParameters parameters = new ClickHouseParameters();
    private Map<String, Object> placeHolder;
    
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

    public ClickHouseParameters getParameters() {
        return parameters;
    }

    public void setParameters(ClickHouseParameters parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(Map<String, Object> placeHolder) {
        this.placeHolder = placeHolder;
    }
}