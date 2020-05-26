package com.ylpu.thales.scheduler.executor.spark;

import java.util.Map;

public class SparkConfig {

    private String fileName;
    private SparkParameters parameters = new SparkParameters();
    private Map<String, Object> placeHolder;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public SparkParameters getParameters() {
        return parameters;
    }

    public void setParameters(SparkParameters parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(Map<String, Object> placeHolder) {
        this.placeHolder = placeHolder;
    }
}