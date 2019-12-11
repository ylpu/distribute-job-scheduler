package com.ylpu.thales.scheduler.executor.sql;

import java.util.Map;

public class SQLConfig {
	
	private String connectionId;
	private String operator;
	private String sql;
	private Map<String,Object> parameters;
	
	public String getConnection() {
		return connectionId;
	}
	public void setConnection(String connectionId) {
		this.connectionId = connectionId;
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
