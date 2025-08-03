package com.hotech.events.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流器工具类
 */
@Slf4j
public class RateLimiter {
    
    private final int maxRequests;
    private final long timeWindowMs;
    private final ConcurrentHashMap<String, RequestWindow> windows = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequests, long timeWindowMs) {
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeWindowMs;
    }
    
    /**
     * 检查是否允许请求
     * 
     * @param key 限流键（如用户ID、IP等）
     * @return 是否允许请求
     */
    public boolean allowRequest(String key) {
        long currentTime = System.currentTimeMillis();
        RequestWindow window = windows.computeIfAbsent(key, k -> new RequestWindow());
        
        synchronized (window) {
            // 检查时间窗口是否需要重置
            if (currentTime - window.windowStart >= timeWindowMs) {
                window.windowStart = currentTime;
                window.requestCount.set(0);
            }
            
            // 检查是否超过限制
            if (window.requestCount.get() >= maxRequests) {
                log.warn("请求被限流: key={}, count={}, limit={}", key, window.requestCount.get(), maxRequests);
                return false;
            }
            
            // 增加请求计数
            window.requestCount.incrementAndGet();
            return true;
        }
    }
    
    /**
     * 获取剩余请求数
     * 
     * @param key 限流键
     * @return 剩余请求数
     */
    public int getRemainingRequests(String key) {
        RequestWindow window = windows.get(key);
        if (window == null) {
            return maxRequests;
        }
        
        long currentTime = System.currentTimeMillis();
        synchronized (window) {
            if (currentTime - window.windowStart >= timeWindowMs) {
                return maxRequests;
            }
            return Math.max(0, maxRequests - window.requestCount.get());
        }
    }
    
    /**
     * 清理过期的时间窗口
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> {
            RequestWindow window = entry.getValue();
            return currentTime - window.windowStart >= timeWindowMs * 2;
        });
    }
    
    /**
     * 请求窗口
     */
    private static class RequestWindow {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger requestCount = new AtomicInteger(0);
    }
}