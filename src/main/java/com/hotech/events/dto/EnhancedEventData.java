package com.hotech.events.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 增强的事件数据模型
 * 扩展EventData以支持地理坐标信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnhancedEventData extends EventData {
    
    /**
     * 事件主体
     */
    @JsonProperty("subject")
    private String subject;
    
    /**
     * 事件客体
     */
    @JsonProperty("object")
    private String object;
    
    /**
     * 主体坐标
     */
    @JsonProperty("subjectCoordinate")
    private GeographicCoordinate subjectCoordinate;
    
    /**
     * 客体坐标
     */
    @JsonProperty("objectCoordinate")
    private GeographicCoordinate objectCoordinate;
    
    /**
     * 事件发生地坐标
     */
    @JsonProperty("eventCoordinate")
    private GeographicCoordinate eventCoordinate;
    
    /**
     * 所属时间段ID
     */
    @JsonProperty("timeSegmentId")
    private String timeSegmentId;
    
    /**
     * 地理信息处理状态
     */
    @JsonProperty("geoProcessingStatus")
    private GeoProcessingStatus geoProcessingStatus;
    
    /**
     * 地理信息处理时间戳
     */
    @JsonProperty("geoProcessedAt")
    private Long geoProcessedAt;
    
    /**
     * 地理信息处理错误信息
     */
    @JsonProperty("geoProcessingError")
    private String geoProcessingError;
    
    /**
     * 地理信息处理状态枚举
     */
    public enum GeoProcessingStatus {
        NOT_PROCESSED("未处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("处理失败"),
        PARTIAL("部分完成");
        
        private final String description;
        
        GeoProcessingStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 默认构造函数
     */
    public EnhancedEventData() {
        super();
        this.geoProcessingStatus = GeoProcessingStatus.NOT_PROCESSED;
    }
    
    /**
     * 从基础EventData创建增强版本
     */
    public static EnhancedEventData fromEventData(EventData eventData) {
        EnhancedEventData enhanced = new EnhancedEventData();
        
        // 复制基础字段
        enhanced.setId(eventData.getId());
        enhanced.setTitle(eventData.getTitle());
        enhanced.setDescription(eventData.getDescription());
        enhanced.setEventTime(eventData.getEventTime());
        enhanced.setLocation(eventData.getLocation());
        enhanced.setLatitude(eventData.getLatitude());
        enhanced.setLongitude(eventData.getLongitude());
        enhanced.setSource(eventData.getSource());
        enhanced.setCredibility(eventData.getCredibility());
        enhanced.setEventType(eventData.getEventType());
        enhanced.setKeywords(eventData.getKeywords());
        enhanced.setRelatedEvents(eventData.getRelatedEvents());
        enhanced.setCreatedAt(eventData.getCreatedAt());
        enhanced.setUpdatedAt(eventData.getUpdatedAt());
        
        // 设置地理处理状态
        enhanced.setGeoProcessingStatus(GeoProcessingStatus.NOT_PROCESSED);
        
        return enhanced;
    }
    
    /**
     * 转换为基础EventData
     */
    public EventData toEventData() {
        EventData eventData = new EventData();
        
        // 复制基础字段
        eventData.setId(this.getId());
        eventData.setTitle(this.getTitle());
        eventData.setDescription(this.getDescription());
        eventData.setEventTime(this.getEventTime());
        eventData.setLocation(this.getLocation());
        eventData.setLatitude(this.getLatitude());
        eventData.setLongitude(this.getLongitude());
        eventData.setSource(this.getSource());
        eventData.setCredibility(this.getCredibility());
        eventData.setEventType(this.getEventType());
        eventData.setKeywords(this.getKeywords());
        eventData.setRelatedEvents(this.getRelatedEvents());
        eventData.setCreatedAt(this.getCreatedAt());
        eventData.setUpdatedAt(this.getUpdatedAt());
        
        return eventData;
    }
    
    /**
     * 检查是否有有效的事件坐标
     */
    public boolean hasValidEventCoordinate() {
        return eventCoordinate != null && eventCoordinate.isValid();
    }
    
    /**
     * 检查是否有有效的主体坐标
     */
    public boolean hasValidSubjectCoordinate() {
        return subjectCoordinate != null && subjectCoordinate.isValid();
    }
    
    /**
     * 检查是否有有效的客体坐标
     */
    public boolean hasValidObjectCoordinate() {
        return objectCoordinate != null && objectCoordinate.isValid();
    }
    
    /**
     * 获取主要坐标（优先级：事件坐标 > 主体坐标 > 基础坐标）
     */
    public GeographicCoordinate getPrimaryCoordinate() {
        if (hasValidEventCoordinate()) {
            return eventCoordinate;
        } else if (hasValidSubjectCoordinate()) {
            return subjectCoordinate;
        } else if (getLatitude() != null && getLongitude() != null) {
            return GeographicCoordinate.builder()
                    .latitude(getLatitude())
                    .longitude(getLongitude())
                    .locationName(getLocation())
                    .build();
        }
        return null;
    }
    
    /**
     * 设置地理处理完成状态
     */
    public void markGeoProcessingCompleted() {
        this.geoProcessingStatus = GeoProcessingStatus.COMPLETED;
        this.geoProcessedAt = System.currentTimeMillis();
        this.geoProcessingError = null;
    }
    
    /**
     * 设置地理处理失败状态
     */
    public void markGeoProcessingFailed(String error) {
        this.geoProcessingStatus = GeoProcessingStatus.FAILED;
        this.geoProcessedAt = System.currentTimeMillis();
        this.geoProcessingError = error;
    }
    
    /**
     * 设置地理处理部分完成状态
     */
    public void markGeoProcessingPartial(String error) {
        this.geoProcessingStatus = GeoProcessingStatus.PARTIAL;
        this.geoProcessedAt = System.currentTimeMillis();
        this.geoProcessingError = error;
    }
    
    /**
     * 检查地理处理是否完成
     */
    public boolean isGeoProcessingCompleted() {
        return geoProcessingStatus == GeoProcessingStatus.COMPLETED || 
               geoProcessingStatus == GeoProcessingStatus.PARTIAL;
    }
}