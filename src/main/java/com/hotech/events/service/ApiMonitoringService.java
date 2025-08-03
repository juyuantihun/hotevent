package com.hotech.events.service;

import com.hotech.events.entity.ApiCallRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API监控服务接口
 * 记录和分析API调用统计信息
 */
public interface ApiMonitoringService {
    
    /**
     * 记录API调用
     * 
     * @param apiType API类型
     * @param requestBody 请求体
     * @param responseStatus 响应状态
     * @param tokenUsage Token使用量
     * @param responseTime 响应时间
     * @param errorMessage 错误信息
     * @param requestId 请求ID
     * @param retryCount 重试次数
     */
    void recordApiCall(String apiType, Map<String, Object> requestBody, String responseStatus, 
                      Integer tokenUsage, Integer responseTime, String errorMessage, 
                      String requestId, Integer retryCount);
    
    /**
     * 获取API调用统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    List<Map<String, Object>> getApiCallStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取API成功率统计
     * 
     * @param since 统计起始时间
     * @return 成功率统计
     */
    List<Map<String, Object>> getSuccessRateStats(LocalDateTime since);
    
    /**
     * 获取API性能统计
     * 
     * @param since 统计起始时间
     * @return 性能统计
     */
    List<Map<String, Object>> getPerformanceStats(LocalDateTime since);
    
    /**
     * 获取错误统计
     * 
     * @param since 统计起始时间
     * @return 错误统计
     */
    List<Map<String, Object>> getErrorStats(LocalDateTime since);
    
    /**
     * 获取最近的API调用记录
     * 
     * @param limit 限制数量
     * @return API调用记录列表
     */
    List<ApiCallRecord> getRecentApiCalls(int limit);
    
    /**
     * 生成API性能报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告
     */
    Map<String, Object> generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 清理过期记录
     * 
     * @param beforeTime 清理此时间之前的记录
     * @return 清理的记录数
     */
    int cleanupOldRecords(LocalDateTime beforeTime);
    
    /**
     * 获取实时监控数据
     * 
     * @return 实时监控数据
     */
    Map<String, Object> getRealTimeMonitoringData();
    
    /**
     * 检查是否需要告警
     * 
     * @return 告警信息，如果没有告警返回null
     */
    List<Map<String, Object>> checkAlerts();
}