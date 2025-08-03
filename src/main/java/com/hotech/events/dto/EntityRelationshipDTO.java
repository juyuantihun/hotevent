package com.hotech.events.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class EntityRelationshipDTO {
    private Long id;

    @NotBlank(message = "源实体类型不能为空")
    @Size(max = 32, message = "源实体类型长度不能超过32个字符")
    private String sourceEntityType; // country/organization/person

    @NotNull(message = "源实体ID不能为空")
    private Long sourceEntityId;

    @NotBlank(message = "目标实体类型不能为空")
    @Size(max = 32, message = "目标实体类型长度不能超过32个字符")
    private String targetEntityType; // country/organization/person

    @NotNull(message = "目标实体ID不能为空")
    private Long targetEntityId;

    @NotBlank(message = "关系类型不能为空")
    @Size(max = 50, message = "关系类型长度不能超过50个字符")
    private String relationshipType;

    @Size(max = 500, message = "关系描述长度不能超过500个字符")
    private String relationshipDescription;

    private Integer status;

    // 扩展字段：源实体信息
    private String sourceEntityName;
    
    // 扩展字段：目标实体信息
    private String targetEntityName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
} 