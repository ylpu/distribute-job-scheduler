package com.ylpu.thales.scheduler.common.curator;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorHelper {
    
    private static final Log LOG = LogFactory.getLog(CuratorHelper.class);

    private static final String DEFAULT_ZKSERVERS = "127.0.0.1:2181";

    private static final int SESSION_TIMEOUT = 5000;

    private static final int CONNECTION_TIMEOUT = 6000;

    public static CuratorFramework getCuratorClient() {
        return getCuratorClient(DEFAULT_ZKSERVERS, SESSION_TIMEOUT, CONNECTION_TIMEOUT);
    }

    public static CuratorFramework getCuratorClient(String zkServers, int sessionTimeout, int connectionTimeout) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkServers, sessionTimeout, connectionTimeout,
                retryPolicy);
        client.start();

        return client;
    }

    public static boolean nodeExist(CuratorFramework client, String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        if (stat == null) {
            return false;
        }
        return true;
    }

    public static void createNodeIfNotExist(CuratorFramework client, String path, CreateMode createMode, byte[] bytes)
            throws Exception {
        if (!nodeExist(client, path)) {
            client.create().withMode(createMode).forPath(path, bytes);
        }
    }

    public static void delete(CuratorFramework client, String path) throws Exception {
        client.delete().forPath(path);
    }

    public static void deleteChildren(CuratorFramework client, String path) throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }

    public static void creatingParentContainersIfNeeded(CuratorFramework client, String path, CreateMode createMode,
            byte[] bytes) throws Exception {
        client.create().creatingParentContainersIfNeeded().withMode(createMode).forPath(path, bytes);
    }

    public static List<String> getChildren(CuratorFramework client, String path) throws Exception {
        List<String> list = client.getChildren().forPath(path);
        return list;
    }

    public static byte[] getData(CuratorFramework client, String path) throws Exception {
        byte[] bytes = client.getData().forPath(path);
        return bytes;
    }

    public static Stat setData(CuratorFramework client, String path, byte[] bytes) throws Exception {
        Stat stat = client.setData().forPath(path, bytes);
        return stat;
    }

    public static void close(CuratorFramework client) {
        if (client != null) {
            client.close();
        }
    }
}
