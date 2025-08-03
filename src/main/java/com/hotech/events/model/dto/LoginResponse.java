package com.hotech.events.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {
    
    @Schema(description = "认证令牌")
    private String token;
    
    @Schema(description = "用户信息")
    private UserInfoResponse user;
}