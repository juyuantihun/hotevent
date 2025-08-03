package com.hotech.events.service.impl;

import com.hotech.events.config.TimelineEnhancementConfig;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.service.GeographicCacheService;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 地理信息缓存服务实现
 * 使用多级缓存策略提高地理坐标查询性能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class GeographicCacheServiceImpl implements GeographicCacheService {

    @Autowired
    private TimelineEnhancementConfig config;

    @Autowired
    private TimelinePerformanceMonitoringService performanceMonitoringService;

    // 缓存存储
    private final Map<String, CacheEntry> coordinateCache = new ConcurrentHashMap<>();
    private final Map<String, GeographicCoordinate> permanentCache = new ConcurrentHashMap<>();

    // 缓存配置
    private long cacheTTL = 3600; // 默认1小时TTL
    private int maxCacheSize = 10000; // 最大缓存条目数

    // 统计信息
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);

    // 读写锁用于缓存操作
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    // 定时清理任务
    private ScheduledExecutorService cleanupExecutor;

    @PostConstruct
    public void init() {
        // 从配置中获取缓存设置
        if (config.getGeographic() != null && config.getGeographic().getCache() != null) {
            this.cacheTTL = config.getGeographic().getCache().getExpireAfterWrite();
            this.maxCacheSize = config.getGeographic().getCache().getMaxEntries();
        }

        // 预加载常用坐标
        preloadCommonCoordinates();

        // 启动定时清理任务
        startCleanupTask();

        log.info("地理信息缓存服务初始化完成，TTL: {}秒, 最大缓存大小: {}", cacheTTL, maxCacheSize);
    }

    @Override
    public Optional<GeographicCoordinate> getCoordinate(String locationName, String locationType) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return Optional.empty();
        }

        totalRequests.incrementAndGet();
        String cacheKey = generateCacheKey(locationName, locationType);

        cacheLock.readLock().lock();
        try {
            // 首先检查永久缓存
            GeographicCoordinate permanent = permanentCache.get(cacheKey);
            if (permanent != null) {
                cacheHits.incrementAndGet();
                performanceMonitoringService.recordCacheHit("GEOGRAPHIC_PERMANENT", true, cacheKey);
                log.debug("从永久缓存获取坐标: {}", locationName);
                return Optional.of(permanent);
            }

            // 检查临时缓存
            CacheEntry entry = coordinateCache.get(cacheKey);
            if (entry != null && !entry.isExpired()) {
                cacheHits.incrementAndGet();
                performanceMonitoringService.recordCacheHit("GEOGRAPHIC_TEMP", true, cacheKey);
                log.debug("从临时缓存获取坐标: {}", locationName);
                return Optional.of(entry.getCoordinate());
            }

            // 缓存未命中
            cacheMisses.incrementAndGet();
            performanceMonitoringService.recordCacheHit("GEOGRAPHIC", false, cacheKey);
            log.debug("缓存未命中: {}", locationName);
            return Optional.empty();

        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, GeographicCoordinate> getCoordinatesBatch(List<String> locationNames, String locationType) {
        Map<String, GeographicCoordinate> results = new HashMap<>();

        if (locationNames == null || locationNames.isEmpty()) {
            return results;
        }

        for (String locationName : locationNames) {
            Optional<GeographicCoordinate> coordinate = getCoordinate(locationName, locationType);
            coordinate.ifPresent(coord -> results.put(locationName, coord));
        }

        log.debug("批量获取坐标完成，请求数: {}, 命中数: {}", locationNames.size(), results.size());
        return results;
    }

    @Override
    public void cacheCoordinate(String locationName, String locationType, GeographicCoordinate coordinate) {
        if (locationName == null || coordinate == null) {
            return;
        }

        String cacheKey = generateCacheKey(locationName, locationType);

        cacheLock.writeLock().lock();
        try {
            // 检查缓存大小限制
            if (coordinateCache.size() >= maxCacheSize) {
                evictOldestEntries();
            }

            CacheEntry entry = new CacheEntry(coordinate, System.currentTimeMillis() + cacheTTL * 1000);
            coordinateCache.put(cacheKey, entry);

            log.debug("缓存地理坐标: {} -> ({}, {})", locationName, coordinate.getLatitude(), coordinate.getLongitude());

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public void cacheCoordinatesBatch(Map<String, GeographicCoordinate> coordinates, String locationType) {
        if (coordinates == null || coordinates.isEmpty()) {
            return;
        }

        cacheLock.writeLock().lock();
        try {
            for (Map.Entry<String, GeographicCoordinate> entry : coordinates.entrySet()) {
                String cacheKey = generateCacheKey(entry.getKey(), locationType);

                // 检查缓存大小限制
                if (coordinateCache.size() >= maxCacheSize) {
                    evictOldestEntries();
                }

                CacheEntry cacheEntry = new CacheEntry(entry.getValue(), System.currentTimeMillis() + cacheTTL * 1000);
                coordinateCache.put(cacheKey, cacheEntry);
            }

            log.debug("批量缓存地理坐标完成，数量: {}", coordinates.size());

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public void preloadCommonCoordinates() {
        log.info("开始预加载常用地理坐标");

        // 预加载主要国家首都坐标
        Map<String, GeographicCoordinate> commonCoordinates = new HashMap<>();

        commonCoordinates.put("中国", GeographicCoordinate.createCapital("中国", "北京", 39.9042, 116.4074));
        commonCoordinates.put("美国", GeographicCoordinate.createCapital("美国", "华盛顿", 38.9072, -77.0369));
        commonCoordinates.put("日本", GeographicCoordinate.createCapital("日本", "东京", 35.6762, 139.6503));
        commonCoordinates.put("英国", GeographicCoordinate.createCapital("英国", "伦敦", 51.5074, -0.1278));
        commonCoordinates.put("法国", GeographicCoordinate.createCapital("法国", "巴黎", 48.8566, 2.3522));
        commonCoordinates.put("德国", GeographicCoordinate.createCapital("德国", "柏林", 52.5200, 13.4050));
        commonCoordinates.put("俄罗斯", GeographicCoordinate.createCapital("俄罗斯", "莫斯科", 55.7558, 37.6176));
        commonCoordinates.put("韩国", GeographicCoordinate.createCapital("韩国", "首尔", 37.5665, 126.9780));

        // 预加载中国主要城市坐标
        commonCoordinates.put("上海", GeographicCoordinate.createCity("上海", 31.2304, 121.4737));
        commonCoordinates.put("广州", GeographicCoordinate.createCity("广州", 23.1291, 113.2644));
        commonCoordinates.put("深圳", GeographicCoordinate.createCity("深圳", 22.5431, 114.0579));

        // 将常用坐标加入永久缓存
        cacheLock.writeLock().lock();
        try {
            for (Map.Entry<String, GeographicCoordinate> entry : commonCoordinates.entrySet()) {
                String cacheKey = generateCacheKey(entry.getKey(), "COUNTRY_OR_CITY");
                permanentCache.put(cacheKey, entry.getValue());
            }
        } finally {
            cacheLock.writeLock().unlock();
        }

        log.info("预加载常用地理坐标完成，数量: {}", commonCoordinates.size());
    }

    @Override
    public void cleanupExpiredCache() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;

        cacheLock.writeLock().lock();
        try {
            Iterator<Map.Entry<String, CacheEntry>> iterator = coordinateCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = iterator.next();
                if (entry.getValue().isExpired(currentTime)) {
                    iterator.remove();
                    removedCount++;
                }
            }

            if (removedCount > 0) {
                cacheEvictions.addAndGet(removedCount);
                log.debug("清理过期缓存完成，移除数量: {}", removedCount);
            }

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        stats.put("cacheHits", hits);
        stats.put("cacheMisses", misses);
        stats.put("totalRequests", total);
        stats.put("hitRate", total > 0 ? (double) hits / total : 0.0);
        stats.put("missRate", total > 0 ? (double) misses / total : 0.0);
        stats.put("cacheEvictions", cacheEvictions.get());

        cacheLock.readLock().lock();
        try {
            stats.put("tempCacheSize", coordinateCache.size());
            stats.put("permanentCacheSize", permanentCache.size());
            stats.put("totalCacheSize", coordinateCache.size() + permanentCache.size());
        } finally {
            cacheLock.readLock().unlock();
        }

        stats.put("maxCacheSize", maxCacheSize);
        stats.put("cacheTTL", cacheTTL);

        return stats;
    }

    @Override
    public void clearAllCache() {
        cacheLock.writeLock().lock();
        try {
            coordinateCache.clear();
            // 不清理永久缓存
            log.info("临时缓存已清空");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public void setCacheTTL(long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL必须大于0");
        }

        this.cacheTTL = ttlSeconds;
        log.info("缓存TTL已更新为: {}秒", ttlSeconds);
    }

    @Override
    public int getCacheSize() {
        cacheLock.readLock().lock();
        try {
            return coordinateCache.size() + permanentCache.size();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public boolean containsCoordinate(String locationName, String locationType) {
        String cacheKey = generateCacheKey(locationName, locationType);

        cacheLock.readLock().lock();
        try {
            // 检查永久缓存
            if (permanentCache.containsKey(cacheKey)) {
                return true;
            }

            // 检查临时缓存
            CacheEntry entry = coordinateCache.get(cacheKey);
            return entry != null && !entry.isExpired();

        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public boolean removeCoordinate(String locationName, String locationType) {
        String cacheKey = generateCacheKey(locationName, locationType);

        cacheLock.writeLock().lock();
        try {
            // 不能移除永久缓存中的条目
            CacheEntry removed = coordinateCache.remove(cacheKey);
            boolean success = removed != null;

            if (success) {
                log.debug("从缓存中移除坐标: {}", locationName);
            }

            return success;

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long total = totalRequests.get();
        return total > 0 ? (double) hits / total : 0.0;
    }

    @Override
    public void resetCacheStatistics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        totalRequests.set(0);

        log.info("缓存统计信息已重置");
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String locationName, String locationType) {
        return String.format("%s_%s",
                locationName.trim().toLowerCase(),
                locationType != null ? locationType.toUpperCase() : "UNKNOWN");
    }

    /**
     * 淘汰最旧的缓存条目
     */
    private void evictOldestEntries() {
        int evictCount = maxCacheSize / 10; // 淘汰10%的条目

        List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(coordinateCache.entrySet());
        entries.sort(Comparator.comparing(e -> e.getValue().getExpirationTime()));

        for (int i = 0; i < Math.min(evictCount, entries.size()); i++) {
            coordinateCache.remove(entries.get(i).getKey());
            cacheEvictions.incrementAndGet();
        }

        log.debug("淘汰最旧缓存条目，数量: {}", evictCount);
    }

    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "geographic-cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        // 每5分钟清理一次过期缓存
        cleanupExecutor.scheduleWithFixedDelay(this::cleanupExpiredCache, 5, 5, TimeUnit.MINUTES);

        log.debug("地理信息缓存清理任务已启动");
    }

    /**
     * 缓存条目类
     */
    private static class CacheEntry {
        private final GeographicCoordinate coordinate;
        private final long expirationTime;

        public CacheEntry(GeographicCoordinate coordinate, long expirationTime) {
            this.coordinate = coordinate;
            this.expirationTime = expirationTime;
        }

        public GeographicCoordinate getCoordinate() {
            return coordinate;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expirationTime;
        }
    }
}