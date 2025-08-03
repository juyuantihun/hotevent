package com.hotech.events.task;

import com.hotech.events.config.DynamicSystemConfig;
import com.hotech.events.service.SystemHealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统健康检查定时任务
 * 定期执行系统健康检查并记录结果
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Component
public class SystemHealthCheckTask {
    
    @Autowired
    private SystemHealthCheckService systemHealthCheckService;
    
    @Autowired
    private DynamicSystemConfig dynamicSystemConfig;
    
    /**
     * 定期执行系统健康检查
     * 执行间隔由动态配置决定，这里使用固定间隔作为备用
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次（备用间隔）
    public void performScheduledHealthCheck() {
        try {
            // 获取动态配置的检查间隔
            DynamicSystemConfig.HealthCheckConfig healthConfig = dynamicSystemConfig.getHealthCheck();
            
            if (!healthConfig.isEnableDetailedCheck()) {
                log.debug("详细健康检查已禁用，跳过本次检查");
                return;
            }
            
            log.info("开始执行定时系统健康检查");
            
            // 执行健康检查
            SystemHealthCheckService.SystemHealthReport report = 
                    systemHealthCheckService.performFullHealthCheck();
            
            // 根据检查结果进行相应处理
            handleHealthCheckResult(report);
            
            log.info("定时系统健康检查完成: 状态={}, 分数={}", 
                    report.getOverallStatus(), report.getOverallScore());
            
        } catch (Exception e) {
            log.error("定时系统健康检查失败", e);
        }
    }
    
    /**
     * 处理健康检查结果
     */
    private void handleHealthCheckResult(SystemHealthCheckService.SystemHealthReport report) {
        String status = report.getOverallStatus();
        double score = report.getOverallScore();
        
        // 根据健康状态采取不同的处理策略
        switch (status) {
            case "CRITICAL":
                handleCriticalStatus(report);
                break;
            case "WARNING":
                handleWarningStatus(report);
                break;
            case "HEALTHY":
                handleHealthyStatus(report);
                break;
            default:
                log.warn("未知的健康状态: {}", status);
        }
        
        // 记录关键指标
        logKeyMetrics(report);
    }
    
    /**
     * 处理严重状态
     */
    private void handleCriticalStatus(SystemHealthCheckService.SystemHealthReport report) {
        log.error("系统健康状态严重异常！分数: {}, 错误数: {}", 
                report.getOverallScore(), report.getErrors().size());
        
        // 记录所有错误
        for (String error : report.getErrors()) {
            log.error("严重错误: {}", error);
        }
        
        // 这里可以添加告警通知逻辑
        // 例如：发送邮件、短信、钉钉通知等
        sendCriticalAlert(report);
    }
    
    /**
     * 处理警告状态
     */
    private void handleWarningStatus(SystemHealthCheckService.SystemHealthReport report) {
        log.warn("系统健康状态异常，分数: {}, 警告数: {}", 
                report.getOverallScore(), report.getWarnings().size());
        
        // 记录所有警告
        for (String warning : report.getWarnings()) {
            log.warn("系统警告: {}", warning);
        }
        
        // 检查是否需要发送警告通知
        DynamicSystemConfig.HealthCheckConfig healthConfig = dynamicSystemConfig.getHealthCheck();
        if (report.getOverallScore() < healthConfig.getAlertThreshold()) {
            sendWarningAlert(report);
        }
    }
    
    /**
     * 处理健康状态
     */
    private void handleHealthyStatus(SystemHealthCheckService.SystemHealthReport report) {
        log.debug("系统健康状态良好，分数: {}", report.getOverallScore());
        
        // 如果之前有告警，可以发送恢复通知
        if (!report.getWarnings().isEmpty()) {
            log.info("系统状态已恢复正常，但仍有 {} 个警告", report.getWarnings().size());
        }
    }
    
