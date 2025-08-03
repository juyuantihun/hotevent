package com.hotech.events.service.impl;

import com.hotech.events.debug.DebuggingEnhancer;
import com.hotech.events.debug.DeepSeekResponseDebugger;
import com.hotech.events.service.ApiMonitoringService;
import com.hotech.events.service.RealTimeMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 实时监控服务实现
 * 提供API调用状态和事件解析情况的实时监控
 */
@Slf4j
@Service
public class RealTimeMonitoringServiceImpl implements RealTimeMonitoringService {

    @Autowired
    private DebuggingEnhancer debuggingEnhancer;

    @Autowired
    private DeepSeekResponseDebugger responseDebugger;

    @Autowired(required = false)
    private ApiMonitoringService apiMonitoringService;

    // 监控数据存储
    private final Map<String, AtomicLong> apiCallCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> apiResponseTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> eventParsingCounters = new ConcurrentHashMap<>();
    private final Map<String, String> recentErrors = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastUpdateTimes = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> alertHistory = Collections.synchronizedList(new ArrayList<>());

    // 性能指标
    private final AtomicLong totalApiCalls = new AtomicLong(0);
    private final AtomicLong successfulApiCalls = new AtomicLong(0);
    private final AtomicLong totalEventsParsed = new AtomicLong(0);
    private final AtomicLong totalParsingAttempts = new AtomicLong(0);

    @Override
    public Map<String, Object> getApiCallStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // API调用统计
            Map<String, Long> callCounts = new HashMap<>();
            apiCallCounters.forEach((key, value) -> callCounts.put(key, value.get()));
            status.put("callCounts", callCounts);
            
            // 响应时间统计
            Map<String, Long> responseTimes = new HashMap<>();
            apiResponseTimes.forEach((key, value) -> responseTimes.put(key, value.get()));
            status.put("averageResponseTimes", responseTimes);
            
            // 成功率计算
            long total = totalApiCalls.get();
            long successful = successfulApiCalls.get();
            double successRate = total > 0 ? (double) successful / total * 100 : 0;
            status.put("successRate", String.format("%.2f%%", successRate));
            
