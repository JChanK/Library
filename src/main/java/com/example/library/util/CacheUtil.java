package com.example.library.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheUtil<K, V> {

    private final LinkedHashMap<K, V> cache;

    public CacheUtil(int capacity) {
        cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void evict(K key) {
        cache.remove(key);
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    public void evictAll() {
        cache.clear();
    }
}