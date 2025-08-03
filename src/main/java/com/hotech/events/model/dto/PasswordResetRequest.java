package com.hotech.events.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 密码重置请求DTO
 */
@Data
@Schema(description = "密码重置请求")
public class PasswordResetRequest {
    
    @Schema(description = "用户名")
    private String username;
}