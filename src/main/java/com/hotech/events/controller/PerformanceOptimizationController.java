package com.hotech.events.controller;

import com.hotech.events.service.ConcurrentApiCallManager;
import com.hotech.events.service.GeographicCacheService;
import com.hotech.events.service.SystemPerformanceCollector;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 性能优化控制器
 * 提供性能监控、缓存管理、并发调优等API接口
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "*")
public class PerformanceOptimizationController {
    
    @Autowired
    private ConcurrentApiCallManager concurrentApiCallManager;
    
    @Autowired
    private GeographicCacheService geographicCacheService;
    
    @Autowired
    private SystemPerformanceCollector performanceCollector;
    
    @Autowired
    private TimelinePerformanceMonitoringService performanceMonitoringService;
    
    /**
     * 获取系统性能概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getPerformanceOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 系统性能快照
            overview.put("systemSnapshot", performanceCollector.collectPerformanceSnapshot());
            
            // API调用统计
            overview.put("apiCallStats", concurrentApiCallManager.getApiCallStatistics());
            
            // 缓存统计
            overview.put("cacheStats", geographicCacheService.getCacheStatistics());
            
            // 性能监控统计
            overview.put("monitoringStats", performanceMonitoringService.getPerformanceStatistics());
            
            // 收集器状态
            overview.put("collectorStatus", performanceCollector.getCollectorStatus());
            
            return ResponseEntity.ok(overview);
            
        } catch (Exception e) {
            log.error("获取性能概览失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取性能概览失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取并发API调用统计
     */
    @GetMapping("/api-calls/stats")
    public ResponseEntity<Map<String, Object>> getApiCallStatistics() {
        try {
            Map<String, Object> stats = concurrentApiCallManager.getApiCallStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取API调用统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取API调用统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 设置API调用并发度
     */
    @PostMapping("/api-calls/concurrency")
    public ResponseEntity<Map<String, Object>> setApiCallConcurrency(@RequestParam int concurrency) {
        try {
            if (concurrency <= 0 || concurrency > 50) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "并发度必须在1-50之间"));
            }
            
            int oldConcurrency = concurrentApiCallManager.getConcurrency();
            concurrentApiCallManager.setConcurrency(concurrency);
            
            Map<String, Object> result = new HashMap<>();
            result.put("oldConcurrency", oldConcurrency);
            result.put("newConcurrency", concurrency);
            result.put("message", "并发度设置成功");
            
            log.info("API调用并发度已从{}更新为{}", oldConcurrency, concurrency);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("设置API调用并发度失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "设置并发度失败: " + e.getMessage()));
        }
    }
    
    /**
     * 设置API调用超时时间
     */
    @PostMapping("/api-calls/timeout")
    public ResponseEntity<Map<String, Object>> setApiCallTimeout(@RequestParam long timeoutMs) {
        try {
            if (timeoutMs <= 0 || timeoutMs > 300000) { // 最大5分钟
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "超时时间必须在1-300000毫秒之间"));
            }
            
            concurrentApiCallManager.setTimeout(timeoutMs);
            
            Map<String, Object> result = new HashMap<>();
            result.put("newTimeout", timeoutMs);
            result.put("message", "超时时间设置成功");
            
