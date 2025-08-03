package com.hotech.events.service;

import com.hotech.events.entity.User;
import com.hotech.events.model.dto.LoginRequest;
import com.hotech.events.model.dto.RegisterRequest;

/**
 * 用户服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 用户认证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，认证失败返回null
     */
    User authenticate(String username, String password);

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 创建的用户对象
     */
    User register(RegisterRequest registerRequest);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);

    /**
     * 根据用户ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Long id);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 更新用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    boolean updatePassword(Long userId, String newPassword);

    /**
     * 重置密码
     * 
     * @param username 用户名
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String username, String newPassword);

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long userId, Integer status);
}