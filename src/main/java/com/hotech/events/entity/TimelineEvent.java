package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间线事件关联实体类
 * 用于存储时间线与事件的多对多关系
 */
@Data
@TableName("timeline_event")
public class TimelineEvent {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 时间线ID
     */
    @TableField("timeline_id")
    private Long timelineId;
    
    /**
     * 事件ID
     */
    @TableField("event_id")
    private Long eventId;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}