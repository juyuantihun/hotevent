package com.hotech.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 主体客体关系DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectObjectRelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 主体编码（来自dictionary表）
     */
    @NotBlank(message = "主体编码不能为空")
    @Size(max = 100, message = "主体编码长度不能超过100个字符")
    private String subjectCode;

    /**
     * 主体名称（从dictionary表关联获取）
     */
    private String subjectName;

    /**
     * 客体编码（来自dictionary表）
     */
    @NotBlank(message = "客体编码不能为空")
    @Size(max = 100, message = "客体编码长度不能超过100个字符")
    private String objectCode;

    /**
     * 客体名称（从dictionary表关联获取）
     */
    private String objectName;

    /**
     * 关系类型
     */
    @NotBlank(message = "关系类型不能为空")
    @Size(max = 100, message = "关系类型长度不能超过100个字符")
    private String relationType;

    /**
     * 关系类型名称（从dictionary表关联获取）
     */
    private String relationTypeName;

    /**
     * 关系名称
     */
    @NotBlank(message = "关系名称不能为空")
    @Size(max = 200, message = "关系名称长度不能超过200个字符")
    private String relationName;

    /**
     * 强度级别（1-5，1最弱，5最强）
     */
    @NotNull(message = "强度级别不能为空")
    @Min(value = 1, message = "强度级别最小值为1")
    @Max(value = 5, message = "强度级别最大值为5")
    private Integer intensityLevel;

    /**
     * 强度级别名称
     */
    private String intensityLevelName;

    /**
     * 关系描述
     */
    @Size(max = 1000, message = "关系描述长度不能超过1000个字符")
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 获取强度级别名称
     */
    public String getIntensityLevelName() {
        if (intensityLevel == null) return null;
        switch (intensityLevel) {
            case 1:
                return "非常弱";
            case 2:
                return "弱";
            case 3:
                return "中等";
            case 4:
                return "强";
            case 5:
                return "非常强";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return null;
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "未知";
        }
    }
} 