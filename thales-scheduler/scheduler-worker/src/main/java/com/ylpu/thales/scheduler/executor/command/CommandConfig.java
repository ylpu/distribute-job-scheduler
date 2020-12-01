package com.ylpu.thales.scheduler.executor.command;

import java.util.Map;

public class CommandConfig {

    private String commandLine;
    private Map<String, Object> parameters;

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
