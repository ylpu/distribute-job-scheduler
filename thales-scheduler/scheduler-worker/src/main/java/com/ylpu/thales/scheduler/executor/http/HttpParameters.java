package com.ylpu.thales.scheduler.executor.http;

public class HttpParameters {
	private String url;
	private String method;
	private Object headers;
	private Object parameters;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Object getHeaders() {
		return headers;
	}
	public void setHeaders(Object headers) {
		this.headers = headers;
	}
	public Object getParameters() {
		return parameters;
	}
	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}
	

}
