package com.hotech.events.service.impl;

import com.hotech.events.service.CacheService;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存服务实现类
 * 基于内存的高效缓存实现
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {
    
    @Autowired
    private SystemMonitoringService monitoringService;
    
    @Value("${app.deepseek.enhanced.cache-ttl:300000}")
    private long defaultTtl; // 默认5分钟TTL
    
    @Value("${app.deepseek.enhanced.enable-cache:true}")
    private boolean cacheEnabled;
    
    // 缓存存储
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
    
    // 统计信息
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    @Override
    public void put(String key, Object value, long ttl, TimeUnit timeUnit) {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(ttl);
            CacheItem item = new CacheItem(value, expirationTime);
            cache.put(key, item);
            
            log.debug("缓存项已存储: key={}, ttl={}ms", key, timeUnit.toMillis(ttl));
            
        } catch (Exception e) {
            log.error("存储缓存项失败: key={}", key, e);
            
            // 记录系统错误
            monitoringService.recordSystemError("CACHE_PUT", "CACHE_STORAGE_ERROR", 
                    e.getMessage(), getStackTrace(e));
        }
    }
    
    @Override
    public void put(String key, Object value) {
        put(key, value, defaultTtl, TimeUnit.MILLISECONDS);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> clazz) {
        if (!cacheEnabled) {
            return Optional.empty();
        }
        
        try {
            CacheItem item = cache.get(key);
            
            if (item == null) {
                missCount.incrementAndGet();
                log.debug("缓存未命中: key={}", key);
                return Optional.empty();
            }
            
            // 检查是否过期
            if (item.isExpired()) {
                cache.remove(key);
                evictionCount.incrementAndGet();
                missCount.incrementAndGet();
                log.debug("缓存项已过期: key={}", key);
                return Optional.empty();
            }
            
            hitCount.incrementAndGet();
            log.debug("缓存命中: key={}", key);
            
            return Optional.of((T) item.getValue());
            
        } catch (Exception e) {
            log.error("获取缓存项失败: key={}", key, e);
            
            // 记录系统错误
            monitoringService.recordSystemError("CACHE_GET", "CACHE_RETRIEVAL_ERROR", 
                    e.getMessage(), getStackTrace(e));
            
            return Optional.empty();
        }
    }
    
    @Override
    public void evict(String key) {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            CacheItem removed = cache.remove(key);
            if (removed != null) {
                evictionCount.incrementAndGet();
                log.debug("缓存项已删除: key={}", key);
            }
            
        } catch (Exception e) {
            log.error("删除缓存项失败: key={}", key, e);
            
            // 记录系统错误
            monitoringService.recordSystemError("CACHE_EVICT", "CACHE_EVICTION_ERROR", 
                    e.getMessage(), getStackTrace(e));
        }
    }
    
    @Override
    public void clear() {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            int size = cache.size();
            cache.clear();
            evictionCount.addAndGet(size);
            
            log.info("缓存已清空: evictedItems={}", size);
            
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            
            // 记录系统错误
            monitoringService.recordSystemError("CACHE_CLEAR", "CACHE_CLEAR_ERROR", 
                    e.getMessage(), getStackTrace(e));
        }
    }
    
    @Override
    public boolean exists(String key) {
        if (!cacheEnabled) {
            return false;
        }
        
        try {
            CacheItem item = cache.get(key);
            if (item == null) {
                return false;
            }
            
            // 检查是否过期
            if (item.isExpired()) {
                cache.remove(key);
                evictionCount.incrementAndGet();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查缓存项存在性失败: key={}", key, e);
            return false;
        }
    }
    
    @Override
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        
        stats.setHitCount(hits);
        stats.setMissCount(misses);
        stats.setEvictionCount(evictionCount.get());
        stats.setSize(cache.size());
        
        if (total > 0) {
            stats.setHitRate((double) hits / total);
            stats.setMissRate((double) misses / total);
        }
        
        return stats;
    }
    
    @Override
    public String generateEventQueryKey(String query) {
        return "event_query:" + generateHash(query);
    }
    
    @Override
    public String generateEventValidationKey(String eventId) {
        return "event_validation:" + eventId;
    }
    
    @Override
    public String generateTimelineKey(Long timelineId) {
        return "timeline:" + timelineId;
    }
    
    /**
     * 定时清理过期缓存项
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupExpiredItems() {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            int removedCount = 0;
            long currentTime = System.currentTimeMillis();
            
            for (String key : cache.keySet()) {
                CacheItem item = cache.get(key);
                if (item != null && item.getExpirationTime() <= currentTime) {
                    cache.remove(key);
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                evictionCount.addAndGet(removedCount);
                log.debug("清理过期缓存项: removedCount={}", removedCount);
            }
            
        } catch (Exception e) {
            log.error("清理过期缓存项失败", e);
        }
    }
    
    /**
     * 定时记录缓存统计信息
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void recordCacheMetrics() {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            CacheStats stats = getCacheStats();
            
            // 记录缓存性能指标
            monitoringService.recordPerformanceMetrics("CACHE_PERFORMANCE", 
                    0, // 响应时间不适用于缓存统计
                    getMemoryUsage(), 
                    getCpuUsage());
            
            log.info("缓存统计信息: hitRate={:.2f}%, size={}, hits={}, misses={}, evictions={}", 
                    stats.getHitRate() * 100, stats.getSize(), stats.getHitCount(), 
                    stats.getMissCount(), stats.getEvictionCount());
            
        } catch (Exception e) {
            log.error("记录缓存统计信息失败", e);
        }
    }
    
    /**
     * 生成字符串的哈希值
     */
    private String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("生成哈希值失败", e);
            return String.valueOf(input.hashCode());
        }
    }
    
    /**
     * 获取内存使用量
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) 
                    java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                    .getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Throwable throwable) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "无法获取堆栈跟踪: " + e.getMessage();
        }
    }
    
    /**
     * 缓存项内部类
     */
    private static class CacheItem {
        private final Object value;
        private final long expirationTime;
        private final LocalDateTime createdAt;
        
        public CacheItem(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
            this.createdAt = LocalDateTime.now();
        }
        
        public Object getValue() {
            return value;
        }
        
        public long getExpirationTime() {
            return expirationTime;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}