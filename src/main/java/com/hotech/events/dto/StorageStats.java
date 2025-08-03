package com.hotech.events.dto;

import java.time.LocalDateTime;

/**
 * 存储统计信息DTO
 * 
 * @author Kiro
 */
public class StorageStats {
    
    /**
     * 总存储事件数
     */
    private Long totalStoredEvents;
    
    /**
     * 新创建事件数
     */
    private Long newEventsCreated;
    
    /**
     * 更新事件数
     */
    private Long eventsUpdated;
    
    /**
     * 去重事件数
     */
    private Long duplicateEventsFound;
    
    /**
     * 字典更新次数
     */
    private Long dictionaryUpdates;
    
    /**
     * 批量操作次数
     */
    private Long batchOperations;
    
    /**
     * 平均存储时间（毫秒）
     */
    private Double averageStorageTime;
    
    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 存储成功率
     */
    private Double successRate;
    
    // 构造函数
    public StorageStats() {
        this.endTime = LocalDateTime.now();
    }
    
    // 计算成功率
    public void calculateSuccessRate() {
        if (totalStoredEvents != null && totalStoredEvents > 0) {
            long successfulOperations = (newEventsCreated != null ? newEventsCreated : 0) + 
                                      (eventsUpdated != null ? eventsUpdated : 0);
            this.successRate = (double) successfulOperations / totalStoredEvents;
        } else {
            this.successRate = 0.0;
        }
    }
    
    // Getter和Setter方法
    public Long getTotalStoredEvents() {
        return totalStoredEvents;
    }
    
    public void setTotalStoredEvents(Long totalStoredEvents) {
        this.totalStoredEvents = totalStoredEvents;
        calculateSuccessRate();
    }
    
    public Long getNewEventsCreated() {
        return newEventsCreated;
    }
    
    public void setNewEventsCreated(Long newEventsCreated) {
        this.newEventsCreated = newEventsCreated;
        calculateSuccessRate();
    }
    
    public Long getEventsUpdated() {
        return eventsUpdated;
    }
    
    public void setEventsUpdated(Long eventsUpdated) {
        this.eventsUpdated = eventsUpdated;
        calculateSuccessRate();
    }
    
    public Long getDuplicateEventsFound() {
        return duplicateEventsFound;
    }
    
    public void setDuplicateEventsFound(Long duplicateEventsFound) {
        this.duplicateEventsFound = duplicateEventsFound;
    }
    
    public Long getDictionaryUpdates() {
        return dictionaryUpdates;
    }
    
    public void setDictionaryUpdates(Long dictionaryUpdates) {
        this.dictionaryUpdates = dictionaryUpdates;
    }
    
    public Long getBatchOperations() {
        return batchOperations;
    }
    
    public void setBatchOperations(Long batchOperations) {
        this.batchOperations = batchOperations;
    }
    
    public Double getAverageStorageTime() {
        return averageStorageTime;
    }
    
    public void setAverageStorageTime(Double averageStorageTime) {
        this.averageStorageTime = averageStorageTime;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    @Override
    public String toString() {
        return "StorageStats{" +
                "totalStoredEvents=" + totalStoredEvents +
                ", newEventsCreated=" + newEventsCreated +
                ", eventsUpdated=" + eventsUpdated +
                ", duplicateEventsFound=" + duplicateEventsFound +
                ", dictionaryUpdates=" + dictionaryUpdates +
                ", batchOperations=" + batchOperations +
                ", averageStorageTime=" + averageStorageTime +
                ", successRate=" + successRate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}