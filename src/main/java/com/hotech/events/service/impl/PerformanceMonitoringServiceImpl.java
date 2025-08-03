package com.hotech.events.service.impl;

import com.hotech.events.service.PerformanceMonitoringService;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 性能监控服务实现类
 * 提供详细的性能监控和资源使用统计功能
 */
@Slf4j
@Service
public class PerformanceMonitoringServiceImpl implements PerformanceMonitoringService {
    
    @Autowired
    private SystemMonitoringService systemMonitoringService;
    
    // 系统监控Bean
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    
    // 监控会话存储
    private final ConcurrentHashMap<String, MonitoringSession> activeSessions = new ConcurrentHashMap<>();
    
    // 性能数据存储
    private final ConcurrentHashMap<String, List<PerformanceRecord>> performanceHistory = new ConcurrentHashMap<>();
    
    // 资源使用趋势数据
    private final List<ResourceUsageTrend> resourceTrends = Collections.synchronizedList(new ArrayList<>());
    
    // 请求计数器
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final AtomicLong errorCounter = new AtomicLong(0);
    
    // 最近一分钟的请求时间戳
    private final List<Long> recentRequests = Collections.synchronizedList(new ArrayList<>());
    
    public PerformanceMonitoringServiceImpl() {
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
    }
    
    @Override
    public String startMonitoring(String operationName, String operationType) {
        try {
            String sessionId = UUID.randomUUID().toString();
            
            MonitoringSession session = new MonitoringSession();
            session.setSessionId(sessionId);
            session.setOperationName(operationName);
            session.setOperationType(operationType);
            session.setStartTime(LocalDateTime.now());
            session.setStartTimestamp(System.currentTimeMillis());
            session.setMetrics(new ConcurrentHashMap<>());
            
            activeSessions.put(sessionId, session);
            
            log.debug("开始性能监控: sessionId={}, operation={}, type={}", 
                    sessionId, operationName, operationType);
            
            return sessionId;
            
        } catch (Exception e) {
            log.error("开始性能监控失败: operation={}, type={}", operationName, operationType, e);
            
            // 记录系统错误
            systemMonitoringService.recordSystemError("PERFORMANCE_MONITORING_START", 
                    "MONITORING_START_ERROR", e.getMessage(), getStackTrace(e));
            
            throw new RuntimeException("开始性能监控失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void recordMetric(String sessionId, String metricName, double value, String unit) {
        try {
            MonitoringSession session = activeSessions.get(sessionId);
            if (session == null) {
                log.warn("未找到监控会话: sessionId={}", sessionId);
                return;
            }
            
            MetricRecord metric = new MetricRecord();
            metric.setName(metricName);
            metric.setValue(value);
            metric.setUnit(unit);
            metric.setTimestamp(LocalDateTime.now());
            
            session.getMetrics().computeIfAbsent(metricName, k -> new ArrayList<>()).add(metric);
            
            log.debug("记录性能指标: sessionId={}, metric={}, value={}, unit={}", 
                    sessionId, metricName, value, unit);
            
        } catch (Exception e) {
            log.error("记录性能指标失败: sessionId={}, metric={}", sessionId, metricName, e);
        }
    }
    
    @Override
    public void endMonitoring(String sessionId, boolean success) {
        try {
            MonitoringSession session = activeSessions.remove(sessionId);
            if (session == null) {
                log.warn("未找到监控会话: sessionId={}", sessionId);
                return;
            }
            
            session.setEndTime(LocalDateTime.now());
            session.setEndTimestamp(System.currentTimeMillis());
            session.setSuccess(success);
            session.setDuration(session.getEndTimestamp() - session.getStartTimestamp());
            
            // 创建性能记录
            PerformanceRecord record = new PerformanceRecord();
            record.setOperationName(session.getOperationName());
            record.setOperationType(session.getOperationType());
            record.setStartTime(session.getStartTime());
            record.setEndTime(session.getEndTime());
            record.setDuration(session.getDuration());
            record.setSuccess(success);
            record.setMetrics(session.getMetrics());
            
            // 存储到历史记录
            performanceHistory.computeIfAbsent(session.getOperationName(), k -> new ArrayList<>()).add(record);
            
            // 更新计数器
            requestCounter.incrementAndGet();
            if (!success) {
                errorCounter.incrementAndGet();
            }
            
            // 记录到系统监控
            systemMonitoringService.recordPerformanceMetrics(session.getOperationName(), 
                    session.getDuration(), getMemoryUsage(), getCpuUsage());
            
            // 更新最近请求列表
            synchronized (recentRequests) {
                recentRequests.add(System.currentTimeMillis());
                // 清理超过1分钟的请求
                long oneMinuteAgo = System.currentTimeMillis() - 60000;
                recentRequests.removeIf(timestamp -> timestamp < oneMinuteAgo);
            }
            
            log.debug("结束性能监控: sessionId={}, operation={}, duration={}ms, success={}", 
                    sessionId, session.getOperationName(), session.getDuration(), success);
            
        } catch (Exception e) {
            log.error("结束性能监控失败: sessionId={}", sessionId, e);
            
            // 记录系统错误
            systemMonitoringService.recordSystemError("PERFORMANCE_MONITORING_END", 
                    "MONITORING_END_ERROR", e.getMessage(), getStackTrace(e));
        }
    }
    
    @Override
    public RealTimeMetrics getRealTimeMetrics() {
        try {
            RealTimeMetrics metrics = new RealTimeMetrics();
            metrics.setTimestamp(LocalDateTime.now());
            
            // CPU使用率
            double cpuUsage = getCpuUsage();
            metrics.setCpuUsage(cpuUsage);
            
            // 内存使用率
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            double memoryUsage = (double) (totalMemory - freeMemory) / totalMemory * 100;
            metrics.setMemoryUsage(memoryUsage);
            
            // 磁盘使用率（简化实现）
            double diskUsage = memoryUsage; // 简化为内存使用率
            metrics.setDiskUsage(diskUsage);
            
            // 活跃线程数
            metrics.setActiveThreads(threadBean.getThreadCount());
            
            // 活跃连接数（简化实现）
            metrics.setActiveConnections(activeSessions.size());
            
            // 每秒请求数
            synchronized (recentRequests) {
                metrics.setRequestsPerSecond(recentRequests.size());
            }
            
            // 平均响应时间
            double avgResponseTime = calculateAverageResponseTime();
            metrics.setAverageResponseTime(avgResponseTime);
            
            // 错误率
            long totalRequests = requestCounter.get();
            long totalErrors = errorCounter.get();
            double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests * 100 : 0;
            metrics.setErrorRate(errorRate);
            
            return metrics;
            
        } catch (Exception e) {
            log.error("获取实时性能指标失败", e);
            return new RealTimeMetrics();
        }
    }
    
    @Override
    public OperationPerformanceStats getOperationStats(String operationName, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<PerformanceRecord> records = performanceHistory.get(operationName);
            if (records == null || records.isEmpty()) {
                return createEmptyOperationStats(operationName);
            }
            
            // 过滤时间范围内的记录
            List<PerformanceRecord> filteredRecords = records.stream()
                    .filter(record -> record.getStartTime().isAfter(startTime) && record.getStartTime().isBefore(endTime))
                    .collect(Collectors.toList());
            
            if (filteredRecords.isEmpty()) {
                return createEmptyOperationStats(operationName);
            }
            
            OperationPerformanceStats stats = new OperationPerformanceStats();
            stats.setOperationName(operationName);
            stats.setTotalCalls(filteredRecords.size());
            
            long successfulCalls = filteredRecords.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            stats.setSuccessfulCalls(successfulCalls);
            stats.setFailedCalls(filteredRecords.size() - successfulCalls);
            stats.setSuccessRate((double) successfulCalls / filteredRecords.size() * 100);
            
            // 响应时间统计
            List<Long> durations = filteredRecords.stream()
                    .map(PerformanceRecord::getDuration)
                    .sorted()
                    .collect(Collectors.toList());
            
            stats.setAverageResponseTime(durations.stream().mapToLong(Long::longValue).average().orElse(0));
            stats.setMinResponseTime(durations.get(0));
            stats.setMaxResponseTime(durations.get(durations.size() - 1));
            
            // 计算百分位数
            int p95Index = (int) (durations.size() * 0.95);
            int p99Index = (int) (durations.size() * 0.99);
            stats.setP95ResponseTime(durations.get(Math.min(p95Index, durations.size() - 1)));
            stats.setP99ResponseTime(durations.get(Math.min(p99Index, durations.size() - 1)));
            
            // 吞吐量（每秒处理数）
            long timeRangeSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            stats.setThroughput(timeRangeSeconds > 0 ? (double) filteredRecords.size() / timeRangeSeconds : 0);
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取操作性能统计失败: operation={}", operationName, e);
            return createEmptyOperationStats(operationName);
        }
    }
    
    @Override
    public List<ResourceUsageTrend> getResourceUsageTrend(int hours) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
            
            return resourceTrends.stream()
                    .filter(trend -> trend.getTimestamp().isAfter(cutoffTime))
                    .sorted(Comparator.comparing(ResourceUsageTrend::getTimestamp))
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取资源使用趋势失败: hours={}", hours, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<PerformanceHotspot> getPerformanceHotspots(int limit) {
        try {
            List<PerformanceHotspot> hotspots = new ArrayList<>();
            
            for (Map.Entry<String, List<PerformanceRecord>> entry : performanceHistory.entrySet()) {
                String operationName = entry.getKey();
                List<PerformanceRecord> records = entry.getValue();
                
                if (records.isEmpty()) continue;
                
                // 计算平均响应时间和调用次数
                double avgResponseTime = records.stream()
                        .mapToLong(PerformanceRecord::getDuration)
                        .average()
                        .orElse(0);
                
                long callCount = records.size();
                double totalTime = records.stream().mapToLong(PerformanceRecord::getDuration).sum();
                
                // 计算影响分数（响应时间 * 调用次数）
                double impactScore = avgResponseTime * callCount;
                
                PerformanceHotspot hotspot = new PerformanceHotspot();
                hotspot.setOperationName(operationName);
                hotspot.setOperationType(records.get(0).getOperationType());
                hotspot.setAverageResponseTime(avgResponseTime);
                hotspot.setCallCount(callCount);
                hotspot.setTotalTime(totalTime);
                hotspot.setImpactScore(impactScore);
                
                hotspots.add(hotspot);
            }
            
            // 按影响分数排序并限制数量
            return hotspots.stream()
                    .sorted(Comparator.comparingDouble(PerformanceHotspot::getImpactScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取性能热点失败: limit={}", limit, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AnomalousPerformanceEvent> getAnomalousEvents(int hours) {
        try {
            List<AnomalousPerformanceEvent> anomalousEvents = new ArrayList<>();
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
            
            for (Map.Entry<String, List<PerformanceRecord>> entry : performanceHistory.entrySet()) {
                String operationName = entry.getKey();
                List<PerformanceRecord> records = entry.getValue().stream()
                        .filter(record -> record.getStartTime().isAfter(cutoffTime))
                        .collect(Collectors.toList());
                
                if (records.isEmpty()) continue;
                
                // 计算平均响应时间和标准差
                double avgResponseTime = records.stream()
                        .mapToLong(PerformanceRecord::getDuration)
                        .average()
                        .orElse(0);
                
                double threshold = avgResponseTime * 2; // 简化：2倍平均时间作为异常阈值
                
                // 查找异常事件
                for (PerformanceRecord record : records) {
                    if (record.getDuration() > threshold) {
                        AnomalousPerformanceEvent event = new AnomalousPerformanceEvent();
                        event.setOperationName(operationName);
                        event.setTimestamp(record.getStartTime());
                        event.setResponseTime(record.getDuration());
                        event.setThreshold(threshold);
                        event.setAnomalyType("SLOW_RESPONSE");
                        event.setDescription(String.format("响应时间 %.2fms 超过阈值 %.2fms", 
                                (double) record.getDuration(), threshold));
                        
                        anomalousEvents.add(event);
                    }
                }
            }
            
            return anomalousEvents.stream()
                    .sorted(Comparator.comparing(AnomalousPerformanceEvent::getTimestamp).reversed())
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取异常性能事件失败: hours={}", hours, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public PerformanceReport generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            PerformanceReport report = new PerformanceReport();
            report.setReportStartTime(startTime);
            report.setReportEndTime(endTime);
            report.setGeneratedAt(LocalDateTime.now());
            
            // 整体指标
            report.setOverallMetrics(getRealTimeMetrics());
            
            // 操作统计
            List<OperationPerformanceStats> operationStats = new ArrayList<>();
            for (String operationName : performanceHistory.keySet()) {
                OperationPerformanceStats stats = getOperationStats(operationName, startTime, endTime);
                if (stats.getTotalCalls() > 0) {
                    operationStats.add(stats);
                }
            }
            report.setOperationStats(operationStats);
            
            // 性能热点
            report.setHotspots(getPerformanceHotspots(10));
            
            // 异常事件
            int hours = (int) java.time.Duration.between(startTime, endTime).toHours();
            report.setAnomalousEvents(getAnomalousEvents(Math.max(hours, 1)));
            
            // 摘要信息
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalOperations", performanceHistory.size());
            summary.put("totalRequests", requestCounter.get());
            summary.put("totalErrors", errorCounter.get());
            summary.put("overallErrorRate", requestCounter.get() > 0 ? 
                    (double) errorCounter.get() / requestCounter.get() * 100 : 0);
            report.setSummary(summary);
            
            return report;
            
        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            return new PerformanceReport();
        }
    }
    
    /**
     * 定时收集资源使用趋势数据
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void collectResourceUsageTrend() {
        try {
            ResourceUsageTrend trend = new ResourceUsageTrend();
            trend.setTimestamp(LocalDateTime.now());
            trend.setCpuUsage(getCpuUsage());
            
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            trend.setMemoryUsage((double) (totalMemory - freeMemory) / totalMemory * 100);
            
            trend.setDiskUsage(trend.getMemoryUsage()); // 简化实现
            trend.setNetworkUsage(0); // 简化实现
            trend.setActiveThreads(threadBean.getThreadCount());
            
            resourceTrends.add(trend);
            
            // 保留最近24小时的数据
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            resourceTrends.removeIf(t -> t.getTimestamp().isBefore(cutoffTime));
            
        } catch (Exception e) {
            log.error("收集资源使用趋势数据失败", e);
        }
    }
    
    /**
     * 定时清理过期的性能记录
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredRecords() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 保留7天数据
            int removedCount = 0;
            
            for (String operationName : performanceHistory.keySet()) {
                List<PerformanceRecord> records = performanceHistory.get(operationName);
                if (records != null) {
                    int originalSize = records.size();
                    records.removeIf(record -> record.getStartTime().isBefore(cutoffTime));
                    removedCount += originalSize - records.size();
                }
            }
            
            if (removedCount > 0) {
                log.info("清理过期性能记录: removedCount={}", removedCount);
            }
            
        } catch (Exception e) {
            log.error("清理过期性能记录失败", e);
        }
    }
    
    /**
     * 计算平均响应时间
     */
    private double calculateAverageResponseTime() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        return performanceHistory.values().stream()
                .flatMap(List::stream)
                .filter(record -> record.getStartTime().isAfter(oneHourAgo))
                .mapToLong(PerformanceRecord::getDuration)
                .average()
                .orElse(0);
    }
    
    /**
     * 创建空的操作统计
     */
    private OperationPerformanceStats createEmptyOperationStats(String operationName) {
        OperationPerformanceStats stats = new OperationPerformanceStats();
        stats.setOperationName(operationName);
        stats.setTotalCalls(0);
        stats.setSuccessfulCalls(0);
        stats.setFailedCalls(0);
        stats.setSuccessRate(0);
        stats.setAverageResponseTime(0);
        stats.setMinResponseTime(0);
        stats.setMaxResponseTime(0);
        stats.setP95ResponseTime(0);
        stats.setP99ResponseTime(0);
        stats.setThroughput(0);
        return stats;
    }
    
    /**
     * 获取内存使用量
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Throwable throwable) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "无法获取堆栈跟踪: " + e.getMessage();
        }
    }
    
    /**
     * 监控会话内部类
     */
    private static class MonitoringSession {
        private String sessionId;
        private String operationName;
        private String operationType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long startTimestamp;
        private long endTimestamp;
        private long duration;
        private boolean success;
        private Map<String, List<MetricRecord>> metrics;
        
        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getStartTimestamp() { return startTimestamp; }
        public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }
        
        public long getEndTimestamp() { return endTimestamp; }
        public void setEndTimestamp(long endTimestamp) { this.endTimestamp = endTimestamp; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Map<String, List<MetricRecord>> getMetrics() { return metrics; }
        public void setMetrics(Map<String, List<MetricRecord>> metrics) { this.metrics = metrics; }
    }
    
    /**
     * 指标记录内部类
     */
    private static class MetricRecord {
        private String name;
        private double value;
        private String unit;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * 性能记录内部类
     */
    private static class PerformanceRecord {
        private String operationName;
        private String operationType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long duration;
        private boolean success;
        private Map<String, List<MetricRecord>> metrics;
        
        // Getters and Setters
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Map<String, List<MetricRecord>> getMetrics() { return metrics; }
        public void setMetrics(Map<String, List<MetricRecord>> metrics) { this.metrics = metrics; }
    }
}