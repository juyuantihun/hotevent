package com.hotech.events.service;

import com.hotech.events.exception.TimelineEnhancementException;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 错误处理服务接口
 * 提供统一的错误处理、重试和降级机制
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface ErrorHandlingService {
    
    /**
     * 执行带重试和降级的操作
     * 
     * @param primaryOperation 主要操作
     * @param fallbackOperation 降级操作
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return 操作结果
     */
    <T> T executeWithRetryAndFallback(Supplier<T> primaryOperation, Supplier<T> fallbackOperation, int maxRetries);
    
    /**
     * 执行带降级的操作
     * 
     * @param primaryOperation 主要操作
     * @param fallbackOperation 降级操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    <T> T executeWithFallback(Supplier<T> primaryOperation, Supplier<T> fallbackOperation);
    
    /**
     * 记录错误信息
     * 
     * @param errorType 错误类型
     * @param errorMessage 错误消息
     * @param exception 异常对象
     */
    void logError(String errorType, String errorMessage, Exception exception);
    
    /**
     * 创建用户友好的错误消息
     * 
     * @param exception 时间线增强异常
     * @return 用户友好的错误消息
     */
    String createUserFriendlyMessage(TimelineEnhancementException exception);
    
    /**
     * 获取错误统计信息
     * 
     * @return 错误统计信息
     */
    Map<String, Object> getErrorStatistics();
    
    /**
     * 重置错误统计信息
     */
    void resetErrorStatistics();
    
    /**
     * 检查是否应该进行重试
     * 
     * @param exception 异常对象
     * @param retryCount 当前重试次数
     * @param maxRetries 最大重试次数
     * @return 是否应该重试
     */
    boolean shouldRetry(Exception exception, int retryCount, int maxRetries);
    
    /**
     * 计算重试延迟时间（毫秒）
     * 
     * @param retryCount 重试次数
     * @return 延迟时间（毫秒）
     */
    long calculateRetryDelay(int retryCount);
    
    /**
     * 检查是否应该触发熔断器
     * 
     * @param errorType 错误类型
     * @return 是否应该触发熔断器
     */
    boolean shouldTriggerCircuitBreaker(String errorType);
    
    /**
     * 重置熔断器状态
     * 
     * @param errorType 错误类型
     */
    void resetCircuitBreaker(String errorType);
    
    /**
     * 获取熔断器状态
     * 
     * @param errorType 错误类型
     * @return 熔断器状态信息
     */
    Map<String, Object> getCircuitBreakerStatus(String errorType);
    
    /**
     * 记录熔断器触发事件
     * 
     * @param errorType 错误类型
     * @param reason 触发原因
     */
    void recordCircuitBreakerTrigger(String errorType, String reason);
}