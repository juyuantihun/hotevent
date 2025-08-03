package com.hotech.events.controller;

import com.hotech.events.exception.TimelineEnhancementException;
import com.hotech.events.service.ErrorHandlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 错误处理控制器
 * 提供错误处理相关的API接口和全局异常处理
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/error-handling")
public class ErrorHandlingController {

    @Autowired
    private ErrorHandlingService errorHandlingService;

    /**
     * 获取错误统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getErrorStatistics() {
        try {
            Map<String, Object> statistics = errorHandlingService.getErrorStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取错误统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取错误统计信息失败", e.getMessage()));
        }
    }

    /**
     * 重置熔断器状态
     */
    @PostMapping("/circuit-breaker/{errorType}/reset")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String errorType) {
        try {
            errorHandlingService.resetCircuitBreaker(errorType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "熔断器已重置");
            response.put("errorType", errorType);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("重置熔断器失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("重置熔断器失败", e.getMessage()));
        }
    }

    /**
     * 检查熔断器状态
     */
    @GetMapping("/circuit-breaker/{errorType}/status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus(@PathVariable String errorType) {
        try {
            boolean shouldTrigger = errorHandlingService.shouldTriggerCircuitBreaker(errorType);

            Map<String, Object> response = new HashMap<>();
            response.put("errorType", errorType);
            response.put("isTriggered", shouldTrigger);
            response.put("status", shouldTrigger ? "OPEN" : "CLOSED");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查熔断器状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("检查熔断器状态失败", e.getMessage()));
        }
    }

    /**
     * 测试错误处理机制
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testErrorHandling(
            @RequestParam String errorType,
            @RequestParam(defaultValue = "false") boolean shouldFail) {

        try {
            String result = errorHandlingService.executeWithFallback(
                    () -> {
                        if (shouldFail) {
                            throw new TimelineEnhancementException(errorType, errorType, "测试错误");
                        }
                        return "主操作成功";
                    },
                    () -> "降级操作成功");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("errorType", errorType);
            response.put("shouldFail", shouldFail);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试错误处理机制失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("测试错误处理机制失败", e.getMessage()));
        }
    }

    /**
     * 全局异常处理器 - 处理TimelineEnhancementException
     */
    @ExceptionHandler(TimelineEnhancementException.class)
    public ResponseEntity<Map<String, Object>> handleTimelineEnhancementException(
            TimelineEnhancementException e) {

        log.error("时间线增强功能异常: {}", e.getMessage(), e);

        // 记录错误
        errorHandlingService.logError(e.getErrorType(), e.getMessage(), e);

        // 创建用户友好的错误消息
        String userFriendlyMessage = errorHandlingService.createUserFriendlyMessage(e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", e.getErrorCode());
        errorResponse.put("errorType", e.getErrorType());
        errorResponse.put("message", userFriendlyMessage);
        errorResponse.put("timestamp", System.currentTimeMillis());

        // 根据错误类型返回不同的HTTP状态码
        HttpStatus status = getHttpStatusForErrorType(e.getErrorType());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 全局异常处理器 - 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("未处理的异常: {}", e.getMessage(), e);

        // 记录错误
        errorHandlingService.logError("UNKNOWN", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", "INTERNAL_ERROR");
        errorResponse.put("errorType", "SYSTEM");
        errorResponse.put("message", "系统内部错误，请稍后重试");
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    /**
     * 根据错误类型获取HTTP状态码
     */
    private HttpStatus getHttpStatusForErrorType(String errorType) {
        switch (errorType) {
            case "SEGMENTATION":
                return HttpStatus.UNPROCESSABLE_ENTITY; // 422
            case "GEOGRAPHIC":
                return HttpStatus.SERVICE_UNAVAILABLE; // 503
            case "API":
                return HttpStatus.BAD_GATEWAY; // 502
            case "CONFIG":
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }
    }
}