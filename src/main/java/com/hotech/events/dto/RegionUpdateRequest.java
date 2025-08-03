package com.hotech.events.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新地区请求DTO
 */
@Data
public class RegionUpdateRequest {
    
    /**
     * 地区ID
     */
    @NotNull(message = "地区ID不能为空")
    private Long id;
    
    /**
     * 地区名称
     */
    @NotBlank(message = "地区名称不能为空")
    @Size(max = 100, message = "地区名称长度不能超过100个字符")
    private String name;
    
    /**
     * 地区类型
     */
    @NotBlank(message = "地区类型不能为空")
    @Size(max = 20, message = "地区类型长度不能超过20个字符")
    private String type;
    
    /**
     * 地区描述
     */
    @Size(max = 500, message = "地区描述长度不能超过500个字符")
    private String description;
    
    /**
     * 字典项ID列表
     */
    @NotNull(message = "字典项ID列表不能为空")
    private List<Long> dictionaryIds;
}