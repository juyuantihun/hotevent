package com.hotech.events.controller;

import com.hotech.events.service.BatchProcessingService;
import com.hotech.events.service.CacheService;
import com.hotech.events.service.PerformanceMonitoringService;
import com.hotech.events.service.ProgressTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 性能监控控制器
 * 提供性能监控、进度跟踪、缓存管理等功能的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@Tag(name = "性能监控", description = "性能监控和优化相关接口")
public class PerformanceController {
    
    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;
    
    @Autowired
    private ProgressTrackingService progressTrackingService;
    
    @Autowired
    private BatchProcessingService batchProcessingService;
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 获取实时性能指标
     */
    @GetMapping("/metrics/realtime")
    @Operation(summary = "获取实时性能指标", description = "获取系统当前的实时性能指标")
    public ResponseEntity<PerformanceMonitoringService.RealTimeMetrics> getRealTimeMetrics() {
        try {
            PerformanceMonitoringService.RealTimeMetrics metrics = performanceMonitoringService.getRealTimeMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("获取实时性能指标失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取操作性能统计
     */
    @GetMapping("/metrics/operation/{operationName}")
    @Operation(summary = "获取操作性能统计", description = "获取指定操作的性能统计信息")
    public ResponseEntity<PerformanceMonitoringService.OperationPerformanceStats> getOperationStats(
            @Parameter(description = "操作名称") @PathVariable String operationName,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            PerformanceMonitoringService.OperationPerformanceStats stats = 
                    performanceMonitoringService.getOperationStats(operationName, startTime, endTime);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取操作性能统计失败: operationName={}", operationName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取资源使用趋势
     */
    @GetMapping("/metrics/resource-trend")
    @Operation(summary = "获取资源使用趋势", description = "获取系统资源使用趋势数据")
    public ResponseEntity<List<PerformanceMonitoringService.ResourceUsageTrend>> getResourceUsageTrend(
            @Parameter(description = "过去几小时") @RequestParam(defaultValue = "24") int hours) {
        try {
            List<PerformanceMonitoringService.ResourceUsageTrend> trends = 
                    performanceMonitoringService.getResourceUsageTrend(hours);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            log.error("获取资源使用趋势失败: hours={}", hours, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取性能热点
     */
    @GetMapping("/metrics/hotspots")
    @Operation(summary = "获取性能热点", description = "获取系统性能热点分析")
    public ResponseEntity<List<PerformanceMonitoringService.PerformanceHotspot>> getPerformanceHotspots(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PerformanceMonitoringService.PerformanceHotspot> hotspots = 
                    performanceMonitoringService.getPerformanceHotspots(limit);
            return ResponseEntity.ok(hotspots);
        } catch (Exception e) {
            log.error("获取性能热点失败: limit={}", limit, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取异常性能事件
     */
    @GetMapping("/metrics/anomalous-events")
    @Operation(summary = "获取异常性能事件", description = "获取异常性能事件列表")
    public ResponseEntity<List<PerformanceMonitoringService.AnomalousPerformanceEvent>> getAnomalousEvents(
            @Parameter(description = "过去几小时") @RequestParam(defaultValue = "24") int hours) {
        try {
            List<PerformanceMonitoringService.AnomalousPerformanceEvent> events = 
                    performanceMonitoringService.getAnomalousEvents(hours);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("获取异常性能事件失败: hours={}", hours, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成性能报告
     */
    @GetMapping("/report")
    @Operation(summary = "生成性能报告", description = "生成指定时间范围的性能报告")
    public ResponseEntity<PerformanceMonitoringService.PerformanceReport> generatePerformanceReport(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            PerformanceMonitoringService.PerformanceReport report = 
                    performanceMonitoringService.generatePerformanceReport(startTime, endTime);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取任务进度
     */
    @GetMapping("/progress/{sessionId}")
    @Operation(summary = "获取任务进度", description = "获取指定会话的任务进度信息")
    public ResponseEntity<ProgressTrackingService.TaskProgress> getTaskProgress(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            ProgressTrackingService.TaskProgress progress = progressTrackingService.getProgress(sessionId);
            if (progress != null) {
                return ResponseEntity.ok(progress);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取任务进度失败: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取任务历史记录
     */
    @GetMapping("/progress/history/{taskId}")
    @Operation(summary = "获取任务历史记录", description = "获取指定任务的历史记录")
    public ResponseEntity<List<ProgressTrackingService.TaskProgress>> getTaskHistory(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            List<ProgressTrackingService.TaskProgress> history = progressTrackingService.getTaskHistory(taskId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取任务历史记录失败: taskId={}", taskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取活跃任务列表
     */
    @GetMapping("/progress/active")
    @Operation(summary = "获取活跃任务列表", description = "获取当前活跃的任务列表")
    public ResponseEntity<List<ProgressTrackingService.TaskProgress>> getActiveTasks() {
        try {
            List<ProgressTrackingService.TaskProgress> activeTasks = progressTrackingService.getActiveTasks();
            return ResponseEntity.ok(activeTasks);
        } catch (Exception e) {
            log.error("获取活跃任务列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取批处理统计信息
     */
    @GetMapping("/batch/stats")
    @Operation(summary = "获取批处理统计信息", description = "获取批处理服务的统计信息")
    public ResponseEntity<BatchProcessingService.BatchProcessingStats> getBatchProcessingStats() {
        try {
            BatchProcessingService.BatchProcessingStats stats = batchProcessingService.getBatchProcessingStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取批处理统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息", description = "获取缓存服务的统计信息")
    public ResponseEntity<CacheService.CacheStats> getCacheStats() {
        try {
            CacheService.CacheStats stats = cacheService.getCacheStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 清空缓存
     */
    @DeleteMapping("/cache/clear")
    @Operation(summary = "清空缓存", description = "清空所有缓存数据")
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            cacheService.clear();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "缓存已清空");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除指定缓存项
     */
    @DeleteMapping("/cache/{key}")
    @Operation(summary = "删除缓存项", description = "删除指定的缓存项")
    public ResponseEntity<Map<String, String>> evictCache(
            @Parameter(description = "缓存键") @PathVariable String key) {
        try {
            cacheService.evict(key);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "缓存项已删除");
            response.put("key", key);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除缓存项失败: key={}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取综合性能仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取性能仪表板数据", description = "获取性能监控仪表板的综合数据")
    public ResponseEntity<Map<String, Object>> getPerformanceDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 实时指标
            dashboard.put("realTimeMetrics", performanceMonitoringService.getRealTimeMetrics());
            
            // 性能热点（前5个）
            dashboard.put("topHotspots", performanceMonitoringService.getPerformanceHotspots(5));
            
            // 最近24小时的异常事件
            dashboard.put("recentAnomalousEvents", performanceMonitoringService.getAnomalousEvents(24));
            
            // 活跃任务
            dashboard.put("activeTasks", progressTrackingService.getActiveTasks());
            
            // 批处理统计
            dashboard.put("batchStats", batchProcessingService.getBatchProcessingStats());
            
            // 缓存统计
            dashboard.put("cacheStats", cacheService.getCacheStats());
            
            // 资源使用趋势（最近6小时）
            dashboard.put("resourceTrend", performanceMonitoringService.getResourceUsageTrend(6));
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("获取性能仪表板数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}