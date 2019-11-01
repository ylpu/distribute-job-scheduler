package com.ylpu.thales.scheduler.core.zk;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.constants.GlobalConstants;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

import java.util.List;
import java.util.Properties;


public class ZKHelper {
    
    private static final String DEFAULT_ZKSERVERS = "127.0.0.1:2181";    
    
    private static final int SESSION_TIMEOUT = 50000;
    
    private static final int CONNECTION_TIMEOUT = 60000;
    
    public static ZkClient getClient() {
        return getClient(DEFAULT_ZKSERVERS,SESSION_TIMEOUT,CONNECTION_TIMEOUT);
    }
    
    public static ZkClient getClient(String zkServers,int sessionTimeout,int connectionTimeout) {
        return new ZkClient(zkServers, sessionTimeout, connectionTimeout,new ZkObjectSerializer());
    }
    
    public static void createNode(ZkClient zk,String path,Object data){
        createNode(zk,path,data,CreateMode.PERSISTENT);
    }
    
    public static void createEphemeralNode(ZkClient zk,String path,Object data){
        createNode(zk,path,data,CreateMode.EPHEMERAL);
    }
    
    public static void createNode(ZkClient zk,String path,Object data,CreateMode mode){
        zk.create(path,data,Ids.OPEN_ACL_UNSAFE,mode);
    }
    
    public static void setData(ZkClient zk,String path,Object data){
        zk.writeData(path, data);
    }
    
    public static <T> T getData(ZkClient zk,String path){
        return zk.readData(path, true);
    }
    
    public static void delete(ZkClient zk,String path){
        zk.delete(path, -1);
    }
    
    public static void close(ZkClient zk) {
    	if(zk != null) {
    		zk.close();
    	}
    }
    
    public static String getActiveMaster() {
        Properties prop = Configuration.getConfig();
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        ZkClient zkClient = null;
        List<String> masters = null;
        try {
            zkClient = getClient(quorum,sessionTimeout,connectionTimeout);
            masters = zkClient.getChildren(GlobalConstants.MASTER_GROUP);
            if(masters == null || masters.size() == 0) {
                throw new RuntimeException("can not get active master");
            }
        }finally {
            close(zkClient);
        }
        return masters.get(0);
    }
}
