package com.ylpu.thales.scheduler.executor.spark;

import java.io.File;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
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
        SparkConfig sparkConfig = JsonUtils.jsonToBean(configFile, SparkConfig.class);
        String fileName = String.valueOf(sparkConfig.getFileName());
        if (!FileUtils.exist(new File(fileName)) || !fileName.endsWith(".sql")) {
            throw new RuntimeException("请输入合法的sql文件" + fileName);
        }
        SparkParameters sparkParameters = sparkConfig.getParameters();
        if (sparkParameters == null) {
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
        fileContent = replaceParameters(sparkConfig.getPlaceHolder(), fileContent);

        commandBuilder.append("\"");
        commandBuilder.append("set spark.app.name=" + requestRpc.getJob().getJobName() + ";");
        commandBuilder.append(fileContent + ";");
        commandBuilder.append("\"");

        commandBuilder.append(" ");

        commandBuilder.append("--master " + sparkParameters.getMasterUrl());
        commandBuilder.append(" ");

        commandBuilder.append("--executor-memory " + sparkParameters.getExecutorMemory());
        commandBuilder.append(" ");

        commandBuilder.append("--executor-cores " + sparkParameters.getExecutorCores());
        commandBuilder.append(" ");

        commandBuilder.append("--total-executor-cores " + sparkParameters.getTotalExecutorCores());

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