package com.hotech.events.service.impl;

import com.hotech.events.service.FaultToleranceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 容错服务实现类
 * 提供重试、降级、熔断等容错机制
 */
@Slf4j
@Service
public class FaultToleranceServiceImpl implements FaultToleranceService {
    
    @Value("${app.deepseek.enhanced.max-retries:3}")
    private int maxRetries;
    
    @Value("${app.deepseek.enhanced.retry-interval:1000}")
    private long retryInterval;
    
    @Value("${app.fault-tolerance.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;
    
    @Value("${app.fault-tolerance.circuit-breaker.timeout:60000}")
    private long circuitBreakerTimeout;
    
    @Value("${app.fault-tolerance.circuit-breaker.half-open-max-calls:3}")
    private int halfOpenMaxCalls;
    
    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // 熔断器状态管理
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    
    // 操作统计
    private final ConcurrentHashMap<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    
    @Override
    public <T> T executeWithRetry(String operation, Supplier<T> supplier, Supplier<T> fallback) {
        OperationMetrics metrics = getOrCreateMetrics(operation);
        metrics.totalCalls.incrementAndGet();
        
        long startTime = System.currentTimeMillis();
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    metrics.retryCount.incrementAndGet();
                    log.info("重试操作: operation={}, attempt={}/{}", operation, attempt, maxRetries);
                    Thread.sleep(retryInterval * attempt); // 指数退避
                }
                
                T result = supplier.get();
                metrics.successfulCalls.incrementAndGet();
                
                long responseTime = System.currentTimeMillis() - startTime;
                updateAverageResponseTime(metrics, responseTime);
                
                log.debug("操作成功: operation={}, attempt={}, responseTime={}ms", 
                         operation, attempt, responseTime);
                return result;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("操作被中断: operation={}", operation);
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败: operation={}, attempt={}/{}, error={}", 
                        operation, attempt, maxRetries, e.getMessage());
                
