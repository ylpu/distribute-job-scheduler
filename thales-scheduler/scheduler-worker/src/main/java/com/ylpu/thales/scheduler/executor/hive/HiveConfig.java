package com.ylpu.thales.scheduler.executor.hive;

import java.util.Map;

import com.ylpu.thales.scheduler.executor.hive.HiveParameters;

public class HiveConfig {

    private String fileName;
    private HiveParameters parameters = new HiveParameters();
    private Map<String, Object> placeHolder;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public HiveParameters getParameters() {
        return parameters;
    }

    public void setParameters(HiveParameters parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(Map<String, Object> placeHolder) {
        this.placeHolder = placeHolder;
    }
}
