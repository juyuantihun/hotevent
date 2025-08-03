package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.entity.ApiCallRecord;
import com.hotech.events.mapper.ApiCallRecordMapper;
import com.hotech.events.service.ApiMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API监控服务实现
 * 记录和分析API调用统计信息
 */
@Slf4j
@Service
public class ApiMonitoringServiceImpl implements ApiMonitoringService {

    @Autowired
    private ApiCallRecordMapper apiCallRecordMapper;
    
    @Value("${app.monitoring.alert.success-rate-threshold:80.0}")
    private double successRateThreshold;
    
    @Value("${app.monitoring.alert.response-time-threshold:10000}")
    private long responseTimeThreshold;
    
    @Value("${app.monitoring.alert.error-count-threshold:10}")
    private int errorCountThreshold;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 实时统计计数器
    private final AtomicLong totalCallsToday = new AtomicLong(0);
    private final AtomicLong successCallsToday = new AtomicLong(0);
    private final AtomicLong errorCallsToday = new AtomicLong(0);
    
    @Override
    public void recordApiCall(String apiType, Map<String, Object> requestBody, String responseStatus, 
                            Integer tokenUsage, Integer responseTime, String errorMessage, 
                            String requestId, Integer retryCount) {
        try {
            ApiCallRecord record = new ApiCallRecord();
            record.setApiType(apiType);
            record.setResponseStatus(responseStatus);
            record.setTokenUsage(tokenUsage);
            record.setResponseTime(responseTime);
            record.setErrorMessage(errorMessage);
            record.setCallTime(LocalDateTime.now());
            record.setRequestId(requestId);
            record.setRetryCount(retryCount);
            
            // 序列化请求参数
            if (requestBody != null) {
                try {
                    String requestParams = objectMapper.writeValueAsString(requestBody);
                    record.setRequestParams(requestParams);
                    record.setRequestSize(requestParams.length());
                } catch (Exception e) {
                    log.warn("序列化请求参数失败", e);
                }
            }
            
            // 设置缓存命中状态（这里简化处理，实际可以从调用方传入）
            record.setCacheHit(false);
            
            // 保存记录
            apiCallRecordMapper.insert(record);
            
            // 更新实时统计
            totalCallsToday.incrementAndGet();
            if ("SUCCESS".equals(responseStatus)) {
                successCallsToday.incrementAndGet();
            } else {
                errorCallsToday.incrementAndGet();
            }
            
            log.debug("API调用记录已保存: apiType={}, status={}, responseTime={}ms, requestId={}", 
                    apiType, responseStatus, responseTime, requestId);
            
        } catch (Exception e) {
            log.error("记录API调用失败", e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getApiCallStats(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return apiCallRecordMapper.getApiStatsByTimeRange(startTime, endTime);
        } catch (Exception e) {
            log.error("获取API调用统计失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getSuccessRateStats(LocalDateTime since) {
        try {
            return apiCallRecordMapper.getSuccessRateStats(since);
        } catch (Exception e) {
            log.error("获取API成功率统计失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getPerformanceStats(LocalDateTime since) {
        try {
            return apiCallRecordMapper.getPerformanceStats(since);
        } catch (Exception e) {
            log.error("获取API性能统计失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getErrorStats(LocalDateTime since) {
        try {
            return apiCallRecordMapper.getErrorStats(since);
        } catch (Exception e) {
            log.error("获取API错误统计失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<ApiCallRecord> getRecentApiCalls(int limit) {
        try {
            return apiCallRecordMapper.getRecentCalls(limit);
        } catch (Exception e) {
            log.error("获取最近API调用记录失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // 基本统计
            List<Map<String, Object>> apiStats = getApiCallStats(startTime, endTime);
            report.put("apiStats", apiStats);
            
            // 成功率统计
            List<Map<String, Object>> successRateStats = getSuccessRateStats(startTime);
            report.put("successRateStats", successRateStats);
            
            // 性能统计
            List<Map<String, Object>> performanceStats = getPerformanceStats(startTime);
            report.put("performanceStats", performanceStats);
            
            // 错误统计
            List<Map<String, Object>> errorStats = getErrorStats(startTime);
            report.put("errorStats", errorStats);
            
            // 计算总体指标
            long totalCalls = 0;
            long successCalls = 0;
            long totalTokens = 0;
            double avgResponseTime = 0;
            
            for (Map<String, Object> stat : apiStats) {
                totalCalls += ((Number) stat.getOrDefault("call_count", 0)).longValue();
                successCalls += ((Number) stat.getOrDefault("success_count", 0)).longValue();
                totalTokens += ((Number) stat.getOrDefault("total_tokens", 0)).longValue();
                avgResponseTime += ((Number) stat.getOrDefault("avg_response_time", 0)).doubleValue();
            }
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalCalls", totalCalls);
            summary.put("successCalls", successCalls);
            summary.put("failedCalls", totalCalls - successCalls);
            summary.put("successRate", totalCalls > 0 ? (double) successCalls / totalCalls * 100 : 0);
            summary.put("totalTokens", totalTokens);
            summary.put("avgResponseTime", apiStats.size() > 0 ? avgResponseTime / apiStats.size() : 0);
            summary.put("reportPeriod", startTime + " 至 " + endTime);
            
            report.put("summary", summary);
            
        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            report.put("error", "生成报告时发生错误: " + e.getMessage());
        }
        
        return report;
    }
    
    @Override
    public int cleanupOldRecords(LocalDateTime beforeTime) {
        try {
            int deletedCount = apiCallRecordMapper.cleanupOldRecords(beforeTime);
            log.info("清理过期API调用记录: deletedCount={}, beforeTime={}", deletedCount, beforeTime);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            return 0;
        }
    }
    
    @Override
    public Map<String, Object> getRealTimeMonitoringData() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minus(1, ChronoUnit.HOURS);
            LocalDateTime oneDayAgo = now.minus(1, ChronoUnit.DAYS);
            
            // 最近1小时统计
            List<Map<String, Object>> hourlyStats = getApiCallStats(oneHourAgo, now);
            data.put("hourlyStats", hourlyStats);
            
            // 最近24小时统计
            List<Map<String, Object>> dailyStats = getApiCallStats(oneDayAgo, now);
            data.put("dailyStats", dailyStats);
            
            // 实时计数器
            Map<String, Object> realTimeCounters = new HashMap<>();
            realTimeCounters.put("totalCallsToday", totalCallsToday.get());
            realTimeCounters.put("successCallsToday", successCallsToday.get());
            realTimeCounters.put("errorCallsToday", errorCallsToday.get());
            
            long total = totalCallsToday.get();
            if (total > 0) {
                realTimeCounters.put("successRateToday", (double) successCallsToday.get() / total * 100);
            } else {
                realTimeCounters.put("successRateToday", 0.0);
            }
            
            data.put("realTimeCounters", realTimeCounters);
            
            // 最近的API调用
            List<ApiCallRecord> recentCalls = getRecentApiCalls(10);
            data.put("recentCalls", recentCalls);
            
            data.put("timestamp", now);
            
        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            data.put("error", "获取数据时发生错误: " + e.getMessage());
        }
        
        return data;
    }
    
    @Override
    public List<Map<String, Object>> checkAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            
            // 检查成功率告警
            List<Map<String, Object>> successRateStats = getSuccessRateStats(oneHourAgo);
            for (Map<String, Object> stat : successRateStats) {
                double successRate = ((Number) stat.getOrDefault("success_rate", 100)).doubleValue();
                if (successRate < successRateThreshold) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "SUCCESS_RATE_LOW");
                    alert.put("severity", "WARNING");
                    alert.put("message", String.format("API %s 成功率过低: %.2f%% (阈值: %.2f%%)", 
                            stat.get("api_type"), successRate, successRateThreshold));
                    alert.put("apiType", stat.get("api_type"));
                    alert.put("currentValue", successRate);
                    alert.put("threshold", successRateThreshold);
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
            
            // 检查响应时间告警
            List<Map<String, Object>> performanceStats = getPerformanceStats(oneHourAgo);
            for (Map<String, Object> stat : performanceStats) {
                double avgResponseTime = ((Number) stat.getOrDefault("avg_response_time", 0)).doubleValue();
                if (avgResponseTime > responseTimeThreshold) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "RESPONSE_TIME_HIGH");
                    alert.put("severity", "WARNING");
                    alert.put("message", String.format("API %s 响应时间过长: %.0fms (阈值: %dms)", 
                            stat.get("api_type"), avgResponseTime, responseTimeThreshold));
                    alert.put("apiType", stat.get("api_type"));
                    alert.put("currentValue", avgResponseTime);
                    alert.put("threshold", responseTimeThreshold);
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
            
            // 检查错误数量告警
            List<Map<String, Object>> errorStats = getErrorStats(oneHourAgo);
            for (Map<String, Object> stat : errorStats) {
                int errorCount = ((Number) stat.getOrDefault("error_count", 0)).intValue();
                if (errorCount > errorCountThreshold) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "ERROR_COUNT_HIGH");
                    alert.put("severity", "ERROR");
                    alert.put("message", String.format("API %s 错误数量过多: %d (阈值: %d)", 
                            stat.get("api_type"), errorCount, errorCountThreshold));
                    alert.put("apiType", stat.get("api_type"));
                    alert.put("errorMessage", stat.get("error_message"));
                    alert.put("currentValue", errorCount);
                    alert.put("threshold", errorCountThreshold);
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
            
            // 检查API可用性告警
            checkApiAvailabilityAlerts(alerts);
            
            // 检查Token使用量告警
            checkTokenUsageAlerts(alerts, oneHourAgo);
            
        } catch (Exception e) {
            log.error("检查告警失败", e);
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "SYSTEM_ERROR");
            alert.put("severity", "ERROR");
            alert.put("message", "监控系统异常: " + e.getMessage());
            alert.put("timestamp", LocalDateTime.now());
            alerts.add(alert);
        }
        
        return alerts;
    }
    
    /**
     * 检查API可用性告警
     */
    private void checkApiAvailabilityAlerts(List<Map<String, Object>> alerts) {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
            
            // 检查最近5分钟是否有成功的API调用
            List<Map<String, Object>> recentStats = getApiCallStats(fiveMinutesAgo, LocalDateTime.now());
            
            Set<String> activeApiTypes = new HashSet<>();
            for (Map<String, Object> stat : recentStats) {
                String apiType = (String) stat.get("api_type");
                int successCount = ((Number) stat.getOrDefault("success_count", 0)).intValue();
                if (successCount > 0) {
                    activeApiTypes.add(apiType);
                }
            }
            
            // 检查预期的API类型是否都有活动
            String[] expectedApiTypes = {"DEEPSEEK_OFFICIAL", "VOLCENGINE_WEB"};
            for (String expectedType : expectedApiTypes) {
                if (!activeApiTypes.contains(expectedType)) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "API_UNAVAILABLE");
                    alert.put("severity", "ERROR");
                    alert.put("message", String.format("API %s 在过去5分钟内无成功调用", expectedType));
                    alert.put("apiType", expectedType);
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
            
        } catch (Exception e) {
            log.warn("检查API可用性告警失败", e);
        }
    }
    
    /**
     * 检查Token使用量告警
     */
    private void checkTokenUsageAlerts(List<Map<String, Object>> alerts, LocalDateTime since) {
        try {
            List<Map<String, Object>> apiStats = getApiCallStats(since, LocalDateTime.now());
            
            for (Map<String, Object> stat : apiStats) {
                long totalTokens = ((Number) stat.getOrDefault("total_tokens", 0)).longValue();
                String apiType = (String) stat.get("api_type");
                
                // 设置Token使用量阈值（每小时）
                long tokenThreshold = 50000; // 可以配置化
                
                if (totalTokens > tokenThreshold) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "TOKEN_USAGE_HIGH");
                    alert.put("severity", "WARNING");
                    alert.put("message", String.format("API %s Token使用量过高: %d (阈值: %d)", 
                            apiType, totalTokens, tokenThreshold));
                    alert.put("apiType", apiType);
                    alert.put("currentValue", totalTokens);
                    alert.put("threshold", tokenThreshold);
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
            
        } catch (Exception e) {
            log.warn("检查Token使用量告警失败", e);
        }
    }
    
    /**
     * 获取API性能趋势分析
     */
    public Map<String, Object> getPerformanceTrend(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> trend = new HashMap<>();
        
        try {
            // 按小时分组获取统计数据
            List<Map<String, Object>> hourlyStats = apiCallRecordMapper.getHourlyStats(startTime, endTime);
            trend.put("hourlyStats", hourlyStats);
            
            // 计算趋势指标
            if (!hourlyStats.isEmpty()) {
                double avgSuccessRate = hourlyStats.stream()
                    .mapToDouble(stat -> ((Number) stat.getOrDefault("success_rate", 0)).doubleValue())
                    .average().orElse(0.0);
                
                double avgResponseTime = hourlyStats.stream()
                    .mapToDouble(stat -> ((Number) stat.getOrDefault("avg_response_time", 0)).doubleValue())
                    .average().orElse(0.0);
                
                trend.put("avgSuccessRate", avgSuccessRate);
                trend.put("avgResponseTime", avgResponseTime);
                
                // 计算趋势方向
                if (hourlyStats.size() >= 2) {
                    Map<String, Object> firstHour = hourlyStats.get(0);
                    Map<String, Object> lastHour = hourlyStats.get(hourlyStats.size() - 1);
                    
                    double successRateTrend = ((Number) lastHour.getOrDefault("success_rate", 0)).doubleValue() - 
                                            ((Number) firstHour.getOrDefault("success_rate", 0)).doubleValue();
                    
                    double responseTimeTrend = ((Number) lastHour.getOrDefault("avg_response_time", 0)).doubleValue() - 
                                             ((Number) firstHour.getOrDefault("avg_response_time", 0)).doubleValue();
                    
                    trend.put("successRateTrend", successRateTrend > 0 ? "IMPROVING" : 
                             successRateTrend < 0 ? "DECLINING" : "STABLE");
                    trend.put("responseTimeTrend", responseTimeTrend < 0 ? "IMPROVING" : 
                             responseTimeTrend > 0 ? "DECLINING" : "STABLE");
                }
            }
            
        } catch (Exception e) {
            log.error("获取性能趋势分析失败", e);
            trend.put("error", "获取趋势数据失败: " + e.getMessage());
        }
        
        return trend;
    }
}