package com.hotech.events.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件获取任务DTO
 */
@Data
public class EventFetchTask {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 时间线生成请求
     */
    private TimelineGenerateRequest request;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 优先级
     */
    private Integer priority = 1;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 状态
     */
    private String status = "PENDING";
    
    /**
     * 重试次数
     */
    private Integer retryCount = 0;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
}