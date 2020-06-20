package com.ylpu.thales.scheduler.executor.clickHouse;

import java.util.Map;

public class ClickHouseConfig {

    private ClickHouseParameters parameters = new ClickHouseParameters();
    private Map<String, Object> placeHolder;

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