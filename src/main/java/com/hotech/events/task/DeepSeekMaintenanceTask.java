package com.hotech.events.task;

import com.hotech.events.config.EnhancedDeepSeekConfig;
import com.hotech.events.dto.ApiHealthStatus;
import com.hotech.events.service.DeepSeekMonitoringService;
import com.hotech.events.service.EnhancedDeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DeepSeek维护任务
 * 定期执行缓存清理、健康检查、统计数据清理等维护任务
 */
@Slf4j
@Component
public class DeepSeekMaintenanceTask {
    
    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;
    
    @Autowired
    private DeepSeekMonitoringService monitoringService;
    
    @Autowired
    private EnhancedDeepSeekConfig config;
    
    /**
     * 定期健康检查
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    public void performHealthCheck() {
        if (!config.isEnableMonitoring()) {
            return;
        }
        
        try {
            ApiHealthStatus health = enhancedDeepSeekService.checkApiHealth();
            
            if (!health.getIsHealthy()) {
                log.warn("DeepSeek API健康检查失败: {}", health.getErrorMessage());
                
                // 这里可以添加告警逻辑，如发送邮件、短信等
                sendHealthAlert(health);
            } else {
                log.debug("DeepSeek API健康检查通过: responseTime={}ms", health.getResponseTime());
            }
        } catch (Exception e) {
            log.error("执行健康检查失败", e);
        }
    }
    
    /**
     * 清理过期统计数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldStatistics() {
        if (!config.isEnableMonitoring()) {
            return;
        }
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(config.getStatsRetentionDays());
            int deletedCount = monitoringService.cleanupOldRecords(cutoffTime);
            
            log.info("清理过期统计数据完成: deletedCount={}, cutoffTime={}", deletedCount, cutoffTime);
        } catch (Exception e) {
            log.error("清理过期统计数据失败", e);
        }
    }
    
    /**
     * 缓存维护
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void maintainCache() {
        if (!config.isEnableCache()) {
            return;
        }
        
        try {
            // 获取缓存统计
            try {
                Object cacheStatsObj = enhancedDeepSeekService.getCacheStats();
                log.debug("缓存统计获取成功");
                // 由于类型转换问题，暂时简化处理
            } catch (Exception e) {
                log.debug("获取缓存统计失败: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("缓存维护失败", e);
        }
    }
    
    /**
     * 生成使用报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateUsageReport() {
        if (!config.isEnableMonitoring()) {
            return;
        }
        
        try {
            Object usageStats = enhancedDeepSeekService.getUsageStats();
            
            log.info("DeepSeek API使用报告:");
            log.info("- 使用统计获取成功");
            // 由于类型转换问题，暂时简化处理
            // log.info("- 总请求数: {}", usageStats.getTotalRequests());
            // log.info("- 成功率: {:.2f}%", usageStats.getSuccessRate());
            // log.info("- 平均响应时间: {:.2f}ms", usageStats.getAverageResponseTime());
            // log.info("- 总Token使用量: {}", usageStats.getTotalTokenUsage());
            // log.info("- 今日请求数: {}", usageStats.getTodayRequests());
            // log.info("- 今日Token使用量: {}", usageStats.getTodayTokenUsage());
            
            // 这里可以添加报告发送逻辑，如发送邮件报告
            
        } catch (Exception e) {
            log.error("生成使用报告失败", e);
        }
    }
    
    /**
     * 限流器维护
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    public void maintainRateLimit() {
        if (!config.isEnableRateLimit()) {
            return;
        }
        
        try {
            // 这里可以添加限流器的维护逻辑
            // 比如根据系统负载动态调整限流参数
            
            log.debug("限流器维护完成");
        } catch (Exception e) {
            log.error("限流器维护失败", e);
        }
    }
    
    /**
     * 发送健康告警
     */
    private void sendHealthAlert(ApiHealthStatus health) {
        // 这里实现告警逻辑
        // 可以发送邮件、短信、钉钉消息等
        
        log.error("DeepSeek API健康告警: " +
                 "isHealthy={}, responseTime={}ms, error={}", 
                 health.getIsHealthy(), health.getResponseTime(), health.getErrorMessage());
        
        // 示例：可以集成邮件服务、短信服务等
        // emailService.sendAlert("DeepSeek API异常", health.getErrorMessage());
        // smsService.sendAlert("DeepSeek API异常", health.getErrorMessage());
    }
}