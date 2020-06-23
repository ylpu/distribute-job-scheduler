package com.ylpu.thales.scheduler.executor.flink;

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class FlinkExecutor extends AbstractCommonExecutor {

    private static final String FLINK_COMMAND = "flink ";

    private JobInstanceRequestRpc requestRpc;

    public FlinkExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
        super(requestRpc, request);
        this.requestRpc = requestRpc;
    }

    /**
     * 
     */
    @Override
    public void kill() throws Exception {
        killProcess();
        Properties prop = Configuration.getConfig();
        String hadoopHome = Configuration.getString(prop, "hadoop.home", "");
        TaskProcessUtils.execCommand("./src/script/killSpark.sh", "/tmp/pid/" + requestRpc.getPid() + ".out",
                "/tmp/pid/" + requestRpc.getPid() + ".error", requestRpc.getJob().getJobName(), hadoopHome);
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
        FlinkConfig flinkConfig = JsonUtils.jsonToBean(configFile, FlinkConfig.class);

        FlinkParameters flinkParameters = flinkConfig.getParameters();
        if (flinkParameters == null) {
            throw new RuntimeException("flink任务参数不能为空");
        }

        Properties prop = Configuration.getConfig();
        String flink_home = Configuration.getString(prop, "flink.home", "");

        if (StringUtils.isBlank(flink_home)) {
            commandBuilder.append("$FLINK_HOME/bin/" + FLINK_COMMAND);
        } else {
            commandBuilder.append(flink_home + "/bin/" + FLINK_COMMAND);
        }

        commandBuilder.append(" ");

        commandBuilder.append("-ys " + flinkParameters.getSlotNumber() == null ? 2:flinkParameters.getSlotNumber());
        commandBuilder.append(" ");

        commandBuilder.append("-ynm " + requestRpc.getJob().getJobName() == null ? "default" : requestRpc.getJob().getJobName());
        commandBuilder.append(" ");

        commandBuilder.append("-yn " + flinkParameters.getTaskManagerNumber() == null ? 2: flinkParameters.getTaskManagerNumber());
        commandBuilder.append(" ");

        commandBuilder.append("-yjm " + flinkParameters.getJobManagerMemory() == null ? "2g": flinkParameters.getJobManagerMemory());
        commandBuilder.append(" ");
        
        commandBuilder.append("-ytm " + flinkParameters.getTaskManagerMemory() == null ? "2g": flinkParameters.getTaskManagerMemory());
        commandBuilder.append(" ");
        
        commandBuilder.append("-c " + flinkConfig.getClassName() == null ? "": flinkConfig.getClassName());
        commandBuilder.append(" ");
        commandBuilder.append(flinkConfig.getJarName() == null ? "" : flinkConfig.getJarName());
        
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