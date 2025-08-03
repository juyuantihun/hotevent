package com.hotech.events.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 请求缓存工具类
 */
@Slf4j
public class RequestCache<T> {
    
    private final ConcurrentHashMap<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();
    private final long ttlMs;
    private final ScheduledExecutorService cleanupExecutor;
    
    public RequestCache(long ttlMs) {
        this.ttlMs = ttlMs;
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // 定期清理过期缓存
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, ttlMs, ttlMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期则返回null
     */
    public T get(String key) {
        CacheEntry<T> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
            cache.remove(key);
            return null;
        }
        
        log.debug("缓存命中: key={}", key);
        return entry.value;
    }
    
    /**
     * 设置缓存值
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String key, T value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
        log.debug("缓存设置: key={}", key);
    }
    
    /**
     * 移除缓存值
     * 
     * @param key 缓存键
     */
    public void remove(String key) {
        cache.remove(key);
        log.debug("缓存移除: key={}", key);
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
        log.debug("缓存清空");
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存大小
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanup() {
        long currentTime = System.currentTimeMillis();
        java.util.concurrent.atomic.AtomicInteger removedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        
        cache.entrySet().removeIf(entry -> {
            boolean expired = currentTime - entry.getValue().timestamp > ttlMs;
            if (expired) {
                removedCount.incrementAndGet();
            }
            return expired;
        });
        
        if (removedCount.get() > 0) {
            log.debug("清理过期缓存: count={}", removedCount.get());
        }
    }
    
    /**
     * 关闭缓存
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        cache.clear();
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry<T> {
        final T value;
        final long timestamp;
        
        CacheEntry(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}