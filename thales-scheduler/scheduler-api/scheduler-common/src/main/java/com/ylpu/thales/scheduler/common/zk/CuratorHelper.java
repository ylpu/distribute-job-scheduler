package com.ylpu.thales.scheduler.common.zk;

import java.util.List;
import java.util.Properties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;

public class CuratorHelper {
	
    private static final String DEFAULT_ZKSERVERS = "127.0.0.1:2181";    
    
    private static final int SESSION_TIMEOUT = 5000;
    
    private static final int CONNECTION_TIMEOUT = 6000;
    
    public static CuratorFramework getCuratorClient() {
        return getCuratorClient(DEFAULT_ZKSERVERS,SESSION_TIMEOUT,CONNECTION_TIMEOUT);
    }
	
	public static CuratorFramework getCuratorClient(String zkServers,int sessionTimeout,int connectionTimeout) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client =
		CuratorFrameworkFactory.newClient(
				zkServers,
				sessionTimeout,
				connectionTimeout,
		        retryPolicy);
		client.start();
		return client;
	}
	
	public static void createNode(CuratorFramework client,String path,
			CreateMode createMode,byte[] bytes) throws Exception {
		client.create().withMode(createMode).forPath(path,bytes);
	}
	
	public static void delete(CuratorFramework client,String path) throws Exception {
		client.delete().forPath(path);
	}
	
	public static void creatingParentContainersIfNeeded(CuratorFramework client,String path,
			CreateMode createMode,byte[] bytes) throws Exception{
		client.create()
		.creatingParentContainersIfNeeded()
		.withMode(createMode)
		.forPath(path,bytes);
	}
	
	public static List<String> getChildren(CuratorFramework client,String path) throws Exception{
		List<String> list = client.getChildren().forPath(path);
		return list;
	}
	
	public static byte[] getData(CuratorFramework client,String path) throws Exception{
		byte[] bytes = client.getData().forPath(path);
		return bytes;
	}
	
	public static Stat setData(CuratorFramework client,String path,byte[] bytes) throws Exception{
		Stat stat = client.setData().forPath(path,bytes);
		return stat;
	}
	
	public static void close(CuratorFramework client) {
		if(client != null) {
			client.close();
		}
	}
	
	public static String getActiveMaster() throws Exception {
	     Properties prop = Configuration.getConfig();
	     String quorum = prop.getProperty("thales.zookeeper.quorum");
	     int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
	     int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
	     CuratorFramework client = null;
	     List<String> masters = null;
	     try {
	          client = getCuratorClient(quorum,sessionTimeout,connectionTimeout);
	          masters = getChildren(client,GlobalConstants.MASTER_GROUP);
	          if(masters == null || masters.size() == 0) {
	              throw new RuntimeException("can not get active master");
	          }
	     }finally {
	          close(client);
	     }
	        return masters.get(0);
	    }
}