                if (attempt == maxRetries) {
                    break;
                }
            }
        }
        
        // 所有重试都失败，执行降级
        metrics.failedCalls.incrementAndGet();
        log.error("操作最终失败，执行降级: operation={}, error={}", operation, 
                 lastException != null ? lastException.getMessage() : "未知错误");
        
        try {
            T fallbackResult = fallback.get();
            log.info("降级操作成功: operation={}", operation);
            return fallbackResult;
        } catch (Exception e) {
            log.error("降级操作也失败: operation={}", operation, e);
            throw new RuntimeException("操作和降级都失败: " + operation, e);
        }
    }
    
    @Override
    public <T> T executeWithCircuitBreaker(String operation, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreakerState circuitBreaker = getOrCreateCircuitBreaker(operation);
        OperationMetrics metrics = getOrCreateMetrics(operation);
        
        // 检查熔断器状态
        if (circuitBreaker.state == CircuitState.OPEN) {
            if (System.currentTimeMillis() - circuitBreaker.lastFailureTime > circuitBreakerTimeout) {
                // 转换到半开状态
                circuitBreaker.state = CircuitState.HALF_OPEN;
                circuitBreaker.halfOpenCalls = 0;
                log.info("熔断器转换到半开状态: operation={}", operation);
            } else {
                // 熔断器仍然开启，直接执行降级
                log.warn("熔断器开启，执行降级: operation={}", operation);
                return fallback.get();
            }
        }
        
        if (circuitBreaker.state == CircuitState.HALF_OPEN && 
            circuitBreaker.halfOpenCalls >= halfOpenMaxCalls) {
            // 半开状态下达到最大调用次数，执行降级
            log.warn("半开状态达到最大调用次数，执行降级: operation={}", operation);
            return fallback.get();
        }
        
        metrics.totalCalls.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        try {
            if (circuitBreaker.state == CircuitState.HALF_OPEN) {
                circuitBreaker.halfOpenCalls++;
            }
            
            T result = supplier.get();
            
            // 操作成功
            metrics.successfulCalls.incrementAndGet();
            circuitBreaker.consecutiveFailures = 0;
            
            if (circuitBreaker.state == CircuitState.HALF_OPEN) {
                // 半开状态下成功，转换到关闭状态
                circuitBreaker.state = CircuitState.CLOSED;
                log.info("熔断器转换到关闭状态: operation={}", operation);
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            updateAverageResponseTime(metrics, responseTime);
            
            return result;
            
        } catch (Exception e) {
            // 操作失败
            metrics.failedCalls.incrementAndGet();
            circuitBreaker.consecutiveFailures++;
            circuitBreaker.lastFailureTime = System.currentTimeMillis();
            
            if (circuitBreaker.consecutiveFailures >= failureThreshold) {
                // 达到失败阈值，打开熔断器
                circuitBreaker.state = CircuitState.OPEN;
                log.error("熔断器打开: operation={}, consecutiveFailures={}", 
                         operation, circuitBreaker.consecutiveFailures);
            }
            
            log.error("熔断器保护下的操作失败，执行降级: operation={}", operation, e);
            return fallback.get();
        }
    }
    
    @Override
    public <T> CompletableFuture<T> executeAsync(String operation, Supplier<T> supplier, Supplier<T> fallback) {
        return CompletableFuture.supplyAsync(() -> 
            executeWithCircuitBreaker(operation, supplier, fallback), executorService);
    }
    
    @Override
    public String getCircuitBreakerState(String operation) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(operation);
        return circuitBreaker != null ? circuitBreaker.state.name() : CircuitState.CLOSED.name();
    }
    
    @Override
    public void resetCircuitBreaker(String operation) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(operation);
        if (circuitBreaker != null) {
            circuitBreaker.state = CircuitState.CLOSED;
            circuitBreaker.consecutiveFailures = 0;
            circuitBreaker.halfOpenCalls = 0;
            log.info("熔断器已重置: operation={}", operation);
        }
    }
    
    @Override
    public OperationStats getOperationStats(String operation) {
        OperationMetrics metrics = operationMetrics.get(operation);
        CircuitBreakerState circuitBreaker = circuitBreakers.get(operation);
        
        OperationStats stats = new OperationStats();
        if (metrics != null) {
            stats.setTotalCalls(metrics.totalCalls.get());
            stats.setSuccessfulCalls(metrics.successfulCalls.get());
            stats.setFailedCalls(metrics.failedCalls.get());
            stats.setRetryCount(metrics.retryCount.get());
            stats.setAverageResponseTime(metrics.averageResponseTime);
        }
        
        if (circuitBreaker != null) {
            stats.setCircuitBreakerState(circuitBreaker.state.name());
        } else {
            stats.setCircuitBreakerState(CircuitState.CLOSED.name());
        }
        
        return stats;
    }
    
    /**
     * 获取或创建熔断器状态
     */
    private CircuitBreakerState getOrCreateCircuitBreaker(String operation) {
        return circuitBreakers.computeIfAbsent(operation, k -> new CircuitBreakerState());
    }
    
    /**
     * 获取或创建操作指标
     */
    private OperationMetrics getOrCreateMetrics(String operation) {
        return operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics());
    }
    
    /**
     * 更新平均响应时间
     */
    private void updateAverageResponseTime(OperationMetrics metrics, long responseTime) {
        synchronized (metrics) {
            long totalCalls = metrics.totalCalls.get();
            if (totalCalls == 1) {
                metrics.averageResponseTime = responseTime;
            } else {
                metrics.averageResponseTime = (metrics.averageResponseTime * (totalCalls - 1) + responseTime) / totalCalls;
            }
        }
    }
    
    /**
     * 熔断器状态
     */
    private static class CircuitBreakerState {
        volatile CircuitState state = CircuitState.CLOSED;
        volatile int consecutiveFailures = 0;
        volatile long lastFailureTime = 0;
        volatile int halfOpenCalls = 0;
    }
    
    /**
     * 操作指标
     */
    private static class OperationMetrics {
        final AtomicLong totalCalls = new AtomicLong(0);
        final AtomicLong successfulCalls = new AtomicLong(0);
        final AtomicLong failedCalls = new AtomicLong(0);
        final AtomicLong retryCount = new AtomicLong(0);
        volatile double averageResponseTime = 0.0;
    }
    
    /**
     * 熔断器状态枚举
     */
    private enum CircuitState {
        CLOSED,    // 关闭状态，正常执行
        OPEN,      // 开启状态，直接降级
        HALF_OPEN  // 半开状态，尝试恢复
    }
}