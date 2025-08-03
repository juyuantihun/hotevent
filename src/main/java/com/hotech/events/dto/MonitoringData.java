package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 监控数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringData {
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 时间线ID
     */
    private Long timelineId;
    
    /**
     * 事件ID
     */
    private Long eventId;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 是否成功
     */
    private boolean successful;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 执行时长（毫秒）
     */
    private Long duration;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 附加详细信息
     */
    private Map<String, Object> details;
    
    /**
     * 监控事件类型枚举
     */
    public static class EventType {
        public static final String TIMELINE_CREATED = "TIMELINE_CREATED";
        public static final String TIMELINE_UPDATED = "TIMELINE_UPDATED";
        public static final String TIMELINE_DELETED = "TIMELINE_DELETED";
        public static final String EVENT_ASSOCIATED = "EVENT_ASSOCIATED";
        public static final String EVENT_DISASSOCIATED = "EVENT_DISASSOCIATED";
        public static final String DIAGNOSIS_PERFORMED = "DIAGNOSIS_PERFORMED";
        public static final String REPAIR_EXECUTED = "REPAIR_EXECUTED";
        public static final String GENERATION_STARTED = "GENERATION_STARTED";
        public static final String GENERATION_COMPLETED = "GENERATION_COMPLETED";
        public static final String GENERATION_FAILED = "GENERATION_FAILED";
    }
    
    /**
     * 操作类型枚举
     */
    public static class Operation {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String QUERY = "QUERY";
        public static final String ASSOCIATE = "ASSOCIATE";
        public static final String DIAGNOSE = "DIAGNOSE";
        public static final String REPAIR = "REPAIR";
        public static final String GENERATE = "GENERATE";
    }
}