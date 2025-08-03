package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API调用记录实体
 * 用于记录和分析API调用统计信息
 */
@Data
@TableName("api_call_record")
public class ApiCallRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB
     */
    private String apiType;
    
    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;
    
    /**
     * 响应状态：SUCCESS, FAILED, TIMEOUT, RATE_LIMITED
     */
    private String responseStatus;
    
    /**
     * Token使用量
     */
    private Integer tokenUsage;
    
    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 调用时间
     */
    private LocalDateTime callTime;
    
    /**
     * 请求ID，用于链路追踪
     */
    private String requestId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 请求大小（字节）
     */
    private Integer requestSize;
    
    /**
     * 响应大小（字节）
     */
    private Integer responseSize;
    
    /**
     * 是否使用缓存
     */
    private Boolean cacheHit;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
}