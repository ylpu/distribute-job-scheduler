package com.ylpu.thales.scheduler.executor.command;

import java.util.Map;

public class CommandConfig {

    private String command;
    private Map<String, Object> parameters;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
