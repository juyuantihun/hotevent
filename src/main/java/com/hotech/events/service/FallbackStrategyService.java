package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimelineGenerateRequest;

import java.util.List;
import java.util.Map;

/**
 * 降级策略服务接口
 * 提供各种降级策略来处理时间线生成失败的情况
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface FallbackStrategyService {
    
    /**
     * 降级到单个API调用
     * 当批量API调用失败时，尝试使用单个API调用
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> fallbackToSingleApiCall(TimelineGenerateRequest request);
    
    /**
     * 降级到缓存数据
     * 当API调用完全失败时，尝试从缓存中获取相关数据
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> fallbackToCache(TimelineGenerateRequest request);
    
    /**
     * 降级到本地数据库
     * 当外部API不可用时，从本地数据库获取历史数据
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> fallbackToLocalDatabase(TimelineGenerateRequest request);
    
    /**
     * 降级到静态数据
     * 最后的降级策略，返回预定义的静态数据
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> fallbackToStaticData(TimelineGenerateRequest request);
    
    /**
     * 获取策略使用统计
     * 
     * @return 策略使用统计信息
     */
    Map<String, Object> getStrategyUsageStatistics();
    
    /**
     * 重置统计信息
     */
    void resetStatistics();
    
    /**
     * 检查缓存可用性
     * 
     * @return 缓存是否可用
     */
    boolean isCacheAvailable();
    
    /**
     * 检查本地数据库可用性
     * 
     * @return 本地数据库是否可用
     */
    boolean isLocalDatabaseAvailable();
    
    /**
     * 获取推荐的降级策略
     * 根据当前系统状态推荐最适合的降级策略
     * 
     * @param request 时间线生成请求
     * @param lastError 上次的错误信息
     * @return 推荐的策略名称
     */
    String getRecommendedStrategy(TimelineGenerateRequest request, Exception lastError);
}