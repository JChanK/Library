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
        System.out.println("Объект добавлен в кэш. Ключ: " + key);
    }

    public V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            System.out.println("Объект найден в кэше. Ключ: " + key);
        } else {
            System.out.println("Объект не найден в кэше. Ключ: " + key);
        }
        return value;
    }

    public void evict(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
            System.out.println("Объект удален из кэша. Ключ: " + key);
        } else {
            System.out.println("Объект не найден в кэше. Ключ: " + key);
        }
    }

    public boolean containsKey(K key) {
        boolean exists = cache.containsKey(key);
        System.out.println("Ключ " + key + " существует в кэше: " + exists);
        return exists;
    }

    public void getAll() {
        System.out.println("Содержимое кэша:");
        cache.forEach((key, value) -> System.out.println("Ключ: " + key + ", Значение: " + value));
    }

    public void evictAll() {
        cache.clear();
    }
}