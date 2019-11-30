package com.ylpu.thales.scheduler.executor.spark;

public class SparkParameters{

	  /**
	   * deploy mode
	   */
	  private String deployMode;
	  
	  /**
	   * master url
	   */
	  
	  private String masterUrl;

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
	   * total-executor-cores Number of cores per executor
	   */
	  private int totalExecutorCores;

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

	  public int getTotalExecutorCores() {
		return totalExecutorCores;
	  }

	  public void setTotalExecutorCores(int totalExecutorCores) {
		this.totalExecutorCores = totalExecutorCores;
	  }

	  public String getMasterUrl() {
		return masterUrl;
	  }

	  public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	  }
}

