package com.hotech.events.service.impl;

import com.hotech.events.config.DynamicSystemConfig;
import com.hotech.events.service.ConfigurationManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 配置管理服务实现类
 * 支持运行时动态调整系统配置
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ConfigurationManagementServiceImpl implements ConfigurationManagementService {
    
    @Autowired
    private DynamicSystemConfig dynamicSystemConfig;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * 配置变更历史记录（内存存储，生产环境建议使用数据库）
     */
    private final ConcurrentLinkedQueue<ConfigChangeRecord> changeHistory = new ConcurrentLinkedQueue<>();
    
    @Override
    public DynamicSystemConfig getCurrentConfig() {
        log.debug("获取当前系统配置");
        return dynamicSystemConfig;
    }
    
    @Override
    public void updateApiSelectionConfig(DynamicSystemConfig.ApiSelectionConfig config) {
        log.info("更新API选择策略配置: {}", config);
        
        // 记录变更前的值
        DynamicSystemConfig.ApiSelectionConfig oldConfig = dynamicSystemConfig.getApiSelection();
        
        // 验证配置
        if (!validateApiSelectionConfig(config)) {
            throw new IllegalArgumentException("API选择配置验证失败");
        }
        
        // 更新配置
        dynamicSystemConfig.setApiSelection(config);
        
        // 记录变更历史
        recordConfigChange("API_SELECTION", oldConfig.toString(), config.toString(), "系统管理员", "运行时配置更新");
        
        // 发布配置变更事件
        eventPublisher.publishEvent(new ConfigChangeEvent("API_SELECTION", config));
        
        log.info("API选择策略配置更新成功");
    }
    
    @Override
    public void updateDuplicationDetectionConfig(DynamicSystemConfig.DuplicationDetectionConfig config) {
        log.info("更新重复检测配置: {}", config);
        
        DynamicSystemConfig.DuplicationDetectionConfig oldConfig = dynamicSystemConfig.getDuplicationDetection();
        
        if (!validateDuplicationDetectionConfig(config)) {
            throw new IllegalArgumentException("重复检测配置验证失败");
        }
        
        dynamicSystemConfig.setDuplicationDetection(config);
        
        recordConfigChange("DUPLICATION_DETECTION", oldConfig.toString(), config.toString(), "系统管理员", "运行时配置更新");
        
        eventPublisher.publishEvent(new ConfigChangeEvent("DUPLICATION_DETECTION", config));
        
        log.info("重复检测配置更新成功");
    }
    
    @Override
    public void updateEventCountConfig(DynamicSystemConfig.EventCountConfig config) {
        log.info("更新事件数量配置: {}", config);
        
        DynamicSystemConfig.EventCountConfig oldConfig = dynamicSystemConfig.getEventCount();
        
        if (!validateEventCountConfig(config)) {
            throw new IllegalArgumentException("事件数量配置验证失败");
        }
        
        dynamicSystemConfig.setEventCount(config);
        
        recordConfigChange("EVENT_COUNT", oldConfig.toString(), config.toString(), "系统管理员", "运行时配置更新");
        
        eventPublisher.publishEvent(new ConfigChangeEvent("EVENT_COUNT", config));
        
        log.info("事件数量配置更新成功");
    }
    
    @Override
    public void updateHealthCheckConfig(DynamicSystemConfig.HealthCheckConfig config) {
        log.info("更新健康检查配置: {}", config);
        
        DynamicSystemConfig.HealthCheckConfig oldConfig = dynamicSystemConfig.getHealthCheck();
        
        if (!validateHealthCheckConfig(config)) {
            throw new IllegalArgumentException("健康检查配置验证失败");
        }
        
        dynamicSystemConfig.setHealthCheck(config);
        
        recordConfigChange("HEALTH_CHECK", oldConfig.toString(), config.toString(), "系统管理员", "运行时配置更新");
        
        eventPublisher.publishEvent(new ConfigChangeEvent("HEALTH_CHECK", config));
        
        log.info("健康检查配置更新成功");
    }
    
    @Override
    public void resetToDefaults() {
        log.info("重置配置为默认值");
        
        DynamicSystemConfig defaultConfig = new DynamicSystemConfig();
        
        dynamicSystemConfig.setApiSelection(defaultConfig.getApiSelection());
        dynamicSystemConfig.setDuplicationDetection(defaultConfig.getDuplicationDetection());
        dynamicSystemConfig.setEventCount(defaultConfig.getEventCount());
        dynamicSystemConfig.setHealthCheck(defaultConfig.getHealthCheck());
        
        recordConfigChange("ALL", "custom", "default", "系统管理员", "重置为默认配置");
        
        eventPublisher.publishEvent(new ConfigChangeEvent("RESET_TO_DEFAULTS", defaultConfig));
        
        log.info("配置重置完成");
    }
    
    @Override
    public boolean validateConfig(DynamicSystemConfig config) {
        try {
            return validateApiSelectionConfig(config.getApiSelection()) &&
                   validateDuplicationDetectionConfig(config.getDuplicationDetection()) &&
                   validateEventCountConfig(config.getEventCount()) &&
                   validateHealthCheckConfig(config.getHealthCheck());
        } catch (Exception e) {
            log.error("配置验证失败", e);
            return false;
        }
    }
    
    @Override
    public List<ConfigChangeRecord> getConfigChangeHistory() {
        return new ArrayList<>(changeHistory);
    }
    
    /**
     * 验证API选择配置
     */
    private boolean validateApiSelectionConfig(DynamicSystemConfig.ApiSelectionConfig config) {
        if (config == null) return false;
        
        // 验证API类型
        if (config.getPrimaryApiType() == null || config.getFallbackApiType() == null) {
            log.error("API类型不能为空");
            return false;
        }
        
        // 验证失败阈值
        if (config.getFailureThreshold() < 0 || config.getFailureThreshold() > 1) {
            log.error("失败阈值必须在0-1之间");
            return false;
        }
        
        // 验证时间间隔
        if (config.getHealthCheckInterval() <= 0 || config.getSwitchCooldown() <= 0) {
            log.error("时间间隔必须大于0");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证重复检测配置
     */
    private boolean validateDuplicationDetectionConfig(DynamicSystemConfig.DuplicationDetectionConfig config) {
        if (config == null) return false;
        
        // 验证相似度阈值
        if (config.getNameSimilarityThreshold() < 0 || config.getNameSimilarityThreshold() > 1) {
            log.error("相似度阈值必须在0-1之间");
            return false;
        }
        
        // 验证时间窗口
        if (config.getTimeWindowMinutes() <= 0 || config.getFingerprintTtlMinutes() <= 0) {
            log.error("时间窗口必须大于0");
            return false;
        }
        
        // 验证记录数量
        if (config.getMaxDetectionRecords() <= 0) {
            log.error("最大记录数必须大于0");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证事件数量配置
     */
    private boolean validateEventCountConfig(DynamicSystemConfig.EventCountConfig config) {
        if (config == null) return false;
        
        // 验证数量关系
        if (config.getMinimumEventCount() <= 0 || 
            config.getTargetEventCount() <= 0 || 
            config.getMaximumEventCount() <= 0) {
            log.error("事件数量必须大于0");
            return false;
        }
        
        if (config.getMinimumEventCount() > config.getTargetEventCount() ||
            config.getTargetEventCount() > config.getMaximumEventCount()) {
            log.error("事件数量关系错误：最小 <= 目标 <= 最大");
            return false;
        }
        
        // 验证比例
        if (config.getFallbackDataRatio() < 0 || config.getFallbackDataRatio() > 1) {
            log.error("备用数据比例必须在0-1之间");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证健康检查配置
     */
    private boolean validateHealthCheckConfig(DynamicSystemConfig.HealthCheckConfig config) {
        if (config == null) return false;
        
        // 验证时间参数
        if (config.getCheckInterval() <= 0 || 
            config.getTimeout() <= 0 || 
            config.getRetryCount() < 0) {
            log.error("健康检查时间参数错误");
            return false;
        }
        
        // 验证告警阈值
        if (config.getAlertThreshold() < 0 || config.getAlertThreshold() > 1) {
            log.error("告警阈值必须在0-1之间");
            return false;
        }
        
        return true;
    }
    
    /**
     * 记录配置变更历史
     */
    private void recordConfigChange(String configType, String oldValue, String newValue, String changedBy, String reason) {
        ConfigChangeRecord record = new ConfigChangeRecord();
        record.setConfigType(configType);
        record.setOldValue(oldValue);
        record.setNewValue(newValue);
        record.setChangedBy(changedBy);
        record.setChangeTime(LocalDateTime.now());
        record.setReason(reason);
        
        changeHistory.offer(record);
        
        // 保持历史记录数量在合理范围内
        while (changeHistory.size() > 1000) {
            changeHistory.poll();
        }
    }
    
    /**
     * 配置变更事件
     */
    public static class ConfigChangeEvent {
        private final String configType;
        private final Object newConfig;
        
        public ConfigChangeEvent(String configType, Object newConfig) {
            this.configType = configType;
            this.newConfig = newConfig;
        }
        
        public String getConfigType() { return configType; }
        public Object getNewConfig() { return newConfig; }
    }
}