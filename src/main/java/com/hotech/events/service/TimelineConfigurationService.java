package com.hotech.events.service;

import com.hotech.events.config.TimelineEnhancementConfig;

import java.util.Map;

/**
 * 时间线配置管理服务接口
 * 提供时间线增强功能的配置管理和热更新功能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface TimelineConfigurationService {
    
    /**
     * 获取当前时间线增强配置
     * 
     * @return 当前配置
     */
    TimelineEnhancementConfig getCurrentConfig();
    
    /**
     * 更新时间段分割配置
     * 
     * @param config 时间段分割配置
     */
    void updateSegmentationConfig(TimelineEnhancementConfig.SegmentationConfig config);
    
    /**
     * 更新地理信息处理配置
     * 
     * @param config 地理信息处理配置
     */
    void updateGeographicConfig(TimelineEnhancementConfig.GeographicConfig config);
    
    /**
     * 更新API调用配置
     * 
     * @param config API调用配置
     */
    void updateApiConfig(TimelineEnhancementConfig.ApiConfig config);
    
    /**
     * 更新性能监控配置
     * 
     * @param config 性能监控配置
     */
    void updateMonitoringConfig(TimelineEnhancementConfig.MonitoringConfig config);
    
    /**
     * 更新缓存配置
     * 
     * @param config 缓存配置
     */
    void updateCacheConfig(TimelineEnhancementConfig.CacheConfig config);
    
    /**
     * 更新前端UI配置
     * 
     * @param config 前端UI配置
     */
    void updateFrontendConfig(TimelineEnhancementConfig.FrontendConfig config);
    
    /**
     * 更新数据验证配置
     * 
     * @param config 数据验证配置
     */
    void updateValidationConfig(TimelineEnhancementConfig.ValidationConfig config);
    
    /**
     * 更新错误处理配置
     * 
     * @param config 错误处理配置
     */
    void updateErrorHandlingConfig(TimelineEnhancementConfig.ErrorHandlingConfig config);
    
    /**
     * 重置配置为默认值
     */
    void resetToDefaults();
    
    /**
     * 验证配置有效性
     * 
     * @param config 配置对象
     * @return 验证结果
     */
    boolean validateConfig(TimelineEnhancementConfig config);
    
    /**
     * 重新加载配置文件
     * 
     * @return 重新加载结果
     */
    boolean reloadConfigFromFile();
    
    /**
     * 保存配置到文件
     * 
     * @return 保存结果
     */
    boolean saveConfigToFile();
    
    /**
     * 获取配置变更历史
     * 
     * @return 配置变更历史列表
     */
    java.util.List<TimelineConfigChangeRecord> getConfigChangeHistory();
    
    /**
     * 获取配置统计信息
     * 
     * @return 配置统计信息
     */
    Map<String, Object> getConfigStatistics();
    
    /**
     * 启用配置热更新监听
     */
    void enableHotReload();
    
    /**
     * 禁用配置热更新监听
     */
    void disableHotReload();
    
    /**
     * 检查配置文件是否已更改
     * 
     * @return 是否已更改
     */
    boolean isConfigFileChanged();
    
    /**
     * 获取配置性能指标
     * 
     * @return 性能指标
     */
    Map<String, Object> getPerformanceMetrics();
    
    /**
     * 时间线配置变更记录
     */
    class TimelineConfigChangeRecord {
        private String configSection;
        private String configKey;
        private String oldValue;
        private String newValue;
        private String changedBy;
        private java.time.LocalDateTime changeTime;
        private String reason;
        private String changeType; // CREATE, UPDATE, DELETE
        
        // 构造函数
        public TimelineConfigChangeRecord() {}
        
        public TimelineConfigChangeRecord(String configSection, String configKey, 
                                        String oldValue, String newValue, 
                                        String changedBy, String reason, String changeType) {
            this.configSection = configSection;
            this.configKey = configKey;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.changedBy = changedBy;
            this.reason = reason;
            this.changeType = changeType;
            this.changeTime = java.time.LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getConfigSection() { return configSection; }
        public void setConfigSection(String configSection) { this.configSection = configSection; }
        
        public String getConfigKey() { return configKey; }
        public void setConfigKey(String configKey) { this.configKey = configKey; }
        
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
        
        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
        
        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
        
        public java.time.LocalDateTime getChangeTime() { return changeTime; }
        public void setChangeTime(java.time.LocalDateTime changeTime) { this.changeTime = changeTime; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
    }
}