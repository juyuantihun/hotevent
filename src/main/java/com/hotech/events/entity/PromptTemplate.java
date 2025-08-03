package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 提示词模板实体类
 * 
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("prompt_template")
public class PromptTemplate {

    /**
     * 模板ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型：event_fetch-事件检索，event_validation-事件验证，timeline_organize-时间线编制
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 模板内容
     */
    @TableField("template_content")
    private String templateContent;

    /**
     * 响应格式模板
     */
    @TableField("response_format")
    private String responseFormat;

    /**
     * 版本号
     */
    @TableField("version")
    private String version;

    /**
     * 是否激活：0-否，1-是
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

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
     * 模板类型枚举
     */
    public enum TemplateType {
        EVENT_FETCH("event_fetch", "事件检索"),
        EVENT_VALIDATION("event_validation", "事件验证"),
        TIMELINE_ORGANIZE("timeline_organize", "时间线编制");

        private final String code;
        private final String description;

        TemplateType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateType fromCode(String code) {
            for (TemplateType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的模板类型: " + code);
        }
    }
}