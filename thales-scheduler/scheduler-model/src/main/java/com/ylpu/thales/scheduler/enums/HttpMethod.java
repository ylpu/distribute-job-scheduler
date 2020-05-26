package com.ylpu.thales.scheduler.enums;

public enum HttpMethod {

    GET, POST, PUT, DELETE;

    public static HttpMethod getMethodByName(String methodName) {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (methodName.equalsIgnoreCase(httpMethod.name())) {
                return httpMethod;
            }
        }
        throw new RuntimeException("unsupported method name " + methodName);
    }

}
