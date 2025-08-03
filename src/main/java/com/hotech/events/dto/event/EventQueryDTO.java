package com.hotech.events.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件查询DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件主体
     */
    private String subject;

    /**
     * 事件客体
     */
    private String object;

    /**
     * 事件地点
     */
    private String eventLocation;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 来源类型：1-自动获取，2-手动录入
     */
    private Integer sourceType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页显示条数
     */
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String sortField = "event_time";

    /**
     * 排序方式：asc-升序，desc-降序
     */
    private String sortOrder = "desc";
} 