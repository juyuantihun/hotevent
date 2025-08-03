package com.hotech.events.service;

import com.hotech.events.config.DynamicSystemConfig;

/**
 * 配置管理服务接口
 * 提供运行时配置调整功能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface ConfigurationManagementService {
    
    /**
     * 获取当前系统配置
     */
    DynamicSystemConfig getCurrentConfig();
    
    /**
     * 更新API选择策略
     */
    void updateApiSelectionConfig(DynamicSystemConfig.ApiSelectionConfig config);
    
    /**
     * 更新重复检测配置
     */
    void updateDuplicationDetectionConfig(DynamicSystemConfig.DuplicationDetectionConfig config);
    
    /**
     * 更新事件数量配置
     */
    void updateEventCountConfig(DynamicSystemConfig.EventCountConfig config);
    
    /**
     * 更新健康检查配置
     */
    void updateHealthCheckConfig(DynamicSystemConfig.HealthCheckConfig config);
    
    /**
     * 重置配置为默认值
     */
    void resetToDefaults();
    
    /**
     * 验证配置有效性
     */
    boolean validateConfig(DynamicSystemConfig config);
    
    /**
     * 获取配置变更历史
     */
    java.util.List<ConfigChangeRecord> getConfigChangeHistory();
    
    /**
     * 配置变更记录
     */
    class ConfigChangeRecord {
        private String configType;
        private String oldValue;
        private String newValue;
        private String changedBy;
        private java.time.LocalDateTime changeTime;
        private String reason;
        
        // getters and setters
        public String getConfigType() { return configType; }
        public void setConfigType(String configType) { this.configType = configType; }
        
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
    }
}