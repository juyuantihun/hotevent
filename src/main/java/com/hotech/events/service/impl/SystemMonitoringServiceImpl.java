package com.hotech.events.service.impl;

import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统监控服务实现类
 */
@Slf4j
@Service
public class SystemMonitoringServiceImpl implements SystemMonitoringService {
    
    @Value("${app.monitoring.cpu-threshold:80.0}")
    private double cpuThreshold;
    
    @Value("${app.monitoring.memory-threshold:85.0}")
    private double memoryThreshold;
    
    @Value("${app.monitoring.disk-threshold:90.0}")
    private double diskThreshold;
    
    @Value("${app.monitoring.error-rate-threshold:10.0}")
    private double errorRateThreshold;
    
    @Value("${app.monitoring.response-time-threshold:5000}")
    private long responseTimeThreshold;
    
    // 系统监控Bean
    private OperatingSystemMXBean osBean;
    private MemoryMXBean memoryBean;
    
    // 错误统计
    private final ConcurrentHashMap<String, ErrorCounter> errorCounters = new ConcurrentHashMap<>();
    
    // 性能统计
    private final ConcurrentHashMap<String, PerformanceCounter> performanceCounters = new ConcurrentHashMap<>();
    
    // 告警管理
    private final ConcurrentHashMap<Long, SystemAlert> activeAlerts = new ConcurrentHashMap<>();
    private final AtomicLong alertIdGenerator = new AtomicLong(1);
    
    // 系统健康状态
    private volatile SystemHealthReport lastHealthReport;
    
    @PostConstruct
    public void init() {
        osBean = ManagementFactory.getOperatingSystemMXBean();
        memoryBean = ManagementFactory.getMemoryMXBean();
        log.info("系统监控服务初始化完成");
    }
    
    @Override
    @Async
    public void recordSystemError(String operation, String errorType, String errorMessage, String stackTrace) {
        try {
            String key = operation + ":" + errorType;
            ErrorCounter counter = errorCounters.computeIfAbsent(key, k -> new ErrorCounter(operation, errorType));
            
            counter.increment();
            counter.setLastErrorMessage(errorMessage);
            counter.setLastStackTrace(stackTrace);
            counter.setLastOccurrence(LocalDateTime.now());
            
            log.error("记录系统错误: operation={}, errorType={}, message={}", operation, errorType, errorMessage);
            
            // 检查是否需要触发告警
            checkErrorRateAlert(operation, errorType, counter);
            
        } catch (Exception e) {
            log.error("记录系统错误失败", e);
        }
    }
    
    @Override
    @Async
    public void recordPerformanceMetrics(String operation, long responseTime, long memoryUsage, double cpuUsage) {
        try {
            PerformanceCounter counter = performanceCounters.computeIfAbsent(operation, k -> new PerformanceCounter(operation));
            
            counter.addMetrics(responseTime, memoryUsage, cpuUsage);
            
            log.debug("记录性能指标: operation={}, responseTime={}ms, memory={}MB, cpu={}%", 
                     operation, responseTime, memoryUsage / 1024 / 1024, cpuUsage);
            
            // 检查是否需要触发性能告警
            checkPerformanceAlert(operation, responseTime, counter);
            
        } catch (Exception e) {
            log.error("记录性能指标失败", e);
        }
    }
    
