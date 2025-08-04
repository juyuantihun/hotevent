package com.hotech.events.service.impl;

import com.hotech.events.exception.TimelineEnhancementException;
import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimelineGenerateRequest;
import com.hotech.events.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强的时间线生成服务实现
 * 集成错误处理、降级机制和异步处理
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EnhancedTimelineGenerationServiceImpl implements EnhancedTimelineGenerationService {
    
    @Autowired(required = false)
    private ErrorHandlingService errorHandlingService;
    
    @Autowired
    private FallbackStrategyService fallbackStrategyService;
    
    @Autowired
    private TimelinePerformanceMonitoringService performanceMonitoringService;
    
    @Autowired(required = false)
    private com.hotech.events.service.EventGeographicEnhancementService eventGeographicEnhancementService;
    
    // 异步任务管理
    private final Map<String, AsyncTask> asyncTasks = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<List<EventData>>> asyncFutures = new ConcurrentHashMap<>();
    
    // 统计信息
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong fallbackRequests = new AtomicLong(0);
    private final AtomicInteger activeAsyncTasks = new AtomicInteger(0);
    
    @Override
    public List<EventData> generateTimelineWithErrorHandling(TimelineGenerateRequest request) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        
        try {
            log.info("开始生成时间线，请求ID: {}, 关键词: {}", request.getRequestId(), request.getKeyword());
            
            // 验证请求
            Map<String, Object> validationResult = validateRequest(request);
            if (!(Boolean) validationResult.get("valid")) {
                throw new TimelineEnhancementException("REQUEST_VALIDATION_ERROR", "VALIDATION", 
                    "请求验证失败: " + validationResult.get("message"));
            }
            
            // 使用错误处理服务执行时间线生成
            List<EventData> result;
            if (errorHandlingService != null) {
                result = errorHandlingService.executeWithRetryAndFallback(
                    () -> generateTimelineCore(request),
                    () -> generateTimelineFallback(request),
                    3 // 最大重试3次
                );
            } else {
                // 如果错误处理服务不可用，直接执行核心逻辑
                try {
                    result = generateTimelineCore(request);
                } catch (Exception e) {
                    result = generateTimelineFallback(request);
                }
            }
            
            successfulRequests.incrementAndGet();
            log.info("时间线生成成功，请求ID: {}, 事件数量: {}, 耗时: {}ms", 
                    request.getRequestId(), result.size(), System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            log.error("时间线生成失败，请求ID: {}", request.getRequestId(), e);
            
            // 记录错误
            if (errorHandlingService != null) {
                errorHandlingService.logError("TIMELINE_GENERATION", "时间线生成失败", e);
            }
            
            // 创建用户友好的错误消息
            if (e instanceof TimelineEnhancementException) {
                String userMessage = errorHandlingService != null ? 
                    errorHandlingService.createUserFriendlyMessage((TimelineEnhancementException) e) :
                    e.getMessage();
                throw new TimelineEnhancementException("TIMELINE_GENERATION_ERROR", "GENERATION", userMessage, e);
            } else {
                throw new TimelineEnhancementException("TIMELINE_GENERATION_ERROR", "GENERATION", 
                    "时间线生成过程中发生未知错误，请稍后重试", e);
            }
        }
    }
    
    @Override
    @Async
    public String generateTimelineAsync(TimelineGenerateRequest request) {
        String taskId = generateTaskId();
        activeAsyncTasks.incrementAndGet();
        
        AsyncTask task = new AsyncTask(taskId, request, AsyncTaskStatus.RUNNING);
        asyncTasks.put(taskId, task);
        
        CompletableFuture<List<EventData>> future = CompletableFuture.supplyAsync(() -> {
            try {
                task.setStartTime(LocalDateTime.now());
                List<EventData> result = generateTimelineWithErrorHandling(request);
                task.setStatus(AsyncTaskStatus.COMPLETED);
                task.setEndTime(LocalDateTime.now());
                task.setResult(result);
                return result;
            } catch (Exception e) {
                task.setStatus(AsyncTaskStatus.FAILED);
                task.setEndTime(LocalDateTime.now());
                task.setErrorMessage(e.getMessage());
                log.error("异步时间线生成失败，任务ID: {}", taskId, e);
                throw new RuntimeException(e);
            } finally {
                activeAsyncTasks.decrementAndGet();
            }
        });
        
        asyncFutures.put(taskId, future);
        
        log.info("异步时间线生成任务已启动，任务ID: {}", taskId);
        return taskId;
    }
    
    @Override
    public Map<String, Object> getAsyncTaskStatus(String taskId) {
        AsyncTask task = asyncTasks.get(taskId);
        if (task == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("exists", false);
            response.put("message", "任务不存在");
            return response;
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("exists", true);
        status.put("taskId", taskId);
        status.put("status", task.getStatus().name());
        status.put("statusDescription", task.getStatus().getDescription());
        status.put("startTime", task.getStartTime());
        status.put("endTime", task.getEndTime());
        status.put("request", task.getRequest());
        
        if (task.getStatus() == AsyncTaskStatus.COMPLETED && task.getResult() != null) {
            status.put("resultCount", task.getResult().size());
        }
        
        if (task.getStatus() == AsyncTaskStatus.FAILED && task.getErrorMessage() != null) {
            status.put("errorMessage", task.getErrorMessage());
        }
        
        // 计算执行时间
        if (task.getStartTime() != null) {
            LocalDateTime endTime = task.getEndTime() != null ? task.getEndTime() : LocalDateTime.now();
            long durationMs = java.time.Duration.between(task.getStartTime(), endTime).toMillis();
            status.put("durationMs", durationMs);
        }
        
        return status;
    }
    
    @Override
    public List<EventData> getAsyncTaskResult(String taskId) {
        AsyncTask task = asyncTasks.get(taskId);
        if (task == null) {
            throw new TimelineEnhancementException("TASK_NOT_FOUND", "ASYNC", "任务不存在: " + taskId);
        }
        
        if (task.getStatus() != AsyncTaskStatus.COMPLETED) {
            throw new TimelineEnhancementException("TASK_NOT_COMPLETED", "ASYNC", 
                "任务尚未完成，当前状态: " + task.getStatus().getDescription());
        }
        
        return task.getResult() != null ? task.getResult() : new ArrayList<>();
    }
    
    @Override
    public boolean cancelAsyncTask(String taskId) {
        CompletableFuture<List<EventData>> future = asyncFutures.get(taskId);
        AsyncTask task = asyncTasks.get(taskId);
        
        if (future == null || task == null) {
            return false;
        }
        
        boolean cancelled = future.cancel(true);
        if (cancelled) {
            task.setStatus(AsyncTaskStatus.CANCELLED);
            task.setEndTime(LocalDateTime.now());
            activeAsyncTasks.decrementAndGet();
            log.info("异步任务已取消，任务ID: {}", taskId);
        }
        
        return cancelled;
    }
    
    @Override
    public Map<String, Object> validateRequest(TimelineGenerateRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // 基本验证
        if (request == null) {
            errors.add("请求对象不能为空");
        } else {
            if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
                errors.add("关键词不能为空");
            }
            
            if (request.getStartTime() == null) {
                errors.add("开始时间不能为空");
            }
            
            if (request.getEndTime() == null) {
                errors.add("结束时间不能为空");
            }
            
            if (request.getStartTime() != null && request.getEndTime() != null) {
                if (!request.getStartTime().isBefore(request.getEndTime())) {
                    errors.add("开始时间必须早于结束时间");
                }
                
                // 检查时间跨度是否合理（不超过1年）
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    request.getStartTime(), request.getEndTime());
                if (daysBetween > 365) {
                    errors.add("时间跨度不能超过1年");
                }
                
                if (daysBetween < 0) {
                    errors.add("时间跨度不能为负数");
                }
            }
            
            // 验证关键词长度
            if (request.getKeyword() != null && request.getKeyword().length() > 100) {
                errors.add("关键词长度不能超过100个字符");
            }
            
            // 验证最大事件数量
            if (request.getMaxEvents() != null && request.getMaxEvents() <= 0) {
                errors.add("最大事件数量必须大于0");
            }
            
            if (request.getMaxEvents() != null && request.getMaxEvents() > 1000) {
                errors.add("最大事件数量不能超过1000");
            }
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("message", errors.isEmpty() ? "验证通过" : String.join("; ", errors));
        
        return result;
    }
    
    @Override
    public Map<String, Object> getGenerationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计
        stats.put("totalRequests", totalRequests.get());
        stats.put("successfulRequests", successfulRequests.get());
        stats.put("failedRequests", failedRequests.get());
        stats.put("fallbackRequests", fallbackRequests.get());
        stats.put("activeAsyncTasks", activeAsyncTasks.get());
        
        // 成功率
        long total = totalRequests.get();
        if (total > 0) {
            stats.put("successRate", (double) successfulRequests.get() / total);
            stats.put("failureRate", (double) failedRequests.get() / total);
            stats.put("fallbackRate", (double) fallbackRequests.get() / total);
        } else {
            stats.put("successRate", 0.0);
            stats.put("failureRate", 0.0);
            stats.put("fallbackRate", 0.0);
        }
        
        // 异步任务统计
        Map<String, Integer> taskStatusCounts = new HashMap<>();
        for (AsyncTask task : asyncTasks.values()) {
            String status = task.getStatus().name();
            taskStatusCounts.put(status, taskStatusCounts.getOrDefault(status, 0) + 1);
        }
        stats.put("asyncTaskStatusCounts", taskStatusCounts);
        
        // 错误处理统计
        if (errorHandlingService != null) {
            stats.put("errorHandlingStatistics", errorHandlingService.getErrorStatistics());
        } else {
            stats.put("errorHandlingStatistics", Collections.emptyMap());
        }
        
        // 降级策略统计
        stats.put("fallbackStrategyStatistics", fallbackStrategyService.getStrategyUsageStatistics());
        
        return stats;
    }
    
    @Override
    public void cleanupExpiredTasks() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // 清理24小时前的任务
        List<String> expiredTaskIds = new ArrayList<>();
        
        for (Map.Entry<String, AsyncTask> entry : asyncTasks.entrySet()) {
            AsyncTask task = entry.getValue();
            if (task.getEndTime() != null && task.getEndTime().isBefore(cutoffTime)) {
                expiredTaskIds.add(entry.getKey());
            }
        }
        
        for (String taskId : expiredTaskIds) {
            asyncTasks.remove(taskId);
            asyncFutures.remove(taskId);
        }
        
        if (!expiredTaskIds.isEmpty()) {
            log.info("清理了{}个过期的异步任务", expiredTaskIds.size());
        }
    }
    
    /**
     * 核心时间线生成逻辑
     */
    private List<EventData> generateTimelineCore(TimelineGenerateRequest request) {
        // 开始性能监控
        String monitoringSessionId = performanceMonitoringService.startSegmentationMonitoring(1);
        
        try {
            // 这里应该调用实际的时间线生成逻辑
            // 为了演示，我们创建一些模拟数据
            List<EventData> events = createMockTimelineEvents(request);
            
            // 增强地理信息（为缺少经纬度的事件补充坐标）
            if (eventGeographicEnhancementService != null) {
                events = eventGeographicEnhancementService.enhanceEventDataListGeographicInfo(events);
                log.debug("已为 {} 个事件进行地理信息增强", events.size());
            }
            
            // 结束性能监控
            performanceMonitoringService.endSegmentationMonitoring(monitoringSessionId, true, events.size());
            
            return events;
            
        } catch (Exception e) {
            // 结束性能监控（失败）
            performanceMonitoringService.endSegmentationMonitoring(monitoringSessionId, false, 0);
            throw e;
        }
    }
    
    /**
     * 时间线生成降级逻辑
     */
    private List<EventData> generateTimelineFallback(TimelineGenerateRequest request) {
        fallbackRequests.incrementAndGet();
        log.warn("时间线生成进入降级模式，请求ID: {}", request.getRequestId());
        
        // 尝试不同的降级策略
        if (errorHandlingService != null) {
            return errorHandlingService.executeWithFallback(
                () -> fallbackStrategyService.fallbackToSingleApiCall(request),
                () -> fallbackStrategyService.fallbackToCache(request)
            );
        } else {
            // 如果错误处理服务不可用，直接尝试降级策略
            try {
                return fallbackStrategyService.fallbackToSingleApiCall(request);
            } catch (Exception e) {
                return fallbackStrategyService.fallbackToCache(request);
            }
        }
    }
    
    /**
     * 创建模拟时间线事件（在实际实现中应该调用真实的API）
     */
    private List<EventData> createMockTimelineEvents(TimelineGenerateRequest request) {
        List<EventData> events = new ArrayList<>();
        
        // 创建一些基于请求的模拟事件
        for (int i = 0; i < 3; i++) {
            EventData event = new EventData();
            event.setId("mock-event-" + System.currentTimeMillis() + "-" + i);
            event.setTitle("关于 \"" + request.getKeyword() + "\" 的事件 " + (i + 1));
            event.setDescription("这是一个关于 \"" + request.getKeyword() + "\" 的模拟事件描述。");
            event.setEventTime(request.getStartTime().plusDays(i));
            event.setSource("模拟数据源");
            event.setLatitude(39.9042 + i * 0.01); // 北京附近的坐标
            event.setLongitude(116.4074 + i * 0.01);
            event.setLocation("北京市区域" + (i + 1));
            
            events.add(event);
        }
        
        return events;
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "timeline-task-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString(new Random().nextInt());
    }
    
    /**
     * 异步任务类
     */
    private static class AsyncTask {
        private final String taskId;
        private final TimelineGenerateRequest request;
        private AsyncTaskStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<EventData> result;
        private String errorMessage;
        
        public AsyncTask(String taskId, TimelineGenerateRequest request, AsyncTaskStatus status) {
            this.taskId = taskId;
            this.request = request;
            this.status = status;
        }
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public TimelineGenerateRequest getRequest() { return request; }
        public AsyncTaskStatus getStatus() { return status; }
        public void setStatus(AsyncTaskStatus status) { this.status = status; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public List<EventData> getResult() { return result; }
        public void setResult(List<EventData> result) { this.result = result; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * 异步任务状态枚举
     */
    private enum AsyncTaskStatus {
        PENDING("待处理"),
        RUNNING("运行中"),
        COMPLETED("已完成"),
        FAILED("失败"),
        CANCELLED("已取消");
        
        private final String description;
        
        AsyncTaskStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}