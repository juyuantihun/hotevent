package com.hotech.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成时间线请求DTO
 */
@Data
public class TimelineGenerateRequest {
    
    /**
     * 时间线名称
     */
    @NotBlank(message = "时间线名称不能为空")
    @Size(max = 100, message = "时间线名称长度不能超过100个字符")
    private String name;
    
    /**
     * 时间线描述
     */
    @Size(max = 500, message = "时间线描述长度不能超过500个字符")
    private String description;
    
    /**
     * 地区ID列表
     */
    @NotEmpty(message = "地区ID列表不能为空")
    private List<Long> regionIds;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;
    
    /**
     * 是否启用去重
     */
    private Boolean enableDeduplication = true;
    
    /**
     * 是否启用字典管理
     */
    private Boolean enableDictionary = true;
    
    /**
     * 是否启用关系分析
     */
    private Boolean enableRelationAnalysis = true;
}