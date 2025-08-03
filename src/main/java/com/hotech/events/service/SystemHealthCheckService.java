package com.hotech.events.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统健康检查服务接口
 * 监控各组件状态和系统整体健康度
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface SystemHealthCheckService {
    
    /**
     * 执行全面的系统健康检查
     */
    SystemHealthReport performFullHealthCheck();
    
    /**
     * 检查API服务健康状态
     */
    ApiHealthStatus checkApiHealth();
    
    /**
     * 检查数据库连接健康状态
     */
    DatabaseHealthStatus checkDatabaseHealth();
    
    /**
     * 检查缓存系统健康状态
     */
    CacheHealthStatus checkCacheHealth();
    
    /**
     * 检查重复检测服务健康状态
     */
    DuplicationDetectionHealthStatus checkDuplicationDetectionHealth();
    
    /**
     * 检查事件解析服务健康状态
     */
    EventParsingHealthStatus checkEventParsingHealth();
    
    /**
     * 获取系统性能指标
     */
    SystemPerformanceMetrics getPerformanceMetrics();
    
    /**
     * 获取健康检查历史记录
     */
    List<HealthCheckRecord> getHealthCheckHistory(int limit);
    
    /**
     * 系统健康报告
     */
    class SystemHealthReport {
        private String overallStatus; // HEALTHY, WARNING, CRITICAL
        private double overallScore; // 0-1
        private LocalDateTime checkTime;
        private ApiHealthStatus apiHealth;
        private DatabaseHealthStatus databaseHealth;
        private CacheHealthStatus cacheHealth;
        private DuplicationDetectionHealthStatus duplicationDetectionHealth;
        private EventParsingHealthStatus eventParsingHealth;
        private SystemPerformanceMetrics performanceMetrics;
        private List<String> warnings;
        private List<String> errors;
        
        // getters and setters
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        
        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
        
        public ApiHealthStatus getApiHealth() { return apiHealth; }
        public void setApiHealth(ApiHealthStatus apiHealth) { this.apiHealth = apiHealth; }
        
        public DatabaseHealthStatus getDatabaseHealth() { return databaseHealth; }
        public void setDatabaseHealth(DatabaseHealthStatus databaseHealth) { this.databaseHealth = databaseHealth; }
        
        public CacheHealthStatus getCacheHealth() { return cacheHealth; }
        public void setCacheHealth(CacheHealthStatus cacheHealth) { this.cacheHealth = cacheHealth; }
        
        public DuplicationDetectionHealthStatus getDuplicationDetectionHealth() { return duplicationDetectionHealth; }
        public void setDuplicationDetectionHealth(DuplicationDetectionHealthStatus duplicationDetectionHealth) { 
            this.duplicationDetectionHealth = duplicationDetectionHealth; 
        }
        
        public EventParsingHealthStatus getEventParsingHealth() { return eventParsingHealth; }
        public void setEventParsingHealth(EventParsingHealthStatus eventParsingHealth) { 
            this.eventParsingHealth = eventParsingHealth; 
        }
        
        public SystemPerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(SystemPerformanceMetrics performanceMetrics) { 
            this.performanceMetrics = performanceMetrics; 
        }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
    
    /**
     * API健康状态
     */
    class ApiHealthStatus {
        private String status; // HEALTHY, WARNING, CRITICAL
        private double successRate;
        private long averageResponseTime;
        private Map<String, ApiEndpointHealth> endpoints;
        private String primaryApi;
        private String fallbackApi;
        private int totalCalls;
        private int failedCalls;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public long getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(long averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public Map<String, ApiEndpointHealth> getEndpoints() { return endpoints; }
        public void setEndpoints(Map<String, ApiEndpointHealth> endpoints) { this.endpoints = endpoints; }
        
        public String getPrimaryApi() { return primaryApi; }
        public void setPrimaryApi(String primaryApi) { this.primaryApi = primaryApi; }
        
        public String getFallbackApi() { return fallbackApi; }
        public void setFallbackApi(String fallbackApi) { this.fallbackApi = fallbackApi; }
        
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
        
        public int getFailedCalls() { return failedCalls; }
        public void setFailedCalls(int failedCalls) { this.failedCalls = failedCalls; }
    }
    
    /**
     * API端点健康状态
     */
    class ApiEndpointHealth {
        private String status;
        private long responseTime;
        private LocalDateTime lastCheck;
        private String lastError;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        
        public LocalDateTime getLastCheck() { return lastCheck; }
        public void setLastCheck(LocalDateTime lastCheck) { this.lastCheck = lastCheck; }
        
        public String getLastError() { return lastError; }
        public void setLastError(String lastError) { this.lastError = lastError; }
    }
    
    /**
     * 数据库健康状态
     */
    class DatabaseHealthStatus {
        private String status;
        private long connectionTime;
        private int activeConnections;
        private int maxConnections;
        private double connectionPoolUsage;
        private LocalDateTime lastCheck;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getConnectionTime() { return connectionTime; }
        public void setConnectionTime(long connectionTime) { this.connectionTime = connectionTime; }
        
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        
        public double getConnectionPoolUsage() { return connectionPoolUsage; }
        public void setConnectionPoolUsage(double connectionPoolUsage) { this.connectionPoolUsage = connectionPoolUsage; }
        
        public LocalDateTime getLastCheck() { return lastCheck; }
        public void setLastCheck(LocalDateTime lastCheck) { this.lastCheck = lastCheck; }
    }
    
    /**
     * 缓存健康状态
     */
    class CacheHealthStatus {
        private String status;
        private double hitRate;
        private long totalRequests;
        private long cacheHits;
        private long cacheMisses;
        private double memoryUsage;
        private LocalDateTime lastCheck;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }
        
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        
        public long getCacheHits() { return cacheHits; }
        public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }
        
        public long getCacheMisses() { return cacheMisses; }
        public void setCacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public LocalDateTime getLastCheck() { return lastCheck; }
        public void setLastCheck(LocalDateTime lastCheck) { this.lastCheck = lastCheck; }
    }
    
    /**
     * 重复检测健康状态
     */
    class DuplicationDetectionHealthStatus {
        private String status;
        private int cacheRecordCount;
        private int expiredRecordCount;
        private double detectionSuccessRate;
        private long averageDetectionTime;
        private LocalDateTime lastCleanup;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getCacheRecordCount() { return cacheRecordCount; }
        public void setCacheRecordCount(int cacheRecordCount) { this.cacheRecordCount = cacheRecordCount; }
        
        public int getExpiredRecordCount() { return expiredRecordCount; }
        public void setExpiredRecordCount(int expiredRecordCount) { this.expiredRecordCount = expiredRecordCount; }
        
        public double getDetectionSuccessRate() { return detectionSuccessRate; }
        public void setDetectionSuccessRate(double detectionSuccessRate) { this.detectionSuccessRate = detectionSuccessRate; }
        
        public long getAverageDetectionTime() { return averageDetectionTime; }
        public void setAverageDetectionTime(long averageDetectionTime) { this.averageDetectionTime = averageDetectionTime; }
        
        public LocalDateTime getLastCleanup() { return lastCleanup; }
        public void setLastCleanup(LocalDateTime lastCleanup) { this.lastCleanup = lastCleanup; }
    }
    
    /**
     * 事件解析健康状态
     */
    class EventParsingHealthStatus {
        private String status;
        private double parsingSuccessRate;
        private long averageParsingTime;
        private int totalParsingAttempts;
        private int successfulParsing;
        private Map<String, Integer> parsingMethodStats;
        
        // getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getParsingSuccessRate() { return parsingSuccessRate; }
        public void setParsingSuccessRate(double parsingSuccessRate) { this.parsingSuccessRate = parsingSuccessRate; }
        
        public long getAverageParsingTime() { return averageParsingTime; }
        public void setAverageParsingTime(long averageParsingTime) { this.averageParsingTime = averageParsingTime; }
        
        public int getTotalParsingAttempts() { return totalParsingAttempts; }
        public void setTotalParsingAttempts(int totalParsingAttempts) { this.totalParsingAttempts = totalParsingAttempts; }
        
        public int getSuccessfulParsing() { return successfulParsing; }
        public void setSuccessfulParsing(int successfulParsing) { this.successfulParsing = successfulParsing; }
        
        public Map<String, Integer> getParsingMethodStats() { return parsingMethodStats; }
        public void setParsingMethodStats(Map<String, Integer> parsingMethodStats) { this.parsingMethodStats = parsingMethodStats; }
    }
    
    /**
     * 系统性能指标
     */
    class SystemPerformanceMetrics {
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private long jvmHeapUsed;
        private long jvmHeapMax;
        private int activeThreads;
        private LocalDateTime measureTime;
        
        // getters and setters
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        
        public long getJvmHeapUsed() { return jvmHeapUsed; }
        public void setJvmHeapUsed(long jvmHeapUsed) { this.jvmHeapUsed = jvmHeapUsed; }
        
        public long getJvmHeapMax() { return jvmHeapMax; }
        public void setJvmHeapMax(long jvmHeapMax) { this.jvmHeapMax = jvmHeapMax; }
        
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
        
        public LocalDateTime getMeasureTime() { return measureTime; }
        public void setMeasureTime(LocalDateTime measureTime) { this.measureTime = measureTime; }
    }
    
    /**
     * 健康检查记录
     */
    class HealthCheckRecord {
        private LocalDateTime checkTime;
        private String overallStatus;
        private double overallScore;
        private String details;
        
        // getters and setters
        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
        
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}