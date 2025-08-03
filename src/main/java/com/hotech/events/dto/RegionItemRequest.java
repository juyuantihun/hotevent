package com.hotech.events.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 地区项目请求DTO
 */
@Data
public class RegionItemRequest {
    
    /**
     * 字典项ID
     */
    @NotNull(message = "字典项ID不能为空")
    private Long dictionaryId;
}