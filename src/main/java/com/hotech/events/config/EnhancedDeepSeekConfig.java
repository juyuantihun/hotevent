package com.hotech.events.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 增强DeepSeek服务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.deepseek.enhanced")
public class EnhancedDeepSeekConfig {
    
    /**
     * 缓存TTL（毫秒）
     */
    private long cacheTtl = 300000; // 默认5分钟
    
    /**
     * 限流配置（每分钟请求数）
     */
    private int rateLimit = 60;
    
    /**
     * 批处理大小
     */
    private int batchSize = 10;
    
    /**
     * 是否启用缓存
     */
    private boolean enableCache = true;
    
    /**
     * 是否启用限流
     */
    private boolean enableRateLimit = true;
    
    /**
     * 是否启用监控
     */
    private boolean enableMonitoring = true;
    
    /**
     * 异步处理线程池大小
     */
    private int asyncThreadPoolSize = 10;
    
    /**
     * 重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;
    
    /**
     * 健康检查间隔（毫秒）
     */
    private long healthCheckInterval = 60000; // 1分钟
    
    /**
     * 统计数据保留天数
     */
    private int statsRetentionDays = 30;
}