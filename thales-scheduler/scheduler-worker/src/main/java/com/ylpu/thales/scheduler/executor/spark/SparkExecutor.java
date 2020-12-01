package com.ylpu.thales.scheduler.executor.spark;

import java.io.File;
import java.util.Properties;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.StringUtils;
import com.ylpu.thales.scheduler.core.utils.TaskProcessUtils;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;

public class SparkExecutor extends AbstractCommonExecutor {

    private static final String SPARK_COMMAND = "spark-sql -e ";

    private JobInstanceRequestRpc requestRpc;

    public SparkExecutor(JobInstanceRequestRpc requestRpc, JobInstanceRequest request) {
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
        SparkConfig sparkConfig = JsonUtils.jsonToBean(configFile, SparkConfig.class);
        String fileName = String.valueOf(sparkConfig.getFileName());
        if (!FileUtils.exist(new File(fileName)) || !fileName.endsWith(".sql")) {
            throw new RuntimeException("请输入合法的sql文件" + fileName);
        }
        Config config = sparkConfig.getConfig();
        if (config == null) {
            throw new RuntimeException("spark任务参数不能为空");
        }

        Properties prop = Configuration.getConfig();
        String spark_home = Configuration.getString(prop, "spark.home", "");

        if (StringUtils.isBlank(spark_home)) {
            commandBuilder.append("$SPARK_HOME/bin/" + SPARK_COMMAND);
        } else {
            commandBuilder.append(spark_home + "/bin/" + SPARK_COMMAND);
        }

        String fileContent = FileUtils.readFile(fileName);
        fileContent = replaceParameters(sparkConfig.getParameters(), fileContent);
                
        commandBuilder.append("\"");
        commandBuilder.append("set spark.app.name=" + getJobName() + ";");
        commandBuilder.append(fileContent + ";");
        commandBuilder.append("\"");

        commandBuilder.append(" ");

        commandBuilder.append("--master " + StringUtils.getValue(config.getMaster(),"yarn"));
        commandBuilder.append(" ");
        
        commandBuilder.append("--deploy-mode " + StringUtils.getValue(config.getDeployMode(),"cluster"));
        commandBuilder.append(" ");
        
        commandBuilder.append("--driver-memory " + StringUtils.getValue(config.getDriverMemory(),"2g"));
        commandBuilder.append(" ");

        commandBuilder.append("--executor-memory " + StringUtils.getValue(config.getExecutorMemory(),"2g"));
        commandBuilder.append(" ");
        
        commandBuilder.append("--queue " + StringUtils.getValue(config.getQueue(),"default"));
        commandBuilder.append(" ");

        commandBuilder.append("--executor-cores " + config.getExecutorCores() == null ? 
                2: config.getExecutorCores());
        commandBuilder.append(" ");

        commandBuilder.append("--total-executor-cores " + config.getTotalExecutorCores() == null ? 
                4 : config.getTotalExecutorCores());

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