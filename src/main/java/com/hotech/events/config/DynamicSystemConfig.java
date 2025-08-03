package com.hotech.events.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 动态系统配置类
 * 支持运行时调整各种系统参数
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.system.dynamic")
public class DynamicSystemConfig {
    
    /**
     * API选择策略配置
     */
    private ApiSelectionConfig apiSelection = new ApiSelectionConfig();
    
    /**
     * 重复检测配置
     */
    private DuplicationDetectionConfig duplicationDetection = new DuplicationDetectionConfig();
    
    /**
     * 事件数量配置
     */
    private EventCountConfig eventCount = new EventCountConfig();
    
    /**
     * 健康检查配置
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();
    
    /**
     * API选择策略配置
     */
    @Data
    public static class ApiSelectionConfig {
        /**
         * 主要API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB
         */
        private String primaryApiType = "DEEPSEEK_OFFICIAL";
        
        /**
         * 备用API类型
         */
        private String fallbackApiType = "VOLCENGINE_WEB";
        
        /**
         * API切换阈值（失败率）
         */
        private double failureThreshold = 0.3;
        
        /**
         * API健康检查间隔（秒）
         */
        private int healthCheckInterval = 60;
        
        /**
         * 是否启用自动切换
         */
        private boolean enableAutoSwitch = true;
        
        /**
         * 切换冷却时间（秒）
         */
        private int switchCooldown = 300;
    }
    
    /**
     * 重复检测配置
     */
    @Data
    public static class DuplicationDetectionConfig {
        /**
         * 名称相似度阈值（0-1）
         */
        private double nameSimilarityThreshold = 0.8;
        
        /**
         * 时间窗口重复检测间隔（分钟）
         */
        private int timeWindowMinutes = 5;
        
        /**
         * 请求指纹有效期（分钟）
         */
        private int fingerprintTtlMinutes = 30;
        
        /**
         * 是否启用严格模式
         */
        private boolean strictMode = false;
        
        /**
         * 最大重复检测记录数
         */
        private int maxDetectionRecords = 1000;
    }
    
    /**
     * 事件数量配置
     */
    @Data
    public static class EventCountConfig {
        /**
         * 最小事件数量
         */
        private int minimumEventCount = 3;
        
        /**
         * 目标事件数量
         */
        private int targetEventCount = 10;
        
        /**
         * 最大事件数量
         */
        private int maximumEventCount = 25;
        
        /**
         * 备用数据生成比例（0-1）
         */
        private double fallbackDataRatio = 0.0;
        
        /**
         * 是否启用智能补充
         */
        private boolean enableSmartSupplement = false;
    }
    
    /**
     * 健康检查配置
     */
    @Data
    public static class HealthCheckConfig {
        /**
         * 健康检查间隔（秒）
         */
        private int checkInterval = 30;
        
        /**
         * 超时时间（秒）
         */
        private int timeout = 10;
        
        /**
         * 重试次数
         */
        private int retryCount = 3;
        
        /**
         * 是否启用详细检查
         */
        private boolean enableDetailedCheck = true;
        
        /**
         * 告警阈值
         */
        private double alertThreshold = 0.8;
    }
}