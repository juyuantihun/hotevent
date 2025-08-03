package com.hotech.events.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API使用统计DTO
 */
@Data
public class ApiUsageStats {
    
    /**
     * 总请求数
     */
    private Long totalRequests;
    
    /**
     * 成功请求数
     */
    private Long successfulRequests;
    
    /**
     * 失败请求数
     */
    private Long failedRequests;
    
    /**
     * 平均响应时间（毫秒）
     */
    private Double averageResponseTime;
    
    /**
     * 总Token使用量
     */
    private Long totalTokenUsage;
    
    /**
     * 今日请求数
     */
    private Long todayRequests;
    
    /**
     * 今日Token使用量
     */
    private Long todayTokenUsage;
    
    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;
    
    /**
     * 成功率
     */
    public Double getSuccessRate() {
        if (totalRequests == null || totalRequests == 0) {
            return 0.0;
        }
        return (double) successfulRequests / totalRequests * 100;
    }
}