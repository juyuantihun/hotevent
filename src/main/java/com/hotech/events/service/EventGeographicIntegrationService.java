package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.entity.Event;

import java.util.List;
import java.util.Optional;

/**
 * 事件地理信息集成服务接口
 * 负责在事件解析阶段集成地理信息处理
 */
public interface EventGeographicIntegrationService {
    
    /**
     * 在事件解析阶段集成地理信息处理
     * @param events 待处理的事件列表
     * @return 处理后的事件列表
     */
    List<EventData> integrateGeographicInfoInParsing(List<EventData> events);
    
    /**
     * 为单个事件集成地理信息
     * @param event 待处理的事件
     * @return 处理后的事件
     */
    EventData integrateGeographicInfoForSingleEvent(EventData event);
    
    /**
     * 从API响应中解析地理信息
     * @param apiResponse API响应内容
     * @param event 事件对象
     * @return 是否成功解析地理信息
     */
    boolean parseGeographicInfoFromApiResponse(String apiResponse, EventData event);
    
    /**
     * 验证和标准化事件地理信息
     * @param event 事件对象
     * @return 验证结果
     */
    GeographicValidationResult validateAndStandardizeEventGeographicInfo(EventData event);
    
    /**
     * 为事件存储准备地理坐标数据
     * @param event 事件对象
     * @return 地理坐标存储结果
     */
    GeographicStorageResult prepareGeographicDataForStorage(EventData event);
    
    /**
     * 从存储的事件中加载地理信息
     * @param event 事件实体
     * @return 包含地理信息的事件DTO
     */
    EventData loadGeographicInfoFromStoredEvent(Event event);
    
    /**
     * 批量从存储的事件中加载地理信息
     * @param events 事件实体列表
     * @return 包含地理信息的事件DTO列表
     */
    List<EventData> loadGeographicInfoFromStoredEvents(List<Event> events);
    
    /**
     * 更新事件的地理信息
     * @param eventId 事件ID
     * @param coordinate 新的地理坐标
     * @return 是否更新成功
     */
    boolean updateEventGeographicInfo(Long eventId, GeographicCoordinate coordinate);
    
    /**
     * 获取地理信息处理统计
     * @return 统计信息
     */
    GeographicProcessingStats getGeographicProcessingStats();
    
    /**
     * 地理信息验证结果
     */
    class GeographicValidationResult {
        private boolean valid;
        private String errorMessage;
        private List<String> warnings;
        private GeographicCoordinate standardizedEventCoordinate;
        private GeographicCoordinate standardizedSubjectCoordinate;
        private GeographicCoordinate standardizedObjectCoordinate;
        
        // 构造函数、getter和setter
        public GeographicValidationResult() {}
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public GeographicCoordinate getStandardizedEventCoordinate() { return standardizedEventCoordinate; }
        public void setStandardizedEventCoordinate(GeographicCoordinate standardizedEventCoordinate) { 
            this.standardizedEventCoordinate = standardizedEventCoordinate; 
        }
        
        public GeographicCoordinate getStandardizedSubjectCoordinate() { return standardizedSubjectCoordinate; }
        public void setStandardizedSubjectCoordinate(GeographicCoordinate standardizedSubjectCoordinate) { 
            this.standardizedSubjectCoordinate = standardizedSubjectCoordinate; 
        }
        
        public GeographicCoordinate getStandardizedObjectCoordinate() { return standardizedObjectCoordinate; }
        public void setStandardizedObjectCoordinate(GeographicCoordinate standardizedObjectCoordinate) { 
            this.standardizedObjectCoordinate = standardizedObjectCoordinate; 
        }
    }
    
    /**
     * 地理信息存储结果
     */
    class GeographicStorageResult {
        private boolean success;
        private String errorMessage;
        private Long eventCoordinateId;
        private Long subjectCoordinateId;
        private Long objectCoordinateId;
        
        // 构造函数、getter和setter
        public GeographicStorageResult() {}
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Long getEventCoordinateId() { return eventCoordinateId; }
        public void setEventCoordinateId(Long eventCoordinateId) { this.eventCoordinateId = eventCoordinateId; }
        
        public Long getSubjectCoordinateId() { return subjectCoordinateId; }
        public void setSubjectCoordinateId(Long subjectCoordinateId) { this.subjectCoordinateId = subjectCoordinateId; }
        
        public Long getObjectCoordinateId() { return objectCoordinateId; }
        public void setObjectCoordinateId(Long objectCoordinateId) { this.objectCoordinateId = objectCoordinateId; }
    }
    
    /**
     * 地理信息处理统计
     */
    class GeographicProcessingStats {
        private long totalProcessed;
        private long successfullyProcessed;
        private long partiallyProcessed;
        private long failed;
        private long coordinatesCached;
        private double successRate;
        
        // 构造函数、getter和setter
        public GeographicProcessingStats() {}
        
        public long getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(long totalProcessed) { this.totalProcessed = totalProcessed; }
        
        public long getSuccessfullyProcessed() { return successfullyProcessed; }
        public void setSuccessfullyProcessed(long successfullyProcessed) { this.successfullyProcessed = successfullyProcessed; }
        
        public long getPartiallyProcessed() { return partiallyProcessed; }
        public void setPartiallyProcessed(long partiallyProcessed) { this.partiallyProcessed = partiallyProcessed; }
        
        public long getFailed() { return failed; }
        public void setFailed(long failed) { this.failed = failed; }
        
        public long getCoordinatesCached() { return coordinatesCached; }
        public void setCoordinatesCached(long coordinatesCached) { this.coordinatesCached = coordinatesCached; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }
}