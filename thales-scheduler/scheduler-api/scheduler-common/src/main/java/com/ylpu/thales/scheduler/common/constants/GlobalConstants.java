package com.ylpu.thales.scheduler.common.constants;

public class GlobalConstants {

    public static final String DEFAULT_API_URL = "http://localhost:9090/api/";

    public static final String DEFAULT_ZKQUORUM = "127.0.0.1:2181";

    public static final int ZOOKEEPER_SESSION_TIMEOUT = 4000;

    public static final int ZOOKEEPER_CONNECTION_TIMEOUT = 60000;

    public static final String CONFIG_FILE = "config.properties";

    public static final String MASTER_GROUP = "/thales/masters";
    
    public static final String WORKER_GROUP = "/thales/workers";
    
    public static final String STRATEGY_GROUP = "/thales/strategy";
    
    public static final String ROOT_GROUP = "/thales";
}
