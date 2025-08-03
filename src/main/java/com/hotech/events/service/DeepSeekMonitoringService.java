package com.hotech.events.service;

import com.hotech.events.dto.ApiUsageStats;
import com.hotech.events.entity.DeepSeekApiUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API监控服务接口
 * 用于监控和统计DeepSeek API的使用情况
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface DeepSeekMonitoringService {
    
    /**
     * 记录API调用
     * 
     * @param requestType 请求类型
     * @param requestParams 请求参数
     * @param responseStatus 响应状态
     * @param tokenUsage Token使用量
     * @param responseTimeMs 响应时间（毫秒）
     * @param errorMessage 错误消息（如果有）
     */
    void recordApiCall(String requestType, String requestParams, String responseStatus, 
                      Integer tokenUsage, Integer responseTimeMs, String errorMessage);
    
    /**
     * 获取使用统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用统计信息
     */
    ApiUsageStats getUsageStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取按请求类型分组的统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 按请求类型分组的统计信息
     */
    List<Map<String, Object>> getRequestTypeStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 清理旧记录
     * 
     * @param beforeTime 清理此时间之前的记录
     * @return 清理的记录数量
     */
    int cleanupOldRecords(LocalDateTime beforeTime);
    
    /**
     * 获取最近的API调用记录
     * 
     * @param limit 限制数量
     * @return API调用记录列表
     */
    List<DeepSeekApiUsage> getRecentApiCalls(int limit);
}