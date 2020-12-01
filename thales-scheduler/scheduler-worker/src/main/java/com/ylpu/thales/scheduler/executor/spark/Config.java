package com.ylpu.thales.scheduler.executor.spark;

public class Config {

    /**
     * deploy mode
     */
    private String deployMode;

    /**
     * master
     */

    private String master;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * driver-cores Number of cores used by the driver, only in cluster mode
     */
    private Integer driverCores;

    /**
     * driver-memory Memory for driver
     */

    private String driverMemory;

    /**
     * num-executors Number of executors to launch
     */
    private Integer numExecutors;

    /**
     * total-executor-cores Number of cores per executor
     */
    private Integer totalExecutorCores;

    /**
     * executor-cores Number of cores per executor
     */
    private Integer executorCores;

    /**
     * Memory per executor
     */
    private String executorMemory;

    /**
     * The YARN queue to submit to
     */
    private String queue;

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public void setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
    }

    public Integer getDriverCores() {
        return driverCores;
    }

    public void setDriverCores(Integer driverCores) {
        this.driverCores = driverCores;
    }

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    public Integer getNumExecutors() {
        return numExecutors;
    }

    public void setNumExecutors(Integer numExecutors) {
        this.numExecutors = numExecutors;
    }

    public Integer getExecutorCores() {
        return executorCores;
    }

    public void setExecutorCores(Integer executorCores) {
        this.executorCores = executorCores;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Integer getTotalExecutorCores() {
        return totalExecutorCores;
    }

    public void setTotalExecutorCores(Integer totalExecutorCores) {
        this.totalExecutorCores = totalExecutorCores;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
}
