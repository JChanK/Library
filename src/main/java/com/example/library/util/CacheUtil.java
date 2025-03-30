package com.example.library.util;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtil<K, V> {

    private final LinkedHashMap<K, V> cache;
    private final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

    public CacheUtil(int capacity) {
        cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > capacity;
                if (shouldRemove) {
                    logger.debug("Удаление устаревшего элемента из кэша. Ключ: {}",
                            eldest.getKey());
                }
                return shouldRemove;
            }
        };
    }

    public void put(K key, V value) {
        cache.put(key, value);
        logger.debug("Объект добавлен в кэш. Ключ: {}", key);
        logger.trace("Добавлен объект: {} = {}", key, value);
    }

    public V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            logger.debug("Объект найден в кэше. Ключ: {}", key);
            logger.trace("Получен объект: {} = {}", key, value);
        } else {
            logger.debug("Объект не найден в кэше. Ключ: {}", key);
        }
        return value;
    }

    public void evict(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
            logger.debug("Объект удален из кэша. Ключ: {}", key);
        } else {
            logger.debug("Объект не найден в кэше. Ключ: {}", key);
        }
    }

    public void clear() {
        cache.clear();
        logger.info("Кэш полностью очищен");
    }
}