    @Override
    public SystemHealthReport checkSystemHealth() {
        try {
            SystemHealthReport report = new SystemHealthReport();
            report.setCheckTime(LocalDateTime.now());
            
            // 获取CPU使用率
            double cpuUsage = 0.0;
            try {
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    cpuUsage = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
                    if (cpuUsage < 0) {
                        cpuUsage = 0; // 某些系统可能返回负值
                    }
                }
            } catch (Exception e) {
                log.debug("获取CPU使用率失败，使用默认值", e);
            }
            report.setCpuUsage(cpuUsage);
            
            // 获取内存使用率
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            report.setMemoryUsage(memoryUsage);
            
            // 获取磁盘使用率（简化实现）
            long totalSpace = Runtime.getRuntime().totalMemory();
            long freeSpace = Runtime.getRuntime().freeMemory();
            double diskUsage = (double) (totalSpace - freeSpace) / totalSpace * 100;
            report.setDiskUsage(diskUsage);
            
            // 活跃连接数（简化实现）
            report.setActiveConnections(Thread.activeCount());
            
            // 检查健康状态
            List<String> issues = new ArrayList<>();
            boolean isHealthy = true;
            
            if (cpuUsage > cpuThreshold) {
                issues.add("CPU使用率过高: " + String.format("%.2f%%", cpuUsage));
                isHealthy = false;
            }
            
            if (memoryUsage > memoryThreshold) {
                issues.add("内存使用率过高: " + String.format("%.2f%%", memoryUsage));
                isHealthy = false;
            }
            
            if (diskUsage > diskThreshold) {
                issues.add("磁盘使用率过高: " + String.format("%.2f%%", diskUsage));
                isHealthy = false;
            }
            
            report.setHealthy(isHealthy);
            report.setIssues(issues);
            
            lastHealthReport = report;
            
            // 触发系统健康告警
            if (!isHealthy) {
                triggerAlert("SYSTEM_HEALTH", "系统健康检查发现问题: " + String.join(", ", issues), AlertSeverity.HIGH);
            }
            
            return report;
            
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
            SystemHealthReport errorReport = new SystemHealthReport();
            errorReport.setCheckTime(LocalDateTime.now());
            errorReport.setHealthy(false);
            errorReport.setIssues(Arrays.asList("健康检查异常: " + e.getMessage()));
            return errorReport;
        }
    }
    
    @Override
    public List<ErrorStatistics> getErrorStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<ErrorStatistics> statistics = new ArrayList<>();
        
        for (ErrorCounter counter : errorCounters.values()) {
            if (counter.getFirstOccurrence().isAfter(startTime) && 
                counter.getLastOccurrence().isBefore(endTime)) {
                
                ErrorStatistics stats = new ErrorStatistics();
                stats.setOperation(counter.getOperation());
                stats.setErrorType(counter.getErrorType());
                stats.setErrorCount(counter.getCount());
                stats.setFirstOccurrence(counter.getFirstOccurrence());
                stats.setLastOccurrence(counter.getLastOccurrence());
                
                statistics.add(stats);
            }
        }
        
        return statistics;
    }
    
    @Override
    public List<PerformanceStatistics> getPerformanceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<PerformanceStatistics> statistics = new ArrayList<>();
        
        for (PerformanceCounter counter : performanceCounters.values()) {
            PerformanceStatistics stats = new PerformanceStatistics();
            stats.setOperation(counter.getOperation());
            stats.setTotalCalls(counter.getTotalCalls());
            stats.setAverageResponseTime(counter.getAverageResponseTime());
            stats.setMaxResponseTime(counter.getMaxResponseTime());
            stats.setMinResponseTime(counter.getMinResponseTime());
            stats.setAverageMemoryUsage(counter.getAverageMemoryUsage());
            stats.setAverageCpuUsage(counter.getAverageCpuUsage());
            
            statistics.add(stats);
        }
        
        return statistics;
    }
    
    @Override
    public void triggerAlert(String alertType, String message, AlertSeverity severity) {
        try {
            // 检查是否已存在相同类型的活跃告警
            boolean existingAlert = activeAlerts.values().stream()
                    .anyMatch(alert -> alert.getAlertType().equals(alertType) && alert.isActive());
            
            if (existingAlert) {
                log.debug("相同类型的告警已存在，跳过: alertType={}", alertType);
                return;
            }
            
            SystemAlert alert = new SystemAlert();
            alert.setId(alertIdGenerator.getAndIncrement());
            alert.setAlertType(alertType);
            alert.setMessage(message);
            alert.setSeverity(severity);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setActive(true);
            
            activeAlerts.put(alert.getId(), alert);
            
            log.warn("触发系统告警: type={}, severity={}, message={}", alertType, severity, message);
            
            // 这里可以集成邮件、短信、钉钉等告警通知
            sendAlertNotification(alert);
            
        } catch (Exception e) {
            log.error("触发告警失败", e);
        }
    }
    
    @Override
    public List<SystemAlert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }
    
    @Override
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        SystemAlert alert = activeAlerts.get(alertId);
        if (alert != null && alert.isActive()) {
            alert.setAcknowledgedAt(LocalDateTime.now());
            alert.setAcknowledgedBy(acknowledgedBy);
            alert.setActive(false);
            
            log.info("告警已确认: alertId={}, acknowledgedBy={}", alertId, acknowledgedBy);
        }
    }
    
    /**
     * 定时健康检查
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void scheduledHealthCheck() {
        try {
            checkSystemHealth();
        } catch (Exception e) {
            log.error("定时健康检查失败", e);
        }
    }
    
    /**
     * 定时清理过期数据
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredData() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
            
            // 清理过期的错误计数器
            errorCounters.entrySet().removeIf(entry -> 
                entry.getValue().getLastOccurrence().isBefore(cutoffTime));
            
            // 清理已确认的告警
            activeAlerts.entrySet().removeIf(entry -> {
                SystemAlert alert = entry.getValue();
                return !alert.isActive() && alert.getAcknowledgedAt() != null && 
                       alert.getAcknowledgedAt().isBefore(cutoffTime);
            });
            
            log.info("清理过期监控数据完成");
            
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }
    }
    
    /**
     * 检查错误率告警
     */
    private void checkErrorRateAlert(String operation, String errorType, ErrorCounter counter) {
        // 简化实现：如果5分钟内错误次数超过阈值，触发告警
        long recentErrors = counter.getRecentErrorCount(5);
        if (recentErrors > errorRateThreshold) {
            String message = String.format("操作错误率过高: operation=%s, errorType=%s, count=%d", 
                                          operation, errorType, recentErrors);
            triggerAlert("HIGH_ERROR_RATE", message, AlertSeverity.HIGH);
        }
    }
    
    /**
     * 检查性能告警
     */
    private void checkPerformanceAlert(String operation, long responseTime, PerformanceCounter counter) {
        if (responseTime > responseTimeThreshold) {
            String message = String.format("响应时间过长: operation=%s, responseTime=%dms", 
                                          operation, responseTime);
            triggerAlert("SLOW_RESPONSE", message, AlertSeverity.MEDIUM);
        }
    }
    
    /**
     * 发送告警通知
     */
    private void sendAlertNotification(SystemAlert alert) {
        // 这里可以实现具体的通知逻辑，如邮件、短信、钉钉等
        log.info("发送告警通知: {}", alert.getMessage());
    }
    
    /**
     * 错误计数器
     */
    private static class ErrorCounter {
        private final String operation;
        private final String errorType;
        private final AtomicLong count = new AtomicLong(0);
        private volatile LocalDateTime firstOccurrence;
        private volatile LocalDateTime lastOccurrence;
        private volatile String lastErrorMessage;
        private volatile String lastStackTrace;
        private final List<LocalDateTime> recentErrors = Collections.synchronizedList(new ArrayList<>());
        
        public ErrorCounter(String operation, String errorType) {
            this.operation = operation;
            this.errorType = errorType;
            this.firstOccurrence = LocalDateTime.now();
        }
        
        public void increment() {
            count.incrementAndGet();
            LocalDateTime now = LocalDateTime.now();
            lastOccurrence = now;
            recentErrors.add(now);
            
            // 清理5分钟前的记录
            recentErrors.removeIf(time -> time.isBefore(now.minusMinutes(5)));
        }
        
        public long getRecentErrorCount(int minutes) {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
            return recentErrors.stream().mapToLong(time -> time.isAfter(cutoff) ? 1 : 0).sum();
        }
        
        // Getters and Setters
        public String getOperation() { return operation; }
        public String getErrorType() { return errorType; }
        public long getCount() { return count.get(); }
        public LocalDateTime getFirstOccurrence() { return firstOccurrence; }
        public LocalDateTime getLastOccurrence() { return lastOccurrence; }
        public String getLastErrorMessage() { return lastErrorMessage; }
        public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
        public String getLastStackTrace() { return lastStackTrace; }
        public void setLastStackTrace(String lastStackTrace) { this.lastStackTrace = lastStackTrace; }
        public void setLastOccurrence(LocalDateTime lastOccurrence) { this.lastOccurrence = lastOccurrence; }
    }
    
    /**
     * 性能计数器
     */
    private static class PerformanceCounter {
        private final String operation;
        private final AtomicLong totalCalls = new AtomicLong(0);
        private volatile double totalResponseTime = 0;
        private volatile double maxResponseTime = 0;
        private volatile double minResponseTime = Double.MAX_VALUE;
        private volatile double totalMemoryUsage = 0;
        private volatile double totalCpuUsage = 0;
        
        public PerformanceCounter(String operation) {
            this.operation = operation;
        }
        
        public synchronized void addMetrics(long responseTime, long memoryUsage, double cpuUsage) {
            totalCalls.incrementAndGet();
            totalResponseTime += responseTime;
            totalMemoryUsage += memoryUsage;
            totalCpuUsage += cpuUsage;
            
            if (responseTime > maxResponseTime) {
                maxResponseTime = responseTime;
            }
            if (responseTime < minResponseTime) {
                minResponseTime = responseTime;
            }
        }
        
        // Getters
        public String getOperation() { return operation; }
        public long getTotalCalls() { return totalCalls.get(); }
        public double getAverageResponseTime() { 
            long calls = totalCalls.get();
            return calls > 0 ? totalResponseTime / calls : 0; 
        }
        public double getMaxResponseTime() { return maxResponseTime; }
        public double getMinResponseTime() { return minResponseTime == Double.MAX_VALUE ? 0 : minResponseTime; }
        public double getAverageMemoryUsage() { 
            long calls = totalCalls.get();
            return calls > 0 ? totalMemoryUsage / calls : 0; 
        }
        public double getAverageCpuUsage() { 
            long calls = totalCalls.get();
            return calls > 0 ? totalCpuUsage / calls : 0; 
        }
    }
}