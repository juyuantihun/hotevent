package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置变更历史实体类
 * 
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("config_change_log")
public class ConfigChangeLog {

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 旧值
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 新值
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 变更类型：CREATE-创建，UPDATE-更新，DELETE-删除
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 变更原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 变更人
     */
    @TableField("changed_by")
    private String changedBy;

    /**
     * 变更时间
     */
    @TableField(value = "changed_at", fill = FieldFill.INSERT)
    private LocalDateTime changedAt;

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        CREATE("CREATE", "创建"),
        UPDATE("UPDATE", "更新"),
        DELETE("DELETE", "删除");

        private final String code;
        private final String description;

        ChangeType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ChangeType fromCode(String code) {
            for (ChangeType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的变更类型: " + code);
        }
    }
}