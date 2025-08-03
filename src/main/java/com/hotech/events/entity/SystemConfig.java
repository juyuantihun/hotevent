package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_config")
public class SystemConfig {

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置类型：STRING-字符串，NUMBER-数字，BOOLEAN-布尔值，JSON-JSON对象
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置分组：deepseek-DeepSeek配置，validation-验证配置，storage-存储配置
     */
    @TableField("config_group")
    private String configGroup;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否加密：0-否，1-是
     */
    @TableField("is_encrypted")
    private Boolean isEncrypted;

    /**
     * 是否必需：0-否，1-是
     */
    @TableField("is_required")
    private Boolean isRequired;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 验证规则（正则表达式或JSON Schema）
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING("STRING", "字符串"),
        NUMBER("NUMBER", "数字"),
        BOOLEAN("BOOLEAN", "布尔值"),
        JSON("JSON", "JSON对象");

        private final String code;
        private final String description;

        ConfigType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigType fromCode(String code) {
            for (ConfigType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的配置类型: " + code);
        }
    }

    /**
     * 配置分组枚举
     */
    public enum ConfigGroup {
        DEEPSEEK("deepseek", "DeepSeek配置"),
        VALIDATION("validation", "验证配置"),
        STORAGE("storage", "存储配置"),
        PROMPT("prompt", "提示词配置");

        private final String code;
        private final String description;

        ConfigGroup(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigGroup fromCode(String code) {
            for (ConfigGroup group : values()) {
                if (group.code.equals(code)) {
                    return group;
                }
            }
            throw new IllegalArgumentException("未知的配置分组: " + code);
        }
    }
}