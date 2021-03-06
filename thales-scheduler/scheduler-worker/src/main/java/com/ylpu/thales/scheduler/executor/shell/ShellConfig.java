package com.ylpu.thales.scheduler.executor.shell;

import java.util.Map;

public class ShellConfig {

    private String fileName;
    private Map<String, Object> parameters;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
