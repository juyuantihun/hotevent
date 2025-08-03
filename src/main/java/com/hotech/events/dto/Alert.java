package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 警告信息数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    
    /**
     * 警告ID
     */
    private Long id;
    
    /**
     * 警告类型
     */
    private AlertType type;
    
    /**
     * 警告严重程度
     */
    private AlertSeverity severity;
    
    /**
     * 警告消息
     */
    private String message;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否已解决
     */
    private boolean resolved;
    
    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;
    
    /**
     * 相关时间线ID
     */
    private Long timelineId;
    
    /**
     * 相关事件ID
     */
    private Long eventId;
    
    /**
     * 警告详细信息
     */
    private Map<String, Object> details;
    
    /**
     * 警告类型枚举
     */
    public enum AlertType {
        DATA_INCONSISTENCY("数据不一致"),
        INVALID_ASSOCIATION("无效关联"),
        GENERATION_TIMEOUT("生成超时"),
        HIGH_ERROR_RATE("高错误率"),
        PERFORMANCE_DEGRADATION("性能下降"),
        SYSTEM_ERROR("系统错误"),
        DUPLICATE_DATA("重复数据"),
        ORPHANED_DATA("孤立数据");
        
        private final String description;
        
        AlertType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 警告严重程度枚举
     */
    public enum AlertSeverity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");
        
        private final String description;
        
        AlertSeverity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}