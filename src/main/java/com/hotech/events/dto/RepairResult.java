package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 修复结果数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairResult {
    
    /**
     * 时间线ID
     */
    private Long timelineId;
    
    /**
     * 修复时间
     */
    private LocalDateTime repairTime;
    
    /**
     * 修复状态
     */
    private RepairStatus status;
    
    /**
     * 执行的修复操作列表
     */
    private List<RepairAction> actions;
    
    /**
     * 修复统计信息
     */
    private RepairStatistics statistics;
    
    /**
     * 修复消息
     */
    private String message;
    
    /**
     * 修复状态枚举
     */
    public enum RepairStatus {
        SUCCESS("修复成功"),
        PARTIAL_SUCCESS("部分修复成功"),
        FAILED("修复失败"),
        NO_ISSUES_FOUND("未发现需要修复的问题");
        
        private final String description;
        
        RepairStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 修复统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepairStatistics {
        private int totalIssuesFound;
        private int issuesRepaired;
        private int issuesFailed;
        private int associationsCreated;
        private int associationsDeleted;
        private int eventCountsUpdated;
        private int statusesFixed;
    }
}