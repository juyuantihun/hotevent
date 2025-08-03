package com.hotech.events.dto.event;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量事件请求DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
public class BatchEventRequestDTO {

    /**
     * 事件列表
     */
    @Valid
    @NotEmpty(message = "事件列表不能为空")
    private List<EventDTO> events;

    /**
     * 事件关联关系列表
     */
    @Valid
    private List<EventRelationDTO> relations;
} 