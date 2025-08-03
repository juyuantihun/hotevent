package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("person")
public class Person {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 10)
    private String gender;

    private LocalDate birthDate;

    private Long countryId; // 国籍
    private Long organizationId; // 所属组织

    @Size(max = 255)
    private String description;

    private Integer status = 1;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
} 