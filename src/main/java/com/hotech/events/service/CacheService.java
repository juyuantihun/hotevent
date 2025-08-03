package com.hotech.events.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供高效的缓存管理功能
 */
public interface CacheService {
    
    /**
     * 存储缓存项
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 生存时间
     * @param timeUnit 时间单位
     */
    void put(String key, Object value, long ttl, TimeUnit timeUnit);
    
    /**
     * 存储缓存项（使用默认TTL）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    void put(String key, Object value);
    
    /**
     * 获取缓存项
     * 
     * @param key 缓存键
     * @param clazz 值类型
     * @return 缓存值
     */
    <T> Optional<T> get(String key, Class<T> clazz);
    
    /**
     * 删除缓存项
     * 
     * @param key 缓存键
     */
    void evict(String key);
    
    /**
     * 清空所有缓存
     */
    void clear();
    
    /**
     * 检查缓存项是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    CacheStats getCacheStats();
    
    /**
     * 生成事件查询缓存键
     * 
     * @param query 查询参数
     * @return 缓存键
     */
    String generateEventQueryKey(String query);
    
    /**
     * 生成事件验证缓存键
     * 
     * @param eventId 事件ID
     * @return 缓存键
     */
    String generateEventValidationKey(String eventId);
    
    /**
     * 生成时间线缓存键
     * 
     * @param timelineId 时间线ID
     * @return 缓存键
     */
    String generateTimelineKey(Long timelineId);
    
    /**
     * 缓存统计信息
     */
    class CacheStats {
        private long hitCount;
        private long missCount;
        private long evictionCount;
        private long size;
        private double hitRate;
        private double missRate;
        
        // Getters and Setters
        public long getHitCount() { return hitCount; }
        public void setHitCount(long hitCount) { this.hitCount = hitCount; }
        
        public long getMissCount() { return missCount; }
        public void setMissCount(long missCount) { this.missCount = missCount; }
        
        public long getEvictionCount() { return evictionCount; }
        public void setEvictionCount(long evictionCount) { this.evictionCount = evictionCount; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }
        
        public double getMissRate() { return missRate; }
        public void setMissRate(double missRate) { this.missRate = missRate; }
    }
}