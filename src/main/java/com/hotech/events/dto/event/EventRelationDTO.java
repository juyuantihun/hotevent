package com.hotech.events.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 事件关联关系DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 源事件ID
     */
    @NotNull(message = "源事件ID不能为空")
    private Long sourceEventId;

    /**
     * 目标事件ID
     */
    @NotNull(message = "目标事件ID不能为空")
    private Long targetEventId;

    /**
     * 关联类型：导致、影响、关联等
     */
    @NotNull(message = "关联类型不能为空")
    @Size(max = 50, message = "关联类型长度不能超过50个字符")
    private String relationType;

    /**
     * 关系名称（必填）
     */
    @NotNull(message = "关系名称不能为空")
    @Size(max = 100, message = "关系名称长度不能超过100个字符")
    private String relationName;

    /**
     * 强度级别（选填）
     */
    private Integer intensityLevel;

    /**
     * 关联描述
     */
    private String relationDescription;

    /**
     * 置信度（0-1）
     */
    private BigDecimal confidence;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 源事件信息
     */
    private EventDTO sourceEvent;

    /**
     * 目标事件信息
     */
    private EventDTO targetEvent;

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
} 