package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 诊断结果数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResult {
    
    /**
     * 时间线ID
     */
    private Long timelineId;
    
    /**
     * 时间线名称
     */
    private String timelineName;
    
    /**
     * 诊断时间
     */
    private LocalDateTime diagnosisTime;
    
    /**
     * 诊断状态
     */
    private DiagnosisStatus status;
    
    /**
     * 发现的问题列表
     */
    private List<DiagnosisIssue> issues;
    
    /**
     * 数据一致性信息
     */
    private DataConsistencyInfo consistencyInfo;
    
    /**
     * 事件关联信息
     */
    private EventAssociationInfo associationInfo;
    
    /**
     * 事件状态信息
     */
    private EventStatusInfo statusInfo;
    
    /**
     * 诊断统计信息
     */
    private Map<String, Object> statistics;
    
    /**
     * 诊断状态枚举
     */
    public enum DiagnosisStatus {
        HEALTHY("健康"),
        ISSUES_FOUND("发现问题"),
        CRITICAL_ISSUES("严重问题"),
        DIAGNOSIS_FAILED("诊断失败");
        
        private final String description;
        
        DiagnosisStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 数据一致性信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataConsistencyInfo {
        private int expectedEventCount;
        private int actualEventCount;
        private int missingAssociations;
        private int invalidAssociations;
        private boolean isConsistent;
    }
    
    /**
     * 事件关联信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAssociationInfo {
        private int totalAssociations;
        private int validAssociations;
        private int invalidEventIds;
        private int invalidTimelineIds;
        private List<Long> orphanedEventIds;
    }
    
    /**
     * 事件状态信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventStatusInfo {
        private int totalEvents;
        private int enabledEvents;
        private int disabledEvents;
        private int eventsWithoutStatus;
        private List<Long> disabledEventIds;
    }
}