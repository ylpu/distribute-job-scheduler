package com.ylpu.thales.scheduler.executor.command;

import org.apache.commons.lang3.StringUtils;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class CommandExecutor extends AbstractCommonExecutor {

    public CommandExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        super(requestRpc, request);
    }

    /**
     * 从日志中获取相关applicationid
     */
    @Override
    public void kill() throws Exception {
        killProcess();
    }

    @Override
    /**
     * {"commandLine" : ""}
     * 
     * @param configFile
     * @return
     */
    public String[] buildCommand(String configFile) throws Exception {
        CommandConfig config = JsonUtils.jsonToBean(configFile, CommandConfig.class);
        String commandLine = config.getCommandLine();
        if (StringUtils.isBlank(commandLine)) {
            throw new RuntimeException("commandLine can not empty");
        }
        commandLine = replaceParameters(config.getParameters(), commandLine);
        String[] strs = new String[3];
        strs[0] = "/bin/bash";
        strs[1] = "-c";
        strs[2] = commandLine;
        return strs;
    }

    @Override
    public void preExecute() throws Exception {

    }

    @Override
    public void postExecute() throws Exception {

    }
}
