package com.ylpu.thales.scheduler.executor.clickHouse;

import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.response.ConnectionResponse;

public class ClickHouseExecutor extends AbstractCommonExecutor {

    private static final String CLICKHOUSE_COMMAND = "clickhouse-client ";

    public ClickHouseExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        super(requestRpc, request);
    }

    /**
     * 
     */
    @Override
    public void kill() throws Exception {
    }

    @Override
    /**
     * {"fileName" : "","parameters" : ""}
     * 
     * @param configFile
     * @return
     */
    public String[] buildCommand(String configFile) throws Exception {
        StringBuilder commandBuilder = new StringBuilder();
        ClickHouseConfig clickHouseConfig = JsonUtils.jsonToBean(configFile, ClickHouseConfig.class);

        ClickHouseParameters clickHouseParameters = clickHouseConfig.getParameters();
        if (clickHouseParameters == null) {
            throw new RuntimeException("clickhouse任务参数不能为空");
        }
        ConnectionResponse cr = JobManager.getConnection(clickHouseParameters.getDsName());
        
        commandBuilder.append(CLICKHOUSE_COMMAND);

        commandBuilder.append(" ");

        commandBuilder.append("--h " + cr.getHostname());
        commandBuilder.append(" ");
        
        commandBuilder.append("--u " + cr.getUsername());
        commandBuilder.append(" ");
        
        commandBuilder.append("--password " + cr.getPassword());
        commandBuilder.append(" ");

        commandBuilder.append("--port " + cr.getPort());
        commandBuilder.append(" ");

        String finalQuery = replaceParameters(clickHouseConfig.getPlaceHolder(), clickHouseParameters.getQuery());
        commandBuilder.append("--query " + finalQuery);
        commandBuilder.append(" ");

        commandBuilder.append("--send_timeout " + clickHouseParameters.getSendTimeout());
        commandBuilder.append(" ");
        
        commandBuilder.append("--receive_timeout " + clickHouseParameters.getReceiveTimeout());
        commandBuilder.append(" ");
        
        commandBuilder.append("--m ");

        String[] commands = new String[1];
        commands[0] = commandBuilder.toString();
        return commands;
    }

    @Override
    public void preExecute() throws Exception {

    }

    @Override
    public void postExecute() throws Exception {

    }
}