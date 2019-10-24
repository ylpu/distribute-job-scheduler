package com.ylpu.kepler.scheduler.common.zk;

import java.io.IOException;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;


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
    
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException{
       
    }
}
