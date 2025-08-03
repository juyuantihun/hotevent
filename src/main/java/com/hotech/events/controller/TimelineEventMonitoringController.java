package com.hotech.events.controller;

import com.hotech.events.dto.Alert;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.service.TimelineEventMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时间线事件监控控制器
 * 提供时间线事件监控和警告管理的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/timeline-monitoring")
@Tag(name = "时间线事件监控", description = "时间线事件监控和警告管理功能")
public class TimelineEventMonitoringController {
    
    @Autowired
    private TimelineEventMonitoringService monitoringService;
    
    /**
     * 启动监控服务
     */
    @PostMapping("/start")
    @Operation(summary = "启动监控服务", description = "启动时间线事件监控服务")
    public ResponseEntity<ApiResponse<String>> startMonitoring() {
        log.info("接收到启动监控服务请求");
        
        try {
            monitoringService.startMonitoring();
            
            log.info("监控服务启动成功");
            return ResponseEntity.ok(ApiResponse.success("监控服务已启动"));
        } catch (Exception e) {
            log.error("启动监控服务失败", e);
            return ResponseEntity.ok(ApiResponse.error("启动失败: " + e.getMessage()));
        }
    }
    
    /**
     * 停止监控服务
     */
    @PostMapping("/stop")
    @Operation(summary = "停止监控服务", description = "停止时间线事件监控服务")
    public ResponseEntity<ApiResponse<String>> stopMonitoring() {
        log.info("接收到停止监控服务请求");
        
        try {
            monitoringService.stopMonitoring();
            
            log.info("监控服务停止成功");
            return ResponseEntity.ok(ApiResponse.success("监控服务已停止"));
        } catch (Exception e) {
            log.error("停止监控服务失败", e);
            return ResponseEntity.ok(ApiResponse.error("停止失败: " + e.getMessage()));
        }
    }    /**

     * 获取实时监控数据
     */
    @GetMapping("/real-time")
    @Operation(summary = "获取实时监控数据", description = "获取当前的实时监控数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealTimeMonitoringData() {
        log.info("接收到获取实时监控数据请求");
        
        try {
            Map<String, Object> data = monitoringService.getRealTimeMonitoringData();
            
            log.debug("实时监控数据获取成功");
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取监控报告
     */
    @GetMapping("/report")
    @Operation(summary = "获取监控报告", description = "获取指定时间范围内的监控报告")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonitoringReport(
            @Parameter(description = "开始时间", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("接收到获取监控报告请求: startTime={}, endTime={}", startTime, endTime);
        
        try {
            Map<String, Object> report = monitoringService.getMonitoringReport(startTime, endTime);
            
            log.info("监控报告获取成功");
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            log.error("获取监控报告失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取性能指标
     */
    @GetMapping("/performance-metrics")
    @Operation(summary = "获取性能指标", description = "获取指定时间范围内的性能指标")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics(
            @Parameter(description = "开始时间", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("接收到获取性能指标请求: startTime={}, endTime={}", startTime, endTime);
        
        try {
            Map<String, Object> metrics = monitoringService.getPerformanceMetrics(startTime, endTime);
            
            log.info("性能指标获取成功");
            return ResponseEntity.ok(ApiResponse.success(metrics));
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查系统健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查系统健康状态", description = "获取系统当前的健康状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSystemHealth() {
        log.info("接收到系统健康检查请求");
        
        try {
            Map<String, Object> health = monitoringService.checkSystemHealth();
            
            log.info("系统健康检查完成");
            return ResponseEntity.ok(ApiResponse.success(health));
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取活跃警告
     */
    @GetMapping("/alerts/active")
    @Operation(summary = "获取活跃警告", description = "获取当前所有活跃的警告")
    public ResponseEntity<ApiResponse<List<Alert>>> getActiveAlerts() {
        log.info("接收到获取活跃警告请求");
        
        try {
            List<Alert> alerts = monitoringService.getActiveAlerts();
            
            log.info("活跃警告获取成功，共 {} 个警告", alerts.size());
            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            log.error("获取活跃警告失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 解决警告
     */
    @PostMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "解决警告", description = "标记指定警告为已解决")
    public ResponseEntity<ApiResponse<Boolean>> resolveAlert(
            @Parameter(description = "警告ID", required = true) @PathVariable Long alertId) {
        log.info("接收到解决警告请求: alertId={}", alertId);
        
        try {
            boolean resolved = monitoringService.resolveAlert(alertId);
            
            if (resolved) {
                log.info("警告 {} 解决成功", alertId);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                log.warn("警告 {} 解决失败，可能不存在或已解决", alertId);
                return ResponseEntity.ok(ApiResponse.error("警告不存在或已解决"));
            }
        } catch (Exception e) {
            log.error("解决警告 {} 失败", alertId, e);
            return ResponseEntity.ok(ApiResponse.error("解决失败: " + e.getMessage()));
        }
    }
    
    /**
     * 设置警告阈值
     */
    @PostMapping("/alerts/threshold")
    @Operation(summary = "设置警告阈值", description = "设置指定类型的警告阈值")
    public ResponseEntity<ApiResponse<String>> setAlertThreshold(
            @Parameter(description = "警告类型", required = true) @RequestParam String alertType,
            @Parameter(description = "阈值", required = true) @RequestParam Integer threshold) {
        log.info("接收到设置警告阈值请求: alertType={}, threshold={}", alertType, threshold);
        
        try {
            monitoringService.setAlertThreshold(alertType, threshold);
            
            log.info("警告阈值设置成功: {} = {}", alertType, threshold);
            return ResponseEntity.ok(ApiResponse.success("阈值设置成功"));
        } catch (Exception e) {
            log.error("设置警告阈值失败", e);
            return ResponseEntity.ok(ApiResponse.error("设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清理过期数据
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期数据", description = "清理指定天数之前的监控数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanupExpiredData(
            @Parameter(description = "保留天数", required = true) @RequestParam Integer retentionDays) {
        log.info("接收到清理过期数据请求: retentionDays={}", retentionDays);
        
        try {
            int cleanedCount = monitoringService.cleanupExpiredData(retentionDays);
            
            Map<String, Object> result = Map.of(
                "cleanedRecords", cleanedCount,
                "retentionDays", retentionDays,
                "cleanupTime", LocalDateTime.now()
            );
            
            log.info("过期数据清理完成，清理 {} 条记录", cleanedCount);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理失败: " + e.getMessage()));
        }
    }
}