package com.hotech.events.service;

import com.hotech.events.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 * 负责管理系统配置和环境变量
 * 
 * @author system
 * @since 2025-01-24
 */
public interface SystemConfigService {
    
    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，如果不存在则返回默认值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 根据配置键获取布尔类型配置值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    Boolean getBooleanConfig(String configKey, Boolean defaultValue);
    
    /**
     * 根据配置键获取整数类型配置值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 整数值
     */
    Integer getIntegerConfig(String configKey, Integer defaultValue);
    
    /**
     * 根据配置键获取长整数类型配置值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 长整数值
     */
    Long getLongConfig(String configKey, Long defaultValue);
    
    /**
     * 根据配置键获取双精度类型配置值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 双精度值
     */
    Double getDoubleConfig(String configKey, Double defaultValue);
    
    /**
     * 根据配置分组获取所有配置
     * @param configGroup 配置分组
     * @return 配置映射
     */
    Map<String, String> getConfigsByGroup(String configGroup);
    
    /**
     * 保存或更新配置
     * @param config 配置对象
     * @return 保存结果
     */
    boolean saveOrUpdateConfig(SystemConfig config);
    
    /**
     * 批量保存或更新配置
     * @param configs 配置列表
     * @return 保存结果
     */
    boolean batchSaveOrUpdateConfigs(List<SystemConfig> configs);
    
    /**
     * 删除配置
     * @param configKey 配置键
     * @return 删除结果
     */
    boolean deleteConfig(String configKey);
    
    /**
     * 获取所有配置
     * @return 配置列表
     */
    List<SystemConfig> getAllConfigs();
    
    /**
     * 获取所有必需的配置
     * @return 配置列表
     */
    List<SystemConfig> getRequiredConfigs();
    
    /**
     * 验证配置值
     * @param config 配置对象
     * @return 验证结果
     */
    boolean validateConfig(SystemConfig config);
    
    /**
     * 刷新配置缓存
     */
    void refreshCache();
    
    /**
     * 获取配置变更历史
     * @param configKey 配置键
     * @return 变更历史列表
     */
    List<com.hotech.events.entity.ConfigChangeLog> getConfigChangeHistory(String configKey);
    
    /**
     * 解析环境变量
     * @param value 配置值（可能包含环境变量占位符）
     * @return 解析后的值
     */
    String resolveEnvironmentVariables(String value);
    
    /**
     * 检查必需配置是否完整
     * @return 检查结果
     */
    boolean checkRequiredConfigs();
    
    /**
     * 获取配置统计信息
     * @return 统计信息
     */
    Map<String, Object> getConfigStats();
}