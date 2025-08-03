package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典实体类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dictionary")
public class Dictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 50, message = "字典类型长度不能超过50个字符")
    @TableField("dict_type")
    private String dictType;

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码长度不能超过100个字符")
    @TableField("dict_code")
    private String dictCode;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 200, message = "字典名称长度不能超过200个字符")
    @TableField("dict_name")
    private String dictName;

    /**
     * 字典描述
     */
    @TableField("dict_description")
    private String dictDescription;

    /**
     * 父级ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否自动添加：0-否，1-是
     */
    @TableField("is_auto_added")
    private Integer isAutoAdded;

    /**
     * 实体类型
     */
    @TableField("entity_type")
    private String entityType;

    /**
     * 实体ID
     */
    @TableField("entity_id")
    private Long entityId;

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
     * 构造函数，用于验证必填字段
     */
    public Dictionary(String dictType, String dictCode, String dictName) {
        if (dictType == null || dictType.trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        if (dictCode == null || dictCode.trim().isEmpty()) {
            throw new IllegalArgumentException("字典编码不能为空");
        }
        if (dictName == null || dictName.trim().isEmpty()) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
        
        this.dictType = dictType;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.parentId = 0L;
        this.sortOrder = 0;
        this.status = 1;
        this.isAutoAdded = 0;
    }
} 