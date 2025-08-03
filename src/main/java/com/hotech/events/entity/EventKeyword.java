package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件关键词实体类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("event_keyword")
public class EventKeyword implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件ID
     */
    @NotNull(message = "事件ID不能为空")
    @TableField("event_id")
    private Long eventId;

    /**
     * 关键词
     */
    @NotBlank(message = "关键词不能为空")
    @Size(max = 100, message = "关键词长度不能超过100个字符")
    @TableField("keyword")
    private String keyword;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 构造函数，用于验证必填字段
     */
    public EventKeyword(Long eventId, String keyword) {
        if (eventId == null) {
            throw new IllegalArgumentException("事件ID不能为空");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        
        this.eventId = eventId;
        this.keyword = keyword;
    }
} 