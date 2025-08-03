package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理坐标数据传输对象
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographicCoordinate {
    
    /**
     * 位置类型枚举
     */
    public enum LocationType {
        
        /**
         * 城市
         */
        CITY("城市"),
        
        /**
         * 国家
         */
        COUNTRY("国家"),
        
        /**
         * 省份/州
         */
        PROVINCE("省份"),
        
        /**
         * 地区
         */
        REGION("地区"),
        
        /**
         * 区县
         */
        DISTRICT("区县"),
        
        /**
         * 建筑物
         */
        BUILDING("建筑物"),
        
        /**
         * 地标
         */
        LANDMARK("地标"),
        
        /**
         * 街道
         */
        STREET("街道"),
        
        /**
         * 机场
         */
        AIRPORT("机场"),
        
        /**
         * 港口
         */
        PORT("港口"),
        
        /**
         * 学校
         */
        SCHOOL("学校"),
        
        /**
         * 医院
         */
        HOSPITAL("医院"),
        
        /**
         * 政府机构
         */
        GOVERNMENT("政府机构"),
        
        /**
         * 商业区
         */
        BUSINESS("商业区"),
        
        /**
         * 住宅区
         */
        RESIDENTIAL("住宅区"),
        
        /**
         * 工业区
         */
        INDUSTRIAL("工业区"),
        
        /**
         * 自然景观
         */
        NATURAL("自然景观"),
        
        /**
         * 未知
         */
        UNKNOWN("未知");
        
        private final String description;
        
        LocationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 根据描述获取位置类型
         * 
         * @param description 描述
         * @return 位置类型
         */
        public static LocationType fromDescription(String description) {
            if (description == null || description.trim().isEmpty()) {
                return UNKNOWN;
            }
            
            String desc = description.toLowerCase().trim();
            
            for (LocationType type : values()) {
                if (type.description.toLowerCase().contains(desc) || 
                    desc.contains(type.description.toLowerCase())) {
                    return type;
                }
            }
            
            // 基于关键词匹配
            if (desc.contains("市") || desc.contains("city")) {
                return CITY;
            } else if (desc.contains("国") || desc.contains("country")) {
                return COUNTRY;
            } else if (desc.contains("省") || desc.contains("州") || desc.contains("province")) {
                return PROVINCE;
            } else if (desc.contains("区") || desc.contains("region")) {
                return REGION;
            } else if (desc.contains("楼") || desc.contains("building")) {
                return BUILDING;
            } else if (desc.contains("街") || desc.contains("路") || desc.contains("street")) {
                return STREET;
            } else if (desc.contains("机场") || desc.contains("airport")) {
                return AIRPORT;
            } else if (desc.contains("港") || desc.contains("port")) {
                return PORT;
            } else if (desc.contains("学校") || desc.contains("大学") || desc.contains("school")) {
                return SCHOOL;
            } else if (desc.contains("医院") || desc.contains("hospital")) {
                return HOSPITAL;
            } else if (desc.contains("政府") || desc.contains("government")) {
                return GOVERNMENT;
            }
            
            return UNKNOWN;
        }
    }
    
    /**
     * 精度级别枚举
     */
    public enum AccuracyLevel {
        
        /**
         * 高精度（GPS级别，误差小于10米）
         */
        HIGH("高精度", 0, 10),
        
        /**
         * 中等精度（误差10-100米）
         */
        MEDIUM("中等精度", 10, 100),
        
        /**
         * 低精度（误差100-1000米）
         */
        LOW("低精度", 100, 1000),
        
        /**
         * 很低精度（误差大于1000米）
         */
        VERY_LOW("很低精度", 1000, Double.MAX_VALUE),
        
        /**
         * 未知精度
         */
        UNKNOWN("未知精度", 0, Double.MAX_VALUE);
        
        private final String description;
        private final double minAccuracy;
        private final double maxAccuracy;
        
        AccuracyLevel(String description, double minAccuracy, double maxAccuracy) {
            this.description = description;
            this.minAccuracy = minAccuracy;
            this.maxAccuracy = maxAccuracy;
        }
        
        public String getDescription() {
            return description;
        }
        
        public double getMinAccuracy() {
            return minAccuracy;
        }
        
        public double getMaxAccuracy() {
            return maxAccuracy;
        }
        
        /**
         * 根据精度值获取精度级别
         * 
         * @param accuracy 精度值（米）
         * @return 精度级别
         */
        public static AccuracyLevel fromAccuracy(Double accuracy) {
            if (accuracy == null || accuracy < 0) {
                return UNKNOWN;
            }
            
            if (accuracy < 10) {
                return HIGH;
            } else if (accuracy < 100) {
                return MEDIUM;
            } else if (accuracy < 1000) {
                return LOW;
            } else {
                return VERY_LOW;
            }
        }
        
        /**
         * 检查给定精度是否在此级别范围内
         * 
         * @param accuracy 精度值
         * @return 是否在范围内
         */
        public boolean isInRange(Double accuracy) {
            if (accuracy == null) {
                return this == UNKNOWN;
            }
            
            return accuracy >= minAccuracy && accuracy < maxAccuracy;
        }
        
        /**
         * 获取推荐的精度值（范围中点）
         * 
         * @return 推荐精度值
         */
        public double getRecommendedAccuracy() {
            if (this == UNKNOWN || maxAccuracy == Double.MAX_VALUE) {
                return minAccuracy * 2; // 返回最小值的两倍作为估计
            }
            
            return (minAccuracy + maxAccuracy) / 2;
        }
    }
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 海拔高度
     */
    private Double altitude;
    
    /**
     * 精度（米）
     */
    private Double accuracy;
    
    /**
     * 坐标系统
     */
    private String coordinateSystem;
    
    /**
     * 位置名称
     */
    private String locationName;
    
    /**
     * 位置类型
     */
    private LocationType locationType;
    
    /**
     * 精度级别
     */
    private AccuracyLevel accuracyLevel;
    
    /**
     * 国家代码（ISO 3166-1 alpha-2）
     */
    private String countryCode;
    
    /**
     * 地区代码
     */
    private String regionCode;
    
    /**
     * 城市代码
     */
    private String cityCode;
    
    /**
     * 地址详情
     */
    private String address;
    
    /**
     * 是否为默认坐标
     */
    private Boolean isDefault;
    
    /**
     * 便捷构造函数
     * 
     * @param latitude 纬度
     * @param longitude 经度
     */
    public GeographicCoordinate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * 便捷构造函数
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param altitude 海拔高度
     */
    public GeographicCoordinate(Double latitude, Double longitude, Double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
    
    /**
     * 创建默认地理坐标
     * 
     * @param locationName 位置名称
     * @param locationType 位置类型
     * @param latitude 纬度
     * @param longitude 经度
     * @return 地理坐标对象
     */
    public static GeographicCoordinate createDefault(String locationName, LocationType locationType, 
                                                   double latitude, double longitude) {
        return GeographicCoordinate.builder()
                .locationName(locationName)
                .locationType(locationType)
                .latitude(latitude)
                .longitude(longitude)
                .coordinateSystem("WGS84")
                .accuracyLevel(AccuracyLevel.MEDIUM)
                .accuracy(50.0) // 默认50米精度
                .build();
    }
    
    /**
     * 创建首都坐标
     * 
     * @param countryName 国家名称
     * @param capitalName 首都名称
     * @param latitude 纬度
     * @param longitude 经度
     * @return 地理坐标对象
     */
    public static GeographicCoordinate createCapital(String countryName, String capitalName, 
                                                   double latitude, double longitude) {
        return GeographicCoordinate.builder()
                .locationName(capitalName)
                .locationType(LocationType.CITY)
                .latitude(latitude)
                .longitude(longitude)
                .coordinateSystem("WGS84")
                .accuracyLevel(AccuracyLevel.HIGH)
                .accuracy(10.0) // 首都坐标通常比较精确
                .build();
    }
    
    /**
     * 创建城市坐标
     * 
     * @param cityName 城市名称
     * @param latitude 纬度
     * @param longitude 经度
     * @return 地理坐标对象
     */
    public static GeographicCoordinate createCity(String cityName, double latitude, double longitude) {
        return createDefault(cityName, LocationType.CITY, latitude, longitude);
    }
    
    /**
     * 创建国家坐标（通常指向首都）
     * 
     * @param countryName 国家名称
     * @param latitude 纬度
     * @param longitude 经度
     * @return 地理坐标对象
     */
    public static GeographicCoordinate createCountry(String countryName, double latitude, double longitude) {
        return createDefault(countryName, LocationType.COUNTRY, latitude, longitude);
    }
    
    /**
     * 创建地区坐标
     * 
     * @param regionName 地区名称
     * @param latitude 纬度
     * @param longitude 经度
     * @return 地理坐标对象
     */
    public static GeographicCoordinate createRegion(String regionName, double latitude, double longitude) {
        return createDefault(regionName, LocationType.REGION, latitude, longitude);
    }
    
    /**
     * 检查坐标是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return latitude != null && longitude != null &&
               latitude >= -90.0 && latitude <= 90.0 &&
               longitude >= -180.0 && longitude <= 180.0;
    }
    
    /**
     * 检查坐标是否在合理范围内
     * 
     * @return 是否在合理范围内
     */
    public boolean isInReasonableRange() {
        if (!isValid()) {
            return false;
        }
        
        // 检查是否为明显错误的坐标（如0,0）
        if (latitude == 0.0 && longitude == 0.0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 计算与另一个坐标的距离（公里）
     * 使用Haversine公式
     * 
     * @param other 另一个坐标
     * @return 距离（公里）
     */
    public double distanceTo(GeographicCoordinate other) {
        if (!this.isValid() || !other.isValid()) {
            return Double.NaN;
        }
        
        final double R = 6371; // 地球半径（公里）
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * 获取坐标字符串表示
     * 
     * @return 坐标字符串，格式为 "纬度,经度"
     */
    public String getCoordinateString() {
        if (latitude == null || longitude == null) {
            return "无效坐标";
        }
        return String.format("%.6f,%.6f", latitude, longitude);
    }
    
    /**
     * 获取详细的坐标信息字符串
     * 
     * @return 详细坐标信息
     */
    public String getDetailedCoordinateString() {
        StringBuilder sb = new StringBuilder();
        
        if (locationName != null && !locationName.trim().isEmpty()) {
            sb.append(locationName).append(" ");
        }
        
        if (latitude != null && longitude != null) {
            sb.append(String.format("(%.6f, %.6f)", latitude, longitude));
        } else {
            sb.append("(无效坐标)");
        }
        
        if (locationType != null && locationType != LocationType.UNKNOWN) {
            sb.append(" [").append(locationType.getDescription()).append("]");
        }
        
        if (accuracyLevel != null && accuracyLevel != AccuracyLevel.UNKNOWN) {
            sb.append(" 精度:").append(accuracyLevel.getDescription());
        }
        
        return sb.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format("GeographicCoordinate[lat=%.6f, lon=%.6f%s]", 
                latitude, longitude, 
                altitude != null ? String.format(", alt=%.2f", altitude) : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GeographicCoordinate that = (GeographicCoordinate) obj;
        
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        return altitude != null ? altitude.equals(that.altitude) : that.altitude == null;
    }
    
    @Override
    public int hashCode() {
        int result = latitude != null ? latitude.hashCode() : 0;
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        return result;
    }
}