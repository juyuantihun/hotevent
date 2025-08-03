package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimelineGenerateRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 容错服务接口
 * 提供重试、降级、熔断等容错机制
 */
public interface FaultToleranceService {
    
    /**
     * 带重试的API调用
     * 
     * @param operation 操作名称
     * @param supplier 执行的操作
     * @param fallback 降级操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    <T> T executeWithRetry(String operation, Supplier<T> supplier, Supplier<T> fallback);
    
    /**
     * 带熔断的API调用
     * 
     * @param operation 操作名称
     * @param supplier 执行的操作
     * @param fallback 降级操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    <T> T executeWithCircuitBreaker(String operation, Supplier<T> supplier, Supplier<T> fallback);
    
    /**
     * 异步执行带容错的操作
     * 
     * @param operation 操作名称
     * @param supplier 执行的操作
     * @param fallback 降级操作
     * @param <T> 返回类型
     * @return 异步结果
     */
    <T> CompletableFuture<T> executeAsync(String operation, Supplier<T> supplier, Supplier<T> fallback);
    
    /**
     * 获取熔断器状态
     * 
     * @param operation 操作名称
     * @return 熔断器状态
     */
    String getCircuitBreakerState(String operation);
    
    /**
     * 重置熔断器
     * 
     * @param operation 操作名称
     */
    void resetCircuitBreaker(String operation);
    
    /**
     * 获取操作统计信息
     * 
     * @param operation 操作名称
     * @return 统计信息
     */
    OperationStats getOperationStats(String operation);
    
    /**
     * 操作统计信息
     */
    class OperationStats {
        private long totalCalls;
        private long successfulCalls;
        private long failedCalls;
        private long retryCount;
        private double averageResponseTime;
        private String circuitBreakerState;
        
        // Getters and Setters
        public long getTotalCalls() { return totalCalls; }
        public void setTotalCalls(long totalCalls) { this.totalCalls = totalCalls; }
        
        public long getSuccessfulCalls() { return successfulCalls; }
        public void setSuccessfulCalls(long successfulCalls) { this.successfulCalls = successfulCalls; }
        
        public long getFailedCalls() { return failedCalls; }
        public void setFailedCalls(long failedCalls) { this.failedCalls = failedCalls; }
        
        public long getRetryCount() { return retryCount; }
        public void setRetryCount(long retryCount) { this.retryCount = retryCount; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public String getCircuitBreakerState() { return circuitBreakerState; }
        public void setCircuitBreakerState(String circuitBreakerState) { this.circuitBreakerState = circuitBreakerState; }
    }
}