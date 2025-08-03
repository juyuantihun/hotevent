package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimelineGenerateRequest;
import com.hotech.events.service.FallbackStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 降级策略服务实现
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class FallbackStrategyServiceImpl implements FallbackStrategyService {
    
    // 统计信息
    private final AtomicLong singleApiCallCount = new AtomicLong(0);
    private final AtomicLong cacheCallCount = new AtomicLong(0);
    private final AtomicLong localDatabaseCallCount = new AtomicLong(0);
    private final AtomicLong staticDataCallCount = new AtomicLong(0);
    
    // 模拟缓存
    private final Map<String, List<EventData>> cache = new HashMap<>();
    
    @Override
    public List<EventData> fallbackToSingleApiCall(TimelineGenerateRequest request) {
        singleApiCallCount.incrementAndGet();
        log.info("执行单API调用降级策略，关键词: {}", request.getKeyword());
        
        try {
            // 模拟单个API调用
            List<EventData> events = new ArrayList<>();
            
            EventData event = new EventData();
            event.setId("fallback-single-" + System.currentTimeMillis());
            event.setTitle("关于 \"" + request.getKeyword() + "\" 的降级事件");
            event.setDescription("通过单API调用获取的事件信息（降级模式）");
            event.setEventTime(request.getStartTime());
            event.setSource("单API调用降级");
            event.setLatitude(39.9042);
            event.setLongitude(116.4074);
            event.setLocation("北京");
            
            events.add(event);
            
            log.info("单API调用降级策略执行成功，返回{}个事件", events.size());
            return events;
            
        } catch (Exception e) {
            log.error("单API调用降级策略执行失败", e);
            throw new RuntimeException("单API调用降级策略失败", e);
        }
    }
    
    @Override
    public List<EventData> fallbackToCache(TimelineGenerateRequest request) {
        cacheCallCount.incrementAndGet();
        log.info("执行缓存降级策略，关键词: {}", request.getKeyword());
        
        try {
            String cacheKey = generateCacheKey(request);
            List<EventData> cachedEvents = cache.get(cacheKey);
            
            if (cachedEvents != null && !cachedEvents.isEmpty()) {
                log.info("从缓存中找到{}个事件", cachedEvents.size());
                return new ArrayList<>(cachedEvents);
            }
            
            // 如果缓存中没有数据，创建一些基本的缓存数据
            List<EventData> events = createBasicCacheData(request);
            cache.put(cacheKey, events);
            
            log.info("缓存降级策略执行成功，返回{}个事件", events.size());
            return events;
            
        } catch (Exception e) {
            log.error("缓存降级策略执行失败", e);
            throw new RuntimeException("缓存降级策略失败", e);
        }
    }
    
    @Override
    public List<EventData> fallbackToLocalDatabase(TimelineGenerateRequest request) {
        localDatabaseCallCount.incrementAndGet();
        log.info("执行本地数据库降级策略，关键词: {}", request.getKeyword());
        
        try {
            // 模拟从本地数据库查询
            List<EventData> events = new ArrayList<>();
            
            EventData event = new EventData();
            event.setId("fallback-db-" + System.currentTimeMillis());
            event.setTitle("本地数据库中关于 \"" + request.getKeyword() + "\" 的事件");
            event.setDescription("从本地数据库获取的历史事件信息（降级模式）");
            event.setEventTime(request.getStartTime().plusHours(1));
            event.setSource("本地数据库降级");
            event.setLatitude(31.2304);
            event.setLongitude(121.4737);
            event.setLocation("上海");
            
            events.add(event);
            
            log.info("本地数据库降级策略执行成功，返回{}个事件", events.size());
            return events;
            
        } catch (Exception e) {
            log.error("本地数据库降级策略执行失败", e);
            throw new RuntimeException("本地数据库降级策略失败", e);
        }
    }
    
    @Override
    public List<EventData> fallbackToStaticData(TimelineGenerateRequest request) {
        staticDataCallCount.incrementAndGet();
        log.info("执行静态数据降级策略，关键词: {}", request.getKeyword());
        
        try {
            List<EventData> events = new ArrayList<>();
            
            // 创建静态事件数据
            EventData event1 = new EventData();
            event1.setId("static-1-" + System.currentTimeMillis());
            event1.setTitle("静态事件：系统维护通知");
            event1.setDescription("系统正在维护中，显示静态数据（最终降级模式）");
            event1.setEventTime(request.getStartTime());
            event1.setSource("静态数据降级");
            event1.setLatitude(39.9042);
            event1.setLongitude(116.4074);
            event1.setLocation("系统默认位置");
            
            EventData event2 = new EventData();
            event2.setId("static-2-" + System.currentTimeMillis());
            event2.setTitle("静态事件：服务恢复预期");
            event2.setDescription("服务预计将在短时间内恢复正常，请稍后重试");
            event2.setEventTime(request.getStartTime().plusHours(2));
            event2.setSource("静态数据降级");
            event2.setLatitude(39.9042);
            event2.setLongitude(116.4074);
            event2.setLocation("系统默认位置");
            
            events.add(event1);
            events.add(event2);
            
            log.info("静态数据降级策略执行成功，返回{}个事件", events.size());
            return events;
            
        } catch (Exception e) {
            log.error("静态数据降级策略执行失败", e);
            // 即使静态数据失败，也要返回一个空列表而不是抛出异常
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getStrategyUsageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("singleApiCallCount", singleApiCallCount.get());
        stats.put("cacheCallCount", cacheCallCount.get());
        stats.put("localDatabaseCallCount", localDatabaseCallCount.get());
        stats.put("staticDataCallCount", staticDataCallCount.get());
        
        long totalCalls = singleApiCallCount.get() + cacheCallCount.get() + 
                         localDatabaseCallCount.get() + staticDataCallCount.get();
        stats.put("totalFallbackCalls", totalCalls);
        
        if (totalCalls > 0) {
            stats.put("singleApiCallRate", (double) singleApiCallCount.get() / totalCalls);
            stats.put("cacheCallRate", (double) cacheCallCount.get() / totalCalls);
            stats.put("localDatabaseCallRate", (double) localDatabaseCallCount.get() / totalCalls);
            stats.put("staticDataCallRate", (double) staticDataCallCount.get() / totalCalls);
        } else {
            stats.put("singleApiCallRate", 0.0);
            stats.put("cacheCallRate", 0.0);
            stats.put("localDatabaseCallRate", 0.0);
            stats.put("staticDataCallRate", 0.0);
        }
        
        stats.put("cacheSize", cache.size());
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
    
    @Override
    public void resetStatistics() {
        singleApiCallCount.set(0);
        cacheCallCount.set(0);
        localDatabaseCallCount.set(0);
        staticDataCallCount.set(0);
        log.info("降级策略统计信息已重置");
    }
    
    @Override
    public boolean isCacheAvailable() {
        // 简单的缓存可用性检查
        return true; // 内存缓存总是可用的
    }
    
    @Override
    public boolean isLocalDatabaseAvailable() {
        // 模拟数据库可用性检查
        try {
            // 这里应该实际检查数据库连接
            return true;
        } catch (Exception e) {
            log.warn("本地数据库不可用", e);
            return false;
        }
    }
    
    @Override
    public String getRecommendedStrategy(TimelineGenerateRequest request, Exception lastError) {
        if (lastError == null) {
            return "singleApiCall";
        }
        
        String errorMessage = lastError.getMessage().toLowerCase();
        
        // 根据错误类型推荐策略
        if (errorMessage.contains("timeout") || errorMessage.contains("connection")) {
            if (isCacheAvailable()) {
                return "cache";
            } else if (isLocalDatabaseAvailable()) {
                return "localDatabase";
            } else {
                return "staticData";
            }
        } else if (errorMessage.contains("rate limit") || errorMessage.contains("quota")) {
            return "cache";
        } else if (errorMessage.contains("api") || errorMessage.contains("service")) {
            if (isLocalDatabaseAvailable()) {
                return "localDatabase";
            } else {
                return "cache";
            }
        } else {
            // 默认推荐顺序
            if (isCacheAvailable()) {
                return "cache";
            } else if (isLocalDatabaseAvailable()) {
                return "localDatabase";
            } else {
                return "staticData";
            }
        }
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(TimelineGenerateRequest request) {
        return String.format("timeline_%s_%s_%s", 
                request.getKeyword(),
                request.getStartTime().toLocalDate(),
                request.getEndTime().toLocalDate());
    }
    
    /**
     * 创建基本的缓存数据
     */
    private List<EventData> createBasicCacheData(TimelineGenerateRequest request) {
        List<EventData> events = new ArrayList<>();
        
        EventData event = new EventData();
        event.setId("cache-" + System.currentTimeMillis());
        event.setTitle("缓存中关于 \"" + request.getKeyword() + "\" 的事件");
        event.setDescription("从缓存系统获取的事件信息（降级模式）");
        event.setEventTime(request.getStartTime().plusMinutes(30));
        event.setSource("缓存降级");
        event.setLatitude(22.3193);
        event.setLongitude(114.1694);
        event.setLocation("深圳");
        
        events.add(event);
        
        return events;
    }
}