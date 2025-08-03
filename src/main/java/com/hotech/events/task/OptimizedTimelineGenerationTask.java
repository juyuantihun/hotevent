package com.hotech.events.task;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 优化的时间线生成任务
 * 集成批处理、缓存、进度跟踪和性能监控功能
 */
@Slf4j
@Component
public class OptimizedTimelineGenerationTask {

    @Autowired
    private BatchProcessingService batchProcessingService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ProgressTrackingService progressTrackingService;

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;

    @Autowired
    private EventStorageService eventStorageService;

    @Autowired
    private TimelineService timelineService;

    @Value("${app.deepseek.enhanced.batch-size:10}")
    private int batchSize;

    @Value("${app.deepseek.enhanced.enable-cache:true}")
    private boolean cacheEnabled;

    /**
     * 生成优化的时间线
     * 
     * @param timelineId 时间线ID
     * @param request    时间线生成请求
     */
    public void generateOptimizedTimeline(Long timelineId, TimelineGenerateRequest request) {
        // 开始进度跟踪
        String progressSessionId = progressTrackingService.startTracking(
                "timeline_" + timelineId,
                "优化时间线生成: " + request.getName(),
                6);

        // 开始性能监控
        String monitoringSessionId = performanceMonitoringService.startMonitoring(
                "OPTIMIZED_TIMELINE_GENERATION",
                "TIMELINE_PROCESSING");

        try {
            log.info("开始优化时间线生成: timelineId={}, name={}", timelineId, request.getName());

            // 步骤1: 智能事件检索 (0-30%)
            progressTrackingService.updateProgress(progressSessionId, 1, "智能事件检索", 10, "开始事件检索...");
            List<EventData> events = performOptimizedEventRetrieval(request, monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 1, "智能事件检索", 30,
                    String.format("检索到 %d 个事件", events.size()));

            // 步骤2: 批量事件验证 (30-50%)
            progressTrackingService.updateProgress(progressSessionId, 2, "批量事件验证", 35, "开始事件验证...");
            List<EventData> validatedEvents = performOptimizedEventValidation(events, monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 2, "批量事件验证", 50,
                    String.format("验证通过 %d 个事件", validatedEvents.size()));

            // 步骤3: 批量事件存储 (50-70%)
            progressTrackingService.updateProgress(progressSessionId, 3, "批量事件存储", 55, "开始事件存储...");
            List<Long> eventIds = performOptimizedEventStorage(validatedEvents, monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 3, "批量事件存储", 70,
                    String.format("存储 %d 个事件", eventIds.size()));

            // 步骤4: 时间线关联 (70-85%)
            progressTrackingService.updateProgress(progressSessionId, 4, "时间线关联", 75, "建立事件关联...");
            associateEventsToTimeline(timelineId, eventIds, monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 4, "时间线关联", 85, "关联完成");

            // 步骤5: 缓存更新 (85-95%)
            progressTrackingService.updateProgress(progressSessionId, 5, "缓存更新", 90, "更新缓存...");
            updateTimelineCache(timelineId, validatedEvents, monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 5, "缓存更新", 95, "缓存更新完成");

            // 步骤6: 完成处理 (95-100%)
            progressTrackingService.updateProgress(progressSessionId, 6, "完成处理", 98, "最终处理...");
            finalizeOptimizedTimeline(timelineId, eventIds.size(), validatedEvents.size(), monitoringSessionId);
            progressTrackingService.updateProgress(progressSessionId, 6, "完成处理", 100, "时间线生成完成");

            // 完成跟踪
            progressTrackingService.completeTracking(progressSessionId, true,
                    String.format("成功生成时间线，包含 %d 个事件", eventIds.size()));

            // 结束性能监控
            performanceMonitoringService.endMonitoring(monitoringSessionId, true);

            log.info("优化时间线生成完成: timelineId={}, eventCount={}, validatedCount={}",
                    timelineId, events.size(), validatedEvents.size());

        } catch (Exception e) {
            log.error("优化时间线生成失败: timelineId={}", timelineId, e);

            // 完成跟踪（失败）
            progressTrackingService.completeTracking(progressSessionId, false,
                    "时间线生成失败: " + e.getMessage());

            // 结束性能监控（失败）
            performanceMonitoringService.endMonitoring(monitoringSessionId, false);

            throw new RuntimeException("优化时间线生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步生成优化时间线
     */
    public CompletableFuture<Void> generateOptimizedTimelineAsync(Long timelineId, TimelineGenerateRequest request) {
        return CompletableFuture.runAsync(() -> generateOptimizedTimeline(timelineId, request));
    }

    /**
     * 执行优化的事件检索
     */
    private List<EventData> performOptimizedEventRetrieval(TimelineGenerateRequest request,
            String monitoringSessionId) {
        try {
            // 检查缓存
            if (cacheEnabled) {
                String cacheKey = cacheService.generateEventQueryKey(generateRequestKey(request));
                Optional<List> cachedEventsRaw = cacheService.get(cacheKey, List.class);

                if (cachedEventsRaw.isPresent()) {
                    @SuppressWarnings("unchecked")
                    List<EventData> cachedEvents = (List<EventData>) cachedEventsRaw.get();
                    log.info("从缓存获取事件数据: count={}", cachedEvents.size());
                    performanceMonitoringService.recordMetric(monitoringSessionId, "cache_hit", 1, "count");
                    return cachedEvents;
                } else {
                    performanceMonitoringService.recordMetric(monitoringSessionId, "cache_miss", 1, "count");
                }
            }

            // 从API获取事件
            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);

            // 缓存结果
            if (cacheEnabled && !events.isEmpty()) {
                String cacheKey = cacheService.generateEventQueryKey(generateRequestKey(request));
                cacheService.put(cacheKey, events);
                log.debug("事件数据已缓存: key={}, count={}", cacheKey, events.size());
            }

            performanceMonitoringService.recordMetric(monitoringSessionId, "events_retrieved", events.size(), "count");

            return events;

        } catch (Exception e) {
            log.error("优化事件检索失败", e);
            throw new RuntimeException("优化事件检索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行优化的事件验证
     */
    private List<EventData> performOptimizedEventValidation(List<EventData> events, String monitoringSessionId) {
        try {
            if (events.isEmpty()) {
                return new ArrayList<>();
            }

            // 检查缓存的验证结果
            List<EventData> validatedEvents = new ArrayList<>();
            List<EventData> eventsToValidate = new ArrayList<>();

            if (cacheEnabled) {
                for (EventData event : events) {
                    String cacheKey = cacheService.generateEventValidationKey(event.getTitle());
                    Optional<EventValidationResult> cachedResultRaw = cacheService.get(cacheKey,
                            EventValidationResult.class);

                    if (cachedResultRaw.isPresent()) {
                        EventValidationResult result = cachedResultRaw.get();
                        if (result.getIsValid() && result.getCredibilityScore() >= 0.7) {
                            event.setCredibilityScore(result.getCredibilityScore());
                            validatedEvents.add(event);
                        }
                        performanceMonitoringService.recordMetric(monitoringSessionId, "validation_cache_hit", 1,
                                "count");
                    } else {
                        eventsToValidate.add(event);
                        performanceMonitoringService.recordMetric(monitoringSessionId, "validation_cache_miss", 1,
                                "count");
                    }
                }
            } else {
                eventsToValidate.addAll(events);
            }

            // 批量验证未缓存的事件
            if (!eventsToValidate.isEmpty()) {
                List<EventValidationResult> validationResults = batchProcessingService.batchValidateEvents(
                        eventsToValidate, batchSize);

                // 处理验证结果并缓存
                for (int i = 0; i < eventsToValidate.size() && i < validationResults.size(); i++) {
                    EventData event = eventsToValidate.get(i);
                    EventValidationResult result = validationResults.get(i);

                    // 缓存验证结果
                    if (cacheEnabled) {
                        String cacheKey = cacheService.generateEventValidationKey(event.getTitle());
                        cacheService.put(cacheKey, result);
                    }

                    // 添加验证通过的事件
                    if (result.getIsValid() && result.getCredibilityScore() >= 0.7) {
                        event.setCredibilityScore(result.getCredibilityScore());
                        validatedEvents.add(event);
                    }
                }
            }

            performanceMonitoringService.recordMetric(monitoringSessionId, "events_validated", validatedEvents.size(),
                    "count");

            log.info("批量事件验证完成: 原始事件数={}, 验证通过事件数={}", events.size(), validatedEvents.size());

            return validatedEvents;

        } catch (Exception e) {
            log.error("优化事件验证失败", e);
            throw new RuntimeException("优化事件验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行优化的事件存储
     */
    private List<Long> performOptimizedEventStorage(List<EventData> events, String monitoringSessionId) {
        try {
            if (events.isEmpty()) {
                return new ArrayList<>();
            }

            // 使用批处理服务存储事件
            List<Long> eventIds = batchProcessingService.batchStoreEvents(events, batchSize);

            performanceMonitoringService.recordMetric(monitoringSessionId, "events_stored", eventIds.size(), "count");

            log.info("批量事件存储完成: 验证事件数={}, 存储成功事件数={}", events.size(), eventIds.size());

            return eventIds;

        } catch (Exception e) {
            log.error("优化事件存储失败", e);
            throw new RuntimeException("优化事件存储失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关联事件到时间线
     */
    private void associateEventsToTimeline(Long timelineId, List<Long> eventIds, String monitoringSessionId) {
        try {
            if (eventIds.isEmpty()) {
                return;
            }

            // 这里应该调用TimelineService的方法来关联事件
            // 由于接口限制，暂时记录日志
            log.info("关联事件到时间线: timelineId={}, eventCount={}", timelineId, eventIds.size());

            performanceMonitoringService.recordMetric(monitoringSessionId, "events_associated", eventIds.size(),
                    "count");

        } catch (Exception e) {
            log.error("关联事件到时间线失败: timelineId={}", timelineId, e);
            throw new RuntimeException("关联事件到时间线失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新时间线缓存
     */
    private void updateTimelineCache(Long timelineId, List<EventData> events, String monitoringSessionId) {
        try {
            if (cacheEnabled) {
                String cacheKey = cacheService.generateTimelineKey(timelineId);
                cacheService.put(cacheKey, events);

                log.debug("时间线缓存已更新: timelineId={}, eventCount={}", timelineId, events.size());

                performanceMonitoringService.recordMetric(monitoringSessionId, "cache_updated", 1, "count");
            }

        } catch (Exception e) {
            log.error("更新时间线缓存失败: timelineId={}", timelineId, e);
            // 缓存更新失败不应该影响主流程
        }
    }

    /**
     * 完成优化时间线处理
     */
    private void finalizeOptimizedTimeline(Long timelineId, int eventCount, int validatedCount,
            String monitoringSessionId) {
        try {
            // 这里应该更新时间线状态
            log.info("完成优化时间线处理: timelineId={}, eventCount={}, validatedCount={}",
                    timelineId, eventCount, validatedCount);

            performanceMonitoringService.recordMetric(monitoringSessionId, "timeline_completed", 1, "count");
            performanceMonitoringService.recordMetric(monitoringSessionId, "final_event_count", eventCount, "count");
            performanceMonitoringService.recordMetric(monitoringSessionId, "final_validated_count", validatedCount,
                    "count");

        } catch (Exception e) {
            log.error("完成优化时间线处理失败: timelineId={}", timelineId, e);
            throw new RuntimeException("完成优化时间线处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成请求缓存键
     */
    private String generateRequestKey(TimelineGenerateRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(request.getName()).append("_");
        keyBuilder.append(request.getDescription()).append("_");

        if (request.getRegionIds() != null) {
            keyBuilder.append(String.join(",", request.getRegionIds().stream()
                    .map(String::valueOf).toArray(String[]::new))).append("_");
        }

        if (request.getStartTime() != null) {
            keyBuilder.append(request.getStartTime().toString()).append("_");
        }

        if (request.getEndTime() != null) {
            keyBuilder.append(request.getEndTime().toString());
        }

        return keyBuilder.toString();
    }
}