            log.info("API调用超时时间已设置为{}ms", timeoutMs);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("设置API调用超时时间失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "设置超时时间失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置API调用统计
     */
    @PostMapping("/api-calls/reset-stats")
    public ResponseEntity<Map<String, Object>> resetApiCallStatistics() {
        try {
            concurrentApiCallManager.resetStatistics();
            
            return ResponseEntity.ok(Map.of("message", "API调用统计已重置"));
            
        } catch (Exception e) {
            log.error("重置API调用统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "重置统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地理信息缓存统计
     */
    @GetMapping("/cache/geographic/stats")
    public ResponseEntity<Map<String, Object>> getGeographicCacheStatistics() {
        try {
            Map<String, Object> stats = geographicCacheService.getCacheStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取地理信息缓存统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取缓存统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 设置地理信息缓存TTL
     */
    @PostMapping("/cache/geographic/ttl")
    public ResponseEntity<Map<String, Object>> setGeographicCacheTTL(@RequestParam long ttlSeconds) {
        try {
            if (ttlSeconds <= 0 || ttlSeconds > 86400) { // 最大24小时
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "TTL必须在1-86400秒之间"));
            }
            
            geographicCacheService.setCacheTTL(ttlSeconds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("newTTL", ttlSeconds);
            result.put("message", "缓存TTL设置成功");
            
            log.info("地理信息缓存TTL已设置为{}秒", ttlSeconds);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("设置地理信息缓存TTL失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "设置缓存TTL失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清空地理信息缓存
     */
    @PostMapping("/cache/geographic/clear")
    public ResponseEntity<Map<String, Object>> clearGeographicCache() {
        try {
            int oldSize = geographicCacheService.getCacheSize();
            geographicCacheService.clearAllCache();
            
            Map<String, Object> result = new HashMap<>();
            result.put("clearedEntries", oldSize);
            result.put("message", "地理信息缓存已清空");
            
            log.info("地理信息缓存已清空，清理了{}个条目", oldSize);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("清空地理信息缓存失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "清空缓存失败: " + e.getMessage()));
        }
    }
    
    /**
     * 预加载常用地理坐标
     */
    @PostMapping("/cache/geographic/preload")
    public ResponseEntity<Map<String, Object>> preloadGeographicCoordinates() {
        try {
            geographicCacheService.preloadCommonCoordinates();
            
            Map<String, Object> result = new HashMap<>();
            result.put("cacheSize", geographicCacheService.getCacheSize());
            result.put("message", "常用地理坐标预加载完成");
            
            log.info("常用地理坐标预加载完成");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("预加载地理坐标失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "预加载失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重置地理信息缓存统计
     */
    @PostMapping("/cache/geographic/reset-stats")
    public ResponseEntity<Map<String, Object>> resetGeographicCacheStatistics() {
        try {
            geographicCacheService.resetCacheStatistics();
            
            return ResponseEntity.ok(Map.of("message", "地理信息缓存统计已重置"));
            
        } catch (Exception e) {
            log.error("重置地理信息缓存统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "重置统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 启动系统性能收集
     */
    @PostMapping("/monitoring/start")
    public ResponseEntity<Map<String, Object>> startPerformanceCollection() {
        try {
            performanceCollector.startCollection();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", performanceCollector.getCollectorStatus());
            result.put("message", "性能收集已启动");
            
            log.info("系统性能收集已启动");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("启动性能收集失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "启动性能收集失败: " + e.getMessage()));
        }
    }
    
    /**
     * 停止系统性能收集
     */
    @PostMapping("/monitoring/stop")
    public ResponseEntity<Map<String, Object>> stopPerformanceCollection() {
        try {
            performanceCollector.stopCollection();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", performanceCollector.getCollectorStatus());
            result.put("message", "性能收集已停止");
            
            log.info("系统性能收集已停止");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("停止性能收集失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "停止性能收集失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取性能监控统计
     */
    @GetMapping("/monitoring/stats")
    public ResponseEntity<Map<String, Object>> getPerformanceMonitoringStatistics() {
        try {
            Map<String, Object> stats = performanceMonitoringService.getPerformanceStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取性能监控统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取监控统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取性能告警信息
     */
    @GetMapping("/monitoring/alerts")
    public ResponseEntity<List<Map<String, Object>>> getPerformanceAlerts() {
        try {
            List<Map<String, Object>> alerts = performanceCollector.getPerformanceAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("获取性能告警失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 设置性能阈值
     */
    @PostMapping("/monitoring/threshold")
    public ResponseEntity<Map<String, Object>> setPerformanceThreshold(
            @RequestParam String metricName,
            @RequestParam double threshold,
            @RequestParam String operator) {
        try {
            if (!isValidOperator(operator)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的操作符，支持: >, <, >=, <=, =="));
            }
            
            performanceCollector.setPerformanceThreshold(metricName, threshold, operator);
            
            Map<String, Object> result = new HashMap<>();
            result.put("metricName", metricName);
            result.put("threshold", threshold);
            result.put("operator", operator);
            result.put("message", "性能阈值设置成功");
            
            log.info("性能阈值已设置: {} {} {}", metricName, operator, threshold);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("设置性能阈值失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "设置阈值失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取历史性能数据
     */
    @GetMapping("/monitoring/history")
    public ResponseEntity<Map<String, Object>> getHistoricalMetrics(
            @RequestParam long startTime,
            @RequestParam long endTime,
            @RequestParam(required = false) String[] metricNames) {
        try {
            if (startTime >= endTime) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "开始时间必须早于结束时间"));
            }
            
            String[] metrics = metricNames != null ? metricNames : new String[0];
            Map<String, Object> history = performanceCollector.getHistoricalMetrics(startTime, endTime, metrics);
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("获取历史性能数据失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取历史数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取性能趋势分析
     */
    @GetMapping("/monitoring/trend")
    public ResponseEntity<Map<String, Object>> getPerformanceTrend(
            @RequestParam String metricName,
            @RequestParam(defaultValue = "1") int timeRange) {
        try {
            if (timeRange <= 0 || timeRange > 168) { // 最大7天
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "时间范围必须在1-168小时之间"));
            }
            
            Map<String, Object> trend = performanceCollector.getPerformanceTrend(metricName, timeRange);
            
            return ResponseEntity.ok(trend);
            
        } catch (Exception e) {
            log.error("获取性能趋势失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取趋势分析失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出性能报告
     */
    @GetMapping("/monitoring/export")
    public ResponseEntity<String> exportPerformanceReport(
            @RequestParam(defaultValue = "JSON") String format,
            @RequestParam long startTime,
            @RequestParam long endTime) {
        try {
            if (startTime >= endTime) {
                return ResponseEntity.badRequest().body("开始时间必须早于结束时间");
            }
            
            String report = performanceCollector.exportPerformanceReport(format, startTime, endTime);
            
            String contentType;
            switch (format.toUpperCase()) {
                case "CSV":
                    contentType = "text/csv";
                    break;
                case "XML":
                    contentType = "application/xml";
                    break;
                default:
                    contentType = "application/json";
                    break;
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=performance-report." + format.toLowerCase())
                    .body(report);
            
        } catch (Exception e) {
            log.error("导出性能报告失败", e);
            return ResponseEntity.internalServerError().body("导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置所有性能指标
     */
    @PostMapping("/monitoring/reset")
    public ResponseEntity<Map<String, Object>> resetAllMetrics() {
        try {
            performanceCollector.resetAllMetrics();
            performanceMonitoringService.resetStatistics();
            
            return ResponseEntity.ok(Map.of("message", "所有性能指标已重置"));
            
        } catch (Exception e) {
            log.error("重置性能指标失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "重置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收集器状态
     */
    @GetMapping("/monitoring/status")
    public ResponseEntity<Map<String, Object>> getCollectorStatus() {
        try {
            Map<String, Object> status = performanceCollector.getCollectorStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取收集器状态失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 记录自定义性能指标
     */
    @PostMapping("/monitoring/metric")
    public ResponseEntity<Map<String, Object>> recordCustomMetric(
            @RequestParam String metricName,
            @RequestParam double value,
            @RequestParam(required = false) Map<String, String> tags) {
        try {
            Map<String, String> metricTags = tags != null ? tags : new HashMap<>();
            performanceCollector.recordCustomMetric(metricName, value, metricTags);
            
            Map<String, Object> result = new HashMap<>();
            result.put("metricName", metricName);
            result.put("value", value);
            result.put("tags", metricTags);
            result.put("message", "自定义指标记录成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("记录自定义指标失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "记录指标失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证操作符是否有效
     */
    private boolean isValidOperator(String operator) {
        return operator != null && 
               (operator.equals(">") || operator.equals("<") || 
                operator.equals(">=") || operator.equals("<=") || 
                operator.equals("=="));
    }
}