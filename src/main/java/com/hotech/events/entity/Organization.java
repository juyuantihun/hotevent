package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@TableName("organization")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String shortName;

    @Size(max = 50)
    private String type; // 组织类型，如国际组织、政府、企业等

    private Long countryId; // 所属国家，可为null

    @Size(max = 255)
    private String description;

    private Integer status = 1;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
} 