package com.hotech.events.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 性能监控服务接口
 * 提供详细的性能监控和资源使用统计功能
 */
public interface PerformanceMonitoringService {
    
    /**
     * 开始性能监控会话
     * 
     * @param operationName 操作名称
     * @param operationType 操作类型
     * @return 监控会话ID
     */
    String startMonitoring(String operationName, String operationType);
    
    /**
     * 记录性能指标
     * 
     * @param sessionId 监控会话ID
     * @param metricName 指标名称
     * @param value 指标值
     * @param unit 单位
     */
    void recordMetric(String sessionId, String metricName, double value, String unit);
    
    /**
     * 结束性能监控会话
     * 
     * @param sessionId 监控会话ID
     * @param success 是否成功
     */
    void endMonitoring(String sessionId, boolean success);
    
    /**
     * 获取实时性能指标
     * 
     * @return 实时性能指标
     */
    RealTimeMetrics getRealTimeMetrics();
    
    /**
     * 获取操作性能统计
     * 
     * @param operationName 操作名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能统计信息
     */
    OperationPerformanceStats getOperationStats(String operationName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取系统资源使用趋势
     * 
     * @param hours 过去几小时
     * @return 资源使用趋势数据
     */
    List<ResourceUsageTrend> getResourceUsageTrend(int hours);
    
    /**
     * 获取性能热点分析
     * 
     * @param limit 返回数量限制
     * @return 性能热点列表
     */
    List<PerformanceHotspot> getPerformanceHotspots(int limit);
    
    /**
     * 获取异常性能事件
     * 
     * @param hours 过去几小时
     * @return 异常性能事件列表
     */
    List<AnomalousPerformanceEvent> getAnomalousEvents(int hours);
    
    /**
     * 生成性能报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告
     */
    PerformanceReport generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 实时性能指标
     */
    class RealTimeMetrics {
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private int activeThreads;
        private int activeConnections;
        private double requestsPerSecond;
        private double averageResponseTime;
        private double errorRate;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
        
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        
        public double getRequestsPerSecond() { return requestsPerSecond; }
        public void setRequestsPerSecond(double requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * 操作性能统计
     */
    class OperationPerformanceStats {
        private String operationName;
        private long totalCalls;
        private long successfulCalls;
        private long failedCalls;
        private double successRate;
        private double averageResponseTime;
        private double minResponseTime;
        private double maxResponseTime;
        private double p95ResponseTime;
        private double p99ResponseTime;
        private double throughput;
        private Map<String, Double> customMetrics;
        
        // Getters and Setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public long getTotalCalls() { return totalCalls; }
        public void setTotalCalls(long totalCalls) { this.totalCalls = totalCalls; }
        
        public long getSuccessfulCalls() { return successfulCalls; }
        public void setSuccessfulCalls(long successfulCalls) { this.successfulCalls = successfulCalls; }
        
        public long getFailedCalls() { return failedCalls; }
        public void setFailedCalls(long failedCalls) { this.failedCalls = failedCalls; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(double minResponseTime) { this.minResponseTime = minResponseTime; }
        
        public double getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
        
        public double getP95ResponseTime() { return p95ResponseTime; }
        public void setP95ResponseTime(double p95ResponseTime) { this.p95ResponseTime = p95ResponseTime; }
        
        public double getP99ResponseTime() { return p99ResponseTime; }
        public void setP99ResponseTime(double p99ResponseTime) { this.p99ResponseTime = p99ResponseTime; }
        
        public double getThroughput() { return throughput; }
        public void setThroughput(double throughput) { this.throughput = throughput; }
        
        public Map<String, Double> getCustomMetrics() { return customMetrics; }
        public void setCustomMetrics(Map<String, Double> customMetrics) { this.customMetrics = customMetrics; }
    }
    
    /**
     * 资源使用趋势
     */
    class ResourceUsageTrend {
        private LocalDateTime timestamp;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private double networkUsage;
        private int activeThreads;
        
        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        
        public double getNetworkUsage() { return networkUsage; }
        public void setNetworkUsage(double networkUsage) { this.networkUsage = networkUsage; }
        
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
    }
    
    /**
     * 性能热点
     */
    class PerformanceHotspot {
        private String operationName;
        private String operationType;
        private double averageResponseTime;
        private long callCount;
        private double totalTime;
        private double impactScore;
        
        // Getters and Setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public long getCallCount() { return callCount; }
        public void setCallCount(long callCount) { this.callCount = callCount; }
        
        public double getTotalTime() { return totalTime; }
        public void setTotalTime(double totalTime) { this.totalTime = totalTime; }
        
        public double getImpactScore() { return impactScore; }
        public void setImpactScore(double impactScore) { this.impactScore = impactScore; }
    }
    
    /**
     * 异常性能事件
     */
    class AnomalousPerformanceEvent {
        private String operationName;
        private LocalDateTime timestamp;
        private double responseTime;
        private double threshold;
        private String anomalyType;
        private String description;
        
        // Getters and Setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getResponseTime() { return responseTime; }
        public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
        
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        
        public String getAnomalyType() { return anomalyType; }
        public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 性能报告
     */
    class PerformanceReport {
        private LocalDateTime reportStartTime;
        private LocalDateTime reportEndTime;
        private LocalDateTime generatedAt;
        private RealTimeMetrics overallMetrics;
        private List<OperationPerformanceStats> operationStats;
        private List<PerformanceHotspot> hotspots;
        private List<AnomalousPerformanceEvent> anomalousEvents;
        private Map<String, Object> summary;
        
        // Getters and Setters
        public LocalDateTime getReportStartTime() { return reportStartTime; }
        public void setReportStartTime(LocalDateTime reportStartTime) { this.reportStartTime = reportStartTime; }
        
        public LocalDateTime getReportEndTime() { return reportEndTime; }
        public void setReportEndTime(LocalDateTime reportEndTime) { this.reportEndTime = reportEndTime; }
        
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        
        public RealTimeMetrics getOverallMetrics() { return overallMetrics; }
        public void setOverallMetrics(RealTimeMetrics overallMetrics) { this.overallMetrics = overallMetrics; }
        
        public List<OperationPerformanceStats> getOperationStats() { return operationStats; }
        public void setOperationStats(List<OperationPerformanceStats> operationStats) { this.operationStats = operationStats; }
        
        public List<PerformanceHotspot> getHotspots() { return hotspots; }
        public void setHotspots(List<PerformanceHotspot> hotspots) { this.hotspots = hotspots; }
        
        public List<AnomalousPerformanceEvent> getAnomalousEvents() { return anomalousEvents; }
        public void setAnomalousEvents(List<AnomalousPerformanceEvent> anomalousEvents) { this.anomalousEvents = anomalousEvents; }
        
        public Map<String, Object> getSummary() { return summary; }
        public void setSummary(Map<String, Object> summary) { this.summary = summary; }
    }
}