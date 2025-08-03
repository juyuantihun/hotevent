package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 诊断问题数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisIssue {
    
    /**
     * 问题类型
     */
    private IssueType type;
    
    /**
     * 问题严重程度
     */
    private IssueSeverity severity;
    
    /**
     * 问题描述
     */
    private String description;
    
    /**
     * 修复建议
     */
    private String recommendation;
    
    /**
     * 问题详细信息
     */
    private Map<String, Object> details;
    
    /**
     * 是否可自动修复
     */
    private boolean autoRepairable;
    
    /**
     * 问题类型枚举
     */
    public enum IssueType {
        MISSING_ASSOCIATION("缺少关联"),
        INVALID_ASSOCIATION("无效关联"),
        EVENT_STATUS_ISSUE("事件状态问题"),
        DATA_INCONSISTENCY("数据不一致"),
        ORPHANED_DATA("孤立数据"),
        DUPLICATE_DATA("重复数据"),
        PERFORMANCE_ISSUE("性能问题");
        
        private final String description;
        
        IssueType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 问题严重程度枚举
     */
    public enum IssueSeverity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");
        
        private final String description;
        
        IssueSeverity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}