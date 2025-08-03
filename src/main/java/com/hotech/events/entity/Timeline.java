package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间线实体类
 * 用于存储时间线基本信息
 */
@Data
@TableName("timeline")
public class Timeline {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 时间线名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 时间线描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /**
     * 状态：GENERATING(生成中), COMPLETED(已完成), FAILED(失败)
     */
    @TableField("status")
    private String status;
    
    /**
     * 事件数量
     */
    @TableField("event_count")
    private Integer eventCount;
    
    /**
     * 关系数量
     */
    @TableField("relation_count")
    private Integer relationCount;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 时间线状态枚举
     */
    public static class Status {
        public static final String GENERATING = "GENERATING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
    }
}