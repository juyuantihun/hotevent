package com.hotech.events.service.impl;


import com.hotech.events.entity.User;
import com.hotech.events.mapper.UserMapper;
import com.hotech.events.model.dto.RegisterRequest;
import com.hotech.events.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User authenticate(String username, String password) {
        // 输入参数安全检查
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            log.debug("认证失败：用户名或密码为空");
            return null;
        }
        
        // 防止SQL注入 - 清理用户名
        String cleanUsername = username.trim();
        if (cleanUsername.length() > 20 || cleanUsername.length() < 3) {
            log.debug("认证失败：用户名长度不符合要求");
            return null;
        }
        
        // 检查用户名格式（只允许字母、数字、下划线和中文）
        if (!cleanUsername.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            log.debug("认证失败：用户名格式不正确");
            return null;
        }

        try {
            User user = userMapper.selectByUsername(cleanUsername);
            
            if (user == null) {
                log.warn("认证失败：用户不存在: {}", cleanUsername);
                return null;
            }

            log.debug("找到用户: {}, 状态: {}", cleanUsername, user.getStatus());

            if (user.getStatus() != null && user.getStatus() == 0) {
                log.warn("认证失败：用户已被禁用: {}", cleanUsername);
                return null;
            }

            // 记录密码验证详情（不记录实际密码）
            log.debug("开始验证用户密码: {}", cleanUsername);
            log.debug("数据库中的密码是否以$2a$开头: {}", user.getPassword() != null && user.getPassword().startsWith("$2a$"));
            
            // 使用密码编码器验证密码
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("用户认证成功: {}", username);
                return user;
            } else {
                log.warn("用户密码错误: {}, 数据库密码长度: {}", username, 
                    user.getPassword() != null ? user.getPassword().length() : 0);
                return null;
            }
        } catch (Exception e) {
            log.error("用户认证过程中发生错误: {}", username, e);
            return null;
        }
    }

    @Override
    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (registerRequest == null || 
            registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty() ||
            registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("注册信息不完整");
        }

        String username = registerRequest.getUsername().trim();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        // 输入安全验证
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度必须在3-20个字符之间");
        }
        
        if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字、下划线和中文字符");
        }
        
        if (password.length() < 6 || password.length() > 50) {
            throw new IllegalArgumentException("密码长度必须在6-50个字符之间");
        }
        
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        try {
            User user = new User();
            user.setUsername(username);
            // 使用密码编码器加密密码
            user.setPassword(passwordEncoder.encode(password));
            user.setStatus(1); // 默认启用
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            int result = userMapper.insert(user);
            
            if (result > 0) {
                log.info("用户注册成功: {}", username);
                return user;
            } else {
                throw new RuntimeException("用户注册失败");
            }
        } catch (Exception e) {
            log.error("用户注册过程中发生错误: {}", username, e);
            throw new RuntimeException("用户注册失败: " + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            return userMapper.selectByUsername(username.trim());
        } catch (Exception e) {
            log.error("根据用户名查询用户失败: {}", username, e);
            return null;
        }
    }

    @Override
    public User findById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        try {
            return userMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID查询用户失败: {}", id, e);
            return null;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try {
            User user = userMapper.selectByUsername(username.trim());
            return user != null;
        } catch (Exception e) {
            log.error("检查用户名是否存在失败: {}", username, e);
            return false;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        try {
            User user = userMapper.selectByEmail(email.trim());
            return user != null;
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: {}", email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String newPassword) {
        if (userId == null || userId <= 0 || 
            newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        try {
            // 使用密码编码器加密新密码
            String encodedPassword = passwordEncoder.encode(newPassword);
            int result = userMapper.updatePassword(userId, encodedPassword);
            
            if (result > 0) {
                log.info("用户密码更新成功: {}", userId);
                return true;
            } else {
                log.warn("用户密码更新失败，用户不存在: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户密码失败: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty() || 
            newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        try {
            User user = findByUsername(username);
            if (user == null) {
                log.warn("重置密码失败，用户不存在: {}", username);
                return false;
            }

            return updatePassword(user.getId(), newPassword);
        } catch (Exception e) {
            log.error("重置用户密码失败: {}", username, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateStatus(Long userId, Integer status) {
        if (userId == null || userId <= 0 || status == null) {
            return false;
        }

        try {
            int result = userMapper.updateStatus(userId, status);
            
            if (result > 0) {
                log.info("用户状态更新成功: {} -> {}", userId, status);
                return true;
            } else {
                log.warn("用户状态更新失败，用户不存在: {}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", userId, e);
            return false;
        }
    }
}