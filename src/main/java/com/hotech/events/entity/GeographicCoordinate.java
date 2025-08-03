package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 地理坐标数据库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("geographic_coordinates")
public class GeographicCoordinate {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地点名称
     */
    @TableField("location_name")
    private String locationName;

    /**
     * 地点类型 (COUNTRY, REGION, CITY, DISTRICT)
     */
    @TableField("location_type")
    private String locationType;

    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 国家代码
     */
    @TableField("country_code")
    private String countryCode;

    /**
     * 地区代码
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 是否为默认坐标
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 精度等级 (HIGH, MEDIUM, LOW, VERY_LOW, UNKNOWN)
     */
    @TableField("accuracy_level")
    private String accuracyLevel;

    /**
     * 数据来源
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 转换为DTO对象
     */
    public com.hotech.events.dto.GeographicCoordinate toDTO() {
        return com.hotech.events.dto.GeographicCoordinate.builder()
                .locationName(this.locationName)
                .locationType(parseLocationType(this.locationType))
                .latitude(this.latitude)
                .longitude(this.longitude)
                .countryCode(this.countryCode)
                .regionCode(this.regionCode)
                .isDefault(this.isDefault)
                .accuracyLevel(parseAccuracyLevel(this.accuracyLevel))
                .build();
    }

    /**
     * 从DTO对象创建实体
     */
    public static GeographicCoordinate fromDTO(com.hotech.events.dto.GeographicCoordinate dto) {
        return GeographicCoordinate.builder()
                .locationName(dto.getLocationName())
                .locationType(dto.getLocationType() != null ? dto.getLocationType().name() : null)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .countryCode(dto.getCountryCode())
                .regionCode(dto.getRegionCode())
                .isDefault(dto.getIsDefault())
                .accuracyLevel(dto.getAccuracyLevel() != null ? dto.getAccuracyLevel().name() : null)
                .build();
    }

    private com.hotech.events.dto.GeographicCoordinate.LocationType parseLocationType(String type) {
        if (type == null)
            return null;
        try {
            return com.hotech.events.dto.GeographicCoordinate.LocationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return com.hotech.events.dto.GeographicCoordinate.LocationType.UNKNOWN;
        }
    }

    private com.hotech.events.dto.GeographicCoordinate.AccuracyLevel parseAccuracyLevel(String level) {
        if (level == null)
            return null;
        try {
            return com.hotech.events.dto.GeographicCoordinate.AccuracyLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return com.hotech.events.dto.GeographicCoordinate.AccuracyLevel.UNKNOWN;
        }
    }
}