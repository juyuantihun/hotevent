package com.hotech.events.service;

import java.util.Map;

/**
 * 时间线性能监控服务接口
 * 提供时间线生成过程的性能监控和统计功能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface TimelinePerformanceMonitoringService {
    
    /**
     * 开始分段监控
     * 
     * @param expectedSegments 预期的分段数量
     * @return 监控会话ID
     */
    String startSegmentationMonitoring(int expectedSegments);
    
    /**
     * 结束分段监控
     * 
     * @param sessionId 监控会话ID
     * @param success 是否成功
     * @param actualSegments 实际处理的分段数量
     */
    void endSegmentationMonitoring(String sessionId, boolean success, int actualSegments);
    
    /**
     * 记录分段处理时间
     * 
     * @param sessionId 监控会话ID
     * @param segmentIndex 分段索引
     * @param processingTimeMs 处理时间（毫秒）
     */
    void recordSegmentProcessingTime(String sessionId, int segmentIndex, long processingTimeMs);
    
    /**
     * 记录API调用性能
     * 
     * @param apiName API名称
     * @param responseTimeMs 响应时间（毫秒）
     * @param success 是否成功
     */
    void recordApiCallPerformance(String apiName, long responseTimeMs, boolean success);
    
    /**
     * 获取性能统计信息
     * 
     * @return 性能统计信息
     */
    Map<String, Object> getPerformanceStatistics();
    
    /**
     * 获取API调用统计信息
     * 
     * @return API调用统计信息
     */
    Map<String, Object> getApiCallStatistics();
    
    /**
     * 重置性能统计信息
     */
    void resetStatistics();
    
    /**
     * 获取当前活跃的监控会话数量
     * 
     * @return 活跃会话数量
     */
    int getActiveSessionCount();
    
    /**
     * 清理过期的监控会话
     */
    void cleanupExpiredSessions();
    
    /**
     * 检查性能阈值
     * 
     * @return 阈值检查结果
     */
    Map<String, Object> checkPerformanceThresholds();
    
    /**
     * 设置性能阈值
     * 
     * @param thresholds 阈值配置
     */
    void setPerformanceThresholds(Map<String, Object> thresholds);
    
    /**
     * 启用性能监控
     */
    void enableMonitoring();
    
    /**
     * 禁用性能监控
     */
    void disableMonitoring();
    
    /**
     * 导出性能报告
     * 
     * @param format 报告格式（JSON, XML, CSV等）
     * @return 格式化的性能报告
     */
    String exportPerformanceReport(String format);
    
    /**
     * 获取系统资源使用情况
     * 
     * @return 系统资源使用情况
     */
    Map<String, Object> getSystemResourceUsage();
    
    /**
     * 开始API调用监控
     * 
     * @param apiType API类型
     * @param expectedCalls 预期的调用数量
     * @return 监控会话ID
     */
    String startApiCallMonitoring(String apiType, int expectedCalls);
    
    /**
     * 结束API调用监控
     * 
     * @param sessionId 监控会话ID
     * @param success 是否成功
     * @param actualCalls 实际调用数量
     * @param totalTokens 总Token数
     */
    void endApiCallMonitoring(String sessionId, boolean success, int actualCalls, int totalTokens);
    
    /**
     * 记录缓存命中情况
     * 
     * @param cacheType 缓存类型
     * @param hit 是否命中
     * @param key 缓存键
     */
    void recordCacheHit(String cacheType, boolean hit, String key);
}