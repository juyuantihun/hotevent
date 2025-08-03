package com.hotech.events.controller;

import com.hotech.events.common.Result;
import com.hotech.events.entity.ApiCallRecord;
import com.hotech.events.service.ApiMonitoringService;
import com.hotech.events.service.EnhancedApiCallManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * API监控控制器
 * 提供API调用统计和监控功能
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "API监控管理")
public class ApiMonitoringController {

    @Autowired
    private ApiMonitoringService apiMonitoringService;
    
    @Autowired
    private EnhancedApiCallManager enhancedApiCallManager;

    @GetMapping("/stats")
    @Operation(summary = "获取API调用统计")
    public Result<List<Map<String, Object>>> getApiCallStats(
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            // 默认查询最近24小时的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            List<Map<String, Object>> stats = apiMonitoringService.getApiCallStats(startTime, endTime);
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取API调用统计失败", e);
            return Result.error("获取API调用统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/success-rate")
    @Operation(summary = "获取API成功率统计")
    public Result<List<Map<String, Object>>> getSuccessRateStats(
            @Parameter(description = "统计起始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        
        try {
            // 默认查询最近24小时的数据
            if (since == null) {
                since = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            }
            
            List<Map<String, Object>> stats = apiMonitoringService.getSuccessRateStats(since);
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取API成功率统计失败", e);
            return Result.error("获取API成功率统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/performance")
    @Operation(summary = "获取API性能统计")
    public Result<List<Map<String, Object>>> getPerformanceStats(
            @Parameter(description = "统计起始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        
        try {
            // 默认查询最近24小时的数据
            if (since == null) {
                since = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            }
            
            List<Map<String, Object>> stats = apiMonitoringService.getPerformanceStats(since);
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取API性能统计失败", e);
            return Result.error("获取API性能统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/errors")
    @Operation(summary = "获取API错误统计")
    public Result<List<Map<String, Object>>> getErrorStats(
            @Parameter(description = "统计起始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        
        try {
            // 默认查询最近24小时的数据
            if (since == null) {
                since = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            }
            
            List<Map<String, Object>> stats = apiMonitoringService.getErrorStats(since);
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取API错误统计失败", e);
            return Result.error("获取API错误统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/recent-calls")
    @Operation(summary = "获取最近的API调用记录")
    public Result<List<ApiCallRecord>> getRecentApiCalls(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "50") int limit) {
        
        try {
            List<ApiCallRecord> records = apiMonitoringService.getRecentApiCalls(limit);
            return Result.success(records);
            
        } catch (Exception e) {
            log.error("获取最近API调用记录失败", e);
            return Result.error("获取最近API调用记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/report")
    @Operation(summary = "生成API性能报告")
    public Result<Map<String, Object>> generatePerformanceReport(
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            // 默认查询最近24小时的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> report = apiMonitoringService.generatePerformanceReport(startTime, endTime);
            return Result.success(report);
            
        } catch (Exception e) {
            log.error("生成API性能报告失败", e);
            return Result.error("生成API性能报告失败: " + e.getMessage());
        }
    }

    @GetMapping("/realtime")
    @Operation(summary = "获取实时监控数据")
    public Result<Map<String, Object>> getRealTimeMonitoringData() {
        try {
            Map<String, Object> data = apiMonitoringService.getRealTimeMonitoringData();
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            return Result.error("获取实时监控数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/alerts")
    @Operation(summary = "检查系统告警")
    public Result<List<Map<String, Object>>> checkAlerts() {
        try {
            List<Map<String, Object>> alerts = apiMonitoringService.checkAlerts();
            return Result.success(alerts);
            
        } catch (Exception e) {
            log.error("检查系统告警失败", e);
            return Result.error("检查系统告警失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期记录")
    public Result<Integer> cleanupOldRecords(
            @Parameter(description = "清理此时间之前的记录") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        
        try {
            // 默认清理30天前的记录
            if (beforeTime == null) {
                beforeTime = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
            }
            
            int deletedCount = apiMonitoringService.cleanupOldRecords(beforeTime);
            return Result.success(deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            return Result.error("清理过期记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/manager/stats")
    @Operation(summary = "获取API调用管理器统计")
    public Result<Map<String, Object>> getApiCallManagerStats() {
        try {
            Map<String, Object> stats = enhancedApiCallManager.getApiCallStats();
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取API调用管理器统计失败", e);
            return Result.error("获取API调用管理器统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/manager/reset-health-cache")
    @Operation(summary = "重置API健康状态缓存")
    public Result<String> resetHealthCache() {
        try {
            enhancedApiCallManager.resetHealthCache();
            return Result.success("API健康状态缓存已重置");
            
        } catch (Exception e) {
            log.error("重置API健康状态缓存失败", e);
            return Result.error("重置API健康状态缓存失败: " + e.getMessage());
        }
    }

    @GetMapping("/manager/strategy")
    @Operation(summary = "获取当前API选择策略")
    public Result<String> getCurrentSelectionStrategy() {
        try {
            String strategy = enhancedApiCallManager.getCurrentSelectionStrategy();
            return Result.success(strategy);
            
        } catch (Exception e) {
            log.error("获取当前API选择策略失败", e);
            return Result.error("获取当前API选择策略失败: " + e.getMessage());
        }
    }
}