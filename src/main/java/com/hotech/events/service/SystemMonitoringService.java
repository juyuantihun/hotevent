package com.hotech.events.service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 系统监控服务接口
 */
public interface SystemMonitoringService {
    
    /**
     * 记录系统异常
     * 
     * @param operation 操作名称
     * @param errorType 错误类型
     * @param errorMessage 错误信息
     * @param stackTrace 堆栈跟踪
     */
    void recordSystemError(String operation, String errorType, String errorMessage, String stackTrace);
    
    /**
     * 记录性能指标
     * 
     * @param operation 操作名称
     * @param responseTime 响应时间
     * @param memoryUsage 内存使用量
     * @param cpuUsage CPU使用率
     */
    void recordPerformanceMetrics(String operation, long responseTime, long memoryUsage, double cpuUsage);
    
    /**
     * 检查系统健康状态
     * 
     * @return 健康状态报告
     */
    SystemHealthReport checkSystemHealth();
    
    /**
     * 获取错误统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误统计信息
     */
    List<ErrorStatistics> getErrorStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取性能统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能统计信息
     */
    List<PerformanceStatistics> getPerformanceStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 触发告警
     * 
     * @param alertType 告警类型
     * @param message 告警消息
     * @param severity 严重程度
     */
    void triggerAlert(String alertType, String message, AlertSeverity severity);
    
    /**
     * 获取活跃告警
     * 
     * @return 活跃告警列表
     */
    List<SystemAlert> getActiveAlerts();
    
    /**
     * 确认告警
     * 
     * @param alertId 告警ID
     * @param acknowledgedBy 确认人
     */
    void acknowledgeAlert(Long alertId, String acknowledgedBy);
    
    /**
     * 系统健康报告
     */
    class SystemHealthReport {
        private LocalDateTime checkTime;
        private boolean isHealthy;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private int activeConnections;
        private List<String> issues;
        
        // Getters and Setters
        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
        
        public boolean isHealthy() { return isHealthy; }
        public void setHealthy(boolean healthy) { isHealthy = healthy; }
        
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
    }
    
    /**
     * 错误统计
     */
    class ErrorStatistics {
        private String operation;
        private String errorType;
        private long errorCount;
        private LocalDateTime firstOccurrence;
        private LocalDateTime lastOccurrence;
        
        // Getters and Setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        
        public LocalDateTime getFirstOccurrence() { return firstOccurrence; }
        public void setFirstOccurrence(LocalDateTime firstOccurrence) { this.firstOccurrence = firstOccurrence; }
        
        public LocalDateTime getLastOccurrence() { return lastOccurrence; }
        public void setLastOccurrence(LocalDateTime lastOccurrence) { this.lastOccurrence = lastOccurrence; }
    }
    
    /**
     * 性能统计
     */
    class PerformanceStatistics {
        private String operation;
        private long totalCalls;
        private double averageResponseTime;
        private double maxResponseTime;
        private double minResponseTime;
        private double averageMemoryUsage;
        private double averageCpuUsage;
        
        // Getters and Setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public long getTotalCalls() { return totalCalls; }
        public void setTotalCalls(long totalCalls) { this.totalCalls = totalCalls; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
        
        public double getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(double minResponseTime) { this.minResponseTime = minResponseTime; }
        
        public double getAverageMemoryUsage() { return averageMemoryUsage; }
        public void setAverageMemoryUsage(double averageMemoryUsage) { this.averageMemoryUsage = averageMemoryUsage; }
        
        public double getAverageCpuUsage() { return averageCpuUsage; }
        public void setAverageCpuUsage(double averageCpuUsage) { this.averageCpuUsage = averageCpuUsage; }
    }
    
    /**
     * 系统告警
     */
    class SystemAlert {
        private Long id;
        private String alertType;
        private String message;
        private AlertSeverity severity;
        private LocalDateTime createdAt;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;
        private boolean isActive;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public AlertSeverity getSeverity() { return severity; }
        public void setSeverity(AlertSeverity severity) { this.severity = severity; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
        
        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }
    
    /**
     * 告警严重程度
     */
    enum AlertSeverity {
        LOW,      // 低
        MEDIUM,   // 中
        HIGH,     // 高
        CRITICAL  // 严重
    }
}