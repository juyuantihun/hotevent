package com.hotech.events.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注册响应DTO
 */
@Data
@Schema(description = "注册响应")
public class RegisterResponse {
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "用户信息")
    private UserInfoResponse user;
}