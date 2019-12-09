package com.ylpu.thales.scheduler.executor.sql;

import java.util.Map;

public class SQLConfig {
	
	private Map<String,Object> datasource;
	private String operator;
	private String sql;
	private Map<String,Object> parameters;
	
	public Map<String, Object> getDatasource() {
		return datasource;
	}
	public void setDatasource(Map<String, Object> datasource) {
		this.datasource = datasource;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
