package com.ylpu.kepler.scheduler.executor.spark;

public class SparkParameters {


  /**
   * major class
   */
  private String mainClass;

  /**
   * deploy mode
   */
  private String deployMode;

  /**
   * arguments
   */
  private String mainArgs;

  /**
   * driver-cores Number of cores used by the driver, only in cluster mode
   */
  private int driverCores;

  /**
   * driver-memory Memory for driver
   */

  private String driverMemory;

  /**
   * num-executors Number of executors to launch
   */
  private int numExecutors;

  /**
   * executor-cores Number of cores per executor
   */
  private int executorCores;

  /**
   * Memory per executor
   */
  private String executorMemory;

  /**
   * The YARN queue to submit to
   */
  private String queue;

  /**
   * other arguments
   */
  private String others;
  
  /**
   * spark jar包
   */
  private String jars;

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

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

  public int getDriverCores() {
    return driverCores;
  }

  public void setDriverCores(int driverCores) {
    this.driverCores = driverCores;
  }

  public String getDriverMemory() {
    return driverMemory;
  }

  public void setDriverMemory(String driverMemory) {
    this.driverMemory = driverMemory;
  }

  public int getNumExecutors() {
    return numExecutors;
  }

  public void setNumExecutors(int numExecutors) {
    this.numExecutors = numExecutors;
  }

  public int getExecutorCores() {
    return executorCores;
  }

  public void setExecutorCores(int executorCores) {
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

  public String getOthers() {
    return others;
  }

  public void setOthers(String others) {
    this.others = others;
  }

  public String getJars() {
    return jars;
  }

  public void setJars(String jars) {
    this.jars = jars;
  }
}