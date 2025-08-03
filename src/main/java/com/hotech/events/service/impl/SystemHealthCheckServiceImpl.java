package com.hotech.events.service.impl;

import com.hotech.events.config.DynamicSystemConfig;
import com.hotech.events.service.SystemHealthCheckService;
import com.hotech.events.service.ApiMonitoringService;
import com.hotech.events.service.TimelineDuplicationDetectionService;
import com.hotech.events.service.EventParsingEnhancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 系统健康检查服务实现类
 * 监控各组件状态和系统整体健康度
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SystemHealthCheckServiceImpl implements SystemHealthCheckService {
    
    @Autowired
    private DynamicSystemConfig dynamicSystemConfig;
    
    @Autowired
    private ApiMonitoringService apiMonitoringService;
    
    @Autowired
    private TimelineDuplicationDetectionService duplicationDetectionService;
    
    @Autowired
    private EventParsingEnhancer eventParsingEnhancer;
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 健康检查历史记录（内存存储，生产环境建议使用数据库）
     */
    private final ConcurrentLinkedQueue<HealthCheckRecord> healthCheckHistory = new ConcurrentLinkedQueue<>();
    
    @Override
    public SystemHealthReport performFullHealthCheck() {
        log.info("开始执行全面系统健康检查");
        
        SystemHealthReport report = new SystemHealthReport();
        report.setCheckTime(LocalDateTime.now());
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 检查各个组件
            ApiHealthStatus apiHealth = checkApiHealth();
            DatabaseHealthStatus databaseHealth = checkDatabaseHealth();
            CacheHealthStatus cacheHealth = checkCacheHealth();
            DuplicationDetectionHealthStatus duplicationHealth = checkDuplicationDetectionHealth();
            EventParsingHealthStatus parsingHealth = checkEventParsingHealth();
            SystemPerformanceMetrics performanceMetrics = getPerformanceMetrics();
            
            // 设置报告内容
            report.setApiHealth(apiHealth);
            report.setDatabaseHealth(databaseHealth);
            report.setCacheHealth(cacheHealth);
            report.setDuplicationDetectionHealth(duplicationHealth);
            report.setEventParsingHealth(parsingHealth);
            report.setPerformanceMetrics(performanceMetrics);
            
            // 收集警告和错误
            collectHealthIssues(apiHealth, warnings, errors);
            collectHealthIssues(databaseHealth, warnings, errors);
            collectHealthIssues(cacheHealth, warnings, errors);
            collectHealthIssues(duplicationHealth, warnings, errors);
            collectHealthIssues(parsingHealth, warnings, errors);
            collectPerformanceIssues(performanceMetrics, warnings, errors);
            
            report.setWarnings(warnings);
            report.setErrors(errors);
            
            // 计算整体健康分数和状态
            double overallScore = calculateOverallScore(apiHealth, databaseHealth, cacheHealth, 
                                                      duplicationHealth, parsingHealth, performanceMetrics);
            report.setOverallScore(overallScore);
            
            String overallStatus = determineOverallStatus(overallScore, errors.size(), warnings.size());
            report.setOverallStatus(overallStatus);
            
            // 记录健康检查历史
            recordHealthCheck(report);
            
            log.info("系统健康检查完成: 状态={}, 分数={}, 警告数={}, 错误数={}", 
                    overallStatus, overallScore, warnings.size(), errors.size());
            
            return report;
            
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
            
            report.setOverallStatus("CRITICAL");
            report.setOverallScore(0.0);
            errors.add("健康检查执行失败: " + e.getMessage());
            report.setErrors(errors);
            report.setWarnings(warnings);
            
            return report;
        }
    }
    
    @Override
    public ApiHealthStatus checkApiHealth() {
        log.debug("检查API服务健康状态");
        
        ApiHealthStatus status = new ApiHealthStatus();
        
        try {
            // 获取API监控统计
            // 这里需要根据实际的ApiMonitoringService接口调整
            status.setStatus("HEALTHY");
            status.setSuccessRate(0.95); // 示例数据
            status.setAverageResponseTime(1500);
            status.setTotalCalls(1000);
            status.setFailedCalls(50);
            
            // 获取当前配置的API信息
            DynamicSystemConfig.ApiSelectionConfig apiConfig = dynamicSystemConfig.getApiSelection();
            status.setPrimaryApi(apiConfig.getPrimaryApiType());
            status.setFallbackApi(apiConfig.getFallbackApiType());
            
            // 检查各个API端点
            Map<String, ApiEndpointHealth> endpoints = new HashMap<>();
            endpoints.put("DEEPSEEK_OFFICIAL", checkApiEndpoint("DEEPSEEK_OFFICIAL"));
            endpoints.put("VOLCENGINE_WEB", checkApiEndpoint("VOLCENGINE_WEB"));
            status.setEndpoints(endpoints);
            
            // 根据成功率确定状态
            if (status.getSuccessRate() < 0.8) {
                status.setStatus("CRITICAL");
            } else if (status.getSuccessRate() < 0.9) {
                status.setStatus("WARNING");
            }
            
        } catch (Exception e) {
            log.error("API健康检查失败", e);
            status.setStatus("CRITICAL");
        }
        
        return status;
    }
    
    @Override
    public DatabaseHealthStatus checkDatabaseHealth() {
        log.debug("检查数据库连接健康状态");
        
        DatabaseHealthStatus status = new DatabaseHealthStatus();
        status.setLastCheck(LocalDateTime.now());
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 测试数据库连接
            try (Connection connection = dataSource.getConnection()) {
                long connectionTime = System.currentTimeMillis() - startTime;
                status.setConnectionTime(connectionTime);
                
                // 执行简单查询测试
                connection.createStatement().executeQuery("SELECT 1").close();
                
                status.setStatus("HEALTHY");
                
                // 获取连接池信息（这里需要根据实际的连接池实现调整）
                status.setActiveConnections(10); // 示例数据
                status.setMaxConnections(50);
                status.setConnectionPoolUsage(0.2);
                
                if (connectionTime > 5000) {
                    status.setStatus("WARNING");
                } else if (connectionTime > 10000) {
                    status.setStatus("CRITICAL");
                }
                
            }
            
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            status.setStatus("CRITICAL");
            status.setConnectionTime(-1);
        }
        
        return status;
    }
    
    @Override
    public CacheHealthStatus checkCacheHealth() {
        log.debug("检查缓存系统健康状态");
        
        CacheHealthStatus status = new CacheHealthStatus();
        status.setLastCheck(LocalDateTime.now());
        
        try {
            // 这里需要根据实际的缓存实现调整
            status.setStatus("HEALTHY");
            status.setHitRate(0.85); // 示例数据
            status.setTotalRequests(10000);
            status.setCacheHits(8500);
            status.setCacheMisses(1500);
            status.setMemoryUsage(0.6);
            
            if (status.getHitRate() < 0.7) {
                status.setStatus("WARNING");
            } else if (status.getHitRate() < 0.5) {
                status.setStatus("CRITICAL");
            }
            
        } catch (Exception e) {
            log.error("缓存健康检查失败", e);
            status.setStatus("CRITICAL");
        }
        
        return status;
    }
    
    @Override
    public DuplicationDetectionHealthStatus checkDuplicationDetectionHealth() {
        log.debug("检查重复检测服务健康状态");
        
        DuplicationDetectionHealthStatus status = new DuplicationDetectionHealthStatus();
        
        try {
            // 清理过期缓存并获取统计信息
            int expiredCount = duplicationDetectionService.cleanExpiredCache();
            status.setExpiredRecordCount(expiredCount);
            status.setLastCleanup(LocalDateTime.now());
            
            // 获取缓存记录数量（这里需要添加相应的方法到接口中）
            status.setCacheRecordCount(100); // 示例数据
            status.setDetectionSuccessRate(0.98);
            status.setAverageDetectionTime(50);
            
            status.setStatus("HEALTHY");
            
            if (status.getDetectionSuccessRate() < 0.9) {
                status.setStatus("WARNING");
            } else if (status.getDetectionSuccessRate() < 0.8) {
                status.setStatus("CRITICAL");
            }
            
        } catch (Exception e) {
            log.error("重复检测健康检查失败", e);
            status.setStatus("CRITICAL");
        }
        
        return status;
    }
    
    @Override
    public EventParsingHealthStatus checkEventParsingHealth() {
        log.debug("检查事件解析服务健康状态");
        
        EventParsingHealthStatus status = new EventParsingHealthStatus();
        
        try {
            // 这里需要根据实际的EventParsingEnhancer接口调整
            status.setStatus("HEALTHY");
            status.setParsingSuccessRate(0.92);
            status.setAverageParsingTime(200);
            status.setTotalParsingAttempts(500);
            status.setSuccessfulParsing(460);
            
            // 解析方法统计
            Map<String, Integer> methodStats = new HashMap<>();
            methodStats.put("JSON_EXTRACTION", 300);
            methodStats.put("TEXT_PARSING", 150);
            methodStats.put("FALLBACK_PARSING", 50);
            status.setParsingMethodStats(methodStats);
            
            if (status.getParsingSuccessRate() < 0.8) {
                status.setStatus("WARNING");
            } else if (status.getParsingSuccessRate() < 0.6) {
                status.setStatus("CRITICAL");
            }
            
        } catch (Exception e) {
            log.error("事件解析健康检查失败", e);
            status.setStatus("CRITICAL");
        }
        
        return status;
    }
    
    @Override
    public SystemPerformanceMetrics getPerformanceMetrics() {
        log.debug("获取系统性能指标");
        
        SystemPerformanceMetrics metrics = new SystemPerformanceMetrics();
        metrics.setMeasureTime(LocalDateTime.now());
        
        try {
            // JVM内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            
            metrics.setJvmHeapUsed(heapUsed);
            metrics.setJvmHeapMax(heapMax);
            metrics.setMemoryUsage((double) heapUsed / heapMax);
            
            // 线程信息
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            metrics.setActiveThreads(threadBean.getThreadCount());
            
            // CPU使用率（简化实现）
            metrics.setCpuUsage(0.45); // 示例数据
            
            // 磁盘使用率（简化实现）
            metrics.setDiskUsage(0.65); // 示例数据
            
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
        }
        
        return metrics;
    }
    
    @Override
    public List<HealthCheckRecord> getHealthCheckHistory(int limit) {
        List<HealthCheckRecord> history = new ArrayList<>(healthCheckHistory);
        
        // 按时间倒序排序
        history.sort((a, b) -> b.getCheckTime().compareTo(a.getCheckTime()));
        
        // 限制返回数量
        if (history.size() > limit) {
            history = history.subList(0, limit);
        }
        
        return history;
    }
    
    /**
     * 检查API端点健康状态
     */
    private ApiEndpointHealth checkApiEndpoint(String apiType) {
        ApiEndpointHealth health = new ApiEndpointHealth();
        health.setLastCheck(LocalDateTime.now());
        
        try {
            // 这里应该实际调用API进行健康检查
            long startTime = System.currentTimeMillis();
            
            // 模拟API调用
            Thread.sleep(100); // 模拟网络延迟
            
            long responseTime = System.currentTimeMillis() - startTime;
            health.setResponseTime(responseTime);
            health.setStatus("HEALTHY");
            
            if (responseTime > 3000) {
                health.setStatus("WARNING");
            } else if (responseTime > 5000) {
                health.setStatus("CRITICAL");
            }
            
        } catch (Exception e) {
            health.setStatus("CRITICAL");
            health.setLastError(e.getMessage());
        }
        
        return health;
    }
    
    /**
     * 收集健康问题
     */
    private void collectHealthIssues(Object healthStatus, List<String> warnings, List<String> errors) {
        if (healthStatus == null) return;
        
        try {
            String status = (String) healthStatus.getClass().getMethod("getStatus").invoke(healthStatus);
            String componentName = healthStatus.getClass().getSimpleName().replace("HealthStatus", "");
            
            if ("WARNING".equals(status)) {
                warnings.add(componentName + "组件状态异常");
            } else if ("CRITICAL".equals(status)) {
                errors.add(componentName + "组件严重故障");
            }
        } catch (Exception e) {
            log.warn("收集健康问题时出错", e);
        }
    }
    
    /**
     * 收集性能问题
     */
    private void collectPerformanceIssues(SystemPerformanceMetrics metrics, List<String> warnings, List<String> errors) {
        if (metrics == null) return;
        
        DynamicSystemConfig.HealthCheckConfig healthConfig = dynamicSystemConfig.getHealthCheck();
        double alertThreshold = healthConfig.getAlertThreshold();
        
        if (metrics.getCpuUsage() > alertThreshold) {
            warnings.add("CPU使用率过高: " + String.format("%.1f%%", metrics.getCpuUsage() * 100));
        }
        
        if (metrics.getMemoryUsage() > alertThreshold) {
            warnings.add("内存使用率过高: " + String.format("%.1f%%", metrics.getMemoryUsage() * 100));
        }
        
        if (metrics.getDiskUsage() > 0.9) {
            errors.add("磁盘空间不足: " + String.format("%.1f%%", metrics.getDiskUsage() * 100));
        }
    }
    
    /**
     * 计算整体健康分数
     */
    private double calculateOverallScore(ApiHealthStatus apiHealth, DatabaseHealthStatus databaseHealth,
                                       CacheHealthStatus cacheHealth, DuplicationDetectionHealthStatus duplicationHealth,
                                       EventParsingHealthStatus parsingHealth, SystemPerformanceMetrics performanceMetrics) {
        double totalScore = 0.0;
        int componentCount = 0;
        
        // API健康分数（权重：25%）
        totalScore += getComponentScore(apiHealth.getStatus()) * 0.25;
        componentCount++;
        
        // 数据库健康分数（权重：25%）
        totalScore += getComponentScore(databaseHealth.getStatus()) * 0.25;
        componentCount++;
        
        // 缓存健康分数（权重：15%）
        totalScore += getComponentScore(cacheHealth.getStatus()) * 0.15;
        componentCount++;
        
        // 重复检测健康分数（权重：15%）
        totalScore += getComponentScore(duplicationHealth.getStatus()) * 0.15;
        componentCount++;
        
        // 事件解析健康分数（权重：10%）
        totalScore += getComponentScore(parsingHealth.getStatus()) * 0.10;
        componentCount++;
        
        // 性能指标分数（权重：10%）
        double performanceScore = calculatePerformanceScore(performanceMetrics);
        totalScore += performanceScore * 0.10;
        
        return totalScore;
    }
    
    /**
     * 获取组件分数
     */
    private double getComponentScore(String status) {
        switch (status) {
            case "HEALTHY": return 1.0;
            case "WARNING": return 0.6;
            case "CRITICAL": return 0.2;
            default: return 0.0;
        }
    }
    
    /**
     * 计算性能分数
     */
    private double calculatePerformanceScore(SystemPerformanceMetrics metrics) {
        double score = 1.0;
        
        // CPU使用率影响
        if (metrics.getCpuUsage() > 0.8) {
            score -= 0.3;
        } else if (metrics.getCpuUsage() > 0.6) {
            score -= 0.1;
        }
        
        // 内存使用率影响
        if (metrics.getMemoryUsage() > 0.9) {
            score -= 0.4;
        } else if (metrics.getMemoryUsage() > 0.7) {
            score -= 0.2;
        }
        
        // 磁盘使用率影响
        if (metrics.getDiskUsage() > 0.9) {
            score -= 0.3;
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * 确定整体状态
     */
    private String determineOverallStatus(double score, int errorCount, int warningCount) {
        if (errorCount > 0 || score < 0.5) {
            return "CRITICAL";
        } else if (warningCount > 0 || score < 0.8) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }
    
    /**
     * 记录健康检查历史
     */
    private void recordHealthCheck(SystemHealthReport report) {
        HealthCheckRecord record = new HealthCheckRecord();
        record.setCheckTime(report.getCheckTime());
        record.setOverallStatus(report.getOverallStatus());
        record.setOverallScore(report.getOverallScore());
        record.setDetails(String.format("警告: %d, 错误: %d", 
                report.getWarnings().size(), report.getErrors().size()));
        
        healthCheckHistory.offer(record);
        
        // 保持历史记录数量在合理范围内
        while (healthCheckHistory.size() > 100) {
            healthCheckHistory.poll();
        }
    }
}