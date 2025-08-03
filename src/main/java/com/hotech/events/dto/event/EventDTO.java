package com.hotech.events.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private Long id;

    /**
     * 事件编码
     */
    @Size(max = 100, message = "事件编码长度不能超过100个字符")
    private String eventCode;

    /**
     * 事件发生时间
     */
    @NotNull(message = "事件时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;

    /**
     * 事件地点
     */
    @Size(max = 500, message = "事件地点长度不能超过500个字符")
    private String eventLocation;

    /**
     * 事件类型
     */
    @Size(max = 100, message = "事件类型长度不能超过100个字符")
    private String eventType;

    /**
     * 事件标题
     */
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String eventTitle;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件主体
     */
    @Size(max = 200, message = "事件主体长度不能超过200个字符")
    private String subject;

    /**
     * 事件客体
     */
    @Size(max = 200, message = "事件客体长度不能超过200个字符")
    private String object;

    /**
     * 主体客体关系类型
     */
    @Size(max = 50, message = "关系类型长度不能超过50个字符")
    private String relationType;

    /**
     * 主体客体关系名称
     */
    @Size(max = 100, message = "关系名称长度不能超过100个字符")
    private String relationName;

    /**
     * 关系强度等级(1-5)
     */
    private Integer intensityLevel;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 来源类型：1-自动获取，2-手动录入
     */
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 关键词列表
     */
    private List<String> keywords;

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