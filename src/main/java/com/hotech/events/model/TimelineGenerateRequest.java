package com.hotech.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * 时间线生成请求
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineGenerateRequest {
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 地理位置
     */
    private String location;
    
    /**
     * 最大事件数量
     */
    private Integer maxEvents;
    
    /**
     * 是否启用地理信息处理
     */
    private Boolean enableGeographicProcessing;
    
    /**
     * 是否启用时间段分割
     */
    private Boolean enableSegmentation;
    
    /**
     * 请求来源
     */
    private String source;
    
    /**
     * 地理区域限制
     */
    private String region;
    
    /**
     * 事件类型过滤
     */
    private String eventType;
    
    /**
     * 最小重要性级别
     */
    private Integer minImportance;
    
    /**
     * 最小可信度
     */
    private Double minCredibility;
    
    /**
     * 是否包含相关事件
     */
    private Boolean includeRelated;
    
    /**
     * 排序方式
     */
    private String sortBy;
    
    /**
     * 排序方向
     */
    private String sortDirection;
    
    /**
     * 便捷构造函数
     * 
     * @param keyword 关键词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param location 地理位置
     */
    public TimelineGenerateRequest(String keyword, LocalDateTime startTime, LocalDateTime endTime, String location) {
        this.requestId = generateRequestId();
        this.keyword = keyword;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        // 设置默认值
        this.maxEvents = 50;
        this.enableGeographicProcessing = true;
        this.enableSegmentation = true;
        this.source = "api";
    }
    
    /**
     * 获取时间跨度（天数）
     * 
     * @return 时间跨度天数
     */
    public long getTimeSpanInDays() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startTime, endTime);
    }
    
    /**
     * 判断是否需要时间段分割
     * 
     * @return 是否需要分割
     */
    public boolean needsSegmentation() {
        return enableSegmentation != null && enableSegmentation && getTimeSpanInDays() > 30;
    }
    
    /**
     * 生成请求ID
     * 
     * @return 唯一的请求ID
     */
    private String generateRequestId() {
        return "timeline-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString(new Random().nextInt());
    }
}