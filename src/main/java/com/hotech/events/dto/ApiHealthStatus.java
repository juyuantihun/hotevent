package com.hotech.events.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API健康状态DTO
 */
@Data
public class ApiHealthStatus {
    
    /**
     * 是否健康
     */
    private Boolean isHealthy;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * API版本
     */
    private String apiVersion;
    
    /**
     * 状态码
     */
    private Integer statusCode;
}