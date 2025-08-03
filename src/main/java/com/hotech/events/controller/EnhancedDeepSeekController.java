package com.hotech.events.controller;

import com.hotech.events.dto.*;
import com.hotech.events.service.EnhancedDeepSeekService;
import com.hotech.events.service.DeepSeekMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 增强DeepSeek服务控制器
 * 提供动态提示词、缓存、限流、批量处理和监控功能的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/enhanced-deepseek")
@Tag(name = "增强DeepSeek服务", description = "提供增强的DeepSeek API功能")
public class EnhancedDeepSeekController {

    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;

    @Autowired
    private DeepSeekMonitoringService monitoringService;

    /**
     * 使用动态提示词获取事件
     */
    @PostMapping("/events/fetch")
    @Operation(summary = "使用动态提示词获取事件", description = "根据时间线生成请求，使用动态提示词从DeepSeek API获取相关事件")
    public ResponseEntity<ApiResponse<List<EventData>>> fetchEventsWithDynamicPrompt(
            @RequestBody @Validated TimelineGenerateRequest request) {
        
        log.info("接收到动态提示词事件获取请求: name={}", request.getName());
        
        try {
            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "成功获取 " + events.size() + " 个事件", events));
        } catch (Exception e) {
            log.error("动态提示词事件获取失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取事件失败: " + e.getMessage()));
        }
    }

    /**
     * 异步获取事件
     */
    @PostMapping("/events/fetch-async")
    @Operation(summary = "异步获取事件", description = "异步方式获取事件，适用于大量数据处理")
    public ResponseEntity<ApiResponse<String>> fetchEventsAsync(
            @RequestBody @Validated TimelineGenerateRequest request) {
        
        log.info("接收到异步事件获取请求: name={}", request.getName());
        
        try {
            CompletableFuture<List<EventData>> future = enhancedDeepSeekService.fetchEventsAsync(request);
            
            // 这里可以返回任务ID，客户端可以通过任务ID查询结果
            String taskId = "task_" + System.currentTimeMillis();
            
            // 异步处理完成后的回调（实际项目中可能需要存储到数据库或缓存）
            future.thenAccept(events -> {
                log.info("异步事件获取完成: taskId={}, eventCount={}", taskId, events.size());
            }).exceptionally(throwable -> {
                log.error("异步事件获取失败: taskId={}", taskId, throwable);
                return null;
            });
            
            return ResponseEntity.ok(ApiResponse.success("异步任务已启动", taskId));
        } catch (Exception e) {
            log.error("启动异步事件获取失败", e);
            return ResponseEntity.ok(ApiResponse.error("启动异步任务失败: " + e.getMessage()));
        }
    }

    /**
     * 验证事件真实性
     */
    @PostMapping("/events/validate")
    @Operation(summary = "验证事件真实性", description = "使用DeepSeek API验证事件的真实性和可信度")
    public ResponseEntity<ApiResponse<List<EventValidationResult>>> validateEvents(
            @RequestBody List<EventData> events) {
        
        log.info("接收到事件验证请求: eventCount={}", events.size());
        
        try {
            List<EventValidationResult> results = enhancedDeepSeekService.validateEvents(events);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "成功验证 " + results.size() + " 个事件", results));
        } catch (Exception e) {
            log.error("事件验证失败", e);
            return ResponseEntity.ok(ApiResponse.error("事件验证失败: " + e.getMessage()));
        }
    }

    /**
     * 批量处理事件检索
     */
    @PostMapping("/events/batch-fetch")
    @Operation(summary = "批量处理事件检索", description = "批量处理多个事件获取任务")
    public ResponseEntity<ApiResponse<String>> fetchEventsBatch(
            @RequestBody List<EventFetchTask> tasks) {
        
        log.info("接收到批量事件获取请求: taskCount={}", tasks.size());
        
        try {
            CompletableFuture<List<EventData>> future = enhancedDeepSeekService.fetchEventsBatch(tasks);
            
            String batchId = "batch_" + System.currentTimeMillis();
            
            // 异步处理完成后的回调
            future.thenAccept(events -> {
                log.info("批量事件获取完成: batchId={}, eventCount={}", batchId, events.size());
            }).exceptionally(throwable -> {
                log.error("批量事件获取失败: batchId={}", batchId, throwable);
                return null;
            });
            
            return ResponseEntity.ok(ApiResponse.success("批量任务已启动", batchId));
        } catch (Exception e) {
            log.error("启动批量事件获取失败", e);
            return ResponseEntity.ok(ApiResponse.error("启动批量任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取API使用统计
     */
    @GetMapping("/stats/usage")
    @Operation(summary = "获取API使用统计", description = "获取DeepSeek API的使用统计信息")
    public ResponseEntity<ApiResponse<ApiUsageStats>> getUsageStats() {
        try {
            ApiUsageStats stats = enhancedDeepSeekService.getUsageStats();
            return ResponseEntity.ok(ApiResponse.success("获取使用统计成功", stats));
        } catch (Exception e) {
            log.error("获取使用统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取使用统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取缓存统计
     */
    @GetMapping("/stats/cache")
    @Operation(summary = "获取缓存统计", description = "获取请求缓存的统计信息")
    public ResponseEntity<ApiResponse<CacheStats>> getCacheStats() {
        try {
            CacheStats stats = enhancedDeepSeekService.getCacheStats();
            return ResponseEntity.ok(ApiResponse.success("获取缓存统计成功", stats));
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取缓存统计失败: " + e.getMessage()));
        }
    }

    /**
     * 检查API健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查API健康状态", description = "检查DeepSeek API的健康状态")
    public ResponseEntity<ApiResponse<ApiHealthStatus>> checkApiHealth() {
        try {
            ApiHealthStatus health = enhancedDeepSeekService.checkApiHealth();
            return ResponseEntity.ok(ApiResponse.success("健康检查完成", health));
        } catch (Exception e) {
            log.error("API健康检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("健康检查失败: " + e.getMessage()));
        }
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "清理缓存", description = "清空所有请求缓存")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        try {
            enhancedDeepSeekService.clearCache();
            return ResponseEntity.ok(ApiResponse.success("缓存清理成功", "OK"));
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理缓存失败: " + e.getMessage()));
        }
    }

    /**
     * 重置限流器
     */
    @PostMapping("/rate-limit/reset")
    @Operation(summary = "重置限流器", description = "重置API请求限流器")
    public ResponseEntity<ApiResponse<String>> resetRateLimit() {
        try {
            enhancedDeepSeekService.resetRateLimit();
            return ResponseEntity.ok(ApiResponse.success("限流器重置成功", "OK"));
        } catch (Exception e) {
            log.error("重置限流器失败", e);
            return ResponseEntity.ok(ApiResponse.error("重置限流器失败: " + e.getMessage()));
        }
    }

    /**
     * 获取详细使用统计
     */
    @GetMapping("/stats/detailed")
    @Operation(summary = "获取详细使用统计", description = "获取指定时间范围内的详细API使用统计")
    public ResponseEntity<ApiResponse<ApiUsageStats>> getDetailedUsageStats(
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            ApiUsageStats stats = monitoringService.getUsageStatistics(startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success("获取详细统计成功", stats));
        } catch (Exception e) {
            log.error("获取详细使用统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取详细统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取请求类型统计
     */
    @GetMapping("/stats/request-types")
    @Operation(summary = "获取请求类型统计", description = "获取不同请求类型的统计信息")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRequestTypeStats(
            @Parameter(description = "开始时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            List<Map<String, Object>> stats = monitoringService.getRequestTypeStatistics(startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success("获取请求类型统计成功", stats));
        } catch (Exception e) {
            log.error("获取请求类型统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取请求类型统计失败: " + e.getMessage()));
        }
    }
}