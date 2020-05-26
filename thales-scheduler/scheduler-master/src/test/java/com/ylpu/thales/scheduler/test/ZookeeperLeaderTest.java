package com.ylpu.thales.scheduler.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.ylpu.thales.scheduler.core.curator.CuratorHelper;

public class ZookeeperLeaderTest {

    public static void addNodeChangeListener(CuratorFramework client, final String groupPath) {
        PathChildrenCache pcCache = new PathChildrenCache(client, groupPath, true);
        try {
            pcCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            pcCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent)
                        throws Exception {
                    switch (pathChildrenCacheEvent.getType()) {
                    case CHILD_ADDED:
                        String addData = new String(
                                CuratorHelper.getData(client, pathChildrenCacheEvent.getData().getPath()));
                        System.out.println("add node" + addData);
                        break;
                    case CHILD_REMOVED:
                        String removeData = new String(
                                CuratorHelper.getData(client, pathChildrenCacheEvent.getData().getPath()));
                        System.out.println("add node" + removeData);
                        break;
                    default:
                        break;
                    }
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static final String PATH = "/thales/lock";

    public static void main(String[] args) throws Exception {

        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 5000, 3000,
                new ExponentialBackoffRetry(1000, 3));

        client.start();

        addNodeChangeListener(client, "/thales/workers");

        new MyLeaderSelectorListenerAdapter(client, PATH, "Client #" + 1);

        for (;;)
            ;

    }
}

class MyLeaderSelectorListenerAdapter extends LeaderSelectorListenerAdapter {

    private final String name;
    private final LeaderSelector leaderSelector;

    public MyLeaderSelectorListenerAdapter(CuratorFramework client, String path, String name) {
        this.name = name;
        leaderSelector = new LeaderSelector(client, path, this);

        // 保证在此实例释放领导权之后还可能获得领导权。
        leaderSelector.autoRequeue();

        leaderSelector.start();
    }

    public void close() {
        leaderSelector.close();
    }

    /**
     * 你可以在takeLeadership进行任务的分配等等，并且不要返回，如果你想要要此实例一直是leader的话可以加一个死循环。
     * 一旦此方法执行完毕之后，就会重新选举
     */
    public void takeLeadership(CuratorFramework client) throws Exception {
        System.out.println(name + " 当选为leader");
        for (;;)
            ;
    }
}
