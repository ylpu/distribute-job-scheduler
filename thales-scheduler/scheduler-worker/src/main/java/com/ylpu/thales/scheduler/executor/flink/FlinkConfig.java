package com.ylpu.thales.scheduler.executor.flink;

public class FlinkConfig {
    
    private String className;
    private String jarName;

    private Config config = new Config();

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

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}