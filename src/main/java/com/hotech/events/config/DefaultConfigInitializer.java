package com.hotech.events.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认配置初始化器
 * 为可能缺失的Bean提供默认实现，确保应用能够启动
 */
@Slf4j
@Configuration
public class DefaultConfigInitializer {

    // 移除了defaultGeographicInfoService bean，因为已经有GeographicInfoServiceImpl实现类

    // 移除了有问题的GeographicCoordinateMapper默认实现
    // 如果需要默认实现，应该创建一个单独的类而不是匿名内部类

    /**
     * 默认的批处理工具类
     */
    @Bean
    @ConditionalOnMissingBean(name = "batchProcessor")
    public com.hotech.events.util.BatchProcessor defaultBatchProcessor() {
        log.info("创建默认的BatchProcessor Bean");
        return new com.hotech.events.util.BatchProcessor();
    }

    /**
     * 默认的限流器
     */
    @Bean
    @ConditionalOnMissingBean(name = "rateLimiter")
    public com.hotech.events.util.RateLimiter defaultRateLimiter() {
        log.info("创建默认的RateLimiter Bean");
        return new com.hotech.events.util.RateLimiter(60, 60000); // 每分钟60次
    }

    /**
     * 默认的请求缓存
     */
    @Bean
    @ConditionalOnMissingBean(name = "requestCache")
    public com.hotech.events.util.RequestCache<String> defaultRequestCache() {
        log.info("创建默认的RequestCache Bean");
        return new com.hotech.events.util.RequestCache<>(300000); // 5分钟TTL
    }
}