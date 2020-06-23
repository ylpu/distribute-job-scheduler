package com.ylpu.thales.scheduler.executor.flink;

public class FlinkConfig {
    
    private String className;
    private String jarName;

    private FlinkParameters parameters = new FlinkParameters();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public FlinkParameters getParameters() {
        return parameters;
    }

    public void setParameters(FlinkParameters parameters) {
        this.parameters = parameters;
    }
}