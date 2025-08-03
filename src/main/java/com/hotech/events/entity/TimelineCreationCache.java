package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间线创建缓存实体类
 * 用于防止重复创建时间线
 */
@Data
@TableName("timeline_creation_cache")
public class TimelineCreationCache {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 请求指纹 - 基于请求参数生成的唯一标识
     */
    @TableField("request_fingerprint")
    private String requestFingerprint;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 时间线名称
     */
    @TableField("timeline_name")
    private String timelineName;
    
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
     * 地区ID列表（JSON格式存储）
     */
    @TableField("region_ids")
    private String regionIds;
    
    /**
     * 关联的时间线ID
     */
    @TableField("timeline_id")
    private Long timelineId;
    
    /**
     * 状态：CREATING(创建中), COMPLETED(已完成), FAILED(失败)
     */
    @TableField("status")
    private String status;
    
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
     * 过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * 状态常量
     */
    public static class Status {
        public static final String CREATING = "CREATING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
    }
}