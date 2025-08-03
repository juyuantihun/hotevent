package com.hotech.events.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户信息响应DTO
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoResponse {
    
    @Schema(description = "用户ID")
    private String id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "显示名称")
    private String name;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "角色列表")
    private List<String> roles;
    
    @Schema(description = "权限列表")
    private List<String> permissions;
}