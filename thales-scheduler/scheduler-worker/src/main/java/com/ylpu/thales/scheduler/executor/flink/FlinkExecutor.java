package com.ylpu.thales.scheduler.executor.flink;

import java.util.Properties;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.StringUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class FlinkExecutor extends AbstractCommonExecutor {

    private static final String FLINK_COMMAND = "flink run ";

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
        TaskProcessUtils.execCommand("./bin/killFlink.sh", "/tmp/pid/" + requestRpc.getPid() + ".out",
                "/tmp/pid/" + requestRpc.getPid() + ".error", getJobName(), hadoopHome);
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

        Config config = flinkConfig.getConfig();
        if (config == null) {
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
        
        commandBuilder.append("-m " + StringUtils.getValue(config.getMode(),"yarn-cluster"));
        commandBuilder.append(" ");

        commandBuilder.append("-ys " + config.getSlotNumber() == null ? 2 : config.getSlotNumber());
        commandBuilder.append(" ");

        commandBuilder.append("-ynm " + getJobName());
        commandBuilder.append(" ");

        commandBuilder.append("-yn " + config.getTaskManagerNumber() == null ? 2: config.getTaskManagerNumber());
        commandBuilder.append(" ");

        commandBuilder.append("-yjm " + StringUtils.getValue(config.getJobManagerMemory(),"2g"));
        commandBuilder.append(" ");
        
        commandBuilder.append("-ytm " + StringUtils.getValue(config.getTaskManagerMemory(),"2g"));
        commandBuilder.append(" ");
        
        commandBuilder.append("-yqu " + StringUtils.getValue(config.getQueue(),"default"));
        commandBuilder.append(" ");
        
        commandBuilder.append("-c " + StringUtils.getValue(flinkConfig.getClassName(),""));
        commandBuilder.append(" ");
        commandBuilder.append(StringUtils.getValue(flinkConfig.getJarName(),""));
        
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