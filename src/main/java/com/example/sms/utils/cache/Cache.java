package com.example.sms.utils.cache;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class Cache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(Cache.class);

    @Getter
    private Map<K, CacheEntry<V>> cache;

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
        CacheEntry<V> entry = cache.get(key);
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
        if(cache.containsKey(key)) {
            CacheEntry<V> entry = cache.get(key);
            entry.value = value;
            entry.frequency++;
        } else {
            if(cache.size() >= capacity) {
                K LFUKey = findKey();
                if (LFUKey != null) {
                    cache.remove(LFUKey);
                    logger.info("Cache evicted least frequently used key: {}", LFUKey);
                }
            }
            cache.put(key, new CacheEntry<>(value));
            logger.info("Key: {} added/updated in cache with value: {}", key, value);
        }
    }

    public void clearCache() {
        cache.clear();
        logger.info("Cache cleared");
    }

    public boolean containsKey(K key) {
        boolean exists = cache.containsKey(key);
        logger.info("Key: {} exists in cache: {}", key, exists);
        return exists;
    }

    public K findKey() {
        K key = null;
        int minFrequency = Integer.MAX_VALUE;;

        for(Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if(entry.getValue().frequency < minFrequency) {
                minFrequency = entry.getValue().frequency;
                key = entry.getKey();
            }
        }

        return key;
    }

    public void remove(K key) {
        if(cache.containsKey(key)) {
            cache.remove(key);
            logger.info("Key: {} removed from cache", key);
        } else {
            logger.info("Key: {} not found in cache", key);
        }
    }

    public Cache() {
        this.capacity = 3;
        this.cache = new HashMap<>();
        logger.info("Cache initialized with capacity {}", capacity);
    }
}
