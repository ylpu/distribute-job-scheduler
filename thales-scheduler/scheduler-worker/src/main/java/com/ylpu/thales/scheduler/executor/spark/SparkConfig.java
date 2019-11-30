package com.ylpu.thales.scheduler.executor.spark;

public class SparkConfig {
	
	private String fileName;
	private SparkParameters parameters = new SparkParameters();
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public SparkParameters getParameters() {
		return parameters;
	}
	public void setParameters(SparkParameters parameters) {
		this.parameters = parameters;
	}
}