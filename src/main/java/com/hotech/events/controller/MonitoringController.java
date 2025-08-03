package com.hotech.events.controller;

import com.hotech.events.service.RealTimeMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控面板控制器
 * 提供实时监控数据的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    @Autowired
    private RealTimeMonitoringService monitoringService;

    /**
     * 获取完整的监控面板数据
     * @return 监控面板数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getMonitoringDashboard() {
        try {
            log.info("获取监控面板数据");
            Map<String, Object> dashboard = monitoringService.getMonitoringDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("获取监控面板数据失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取监控面板数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取API调用状态
     * @return API调用状态数据
     */
    @GetMapping("/api-status")
    public ResponseEntity<Map<String, Object>> getApiCallStatus() {
        try {
            log.debug("获取API调用状态");
            Map<String, Object> status = monitoringService.getApiCallStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取API调用状态失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取API调用状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取事件解析状态
     * @return 事件解析状态数据
     */
    @GetMapping("/parsing-status")
    public ResponseEntity<Map<String, Object>> getEventParsingStatus() {
        try {
            log.debug("获取事件解析状态");
            Map<String, Object> status = monitoringService.getEventParsingStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取事件解析状态失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取事件解析状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取系统健康状态
     * @return 系统健康状态数据
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealthStatus() {
        try {
            log.debug("获取系统健康状态");
            Map<String, Object> health = monitoringService.getSystemHealthStatus();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("获取系统健康状态失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取系统健康状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取性能指标
     * @return 性能指标数据
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            log.debug("获取性能指标");
            Map<String, Object> metrics = monitoringService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("获取性能指标失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取性能指标失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取告警状态
     * @return 告警状态数据
     */
    @GetMapping("/alert-status")
    public ResponseEntity<Map<String, Object>> getAlertStatus() {
        try {
            log.debug("获取告警状态");
            Map<String, Object> alerts = monitoringService.getAlertStatus();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("获取告警状态失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取告警状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 手动清理过期数据
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredData() {
        try {
            log.info("手动触发数据清理");
            monitoringService.cleanupExpiredData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "数据清理完成");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("数据清理失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "数据清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取监控统计概览
     * @return 统计概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getMonitoringOverview() {
        try {
            log.debug("获取监控统计概览");
            
            Map<String, Object> overview = new HashMap<>();
            
            // 获取各项状态的简要信息
            Map<String, Object> apiStatus = monitoringService.getApiCallStatus();
            Map<String, Object> parsingStatus = monitoringService.getEventParsingStatus();
            Map<String, Object> healthStatus = monitoringService.getSystemHealthStatus();
            Map<String, Object> alertStatus = monitoringService.getAlertStatus();
            
            // 提取关键指标
            overview.put("apiSuccessRate", apiStatus.get("successRate"));
            overview.put("parsingSuccessRate", parsingStatus.get("parsingSuccessRate"));
            overview.put("systemHealthStatus", healthStatus.get("status"));
            overview.put("currentAlertCount", alertStatus.get("alertCount"));
            overview.put("lastUpdate", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("获取监控统计概览失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取统计概览失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试监控服务连接
     * @return 连接测试结果
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testMonitoringService() {
        try {
            log.info("测试监控服务连接");
            
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("status", "OK");
            testResult.put("message", "监控服务运行正常");
            testResult.put("timestamp", java.time.LocalDateTime.now().toString());
            testResult.put("serviceAvailable", monitoringService != null);
            
            // 简单的功能测试
            if (monitoringService != null) {
                Map<String, Object> healthCheck = monitoringService.getSystemHealthStatus();
                testResult.put("healthCheckWorking", healthCheck != null && !healthCheck.containsKey("error"));
            }
            
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            log.error("监控服务测试失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("error", "监控服务测试失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}