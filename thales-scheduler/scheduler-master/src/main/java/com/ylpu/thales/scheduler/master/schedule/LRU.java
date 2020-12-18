package com.ylpu.thales.scheduler.master.schedule;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRU<K, V> {
    
    private final int MAX_CACHE_SIZE;
    private final float DEFAULT_LOAD_FACTORY = 0.75f;
 
    LinkedHashMap<K, V> map;
 
    public LRU(int cacheSize) {
        MAX_CACHE_SIZE = cacheSize;
        int capacity = (int)Math.ceil(MAX_CACHE_SIZE / DEFAULT_LOAD_FACTORY) + 1;
        /*
         * 第三个参数设置为true，代表linkedlist按访问顺序排序，可作为LRU缓存
         * 第三个参数设置为false，代表按插入顺序排序，可作为FIFO缓存
         */
        map = new LinkedHashMap<K, V>(capacity, DEFAULT_LOAD_FACTORY, false) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }
 
    public synchronized void put(K key, V value) {
        map.put(key, value);
    }
 
    public synchronized V get(K key) {
        return map.get(key);
    }
 
    public synchronized void remove(K key) {
        map.remove(key);
    }
 
    public LinkedHashMap<K, V> get() {
        return map;
    }
}