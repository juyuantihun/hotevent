package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimeSegment;
import com.hotech.events.model.TimelineGenerateRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 并发API调用管理器接口
 * 用于优化批量API调用的并发性能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface ConcurrentApiCallManager {
    
    /**
     * 并发执行多个时间段的API调用
     * 
     * @param segments 时间段列表
     * @param request 时间线生成请求
     * @return 合并后的事件列表
     */
    CompletableFuture<List<EventData>> executeConcurrentApiCalls(List<TimeSegment> segments, TimelineGenerateRequest request);
    
    /**
     * 异步执行单个时间段的API调用
     * 
     * @param segment 时间段
     * @param request 时间线生成请求
     * @return 事件列表的Future
     */
    CompletableFuture<List<EventData>> executeAsyncApiCall(TimeSegment segment, TimelineGenerateRequest request);
    
    /**
     * 批量处理API调用结果
     * 
     * @param futures API调用Future列表
     * @return 合并后的事件列表
     */
    CompletableFuture<List<EventData>> processBatchResults(List<CompletableFuture<List<EventData>>> futures);
    
    /**
     * 设置并发度
     * 
     * @param concurrency 并发线程数
     */
    void setConcurrency(int concurrency);
    
    /**
     * 获取当前并发度
     * 
     * @return 并发线程数
     */
    int getConcurrency();
    
    /**
     * 设置超时时间
     * 
     * @param timeoutMs 超时时间（毫秒）
     */
    void setTimeout(long timeoutMs);
    
    /**
     * 获取API调用统计信息
     * 
     * @return 统计信息
     */
    java.util.Map<String, Object> getApiCallStatistics();
    
    /**
     * 重置统计信息
     */
    void resetStatistics();
    
    /**
     * 关闭并发管理器
     */
    void shutdown();
}