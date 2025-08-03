package com.hotech.events.controller;

import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimelineGenerateRequest;
import com.hotech.events.service.EnhancedTimelineGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增强的时间线控制器
 * 集成错误处理和降级机制的时间线生成API
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/enhanced-timeline")
@Tag(name = "增强时间线", description = "集成错误处理和降级机制的时间线生成API")
public class EnhancedTimelineController {

    @Autowired
    private EnhancedTimelineGenerationService timelineGenerationService;

    /**
     * 生成时间线（同步）
     */
    @PostMapping("/generate")
    @Operation(summary = "生成时间线", description = "根据关键词和时间范围生成事件时间线")
    public ResponseEntity<Map<String, Object>> generateTimeline(
            @RequestBody TimelineGenerateRequest request) {

        try {
            log.info("收到时间线生成请求: {}", request);

            // 验证请求
            Map<String, Object> validationResult = timelineGenerationService.validateRequest(request);
            if (!(Boolean) validationResult.get("valid")) {
                Map<String, Object> errorResponse = createErrorResponse(
                        "请求验证失败", validationResult.get("message").toString());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 生成时间线
            List<EventData> events = timelineGenerationService.generateTimelineWithErrorHandling(request);

            Map<String, Object> response = createSuccessResponse(events, request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("时间线生成失败", e);
            Map<String, Object> errorResponse = createErrorResponse("时间线生成失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 生成时间线（GET方式，便于测试）
     */
    @GetMapping("/generate")
    @Operation(summary = "生成时间线（GET）", description = "通过GET参数生成事件时间线")
    public ResponseEntity<Map<String, Object>> generateTimelineGet(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "地理位置") @RequestParam(required = false) String location,
            @Parameter(description = "最大事件数量") @RequestParam(required = false, defaultValue = "50") Integer maxEvents,
            @Parameter(description = "启用地理信息处理") @RequestParam(required = false, defaultValue = "true") Boolean enableGeographic,
            @Parameter(description = "启用时间段分割") @RequestParam(required = false, defaultValue = "true") Boolean enableSegmentation) {

        try {
            // 构建请求对象
            TimelineGenerateRequest request = new TimelineGenerateRequest(keyword, startTime, endTime, location);
            request.setMaxEvents(maxEvents);
            request.setEnableGeographicProcessing(enableGeographic);
            request.setEnableSegmentation(enableSegmentation);
            request.setSource("web-api");

            return generateTimeline(request);

        } catch (Exception e) {
            log.error("时间线生成失败", e);
            Map<String, Object> errorResponse = createErrorResponse("时间线生成失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 异步生成时间线
     */
    @PostMapping("/generate/async")
    @Operation(summary = "异步生成时间线", description = "异步生成事件时间线，返回任务ID")
    public ResponseEntity<Map<String, Object>> generateTimelineAsync(
            @RequestBody TimelineGenerateRequest request) {

        try {
            log.info("收到异步时间线生成请求: {}", request);

            // 验证请求
            Map<String, Object> validationResult = timelineGenerationService.validateRequest(request);
            if (!(Boolean) validationResult.get("valid")) {
                Map<String, Object> errorResponse = createErrorResponse(
                        "请求验证失败", validationResult.get("message").toString());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 启动异步任务
            String taskId = timelineGenerationService.generateTimelineAsync(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("message", "异步任务已启动");
            response.put("statusUrl", "/api/enhanced-timeline/task/" + taskId + "/status");
            response.put("resultUrl", "/api/enhanced-timeline/task/" + taskId + "/result");

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("异步时间线生成启动失败", e);
            Map<String, Object> errorResponse = createErrorResponse("异步任务启动失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取异步任务状态
     */
    @GetMapping("/task/{taskId}/status")
    @Operation(summary = "获取异步任务状态", description = "查询异步时间线生成任务的状态")
    public ResponseEntity<Map<String, Object>> getAsyncTaskStatus(
            @Parameter(description = "任务ID") @PathVariable String taskId) {

        try {
            Map<String, Object> status = timelineGenerationService.getAsyncTaskStatus(taskId);

            if (!(Boolean) status.get("exists")) {
                Map<String, Object> errorResponse = createErrorResponse("任务不存在", "找不到指定的任务ID: " + taskId);
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskStatus", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取异步任务状态失败", e);
            Map<String, Object> errorResponse = createErrorResponse("获取任务状态失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取异步任务结果
     */
    @GetMapping("/task/{taskId}/result")
    @Operation(summary = "获取异步任务结果", description = "获取异步时间线生成任务的结果")
    public ResponseEntity<Map<String, Object>> getAsyncTaskResult(
            @Parameter(description = "任务ID") @PathVariable String taskId) {

        try {
            List<EventData> events = timelineGenerationService.getAsyncTaskResult(taskId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("events", events);
            response.put("eventCount", events.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取异步任务结果失败", e);
            Map<String, Object> errorResponse = createErrorResponse("获取任务结果失败", e.getMessage());

            // 根据异常类型返回不同的HTTP状态码
            if (e.getMessage().contains("任务不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (e.getMessage().contains("尚未完成")) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    }

    /**
     * 取消异步任务
     */
    @DeleteMapping("/task/{taskId}")
    @Operation(summary = "取消异步任务", description = "取消正在执行的异步时间线生成任务")
    public ResponseEntity<Map<String, Object>> cancelAsyncTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {

        try {
            boolean cancelled = timelineGenerationService.cancelAsyncTask(taskId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", cancelled);
            response.put("taskId", taskId);
            response.put("message", cancelled ? "任务已取消" : "任务取消失败或任务不存在");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("取消异步任务失败", e);
            Map<String, Object> errorResponse = createErrorResponse("取消任务失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 验证时间线生成请求
     */
    @PostMapping("/validate")
    @Operation(summary = "验证请求", description = "验证时间线生成请求的有效性")
    public ResponseEntity<Map<String, Object>> validateRequest(
            @RequestBody TimelineGenerateRequest request) {

        try {
            Map<String, Object> validationResult = timelineGenerationService.validateRequest(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("validation", validationResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("请求验证失败", e);
            Map<String, Object> errorResponse = createErrorResponse("请求验证失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取生成统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取时间线生成的统计信息")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = timelineGenerationService.getGenerationStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            Map<String, Object> errorResponse = createErrorResponse("获取统计信息失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 清理过期任务
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期任务", description = "清理过期的异步任务")
    public ResponseEntity<Map<String, Object>> cleanupExpiredTasks() {
        try {
            timelineGenerationService.cleanupExpiredTasks();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "过期任务清理完成");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("清理过期任务失败", e);
            Map<String, Object> errorResponse = createErrorResponse("清理过期任务失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查增强时间线服务的健康状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "EnhancedTimelineGenerationService");
            health.put("timestamp", System.currentTimeMillis());

            // 获取基本统计信息
            Map<String, Object> statistics = timelineGenerationService.getGenerationStatistics();
            health.put("activeAsyncTasks", statistics.get("activeAsyncTasks"));
            health.put("totalRequests", statistics.get("totalRequests"));
            health.put("successRate", statistics.get("successRate"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("health", health);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("健康检查失败", e);

            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("health", health);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(List<EventData> events, TimelineGenerateRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requestId", request.getRequestId());
        response.put("keyword", request.getKeyword());
        response.put("timeRange", Map.of(
                "startTime", request.getStartTime(),
                "endTime", request.getEndTime()));
        response.put("events", events);
        response.put("eventCount", events.size());
        response.put("timestamp", System.currentTimeMillis());

        // 添加处理信息
        Map<String, Object> processingInfo = new HashMap<>();
        processingInfo.put("enabledGeographicProcessing", request.getEnableGeographicProcessing());
        processingInfo.put("enabledSegmentation", request.getEnableSegmentation());
        processingInfo.put("timeSpanDays", request.getTimeSpanInDays());
        processingInfo.put("needsSegmentation", request.needsSegmentation());
        response.put("processingInfo", processingInfo);

        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("details", details);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}