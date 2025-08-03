package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@TableName("entity_relationship")
public class EntityRelationship {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "源实体类型不能为空")
    @Size(max = 32, message = "源实体类型长度不能超过32个字符")
    @TableField("source_entity_type")
    private String sourceEntityType; // country/organization/person

    @TableField("source_entity_id")
    private Long sourceEntityId;

    @NotBlank(message = "目标实体类型不能为空")
    @Size(max = 32, message = "目标实体类型长度不能超过32个字符")
    @TableField("target_entity_type")
    private String targetEntityType; // country/organization/person

    @TableField("target_entity_id")
    private Long targetEntityId;

    @NotBlank(message = "关系类型不能为空")
    @Size(max = 50, message = "关系类型长度不能超过50个字符")
    @TableField("relationship_type")
    private String relationshipType;

    @Size(max = 500, message = "关系描述长度不能超过500个字符")
    @TableField("relationship_description")
    private String relationshipDescription;

    private Integer status = 1;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // 构造函数用于验证
    public EntityRelationship(String sourceEntityType, Long sourceEntityId, 
                             String targetEntityType, Long targetEntityId, 
                             String relationshipType) {
        if (sourceEntityType == null || sourceEntityType.trim().isEmpty()) {
            throw new IllegalArgumentException("源实体类型不能为空");
        }
        if (targetEntityType == null || targetEntityType.trim().isEmpty()) {
            throw new IllegalArgumentException("目标实体类型不能为空");
        }
        if (relationshipType == null || relationshipType.trim().isEmpty()) {
            throw new IllegalArgumentException("关系类型不能为空");
        }
        if (sourceEntityId == null || targetEntityId == null) {
            throw new IllegalArgumentException("实体ID不能为空");
        }
        if (sourceEntityType.equals(targetEntityType) && sourceEntityId.equals(targetEntityId)) {
            throw new IllegalArgumentException("不能建立自己与自己的关系");
        }
        
        this.sourceEntityType = sourceEntityType;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
        this.relationshipType = relationshipType;
        this.status = 1;
    }

    public EntityRelationship() {}
} 