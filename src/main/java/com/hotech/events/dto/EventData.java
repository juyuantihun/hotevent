package com.hotech.events.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件数据传输对象
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
public class EventData {
    
    /**
     * 事件ID
     */
    private String id;
    
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 事件来源
     */
    private String source;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 位置名称
     */
    private String location;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 重要性级别
     */
    private Integer importance;
    
    /**
     * 可信度
     */
    private Double credibility;
    
    /**
     * 相关标签
     */
    private String tags;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 扩展字段以支持现有代码
    
    /**
     * 事件主体
     */
    private String subject;
    
    /**
     * 事件客体
     */
    private String object;
    
    /**
     * 关键词列表
     */
    private List<String> keywords;
    
    /**
     * 来源列表
     */
    private List<String> sources;
    
    /**
     * 可信度分数
     */
    private Double credibilityScore;
    
    /**
     * 获取方法
     */
    private String fetchMethod;
    
    /**
     * 验证状态
     */
    private String validationStatus;
    
    /**
     * 相关事件列表
     */
    private List<EventData> relatedEvents;
    
    /**
     * 创建时间（别名）
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间（别名）
     */
    private LocalDateTime updatedAt;
    
    /**
     * 事件坐标
     */
    private GeographicCoordinate eventCoordinate;
    
    /**
     * 主体坐标
     */
    private GeographicCoordinate subjectCoordinate;
    
    /**
     * 客体坐标
     */
    private GeographicCoordinate objectCoordinate;
    
    /**
     * 主要坐标
     */
    private GeographicCoordinate primaryCoordinate;
    
    /**
     * 检查是否有有效的地理信息
     */
    public boolean hasValidGeographicInfo() {
        return (latitude != null && longitude != null) ||
               (eventCoordinate != null && eventCoordinate.isValid()) ||
               (primaryCoordinate != null && primaryCoordinate.isValid());
    }
    
    /**
     * 设置可信度分数（重载方法）
     */
    public void setCredibilityScore(double score) {
        this.credibilityScore = score;
    }
    
    /**
     * 设置可信度分数（Double类型）
     */
    public void setCredibilityScore(Double score) {
        this.credibilityScore = score;
    }
}