package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 事件实体类
 */
@Data
@TableName("event")
public class Event {
    
    /**
     * 事件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 事件编码
     */
    @TableField("event_code")
    private String eventCode;
    
    /**
     * 事件发生时间
     */
    @TableField("event_time")
    private LocalDateTime eventTime;
    
    /**
     * 事件地点
     */
    @TableField("event_location")
    private String eventLocation;
    
    /**
     * 事件类型
     */
    @TableField("event_type")
    private String eventType;
    
    /**
     * 事件描述
     */
    @TableField("event_description")
    private String eventDescription;
    
    /**
     * 事件主体
     */
    @TableField("subject")
    private String subject;
    
    /**
     * 事件客体
     */
    @TableField("object")
    private String object;
    
    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;
    
    /**
     * 来源类型：1-自动获取，2-手动录入
     */
    @TableField("source_type")
    private Integer sourceType;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;
    
    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;
    
    /**
     * 关系类型
     */
    @TableField("relation_type")
    private String relationType;
    
    /**
     * 关系名称
     */
    @TableField("relation_name")
    private String relationName;
    
    /**
     * 强度等级(1-5)
     */
    @TableField("intensity_level")
    private Integer intensityLevel;
    
    /**
     * 事件标题
     */
    @TableField("event_title")
    private String eventTitle;
    
    /**
     * 可信度评分
     */
    @TableField("credibility_score")
    private Double credibilityScore;
    
    /**
     * 验证状态
     */
    @TableField("validation_status")
    private String validationStatus;
    
    /**
     * 来源URL列表
     */
    @TableField("source_urls")
    private String sourceUrls;
    
    /**
     * 获取方式
     */
    @TableField("fetch_method")
    private String fetchMethod;
    
    /**
     * 最后验证时间
     */
    @TableField("last_validated_at")
    private LocalDateTime lastValidatedAt;
    
    /**
     * 事件主体地理坐标ID（关联geographic_coordinates表）
     */
    @TableField("subject_coordinate_id")
    private Long subjectCoordinateId;
    
    /**
     * 事件客体地理坐标ID（关联geographic_coordinates表）
     */
    @TableField("object_coordinate_id")
    private Long objectCoordinateId;
    
    /**
     * 事件发生地坐标ID（关联geographic_coordinates表）
     */
    @TableField("event_coordinate_id")
    private Long eventCoordinateId;
    
    /**
     * 地理信息处理状态：0-未处理，1-已处理，2-处理失败
     */
    @TableField("geographic_status")
    private Integer geographicStatus;
    
    /**
     * 地理信息最后更新时间
     */
    @TableField("geographic_updated_at")
    private LocalDateTime geographicUpdatedAt;
    
    /**
     * 检查是否有地理坐标信息
     */
    public boolean hasGeographicCoordinates() {
        return subjectCoordinateId != null || objectCoordinateId != null || 
               eventCoordinateId != null || (latitude != null && longitude != null);
    }
    
    /**
     * 获取基础坐标（向后兼容）
     */
    public com.hotech.events.dto.GeographicCoordinate getBasicCoordinate() {
        if (latitude != null && longitude != null) {
            return com.hotech.events.dto.GeographicCoordinate.builder()
                    .latitude(latitude.doubleValue())
                    .longitude(longitude.doubleValue())
                    .locationName(eventLocation)
                    .locationType(com.hotech.events.dto.GeographicCoordinate.LocationType.UNKNOWN)
                    .accuracyLevel(com.hotech.events.dto.GeographicCoordinate.AccuracyLevel.MEDIUM)
                    .build();
        }
        return null;
    }
    
    /**
     * 设置基础坐标（向后兼容）
     */
    public void setBasicCoordinate(com.hotech.events.dto.GeographicCoordinate coordinate) {
        if (coordinate != null && coordinate.isValid()) {
            this.latitude = BigDecimal.valueOf(coordinate.getLatitude());
            this.longitude = BigDecimal.valueOf(coordinate.getLongitude());
            if (coordinate.getLocationName() != null && this.eventLocation == null) {
                this.eventLocation = coordinate.getLocationName();
            }
        }
    }
}