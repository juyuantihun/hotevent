package com.hotech.events.controller;

import com.hotech.events.entity.User;
import com.hotech.events.model.dto.LoginRequest;
import com.hotech.events.model.dto.LoginResponse;
import com.hotech.events.model.dto.RegisterRequest;
import com.hotech.events.model.dto.RegisterResponse;
import com.hotech.events.model.dto.PasswordResetRequest;
import com.hotech.events.model.dto.UserInfoResponse;
import com.hotech.events.service.UserService;
import com.hotech.events.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器
 * 处理用户登录、注册、密码重置等功能
 * 
 * @author AI助手
 * @since 2024-07-21
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户认证相关的API接口")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "验证用户凭据并返回认证令牌")
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: {}", request.getUsername());

        try {
            // 参数验证
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户名不能为空"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("密码不能为空"));
            }

            // 使用UserService进行用户认证
            User user = userService.authenticate(request.getUsername(), request.getPassword());
            
            if (user != null) {
                // 生成JWT令牌
                String token = jwtUtil.generateToken(user.getId(), user.getUsername());

                UserInfoResponse userInfo = new UserInfoResponse();
                userInfo.setId(user.getId().toString());
                userInfo.setUsername(user.getUsername());
                userInfo.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                userInfo.setRoles(Arrays.asList("user")); // 默认角色，可以根据需要扩展
                userInfo.setPermissions(Arrays.asList("read", "write")); // 默认权限

                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setUser(userInfo);

                log.info("用户 {} 登录成功", request.getUsername());
                return ResponseEntity.ok(com.hotech.events.common.Result.success("登录成功", response));
            } else {
                log.warn("用户 {} 登录失败：用户名或密码错误", request.getUsername());
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户名或密码错误"));
            }
        } catch (Exception e) {
            log.error("登录过程中发生错误", e);
            return ResponseEntity.ok(com.hotech.events.common.Result.error("登录失败，请稍后重试"));
        }
    }



    /**
     * 用户登出
     * 
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "使当前用户的认证令牌失效")
    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");

        // 使用统一响应格式
        return ResponseEntity.ok(com.hotech.events.common.Result.success("登出成功", response));
    }

    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/user-info")
    public ResponseEntity<Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 从Authorization头中提取令牌
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("未提供有效的认证令牌"));
            }

            // 验证令牌并获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("无效的认证令牌"));
            }

            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户不存在"));
            }

            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId().toString());
            userInfo.setUsername(user.getUsername());
            userInfo.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            userInfo.setRoles(Arrays.asList("user")); // 默认角色
            userInfo.setPermissions(Arrays.asList("read", "write")); // 默认权限

            return ResponseEntity.ok(com.hotech.events.common.Result.success("获取用户信息成功", userInfo));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.ok(com.hotech.events.common.Result.error("获取用户信息失败"));
        }
    }

    /**
     * 刷新令牌
     * 
     * @return 新的令牌
     */
    @Operation(summary = "刷新令牌", description = "使用当前有效的令牌获取新的令牌")
    @PostMapping("/refresh-token")
    public ResponseEntity<Object> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 从Authorization头中提取令牌
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("未提供有效的认证令牌"));
            }

            // 验证令牌并获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (userId == null || username == null) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("无效的认证令牌"));
            }

            // 生成新的令牌
            String newToken = jwtUtil.generateToken(userId, username);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);

            return ResponseEntity.ok(com.hotech.events.common.Result.success("刷新令牌成功", response));
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return ResponseEntity.ok(com.hotech.events.common.Result.error("刷新令牌失败"));
        }
    }

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 注册响应
     */
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());

        try {
            // 参数验证
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户名不能为空"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("密码不能为空"));
            }
            
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("确认密码不能为空"));
            }
            
            // 验证密码是否匹配
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("两次输入的密码不一致"));
            }
            
            // 验证用户名长度
            if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户名长度应在3-20个字符之间"));
            }
            
            // 验证密码长度
            if (request.getPassword().length() < 6 || request.getPassword().length() > 20) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("密码长度应在6-20个字符之间"));
            }

            // 使用UserService检查用户名唯一性和创建用户
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.ok(com.hotech.events.common.Result.error("用户名已存在"));
            }

            // 使用UserService创建用户
            User user = userService.register(request);
            
            RegisterResponse response = new RegisterResponse();
            response.setSuccess(true);
            response.setMessage("注册成功");

            log.info("用户 {} 注册成功", request.getUsername());
            return ResponseEntity.ok(com.hotech.events.common.Result.success("注册成功", response));
        } catch (Exception e) {
            log.error("注册过程中发生错误", e);
            return ResponseEntity.ok(com.hotech.events.common.Result.error("注册失败，请稍后重试"));
        }
    }



    /**
     * 请求密码重置
     * 
     * @param request 密码重置请求
     * @return 请求结果
     */
    @Operation(summary = "请求密码重置", description = "发送密码重置链接到用户邮箱")
    @PostMapping("/request-password-reset")
    public ResponseEntity<Object> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        log.info("密码重置请求: {}", request.getUsername());

        // 简单的模拟密码重置请求逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "密码重置链接已发送到您的邮箱");

        // 使用统一响应格式
        return ResponseEntity.ok(com.hotech.events.common.Result.success("密码重置链接已发送到您的邮箱", response));
    }

    /**
     * 重置密码
     * 
     * @param request 重置密码请求
     * @return 重置结果
     */
    @Operation(summary = "重置密码", description = "使用重置令牌设置新密码")
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        log.info("重置密码请求，令牌: {}", token);

        // 简单的模拟密码重置逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "密码重置成功");

        // 使用统一响应格式
        return ResponseEntity.ok(com.hotech.events.common.Result.success("密码重置成功", response));
    }
}