            // 最近更新时间
            status.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.debug("API调用状态: {}", status);
            
        } catch (Exception e) {
            log.error("获取API调用状态失败: {}", e.getMessage(), e);
            status.put("error", "获取状态失败: " + e.getMessage());
        }
        
        return status;
    }

    @Override
    public Map<String, Object> getEventParsingStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 解析统计
            Map<String, Integer> parsingCounts = new HashMap<>();
            eventParsingCounters.forEach((key, value) -> parsingCounts.put(key, value.get()));
            status.put("parsingCounts", parsingCounts);
            
            // 解析成功率
            long totalAttempts = totalParsingAttempts.get();
            long totalParsed = totalEventsParsed.get();
            double parsingSuccessRate = totalAttempts > 0 ? (double) totalParsed / totalAttempts * 100 : 0;
            status.put("parsingSuccessRate", String.format("%.2f%%", parsingSuccessRate));
            
            // 平均事件数量
            double avgEventsPerParsing = totalAttempts > 0 ? (double) totalParsed / totalAttempts : 0;
            status.put("averageEventsPerParsing", String.format("%.2f", avgEventsPerParsing));
            
            // 最近错误
            status.put("recentErrors", new HashMap<>(recentErrors));
            
            status.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.debug("事件解析状态: {}", status);
            
        } catch (Exception e) {
            log.error("获取事件解析状态失败: {}", e.getMessage(), e);
            status.put("error", "获取状态失败: " + e.getMessage());
        }
        
        return status;
    }

    @Override
    public Map<String, Object> getSystemHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 系统资源信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("totalMemory", runtime.totalMemory());
            memoryInfo.put("freeMemory", runtime.freeMemory());
            memoryInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            memoryInfo.put("maxMemory", runtime.maxMemory());
            
            double memoryUsagePercent = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100;
            memoryInfo.put("usagePercent", String.format("%.2f%%", memoryUsagePercent));
            
            health.put("memory", memoryInfo);
            
            // 系统状态评估
            String healthStatus = "HEALTHY";
            List<String> issues = new ArrayList<>();
            
            if (memoryUsagePercent > 80) {
                healthStatus = "WARNING";
                issues.add("内存使用率过高");
            }
            
            long totalCalls = totalApiCalls.get();
            long successfulCalls = successfulApiCalls.get();
            if (totalCalls > 10 && (double) successfulCalls / totalCalls < 0.8) {
                healthStatus = "WARNING";
                issues.add("API调用成功率过低");
            }
            
            health.put("status", healthStatus);
            health.put("issues", issues);
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.debug("系统健康状态: {}", health);
            
        } catch (Exception e) {
            log.error("获取系统健康状态失败: {}", e.getMessage(), e);
            health.put("status", "ERROR");
            health.put("error", "获取状态失败: " + e.getMessage());
        }
        
        return health;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 基本性能指标
            metrics.put("totalApiCalls", totalApiCalls.get());
            metrics.put("successfulApiCalls", successfulApiCalls.get());
            metrics.put("totalEventsParsed", totalEventsParsed.get());
            metrics.put("totalParsingAttempts", totalParsingAttempts.get());
            
            // 计算平均值
            long totalCalls = totalApiCalls.get();
            if (totalCalls > 0) {
                long totalResponseTime = apiResponseTimes.values().stream()
                    .mapToLong(AtomicLong::get).sum();
                double avgResponseTime = (double) totalResponseTime / totalCalls;
                metrics.put("averageResponseTime", String.format("%.2f ms", avgResponseTime));
            }
            
            // 获取调试统计信息
            if (debuggingEnhancer != null) {
                Map<String, Object> debugStats = debuggingEnhancer.getDebugStatistics();
                metrics.put("debugStatistics", debugStats);
            }
            
            // 获取响应调试统计信息
            if (responseDebugger != null) {
                Map<String, Object> responseStats = responseDebugger.getDebugStatistics();
                metrics.put("responseDebugStatistics", responseStats);
            }
            
            metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.debug("性能指标: {}", metrics);
            
        } catch (Exception e) {
            log.error("获取性能指标失败: {}", e.getMessage(), e);
            metrics.put("error", "获取指标失败: " + e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getAlertStatus() {
        Map<String, Object> alerts = new HashMap<>();
        
        try {
            List<Map<String, Object>> currentAlerts = new ArrayList<>();
            
            // 检查API调用成功率告警
            long totalCalls = totalApiCalls.get();
            long successfulCalls = successfulApiCalls.get();
            if (totalCalls > 10) {
                double successRate = (double) successfulCalls / totalCalls;
                if (successRate < 0.8) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "API_SUCCESS_RATE_LOW");
                    alert.put("severity", "WARNING");
                    alert.put("message", String.format("API调用成功率过低: %.2f%%", successRate * 100));
                    alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    currentAlerts.add(alert);
                }
            }
            
            // 检查内存使用告警
            Runtime runtime = Runtime.getRuntime();
            double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();
            if (memoryUsage > 0.8) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "HIGH_MEMORY_USAGE");
                alert.put("severity", "WARNING");
                alert.put("message", String.format("内存使用率过高: %.2f%%", memoryUsage * 100));
                alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                currentAlerts.add(alert);
            }
            
            // 检查事件解析告警
            long totalAttempts = totalParsingAttempts.get();
            long totalParsed = totalEventsParsed.get();
            if (totalAttempts > 5) {
                double avgEventsPerAttempt = (double) totalParsed / totalAttempts;
                if (avgEventsPerAttempt < 5) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "LOW_EVENT_COUNT");
                    alert.put("severity", "WARNING");
                    alert.put("message", String.format("平均事件数量过低: %.2f", avgEventsPerAttempt));
                    alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    currentAlerts.add(alert);
                }
            }
            
            alerts.put("currentAlerts", currentAlerts);
            alerts.put("alertCount", currentAlerts.size());
            alerts.put("alertHistory", new ArrayList<>(alertHistory));
            alerts.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 保存告警历史
            if (!currentAlerts.isEmpty()) {
                alertHistory.addAll(currentAlerts);
                // 保持历史记录不超过100条
                while (alertHistory.size() > 100) {
                    alertHistory.remove(0);
                }
            }
            
            log.debug("告警状态: {}", alerts);
            
        } catch (Exception e) {
            log.error("获取告警状态失败: {}", e.getMessage(), e);
            alerts.put("error", "获取告警失败: " + e.getMessage());
        }
        
        return alerts;
    }

    @Override
    public Map<String, Object> getMonitoringDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            log.info("生成监控面板数据");
            
            // 汇总所有监控数据
            dashboard.put("apiCallStatus", getApiCallStatus());
            dashboard.put("eventParsingStatus", getEventParsingStatus());
            dashboard.put("systemHealth", getSystemHealthStatus());
            dashboard.put("performanceMetrics", getPerformanceMetrics());
            dashboard.put("alerts", getAlertStatus());
            
            // 添加概览信息
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalApiCalls", totalApiCalls.get());
            overview.put("totalEventsParsed", totalEventsParsed.get());
            overview.put("uptime", getSystemUptime());
            overview.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            dashboard.put("overview", overview);
            
            log.info("监控面板数据生成完成");
            
        } catch (Exception e) {
            log.error("生成监控面板数据失败: {}", e.getMessage(), e);
            dashboard.put("error", "生成面板数据失败: " + e.getMessage());
        }
        
        return dashboard;
    }

    @Override
    public void recordApiCall(String apiType, boolean success, long responseTime, String errorMessage) {
        try {
            // 更新计数器
            totalApiCalls.incrementAndGet();
            if (success) {
                successfulApiCalls.incrementAndGet();
            }
            
            // 记录API类型统计
            String counterKey = apiType + (success ? "_success" : "_failure");
            apiCallCounters.computeIfAbsent(counterKey, k -> new AtomicLong(0)).incrementAndGet();
            
            // 记录响应时间
            String timeKey = apiType + "_response_time";
            apiResponseTimes.computeIfAbsent(timeKey, k -> new AtomicLong(0)).addAndGet(responseTime);
            
            // 记录错误信息
            if (!success && errorMessage != null) {
                recentErrors.put(apiType + "_" + System.currentTimeMillis(), errorMessage);
                // 保持错误记录不超过50条
                if (recentErrors.size() > 50) {
                    String oldestKey = recentErrors.keySet().iterator().next();
                    recentErrors.remove(oldestKey);
                }
            }
            
            // 更新最后更新时间
            lastUpdateTimes.put("api_call", LocalDateTime.now());
            
            log.debug("记录API调用: type={}, success={}, responseTime={}ms", apiType, success, responseTime);
            
        } catch (Exception e) {
            log.error("记录API调用失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void recordEventParsing(String parseMethod, boolean success, int eventCount, String errorMessage) {
        try {
            // 更新计数器
            totalParsingAttempts.incrementAndGet();
            if (success) {
                totalEventsParsed.addAndGet(eventCount);
            }
            
            // 记录解析方法统计
            String counterKey = parseMethod + (success ? "_success" : "_failure");
            eventParsingCounters.computeIfAbsent(counterKey, k -> new AtomicInteger(0)).incrementAndGet();
            
            // 记录错误信息
            if (!success && errorMessage != null) {
                recentErrors.put(parseMethod + "_" + System.currentTimeMillis(), errorMessage);
                // 保持错误记录不超过50条
                if (recentErrors.size() > 50) {
                    String oldestKey = recentErrors.keySet().iterator().next();
                    recentErrors.remove(oldestKey);
                }
            }
            
            // 更新最后更新时间
            lastUpdateTimes.put("event_parsing", LocalDateTime.now());
            
            log.debug("记录事件解析: method={}, success={}, eventCount={}", parseMethod, success, eventCount);
            
        } catch (Exception e) {
            log.error("记录事件解析失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void cleanupExpiredData() {
        try {
            log.info("开始清理过期监控数据");
            
            // 清理过期错误记录（保留最近1小时的）
            long oneHourAgo = System.currentTimeMillis() - 3600000;
            recentErrors.entrySet().removeIf(entry -> {
                try {
                    String[] parts = entry.getKey().split("_");
                    long timestamp = Long.parseLong(parts[parts.length - 1]);
                    return timestamp < oneHourAgo;
                } catch (Exception e) {
                    return true; // 无法解析的记录也删除
                }
            });
            
            // 清理过期告警历史（保留最近24小时的）
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            alertHistory.removeIf(alert -> {
                try {
                    String timestamp = (String) alert.get("timestamp");
                    LocalDateTime alertTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return alertTime.isBefore(oneDayAgo);
                } catch (Exception e) {
                    return true; // 无法解析的记录也删除
                }
            });
            
            // 清理调试数据
            if (debuggingEnhancer != null) {
                debuggingEnhancer.cleanupDebugData();
            }
            
            if (responseDebugger != null) {
                responseDebugger.cleanupDebugData();
            }
            
            log.info("监控数据清理完成");
            
        } catch (Exception e) {
            log.error("清理监控数据失败: {}", e.getMessage(), e);
        }
    }

    // 私有辅助方法

    private String getSystemUptime() {
        try {
            long uptimeMs = System.currentTimeMillis() - getStartTime();
            long hours = uptimeMs / (1000 * 60 * 60);
            long minutes = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60);
            return String.format("%d小时%d分钟", hours, minutes);
        } catch (Exception e) {
            return "未知";
        }
    }

    private long getStartTime() {
        // 简单实现，实际应用中可以在应用启动时记录
        return System.currentTimeMillis() - 3600000; // 假设运行了1小时
    }
}