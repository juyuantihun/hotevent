package com.hotech.events.config;

import com.hotech.events.service.FallbackStrategyService;
import com.hotech.events.service.ErrorHandlingService;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import com.hotech.events.service.impl.FallbackStrategyServiceImpl;
import com.hotech.events.service.impl.ErrorHandlingServiceImpl;
import com.hotech.events.service.impl.TimelinePerformanceMonitoringServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 临时Bean配置类
 * 用于解决Bean依赖问题
 */
@Slf4j
@Configuration
public class TemporaryBeanConfiguration {

    /**
     * 确保FallbackStrategyService Bean存在
     */
    @Bean
    @ConditionalOnMissingBean(FallbackStrategyService.class)
    public FallbackStrategyService fallbackStrategyService() {
        log.info("创建FallbackStrategyService Bean");
        return new FallbackStrategyServiceImpl();
    }

    /**
     * 确保ErrorHandlingService Bean存在
     */
    @Bean
    @ConditionalOnMissingBean(ErrorHandlingService.class)
    public ErrorHandlingService errorHandlingService() {
        log.info("创建ErrorHandlingService Bean");
        return new ErrorHandlingServiceImpl();
    }

    /**
     * 确保TimelinePerformanceMonitoringService Bean存在
     */
    @Bean
    @ConditionalOnMissingBean(TimelinePerformanceMonitoringService.class)
    public TimelinePerformanceMonitoringService timelinePerformanceMonitoringService() {
        log.info("创建TimelinePerformanceMonitoringService Bean");
        return new TimelinePerformanceMonitoringServiceImpl();
    }
}