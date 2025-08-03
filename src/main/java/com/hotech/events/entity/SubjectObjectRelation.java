package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 主体客体关系实体
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("subject_object_relation")
public class SubjectObjectRelation {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主体编码（来自dictionary表）
     */
    @NotBlank(message = "主体编码不能为空")
    @Size(max = 100, message = "主体编码长度不能超过100个字符")
    @TableField("subject_code")
    private String subjectCode;

    /**
     * 客体编码（来自dictionary表）
     */
    @NotBlank(message = "客体编码不能为空")
    @Size(max = 100, message = "客体编码长度不能超过100个字符")
    @TableField("object_code")
    private String objectCode;

    /**
     * 关系类型
     */
    @NotBlank(message = "关系类型不能为空")
    @Size(max = 100, message = "关系类型长度不能超过100个字符")
    @TableField("relation_type")
    private String relationType;

    /**
     * 关系名称
     */
    @NotBlank(message = "关系名称不能为空")
    @Size(max = 200, message = "关系名称长度不能超过200个字符")
    @TableField("relation_name")
    private String relationName;

    /**
     * 强度级别（1-5，1最弱，5最强）
     */
    @NotNull(message = "强度级别不能为空")
    @Min(value = 1, message = "强度级别最小值为1")
    @Max(value = 5, message = "强度级别最大值为5")
    @TableField("intensity_level")
    private Integer intensityLevel;

    /**
     * 关系描述
     */
    @Size(max = 1000, message = "关系描述长度不能超过1000个字符")
    @TableField("description")
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status;

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
     * 规范构造函数
     */
    public SubjectObjectRelation(String subjectCode, String objectCode, String relationType, 
                                String relationName, Integer intensityLevel, String description) {
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            throw new IllegalArgumentException("主体编码不能为空");
        }
        if (objectCode == null || objectCode.trim().isEmpty()) {
            throw new IllegalArgumentException("客体编码不能为空");
        }
        if (relationType == null || relationType.trim().isEmpty()) {
            throw new IllegalArgumentException("关系类型不能为空");
        }
        if (relationName == null || relationName.trim().isEmpty()) {
            throw new IllegalArgumentException("关系名称不能为空");
        }
        if (intensityLevel == null || intensityLevel < 1 || intensityLevel > 5) {
            throw new IllegalArgumentException("强度级别必须在1-5之间");
        }
        
        this.subjectCode = subjectCode.trim();
        this.objectCode = objectCode.trim();
        this.relationType = relationType.trim();
        this.relationName = relationName.trim();
        this.intensityLevel = intensityLevel;
        this.description = description != null ? description.trim() : null;
        this.status = 1; // 默认启用
    }
} 