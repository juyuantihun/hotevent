package com.hotech.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理坐标模型
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeographicCoordinate {
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 地点名称
     */
    private String locationName;
    
    /**
     * 地点类型（COUNTRY, REGION, CITY, CAPITAL等）
     */
    private String locationType;
    
    /**
     * 国家代码
     */
    private String countryCode;
    
    /**
     * 地区代码
     */
    private String regionCode;
    
    /**
     * 是否为默认坐标（首都/首府）
     */
    private Boolean isDefault;
    
    /**
     * 构造函数 - 基本坐标信息
     */
    public GeographicCoordinate(Double latitude, Double longitude, String locationName, String locationType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.locationType = locationType;
        this.isDefault = false;
    }
    
    /**
     * 构造函数 - 包含默认标识
     */
    public GeographicCoordinate(Double latitude, Double longitude, String locationName, 
                               String locationType, Boolean isDefault) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.locationType = locationType;
        this.isDefault = isDefault;
    }
    
    /**
     * 检查坐标是否有效
     */
    public boolean isValid() {
        return latitude != null && longitude != null &&
               latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180;
    }
    
    /**
     * 计算与另一个坐标的距离（简化的欧几里得距离）
     */
    public double distanceTo(GeographicCoordinate other) {
        if (other == null || !this.isValid() || !other.isValid()) {
            return Double.MAX_VALUE;
        }
        
        double latDiff = this.latitude - other.latitude;
        double lonDiff = this.longitude - other.longitude;
        
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }
    
    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        return String.format("GeographicCoordinate{lat=%.4f, lon=%.4f, location='%s', type='%s', default=%s}",
                           latitude, longitude, locationName, locationType, isDefault);
    }
    
    /**
     * 创建默认坐标
     */
    public static GeographicCoordinate createDefault(String locationName, Double latitude, Double longitude) {
        return new GeographicCoordinate(latitude, longitude, locationName, "DEFAULT", true);
    }
    
    /**
     * 创建国家首都坐标
     */
    public static GeographicCoordinate createCapital(String countryName, String capitalName, 
                                                   Double latitude, Double longitude) {
        GeographicCoordinate coordinate = new GeographicCoordinate(latitude, longitude, capitalName, "CAPITAL", true);
        coordinate.setCountryCode(countryName);
        return coordinate;
    }
    
    /**
     * 创建地区首府坐标
     */
    public static GeographicCoordinate createRegionCenter(String regionName, String centerName, 
                                                        Double latitude, Double longitude) {
        GeographicCoordinate coordinate = new GeographicCoordinate(latitude, longitude, centerName, "REGION_CENTER", true);
        coordinate.setRegionCode(regionName);
        return coordinate;
    }
}