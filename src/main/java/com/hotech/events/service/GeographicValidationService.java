package com.hotech.events.service;

import com.hotech.events.dto.EnhancedEventData;
import com.hotech.events.dto.GeographicCoordinate;

import java.util.List;
import java.util.Map;

/**
 * 地理信息验证服务接口
 * 负责验证和标准化地理坐标信息
 */
public interface GeographicValidationService {
    
    /**
     * 验证地理坐标的有效性
     * 
     * @param coordinate 地理坐标
     * @return 验证结果
     */
    GeographicValidationResult validateCoordinate(GeographicCoordinate coordinate);
    
    /**
     * 标准化地理坐标
     * 
     * @param coordinate 原始坐标
     * @return 标准化后的坐标
     */
    GeographicCoordinate standardizeCoordinate(GeographicCoordinate coordinate);
    
    /**
     * 批量验证事件的地理信息
     * 
     * @param events 事件列表
     * @return 验证结果列表
     */
    List<GeographicValidationResult> validateEventsGeographicInfo(List<EnhancedEventData> events);
    
    /**
     * 验证事件的地理信息完整性
     * 
     * @param event 事件数据
     * @return 验证结果
     */
    GeographicValidationResult validateEventGeographicInfo(EnhancedEventData event);
    
    /**
     * 修复无效的地理坐标
     * 
     * @param coordinate 无效坐标
     * @param locationName 地点名称
     * @return 修复后的坐标
     */
    GeographicCoordinate repairInvalidCoordinate(GeographicCoordinate coordinate, String locationName);
    
    /**
     * 检查坐标是否在合理范围内
     * 
     * @param coordinate 坐标
     * @param expectedRegion 预期地区
     * @return 是否在合理范围内
     */
    boolean isCoordinateInReasonableRange(GeographicCoordinate coordinate, String expectedRegion);
    
    /**
     * 获取验证统计信息
     * 
     * @return 验证统计
     */
    Map<String, Object> getValidationStatistics();
    
    /**
     * 地理信息验证结果
     */
    class GeographicValidationResult {
        private boolean isValid;
        private String errorMessage;
        private GeographicCoordinate originalCoordinate;
        private GeographicCoordinate standardizedCoordinate;
        private List<String> warnings;
        private ValidationLevel validationLevel;
        
        public enum ValidationLevel {
            VALID("有效"),
            WARNING("警告"),
            ERROR("错误"),
            CRITICAL("严重错误");
            
            private final String description;
            
            ValidationLevel(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
        
        // Getters and Setters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public GeographicCoordinate getOriginalCoordinate() { return originalCoordinate; }
        public void setOriginalCoordinate(GeographicCoordinate originalCoordinate) { this.originalCoordinate = originalCoordinate; }
        
        public GeographicCoordinate getStandardizedCoordinate() { return standardizedCoordinate; }
        public void setStandardizedCoordinate(GeographicCoordinate standardizedCoordinate) { this.standardizedCoordinate = standardizedCoordinate; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public ValidationLevel getValidationLevel() { return validationLevel; }
        public void setValidationLevel(ValidationLevel validationLevel) { this.validationLevel = validationLevel; }
    }
}