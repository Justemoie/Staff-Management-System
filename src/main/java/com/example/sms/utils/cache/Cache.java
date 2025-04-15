package com.example.sms.utils.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Cache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(Cache.class);

    @Getter
    private Map<K, CacheEntry<V>> cacheEntryMap;

    @Getter
    private int capacity;

    private static class CacheEntry<V> {
        V value;
        int frequency;

        CacheEntry(V value) {
            this.value = value;
            this.frequency = 1;
        }
    }

    public V get(K key) {
        CacheEntry<V> entry = cacheEntryMap.get(key);
        if (entry != null) {
            logger.info("Cache hit for key: {}", key);
            logger.info("frequency {}", entry.frequency);
            entry.frequency++;
            return entry.value;
        }
        logger.info("Cache miss for key: {}", key);
        logger.info("capacity: {}", capacity);
        return null;
    }

    public void put(K key, V value) {
        if (cacheEntryMap.containsKey(key)) {
            CacheEntry<V> entry = cacheEntryMap.get(key);
            entry.value = value;
            entry.frequency++;
        } else {
            if (cacheEntryMap.size() >= capacity) {
                K lfuKey = findKey();
                if (lfuKey != null) {
                    cacheEntryMap.remove(lfuKey);
                    logger.info("Cache evicted least frequently used key: {}", lfuKey);
                }
            }
            cacheEntryMap.put(key, new CacheEntry<>(value));
            logger.info("Key: {} added/updated in cache with value: {}", key, value);
        }
    }

    public void clearCache() {
        cacheEntryMap.clear();
        logger.info("Cache cleared");
    }

    public boolean containsKey(K key) {
        boolean exists = cacheEntryMap.containsKey(key);
        logger.info("Key: {} exists in cache: {}", key, exists);
        return exists;
    }

    public K findKey() {
        K key = null;
        int minFrequency = Integer.MAX_VALUE;;

        for (Map.Entry<K, CacheEntry<V>> entry : cacheEntryMap.entrySet()) {
            if (entry.getValue().frequency < minFrequency) {
                minFrequency = entry.getValue().frequency;
                key = entry.getKey();
            }
        }

        return key;
    }

    public void remove(K key) {
        if (cacheEntryMap.containsKey(key)) {
            cacheEntryMap.remove(key);
            logger.info("Key: {} removed from cache", key);
        } else {
            logger.info("Key: {} not found in cache", key);
        }
    }

    public Cache() {
        this.capacity = 3;
        this.cacheEntryMap = new HashMap<>();
        logger.info("Cache initialized with capacity {}", capacity);
    }
}
