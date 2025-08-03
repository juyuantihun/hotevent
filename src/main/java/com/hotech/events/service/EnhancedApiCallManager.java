package com.hotech.events.service;

import com.hotech.events.config.DynamicApiConfigManager;
import com.hotech.events.dto.ApiHealthStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 增强的API调用管理器接口
 * 提供动态API选择和重试机制
 */
public interface EnhancedApiCallManager {
    
    /**
     * 动态选择最优API配置
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return API配置
     */
    DynamicApiConfigManager.ApiConfig selectOptimalApi(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 带重试机制的API调用
     * 
     * @param config API配置
     * @param prompt 提示词
     * @param maxRetries 最大重试次数
     * @return API响应内容
     */
    String callApiWithRetry(DynamicApiConfigManager.ApiConfig config, String prompt, int maxRetries);
    
    /**
     * 带重试机制的API调用（带请求ID）
     * 
     * @param config API配置
     * @param prompt 提示词
     * @param maxRetries 最大重试次数
     * @param requestId 请求ID
     * @return API响应内容
     */
    String callApiWithRetry(DynamicApiConfigManager.ApiConfig config, String prompt, int maxRetries, String requestId);
    
    /**
     * 检查API健康状态
     * 
     * @param config API配置
     * @return 健康状态
     */
    boolean isApiHealthy(DynamicApiConfigManager.ApiConfig config);
    
    /**
     * 获取API详细健康状态
     * 
     * @param config API配置
     * @return 详细健康状态
     */
    ApiHealthStatus getApiHealthStatus(DynamicApiConfigManager.ApiConfig config);
    
    /**
     * 带备用API的调用
     * 
     * @param prompt 提示词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return API响应内容
     */
    String callWithFallback(String prompt, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 带备用API的调用（带请求ID）
     * 
     * @param prompt 提示词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param requestId 请求ID
     * @return API响应内容
     */
    String callWithFallback(String prompt, LocalDateTime startTime, LocalDateTime endTime, String requestId);
    
    /**
     * 获取API调用统计
     * 
     * @return 统计信息
     */
    Map<String, Object> getApiCallStats();
    
    /**
     * 重置API健康状态缓存
     */
    void resetHealthCache();
    
    /**
     * 获取当前API选择策略
     * 
     * @return 策略描述
     */
    String getCurrentSelectionStrategy();
    
    /**
     * 调用API获取事件（支持大token返回）
     * 
     * @param config API配置
     * @param prompt 提示词
     * @param maxTokens 最大token数
     * @param timeSegment 时间段信息
     * @return API响应
     */
    String callApiWithLargeTokens(DynamicApiConfigManager.ApiConfig config, String prompt, int maxTokens, com.hotech.events.dto.TimeSegment timeSegment);
    
    /**
     * 检查API响应完整性
     * 
     * @param response API响应
     * @param expectedEventCount 预期事件数量
     * @return 是否完整
     */
    boolean isResponseComplete(String response, int expectedEventCount);
    
    /**
     * 批量并发API调用
     * 
     * @param segments 时间段列表
     * @param prompt 基础提示词
     * @return 响应列表
     */
    List<String> callApiBatch(List<com.hotech.events.dto.TimeSegment> segments, String prompt);
}