package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("country")
public class Country {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;        // 国家名称
    private String shortName;   // 简称
    private Long population;    // 人口
    private Double area;        // 面积
    private String capital;     // 首都
    private String language;    // 语言
    private String currency;    // 货币
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 