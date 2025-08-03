package com.hotech.events.service.impl;

import com.hotech.events.dto.Alert;
import com.hotech.events.dto.MonitoringData;
import com.hotech.events.service.TimelineEventMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间线事件监控服务实现类
 */
@Slf4j
@Service
public class TimelineEventMonitoringServiceImpl implements TimelineEventMonitoringService {
    
    // 监控状态
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    
    // 监控数据存储（内存中，实际应用中应该使用数据库或缓存）
    private final List<MonitoringData> monitoringDataList = new CopyOnWriteArrayList<>();
    
    // 活跃警告列表
    private final List<Alert> activeAlerts = new CopyOnWriteArrayList<>();
    
    // 警告阈值配置
    private final Map<String, Integer> alertThresholds = new ConcurrentHashMap<>();
    
    // 警告ID生成器
    private final AtomicLong alertIdGenerator = new AtomicLong(1);
    
    // 性能统计
    private final Map<String, AtomicLong> performanceCounters = new ConcurrentHashMap<>();
    
    @Override
    public void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            log.info("时间线事件监控服务已启动");
            initializeDefaultThresholds();
            initializePerformanceCounters();
        } else {
            log.warn("监控服务已经在运行中");
        }
    }
    
    @Override
    public void stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            log.info("时间线事件监控服务已停止");
        } else {
            log.warn("监控服务未在运行");
        }
    }
    
    @Override
    public void recordMonitoringData(MonitoringData data) {
        if (!isMonitoring.get()) {
            return;
        }
        
        // 设置时间戳
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        
        // 记录监控数据
        monitoringDataList.add(data);
        
        // 更新性能计数器
        updatePerformanceCounters(data);
        
        // 检查是否需要触发警告
        checkAndCreateAlerts(data);
        
        log.debug("记录监控数据: {}", data);
    }    
