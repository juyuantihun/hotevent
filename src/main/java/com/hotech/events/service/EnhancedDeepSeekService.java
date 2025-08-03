package com.hotech.events.service;

import com.hotech.events.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 增强的DeepSeek服务接口
 * 扩展原有DeepSeekService，添加动态提示词、缓存、限流、批量处理和监控功能
 */
public interface EnhancedDeepSeekService extends DeepSeekService {
    
    /**
     * 使用动态提示词获取事件
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> fetchEventsWithDynamicPrompt(TimelineGenerateRequest request);
    
    /**
     * 验证事件真实性
     * 
     * @param events 事件列表
     * @return 验证结果列表
     */
    List<EventValidationResult> validateEvents(List<EventData> events);
    
    /**
     * 批量处理事件检索
     * 
     * @param tasks 事件获取任务列表
     * @return 异步事件数据列表
     */
    CompletableFuture<List<EventData>> fetchEventsBatch(List<EventFetchTask> tasks);
    
    /**
     * 异步获取事件
     * 
     * @param request 时间线生成请求
     * @return 异步事件数据列表
     */
    CompletableFuture<List<EventData>> fetchEventsAsync(TimelineGenerateRequest request);
    
    /**
     * 获取API调用统计
     * 
     * @return API使用统计
     */
    ApiUsageStats getUsageStats();
    
    /**
     * 清理缓存
     */
    void clearCache();
    
    /**
     * 重置限流器
     */
    void resetRateLimit();
    
    /**
     * 检查API健康状态
     * 
     * @return 健康状态信息
     */
    ApiHealthStatus checkApiHealth();
    
    /**
     * 获取缓存统计
     * 
     * @return 缓存统计信息
     */
    CacheStats getCacheStats();
}