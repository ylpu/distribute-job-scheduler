package com.ylpu.thales.scheduler.common.curator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;

public class CuratorHelper {

    private static final Log LOG = LogFactory.getLog(CuratorHelper.class);

    private static final String DEFAULT_ZKSERVERS = "127.0.0.1:2181";

    private static final int SESSION_TIMEOUT = 5000;

    private static final int CONNECTION_TIMEOUT = 6000;

    public static CuratorFramework getCuratorClient() {
        return getCuratorClient(DEFAULT_ZKSERVERS, SESSION_TIMEOUT, CONNECTION_TIMEOUT);
    }

    public static CuratorFramework getCuratorClient(String zkServers, int sessionTimeout, int connectionTimeout) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 1);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkServers, sessionTimeout, connectionTimeout,
                retryPolicy);
        client.start();
        return client;
    }

    public static void createNode(CuratorFramework client, String path, CreateMode createMode, byte[] bytes)
            throws Exception {
        client.create().withMode(createMode).forPath(path, bytes);
    }

    public static void delete(CuratorFramework client, String path) throws Exception {
        client.delete().forPath(path);
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

    public static String getMasterServiceUri(int id) {
        Properties prop = Configuration.getConfig(GlobalConstants.CONFIG_FILE);
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout",
                GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout",
                GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        int masterRetryInterval = Configuration.getInt(prop, "thales.master.retry.interval", 1000);
        CuratorFramework client = null;
        List<String> masters = null;
        int i = 1;
        while (true) {
            try {
                client = CuratorHelper.getCuratorClient(quorum, sessionTimeout, connectionTimeout);
                masters = CuratorHelper.getChildren(client, GlobalConstants.MASTER_GROUP);
                if (masters != null && masters.size() > 0) {
                    StringBuilder sb = new StringBuilder("http://");
                    sb.append(masters.get(0).split(":")[0]);
                    sb.append(":");
                    sb.append(Configuration.getInt(prop, "thales.master.service.port", 9090));
                    if (isMasterAlive(sb.toString())) {
                        return sb.append("/api/").toString();
                    } else {
                        try {
                            Thread.sleep(masterRetryInterval);
                        } catch (InterruptedException e) {
                            LOG.error(e);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(masterRetryInterval);
                    } catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
                i++;
                if (i > 3) {
                    return null;
                }
            } catch (Exception e) {
                LOG.error(e);
                return null;
            } finally {
                CuratorHelper.close(client);
            }
        }
    }

    private static boolean isMasterAlive(String url) {
        boolean isAlive = true;
        HttpURLConnection conn = null;
        try {
            URL theURL = new URL(url);
            conn = (HttpURLConnection) theURL.openConnection();
            conn.setConnectTimeout(20000);
            conn.connect();
            int code = conn.getResponseCode();
            boolean success = (code >= 200) && (code < 300);
            if (!success) {
                isAlive = false;
            }
        } catch (MalformedURLException e) {
            isAlive = false;
        } catch (IOException e) {
            isAlive = false;
        } catch (Exception e) {
            isAlive = false;
        }
        return isAlive;
    }
}