@Override
    public void recordTimelineOperation(Long timelineId, String operation, boolean successful, 
                                      Long duration, String errorMessage) {
        MonitoringData data = MonitoringData.builder()
            .eventType(getEventTypeForOperation(operation))
            .timelineId(timelineId)
            .operation(operation)
            .successful(successful)
            .duration(duration)
            .errorMessage(errorMessage)
            .build();
        
        recordMonitoringData(data);
    }
    
    @Override
    public void recordEventAssociation(Long timelineId, Long eventId, String operation, 
                                     boolean successful, String errorMessage) {
        MonitoringData data = MonitoringData.builder()
            .eventType(MonitoringData.EventType.EVENT_ASSOCIATED)
            .timelineId(timelineId)
            .eventId(eventId)
            .operation(operation)
            .successful(successful)
            .errorMessage(errorMessage)
            .build();
        
        recordMonitoringData(data);
    }
    
    @Override
    public void recordDiagnosisOperation(Long timelineId, int issuesFound, Long duration) {
        MonitoringData data = MonitoringData.builder()
            .eventType(MonitoringData.EventType.DIAGNOSIS_PERFORMED)
            .timelineId(timelineId)
            .operation(MonitoringData.Operation.DIAGNOSE)
            .successful(true)
            .duration(duration)
            .details(Map.of("issuesFound", issuesFound))
            .build();
        
        recordMonitoringData(data);
    }
    
    @Override
    public void recordRepairOperation(Long timelineId, int issuesRepaired, Long duration, boolean successful) {
        MonitoringData data = MonitoringData.builder()
            .eventType(MonitoringData.EventType.REPAIR_EXECUTED)
            .timelineId(timelineId)
            .operation(MonitoringData.Operation.REPAIR)
            .successful(successful)
            .duration(duration)
            .details(Map.of("issuesRepaired", issuesRepaired))
            .build();
        
        recordMonitoringData(data);
    }
    
    @Override
    public Map<String, Object> getMonitoringReport(LocalDateTime startTime, LocalDateTime endTime) {
        List<MonitoringData> filteredData = monitoringDataList.stream()
            .filter(data -> data.getTimestamp().isAfter(startTime) && data.getTimestamp().isBefore(endTime))
            .toList();
        
        Map<String, Object> report = new HashMap<>();
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        report.put("totalOperations", filteredData.size());
        report.put("successfulOperations", filteredData.stream().mapToInt(data -> data.isSuccessful() ? 1 : 0).sum());
        report.put("failedOperations", filteredData.stream().mapToInt(data -> data.isSuccessful() ? 0 : 1).sum());
        
        // 按操作类型统计
        Map<String, Long> operationStats = filteredData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                MonitoringData::getOperation,
                java.util.stream.Collectors.counting()
            ));
        report.put("operationStatistics", operationStats);
        
        // 按事件类型统计
        Map<String, Long> eventTypeStats = filteredData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                MonitoringData::getEventType,
                java.util.stream.Collectors.counting()
            ));
        report.put("eventTypeStatistics", eventTypeStats);
        
        // 平均执行时长
        OptionalDouble avgDuration = filteredData.stream()
            .filter(data -> data.getDuration() != null)
            .mapToLong(MonitoringData::getDuration)
            .average();
        report.put("averageDuration", avgDuration.orElse(0.0));
        
        return report;
    }
    
    @Override
    public Map<String, Object> getRealTimeMonitoringData() {
        Map<String, Object> realTimeData = new HashMap<>();
        
        // 最近5分钟的数据
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<MonitoringData> recentData = monitoringDataList.stream()
            .filter(data -> data.getTimestamp().isAfter(fiveMinutesAgo))
            .toList();
        
        realTimeData.put("recentOperations", recentData.size());
        realTimeData.put("recentSuccessRate", calculateSuccessRate(recentData));
        realTimeData.put("activeAlerts", activeAlerts.size());
        realTimeData.put("isMonitoring", isMonitoring.get());
        realTimeData.put("lastUpdateTime", LocalDateTime.now());
        
        // 性能计数器
        Map<String, Long> counters = new HashMap<>();
        performanceCounters.forEach((key, value) -> counters.put(key, value.get()));
        realTimeData.put("performanceCounters", counters);
        
        return realTimeData;
    }    

    @Override
    public void setAlertThreshold(String alertType, int threshold) {
        alertThresholds.put(alertType, threshold);
        log.info("设置警告阈值: {} = {}", alertType, threshold);
    }
    
    @Override
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.stream()
            .filter(alert -> !alert.isResolved())
            .toList());
    }
    
    @Override
    public Alert createAlert(Alert.AlertType type, Alert.AlertSeverity severity, String message,
                           Long timelineId, Long eventId, Map<String, Object> details) {
        Alert alert = Alert.builder()
            .id(alertIdGenerator.getAndIncrement())
            .type(type)
            .severity(severity)
            .message(message)
            .createdAt(LocalDateTime.now())
            .resolved(false)
            .timelineId(timelineId)
            .eventId(eventId)
            .details(details)
            .build();
        
        activeAlerts.add(alert);
        log.warn("创建警告: {} - {}", type, message);
        
        return alert;
    }
    
    @Override
    public boolean resolveAlert(Long alertId) {
        for (Alert alert : activeAlerts) {
            if (alert.getId().equals(alertId) && !alert.isResolved()) {
                alert.setResolved(true);
                alert.setResolvedAt(LocalDateTime.now());
                log.info("解决警告: {} - {}", alert.getType(), alert.getMessage());
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // 检查监控服务状态
        health.put("monitoringServiceActive", isMonitoring.get());
        
        // 检查最近的错误率
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<MonitoringData> recentData = monitoringDataList.stream()
            .filter(data -> data.getTimestamp().isAfter(oneHourAgo))
            .toList();
        
        double errorRate = calculateErrorRate(recentData);
        health.put("errorRate", errorRate);
        health.put("healthStatus", determineHealthStatus(errorRate));
        
        // 检查活跃警告数量
        long criticalAlerts = activeAlerts.stream()
            .filter(alert -> !alert.isResolved() && alert.getSeverity() == Alert.AlertSeverity.CRITICAL)
            .count();
        health.put("criticalAlerts", criticalAlerts);
        
        // 检查数据存储状态
        health.put("monitoringDataCount", monitoringDataList.size());
        health.put("lastDataTimestamp", 
            monitoringDataList.isEmpty() ? null : 
            monitoringDataList.get(monitoringDataList.size() - 1).getTimestamp());
        
        return health;
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        List<MonitoringData> filteredData = monitoringDataList.stream()
            .filter(data -> data.getTimestamp().isAfter(startTime) && data.getTimestamp().isBefore(endTime))
            .toList();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // 响应时间统计
        List<Long> durations = filteredData.stream()
            .filter(data -> data.getDuration() != null)
            .map(MonitoringData::getDuration)
            .sorted()
            .toList();
        
        if (!durations.isEmpty()) {
            metrics.put("minDuration", durations.get(0));
            metrics.put("maxDuration", durations.get(durations.size() - 1));
            metrics.put("avgDuration", durations.stream().mapToLong(Long::longValue).average().orElse(0.0));
            metrics.put("p95Duration", getPercentile(durations, 0.95));
            metrics.put("p99Duration", getPercentile(durations, 0.99));
        }
        
        // 吞吐量统计
        metrics.put("totalOperations", filteredData.size());
        metrics.put("operationsPerMinute", calculateOperationsPerMinute(filteredData, startTime, endTime));
        
        // 成功率统计
        metrics.put("successRate", calculateSuccessRate(filteredData));
        metrics.put("errorRate", calculateErrorRate(filteredData));
        
        return metrics;
    }
    
    @Override
    public int cleanupExpiredData(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        
        int removedCount = 0;
        Iterator<MonitoringData> iterator = monitoringDataList.iterator();
        while (iterator.hasNext()) {
            MonitoringData data = iterator.next();
            if (data.getTimestamp().isBefore(cutoffTime)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        // 清理已解决的旧警告
        Iterator<Alert> alertIterator = activeAlerts.iterator();
        while (alertIterator.hasNext()) {
            Alert alert = alertIterator.next();
            if (alert.isResolved() && alert.getResolvedAt() != null && 
                alert.getResolvedAt().isBefore(cutoffTime)) {
                alertIterator.remove();
            }
        }
        
        log.info("清理过期监控数据: 删除 {} 条记录", removedCount);
        return removedCount;
    }   
    //辅助方法
    private void initializeDefaultThresholds() {
        alertThresholds.put("ERROR_RATE", 10); // 错误率超过10%触发警告
        alertThresholds.put("RESPONSE_TIME", 5000); // 响应时间超过5秒触发警告
        alertThresholds.put("FAILED_OPERATIONS", 5); // 5分钟内失败操作超过5次触发警告
    }
    
    private void initializePerformanceCounters() {
        performanceCounters.put("totalOperations", new AtomicLong(0));
        performanceCounters.put("successfulOperations", new AtomicLong(0));
        performanceCounters.put("failedOperations", new AtomicLong(0));
        performanceCounters.put("timelineOperations", new AtomicLong(0));
        performanceCounters.put("eventOperations", new AtomicLong(0));
        performanceCounters.put("diagnosisOperations", new AtomicLong(0));
        performanceCounters.put("repairOperations", new AtomicLong(0));
    }
    
    private void updatePerformanceCounters(MonitoringData data) {
        performanceCounters.get("totalOperations").incrementAndGet();
        
        if (data.isSuccessful()) {
            performanceCounters.get("successfulOperations").incrementAndGet();
        } else {
            performanceCounters.get("failedOperations").incrementAndGet();
        }
        
        // 根据操作类型更新计数器
        switch (data.getOperation()) {
            case MonitoringData.Operation.CREATE:
            case MonitoringData.Operation.UPDATE:
            case MonitoringData.Operation.DELETE:
                performanceCounters.get("timelineOperations").incrementAndGet();
                break;
            case MonitoringData.Operation.ASSOCIATE:
                performanceCounters.get("eventOperations").incrementAndGet();
                break;
            case MonitoringData.Operation.DIAGNOSE:
                performanceCounters.get("diagnosisOperations").incrementAndGet();
                break;
            case MonitoringData.Operation.REPAIR:
                performanceCounters.get("repairOperations").incrementAndGet();
                break;
        }
    }
    
    private void checkAndCreateAlerts(MonitoringData data) {
        // 检查响应时间警告
        if (data.getDuration() != null && data.getDuration() > alertThresholds.get("RESPONSE_TIME")) {
            createAlert(
                Alert.AlertType.PERFORMANCE_DEGRADATION,
                Alert.AlertSeverity.MEDIUM,
                String.format("操作响应时间过长: %d ms", data.getDuration()),
                data.getTimelineId(),
                data.getEventId(),
                Map.of("duration", data.getDuration(), "operation", data.getOperation())
            );
        }
        
        // 检查错误率警告
        if (!data.isSuccessful()) {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            long recentFailures = monitoringDataList.stream()
                .filter(d -> d.getTimestamp().isAfter(fiveMinutesAgo))
                .filter(d -> !d.isSuccessful())
                .count();
            
            if (recentFailures >= alertThresholds.get("FAILED_OPERATIONS")) {
                createAlert(
                    Alert.AlertType.HIGH_ERROR_RATE,
                    Alert.AlertSeverity.HIGH,
                    String.format("5分钟内失败操作过多: %d 次", recentFailures),
                    data.getTimelineId(),
                    data.getEventId(),
                    Map.of("failureCount", recentFailures, "timeWindow", "5分钟")
                );
            }
        }
    }
    
    private String getEventTypeForOperation(String operation) {
        switch (operation) {
            case MonitoringData.Operation.CREATE:
                return MonitoringData.EventType.TIMELINE_CREATED;
            case MonitoringData.Operation.UPDATE:
                return MonitoringData.EventType.TIMELINE_UPDATED;
            case MonitoringData.Operation.DELETE:
                return MonitoringData.EventType.TIMELINE_DELETED;
            case MonitoringData.Operation.GENERATE:
                return MonitoringData.EventType.GENERATION_STARTED;
            default:
                return "UNKNOWN_OPERATION";
        }
    }
    
    private double calculateSuccessRate(List<MonitoringData> data) {
        if (data.isEmpty()) return 100.0;
        
        long successCount = data.stream().mapToLong(d -> d.isSuccessful() ? 1 : 0).sum();
        return (double) successCount / data.size() * 100.0;
    }
    
    private double calculateErrorRate(List<MonitoringData> data) {
        return 100.0 - calculateSuccessRate(data);
    }
    
    private String determineHealthStatus(double errorRate) {
        if (errorRate < 1.0) return "HEALTHY";
        if (errorRate < 5.0) return "WARNING";
        if (errorRate < 10.0) return "DEGRADED";
        return "CRITICAL";
    }
    
    private long getPercentile(List<Long> sortedList, double percentile) {
        if (sortedList.isEmpty()) return 0;
        
        int index = (int) Math.ceil(percentile * sortedList.size()) - 1;
        index = Math.max(0, Math.min(index, sortedList.size() - 1));
        return sortedList.get(index);
    }
    
    private double calculateOperationsPerMinute(List<MonitoringData> data, LocalDateTime startTime, LocalDateTime endTime) {
        if (data.isEmpty()) return 0.0;
        
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (minutes == 0) minutes = 1;
        
        return (double) data.size() / minutes;
    }
}