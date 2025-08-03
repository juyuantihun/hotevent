package com.hotech.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 修复操作数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairAction {
    
    /**
     * 操作类型
     */
    private ActionType type;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 是否成功
     */
    private boolean successful;
    
    /**
     * 错误消息（如果失败）
     */
    private String errorMessage;
    
    /**
     * 操作详细信息
     */
    private Map<String, Object> details;
    
    /**
     * 影响的记录数量
     */
    private int affectedRecords;
    
    /**
     * 操作类型枚举
     */
    public enum ActionType {
        CREATE_ASSOCIATION("创建关联"),
        DELETE_ASSOCIATION("删除关联"),
        UPDATE_EVENT_COUNT("更新事件数量"),
        FIX_EVENT_STATUS("修复事件状态"),
        CLEAN_DUPLICATE("清理重复数据"),
        REBUILD_ASSOCIATION("重建关联"),
        SYNC_DATA("同步数据");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}