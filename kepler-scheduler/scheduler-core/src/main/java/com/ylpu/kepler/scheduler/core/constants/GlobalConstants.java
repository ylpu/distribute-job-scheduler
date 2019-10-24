package com.ylpu.kepler.scheduler.core.constants;

public class GlobalConstants {
    
    public static final String DEFAULT_API_URL = "http://localhost:8085/api/";
    
    public static final String DEFAULT_ZKQUORUM = "127.0.0.1:2181";
    
    public static final String DEFAULT_WORKER_STRATEGY = "MEMORY";
    
    public static final String DEFAULT_SCHEDULER_TRIGGER_GROUP = "kepler_";
    
    public static final String DEFAULT_SCHEDULER_JOB_GROUP = "kepler_";
    
    public static final String ROOT_GROUP = "/kepler";
    
    public static final int ZOOKEEPER_SESSION_TIMEOUT = 4000;
    
    public static final int ZOOKEEPER_CONNECTION_TIMEOUT = 60000;
    
    public static final String WORKER_GROUP = "/kepler/workers";
    
    public static final String MASTER_GROUP = "/kepler/masters";
    
    public static final String KEPLER = "kepler";
    
    public static final String MASTERS = "masters";
    
    public static final String WORKERS = "workers";
    
    public static final String CONFIG_FILE = "config.properties";
    
    public static final String APPLICATION_REGEX = "application_\\d+_\\d+";
    
    public static final String DEFAULT_WORKER_LOG_DIR = "/tmp/log/worker";
}
