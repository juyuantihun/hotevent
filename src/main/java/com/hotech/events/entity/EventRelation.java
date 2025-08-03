package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件关系实体类
 */
@Data
@TableName("event_relation")
public class EventRelation {
    
    /**
     * 关系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 源事件ID
     */
    @TableField("source_event_id")
    private Long sourceEventId;
    
    /**
     * 目标事件ID
     */
    @TableField("target_event_id")
    private Long targetEventId;
    
    /**
     * 关系类型
     */
    @TableField("relation_type")
    private String relationType;
    
    /**
     * 关系描述
     */
    @TableField("relation_description")
    private String relationDescription;
    
    /**
     * 置信度
     */
    @TableField("confidence")
    private Double confidence;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
    
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
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;
    
    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;
}