package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hotech.events.entity.ConfigChangeLog;
import com.hotech.events.entity.SystemConfig;
import com.hotech.events.mapper.ConfigChangeLogMapper;
import com.hotech.events.mapper.SystemConfigMapper;
import com.hotech.events.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

// import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统配置服务实现类
 * 
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Autowired
    private ConfigChangeLogMapper configChangeLogMapper;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private com.hotech.events.service.ConfigEncryptionService configEncryptionService;
    
    // 配置缓存
    private final Map<String, SystemConfig> configCache = new ConcurrentHashMap<>();
    
    // 环境变量占位符正则表达式
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    // @PostConstruct
    public void init() {
        log.info("初始化系统配置服务...");
        refreshCache();
        checkRequiredConfigs();
    }
    
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        try {
            SystemConfig config = getConfigFromCache(configKey);
            if (config != null && StringUtils.hasText(config.getConfigValue())) {
                String value = resolveEnvironmentVariables(config.getConfigValue());
                
                // 如果配置标记为加密，则解密
                if (Boolean.TRUE.equals(config.getIsEncrypted())) {
                    value = configEncryptionService.decrypt(value);
                }
                
                return value;
            }
            
            // 如果数据库中没有配置，尝试从环境变量或默认值获取
            String envValue = environment.getProperty(configKey);
            if (StringUtils.hasText(envValue)) {
                return envValue;
            }
            
            return defaultValue;
        } catch (Exception e) {
            log.error("获取配置值失败: configKey={}", configKey, e);
            return defaultValue;
        }
    }
    
    @Override
    public Boolean getBooleanConfig(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    @Override
    public Integer getIntegerConfig(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的整数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    public Long getLongConfig(String configKey, Long defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的长整数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    public Double getDoubleConfig(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey);
        if (StringUtils.hasText(value)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                log.warn("配置值不是有效的双精度数: configKey={}, value={}", configKey, value);
            }
        }
        return defaultValue;
    }
    
    @Override
    public Map<String, String> getConfigsByGroup(String configGroup) {
        Map<String, String> result = new HashMap<>();
        try {
            List<SystemConfig> configs = systemConfigMapper.selectByGroup(configGroup);
            for (SystemConfig config : configs) {
                String value = resolveEnvironmentVariables(config.getConfigValue());
                
                // 如果配置标记为加密，则解密
                if (Boolean.TRUE.equals(config.getIsEncrypted())) {
                    value = configEncryptionService.decrypt(value);
                }
                
                result.put(config.getConfigKey(), value);
            }
        } catch (Exception e) {
            log.error("获取配置分组失败: configGroup={}", configGroup, e);
        }
        return result;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#config.configKey")
    public boolean saveOrUpdateConfig(SystemConfig config) {
        try {
            // 验证配置
            if (!validateConfig(config)) {
                return false;
            }
            
            // 如果配置标记为加密且值未加密，则加密
            if (Boolean.TRUE.equals(config.getIsEncrypted()) && 
                StringUtils.hasText(config.getConfigValue()) &&
                !configEncryptionService.isEncrypted(config.getConfigValue())) {
                config.setConfigValue(configEncryptionService.encrypt(config.getConfigValue()));
            }
            
            SystemConfig existingConfig = systemConfigMapper.selectByKey(config.getConfigKey());
            String oldValue = existingConfig != null ? existingConfig.getConfigValue() : null;
            
            boolean result;
            if (existingConfig != null) {
                // 更新配置
                config.setId(existingConfig.getId());
                result = systemConfigMapper.updateById(config) > 0;
                
                // 记录变更历史
                if (result) {
                    recordConfigChange(config.getConfigKey(), oldValue, config.getConfigValue(), 
                                    ConfigChangeLog.ChangeType.UPDATE, "系统更新", config.getUpdatedBy());
                }
            } else {
                // 新增配置
                result = systemConfigMapper.insert(config) > 0;
                
                // 记录变更历史
                if (result) {
                    recordConfigChange(config.getConfigKey(), null, config.getConfigValue(), 
                                    ConfigChangeLog.ChangeType.CREATE, "系统创建", config.getCreatedBy());
                }
            }
            
            if (result) {
                // 更新缓存
                configCache.put(config.getConfigKey(), config);
                log.info("配置保存成功: configKey={}", config.getConfigKey());
            }
            
            return result;
        } catch (Exception e) {
            log.error("保存配置失败: configKey={}", config.getConfigKey(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean batchSaveOrUpdateConfigs(List<SystemConfig> configs) {
        boolean allSuccess = true;
        for (SystemConfig config : configs) {
            if (!saveOrUpdateConfig(config)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public boolean deleteConfig(String configKey) {
        try {
            SystemConfig existingConfig = systemConfigMapper.selectByKey(configKey);
            if (existingConfig == null) {
                return true; // 配置不存在，认为删除成功
            }
            
            boolean result = systemConfigMapper.deleteById(existingConfig.getId()) > 0;
            
            if (result) {
                // 记录变更历史
                recordConfigChange(configKey, existingConfig.getConfigValue(), null, 
                                ConfigChangeLog.ChangeType.DELETE, "系统删除", "system");
                
                // 从缓存中移除
                configCache.remove(configKey);
                log.info("配置删除成功: configKey={}", configKey);
            }
            
            return result;
        } catch (Exception e) {
            log.error("删除配置失败: configKey={}", configKey, e);
            return false;
        }
    }
    
    @Override
    public List<SystemConfig> getAllConfigs() {
        try {
            return systemConfigMapper.selectList(null);
        } catch (Exception e) {
            log.error("获取所有配置失败", e);
            return List.of();
        }
    }
    
    @Override
    public List<SystemConfig> getRequiredConfigs() {
        try {
            return systemConfigMapper.selectAllRequired();
        } catch (Exception e) {
            log.error("获取必需配置失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean validateConfig(SystemConfig config) {
        if (config == null || !StringUtils.hasText(config.getConfigKey())) {
            log.warn("配置键不能为空");
            return false;
        }
        
        // 验证配置类型
        if (StringUtils.hasText(config.getConfigType())) {
            try {
                SystemConfig.ConfigType.fromCode(config.getConfigType());
            } catch (IllegalArgumentException e) {
                log.warn("无效的配置类型: {}", config.getConfigType());
                return false;
            }
        }
        
        // 验证配置分组
        if (StringUtils.hasText(config.getConfigGroup())) {
            try {
                SystemConfig.ConfigGroup.fromCode(config.getConfigGroup());
            } catch (IllegalArgumentException e) {
                log.warn("无效的配置分组: {}", config.getConfigGroup());
                // 对于测试环境，允许test分组
                if (!"test".equals(config.getConfigGroup())) {
                    return false;
                }
            }
        }
        
        // 验证配置值格式
        if (StringUtils.hasText(config.getValidationRule()) && StringUtils.hasText(config.getConfigValue())) {
            try {
                Pattern pattern = Pattern.compile(config.getValidationRule());
                if (!pattern.matcher(config.getConfigValue()).matches()) {
                    log.warn("配置值不符合验证规则: configKey={}, value={}, rule={}", 
                            config.getConfigKey(), config.getConfigValue(), config.getValidationRule());
                    return false;
                }
            } catch (Exception e) {
                log.warn("验证规则格式错误: configKey={}, rule={}", config.getConfigKey(), config.getValidationRule());
            }
        }
        
        return true;
    }
    
    @Override
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void refreshCache() {
        try {
            configCache.clear();
            List<SystemConfig> configs = systemConfigMapper.selectList(null);
            for (SystemConfig config : configs) {
                configCache.put(config.getConfigKey(), config);
            }
            log.info("配置缓存刷新完成，共加载{}个配置", configs.size());
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
        }
    }
    
    @Override
    public List<ConfigChangeLog> getConfigChangeHistory(String configKey) {
        try {
            QueryWrapper<ConfigChangeLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("config_key", configKey)
                       .orderByDesc("changed_at");
            return configChangeLogMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取配置变更历史失败: configKey={}", configKey, e);
            return List.of();
        }
    }
    
    @Override
    public String resolveEnvironmentVariables(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        
        Matcher matcher = ENV_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String envKey = matcher.group(1);
            String defaultValue = "";
            
            // 支持 ${ENV_KEY:defaultValue} 格式
            if (envKey.contains(":")) {
                String[] parts = envKey.split(":", 2);
                envKey = parts[0];
                defaultValue = parts[1];
            }
            
            String envValue = environment.getProperty(envKey, defaultValue);
            matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    @Override
    public boolean checkRequiredConfigs() {
        try {
            List<SystemConfig> requiredConfigs = getRequiredConfigs();
            boolean allPresent = true;
            
            for (SystemConfig config : requiredConfigs) {
                String value = getConfigValue(config.getConfigKey());
                if (!StringUtils.hasText(value)) {
                    log.warn("必需配置缺失: configKey={}, description={}", 
                            config.getConfigKey(), config.getDescription());
                    allPresent = false;
                }
            }
            
            if (allPresent) {
                log.info("所有必需配置检查通过");
            } else {
                log.warn("存在缺失的必需配置");
            }
            
            return allPresent;
        } catch (Exception e) {
            log.error("检查必需配置失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getConfigStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<SystemConfig> allConfigs = getAllConfigs();
            stats.put("totalConfigs", allConfigs.size());
            
            Map<String, Integer> groupStats = new HashMap<>();
            Map<String, Integer> typeStats = new HashMap<>();
            int encryptedCount = 0;
            int requiredCount = 0;
            
            for (SystemConfig config : allConfigs) {
                // 按分组统计
                groupStats.merge(config.getConfigGroup(), 1, Integer::sum);
                
                // 按类型统计
                typeStats.merge(config.getConfigType(), 1, Integer::sum);
                
                // 加密配置统计
                if (Boolean.TRUE.equals(config.getIsEncrypted())) {
                    encryptedCount++;
                }
                
                // 必需配置统计
                if (Boolean.TRUE.equals(config.getIsRequired())) {
                    requiredCount++;
                }
            }
            
            stats.put("groupStats", groupStats);
            stats.put("typeStats", typeStats);
            stats.put("encryptedCount", encryptedCount);
            stats.put("requiredCount", requiredCount);
            stats.put("cacheSize", configCache.size());
            
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
        }
        return stats;
    }
    
    /**
     * 从缓存获取配置
     */
    private SystemConfig getConfigFromCache(String configKey) {
        SystemConfig config = configCache.get(configKey);
        if (config == null) {
            // 缓存中没有，从数据库查询
            config = systemConfigMapper.selectByKey(configKey);
            if (config != null) {
                configCache.put(configKey, config);
            }
        }
        return config;
    }
    
    /**
     * 记录配置变更历史
     */
    private void recordConfigChange(String configKey, String oldValue, String newValue, 
                                  ConfigChangeLog.ChangeType changeType, String reason, String changedBy) {
        try {
            ConfigChangeLog changeLog = new ConfigChangeLog();
            changeLog.setConfigKey(configKey);
            changeLog.setOldValue(oldValue);
            changeLog.setNewValue(newValue);
            changeLog.setChangeType(changeType.getCode());
            changeLog.setChangeReason(reason);
            changeLog.setChangedBy(changedBy);
            
            configChangeLogMapper.insert(changeLog);
        } catch (Exception e) {
            log.error("记录配置变更历史失败: configKey={}", configKey, e);
        }
    }
}