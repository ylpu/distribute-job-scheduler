package com.ylpu.thales.scheduler.executor.flink;

public class FlinkConfig {

    private FlinkParameters parameters = new FlinkParameters();

    public FlinkParameters getParameters() {
        return parameters;
    }

    public void setParameters(FlinkParameters parameters) {
        this.parameters = parameters;
    }
}