    /**
     * 记录关键指标
     */
    private void logKeyMetrics(SystemHealthCheckService.SystemHealthReport report) {
        SystemHealthCheckService.SystemPerformanceMetrics metrics = report.getPerformanceMetrics();
        if (metrics != null) {
            log.info("系统性能指标 - CPU: {:.1f}%, 内存: {:.1f}%, 磁盘: {:.1f}%, 活跃线程: {}", 
                    metrics.getCpuUsage() * 100,
                    metrics.getMemoryUsage() * 100,
                    metrics.getDiskUsage() * 100,
                    metrics.getActiveThreads());
        }
        
        SystemHealthCheckService.ApiHealthStatus apiHealth = report.getApiHealth();
        if (apiHealth != null) {
            log.info("API健康指标 - 成功率: {:.1f}%, 平均响应时间: {}ms, 总调用: {}, 失败: {}", 
                    apiHealth.getSuccessRate() * 100,
                    apiHealth.getAverageResponseTime(),
                    apiHealth.getTotalCalls(),
                    apiHealth.getFailedCalls());
        }
        
        SystemHealthCheckService.DatabaseHealthStatus dbHealth = report.getDatabaseHealth();
        if (dbHealth != null) {
            log.info("数据库健康指标 - 连接时间: {}ms, 活跃连接: {}/{}, 连接池使用率: {:.1f}%", 
                    dbHealth.getConnectionTime(),
                    dbHealth.getActiveConnections(),
                    dbHealth.getMaxConnections(),
                    dbHealth.getConnectionPoolUsage() * 100);
        }
    }
    
    /**
     * 发送严重告警
     */
    private void sendCriticalAlert(SystemHealthCheckService.SystemHealthReport report) {
        // 这里实现具体的告警逻辑
        // 例如：邮件、短信、钉钉、企业微信等
        log.error("发送严重告警通知 - 系统健康分数: {}, 错误数: {}", 
                report.getOverallScore(), report.getErrors().size());
        
        // 示例：构建告警消息
        StringBuilder alertMessage = new StringBuilder();
        alertMessage.append("【严重告警】系统健康检查异常\n");
        alertMessage.append("时间: ").append(report.getCheckTime()).append("\n");
        alertMessage.append("健康分数: ").append(String.format("%.2f", report.getOverallScore())).append("\n");
        alertMessage.append("错误数量: ").append(report.getErrors().size()).append("\n");
        
        if (!report.getErrors().isEmpty()) {
            alertMessage.append("错误详情:\n");
            for (String error : report.getErrors()) {
                alertMessage.append("- ").append(error).append("\n");
            }
        }
        
        // 这里调用实际的告警发送服务
        log.error("告警消息: {}", alertMessage.toString());
    }
    
    /**
     * 发送警告通知
     */
    private void sendWarningAlert(SystemHealthCheckService.SystemHealthReport report) {
        log.warn("发送警告通知 - 系统健康分数: {}, 警告数: {}", 
                report.getOverallScore(), report.getWarnings().size());
        
        // 构建警告消息
        StringBuilder warningMessage = new StringBuilder();
        warningMessage.append("【系统警告】系统健康检查发现异常\n");
        warningMessage.append("时间: ").append(report.getCheckTime()).append("\n");
        warningMessage.append("健康分数: ").append(String.format("%.2f", report.getOverallScore())).append("\n");
        warningMessage.append("警告数量: ").append(report.getWarnings().size()).append("\n");
        
        if (!report.getWarnings().isEmpty()) {
            warningMessage.append("警告详情:\n");
            for (String warning : report.getWarnings()) {
                warningMessage.append("- ").append(warning).append("\n");
            }
        }
        
        // 这里调用实际的警告发送服务
        log.warn("警告消息: {}", warningMessage.toString());
    }
    
    /**
     * 清理过期的健康检查记录
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupHealthCheckHistory() {
        try {
            log.info("开始清理过期的健康检查记录");
            
            // 获取最近100条记录，其余的会被自动清理
            systemHealthCheckService.getHealthCheckHistory(100);
            
            log.info("健康检查记录清理完成");
            
        } catch (Exception e) {
            log.error("清理健康检查记录失败", e);
        }
    }
}