package com.hotech.events.task;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Region;
import com.hotech.events.mapper.RegionMapper;
import com.hotech.events.mapper.TimelineEventMapper;
import com.hotech.events.service.EnhancedDeepSeekService;
import com.hotech.events.service.EventStorageService;
import com.hotech.events.service.EventValidationService;
import com.hotech.events.service.impl.TimelineServiceImpl;
import com.hotech.events.service.EnhancedApiCallManager;
import com.hotech.events.service.EventParsingEnhancer;
import com.hotech.events.service.FallbackDataGenerator;
import com.hotech.events.service.RealTimeMonitoringService;
import com.hotech.events.service.TimeSegmentationService;
import com.hotech.events.dto.TimeSegment;
import com.hotech.events.config.DynamicApiConfigManager;
import com.hotech.events.config.DynamicSystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 重构后的时间线生成任务
 * 集成动态提示词、事件验证和存储功能
 */
@Slf4j
@Component // 启用组件注册
public class TimelineGenerationTask {

    private final TimelineServiceImpl timelineService;
    private final RegionMapper regionMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final EnhancedDeepSeekService enhancedDeepSeekService;
    private final EventValidationService eventValidationService;
    private final EventStorageService eventStorageService;
    private final com.hotech.events.service.DynamicDeepSeekService dynamicDeepSeekService;
    private final EnhancedApiCallManager enhancedApiCallManager;
    private final EventParsingEnhancer eventParsingEnhancer;
    private final FallbackDataGenerator fallbackDataGenerator;
    private final TimeSegmentationService timeSegmentationService;
    private final DynamicSystemConfig dynamicSystemConfig;

    @Autowired
    public TimelineGenerationTask(
            @Lazy TimelineServiceImpl timelineService,
            RegionMapper regionMapper,
            TimelineEventMapper timelineEventMapper,
            @Autowired(required = false) EnhancedDeepSeekService enhancedDeepSeekService,
            @Autowired(required = false) EventValidationService eventValidationService,
            @Autowired(required = false) EventStorageService eventStorageService,
            @Autowired(required = false) com.hotech.events.service.DynamicDeepSeekService dynamicDeepSeekService,
            @Autowired(required = false) EnhancedApiCallManager enhancedApiCallManager,
            EventParsingEnhancer eventParsingEnhancer,
            FallbackDataGenerator fallbackDataGenerator,
            TimeSegmentationService timeSegmentationService,
            DynamicSystemConfig dynamicSystemConfig) {
        this.timelineService = timelineService;
        this.regionMapper = regionMapper;
        this.timelineEventMapper = timelineEventMapper;
        this.enhancedDeepSeekService = enhancedDeepSeekService;
        this.eventValidationService = eventValidationService;
        this.eventStorageService = eventStorageService;
        this.dynamicDeepSeekService = dynamicDeepSeekService;
        this.enhancedApiCallManager = enhancedApiCallManager;
        this.eventParsingEnhancer = eventParsingEnhancer;
        this.fallbackDataGenerator = fallbackDataGenerator;
        this.timeSegmentationService = timeSegmentationService;
        this.dynamicSystemConfig = dynamicSystemConfig;
    }

    /**
     * 生成时间线（重构版本）
     * 
     * @param timelineId 时间线ID
     * @param regionIds  地区ID列表
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    public void generateTimeline(Long timelineId, List<Long> regionIds,
            LocalDateTime startTime, LocalDateTime endTime) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        log.info("🚀 [{}] ========== 开始重构版时间线生成 ==========", taskId);
        log.info("🚀 [{}] 时间线ID: {}", taskId, timelineId);
        log.info("🚀 [{}] 地区ID列表: {}", taskId, regionIds);
        log.info("🚀 [{}] 开始时间: {}", taskId, startTime);
        log.info("🚀 [{}] 结束时间: {}", taskId, endTime);
        log.info("🚀 [{}] 时间跨度: {} 天", taskId,
                startTime != null && endTime != null ? java.time.temporal.ChronoUnit.DAYS.between(startTime, endTime)
                        : "未知");

        long startTimestamp = System.currentTimeMillis();

        try {
            // 阶段1：准备工作 (0-15%)
            log.info("📋 [{}] ========== 阶段1: 准备工作 (0-15%) ==========", taskId);
            timelineService.updateGenerationProgress(timelineId, 5, 0, 0, "正在准备时间线生成...");
            log.info("📋 [{}] 更新进度: 5% - 正在准备时间线生成...", taskId);

            // 构建时间线生成请求
            log.info("📋 [{}] 开始构建时间线生成请求...", taskId);
            TimelineGenerateRequest request = buildTimelineRequest(timelineId, regionIds, startTime, endTime);
            log.info("📋 [{}] 时间线生成请求构建完成: name='{}', description='{}'",
                    taskId, request.getName(), request.getDescription());

            // 获取地区信息
            log.info("📋 [{}] 开始获取地区信息...", taskId);
            List<Region> regions = getRegionsInfo(regionIds);
            log.info("📋 [{}] 地区信息获取完成: 共{}个地区", taskId, regions.size());
            for (int i = 0; i < regions.size(); i++) {
                Region region = regions.get(i);
                log.info("📋 [{}] 地区{}: ID={}, 名称='{}', 类型='{}'",
                        taskId, i + 1, region.getId(), region.getName(), region.getType());
            }

            timelineService.updateGenerationProgress(timelineId, 15, 0, 0, "准备工作完成，开始事件检索...");
            log.info("📋 [{}] 更新进度: 15% - 准备工作完成，开始事件检索...", taskId);
            log.info("📋 [{}] 阶段1完成，耗时: {}ms", taskId, System.currentTimeMillis() - startTimestamp);

            // 阶段2：智能事件检索并直接入库 (15-60%)
            log.info("🔍 [{}] ========== 阶段2: 智能事件检索并直接入库 (15-60%) ==========", taskId);
            long retrievalStartTime = System.currentTimeMillis();
            List<EventData> retrievedEvents = performIntelligentEventRetrieval(timelineId, request);
            long retrievalEndTime = System.currentTimeMillis();

            if (CollectionUtils.isEmpty(retrievedEvents)) {
                log.error("🔍 [{}] 事件检索失败: 未检索到相关事件数据", taskId);
                throw new RuntimeException("未检索到相关事件数据");
            }

            log.info("🔍 [{}] 事件检索完成: 共检索到{}个事件，耗时: {}ms",
                    taskId, retrievedEvents.size(), retrievalEndTime - retrievalStartTime);

            // 打印前几个事件的详细信息
            int printCount = Math.min(3, retrievedEvents.size());
            for (int i = 0; i < printCount; i++) {
                EventData event = retrievedEvents.get(i);
                log.info("🔍 [{}] 检索事件{}: 标题='{}', 时间={}, 位置='{}'",
                        taskId, i + 1, event.getTitle(), event.getEventTime(), event.getLocation());
            }
            if (retrievedEvents.size() > printCount) {
                log.info("🔍 [{}] ... 还有{}个事件未显示", taskId, retrievedEvents.size() - printCount);
            }

            // 直接将检索到的事件存储到event表
            log.info("💾 [{}] 开始将检索到的事件直接存储到event表...", taskId);
            timelineService.updateGenerationProgress(timelineId, 45, retrievedEvents.size(), 0, "正在存储检索到的事件...");

            long storageStartTime = System.currentTimeMillis();
            List<Long> storedEventIds = performEventStorage(timelineId, retrievedEvents);
            long storageEndTime = System.currentTimeMillis();

            log.info("💾 [{}] 事件存储完成: 检索{}个事件，成功存储{}个事件，耗时: {}ms",
                    taskId, retrievedEvents.size(), storedEventIds.size(),
                    storageEndTime - storageStartTime);
            log.info("💾 [{}] 存储的事件ID列表: {}", taskId,
                    storedEventIds.size() <= 10 ? storedEventIds
                            : storedEventIds.subList(0, 10) + "... (共" + storedEventIds.size() + "个)");

            // 阶段3：事件验证 (40-60%) - 暂时注释掉
            /*
             * log.info("✅ [{}] ========== 阶段3: 事件验证 (40-60%) ==========", taskId);
             * long validationStartTime = System.currentTimeMillis();
             * List<EventData> validatedEvents = performEventValidation(timelineId,
             * retrievedEvents);
             * long validationEndTime = System.currentTimeMillis();
             * 
             * log.info("✅ [{}] 事件验证完成: 输入{}个事件，验证通过{}个事件，耗时: {}ms",
             * taskId, retrievedEvents.size(), validatedEvents.size(),
             * validationEndTime - validationStartTime);
             */

            // 由于跳过了验证阶段，直接使用检索到的事件作为验证后的事件
            List<EventData> validatedEvents = retrievedEvents;
            log.info("✅ [{}] 跳过事件验证阶段，直接使用检索到的{}个事件", taskId, validatedEvents.size());

            // 阶段5：时间线编制 (75-90%)
            log.info("🔗 [{}] ========== 阶段5: 时间线编制 (75-90%) ==========", taskId);
            long compilationStartTime = System.currentTimeMillis();
            performTimelineCompilation(timelineId, storedEventIds, validatedEvents);
            long compilationEndTime = System.currentTimeMillis();

            log.info("🔗 [{}] 时间线编制完成: 处理{}个事件，耗时: {}ms",
                    taskId, storedEventIds.size(), compilationEndTime - compilationStartTime);

            // 阶段6：完成处理 (90-100%)
            log.info("🎯 [{}] ========== 阶段6: 完成处理 (90-100%) ==========", taskId);
            long finalizationStartTime = System.currentTimeMillis();
            finalizeTimelineGeneration(timelineId, storedEventIds.size(), validatedEvents.size());
            long finalizationEndTime = System.currentTimeMillis();

            log.info("🎯 [{}] 时间线生成最终完成，耗时: {}ms", taskId,
                    finalizationEndTime - finalizationStartTime);

            // 总结信息
            long totalTime = System.currentTimeMillis() - startTimestamp;
            log.info("🎉 [{}] ========== 时间线生成总结 ==========", taskId);
            log.info("🎉 [{}] 时间线ID: {}", taskId, timelineId);
            log.info("🎉 [{}] 检索事件数: {}", taskId, retrievedEvents.size());
            log.info("🎉 [{}] 验证事件数: {}", taskId, validatedEvents.size());
            log.info("🎉 [{}] 存储事件数: {}", taskId, storedEventIds.size());
            log.info("🎉 [{}] 总耗时: {}ms ({}秒)", taskId, totalTime, totalTime / 1000.0);
            log.info("🎉 [{}] 平均每个事件处理时间: {}ms", taskId,
                    storedEventIds.size() > 0 ? totalTime / storedEventIds.size() : 0);
            log.info("🎉 [{}] ========== 重构版时间线生成完成 ==========", taskId);

        } catch (Exception e) {
            long errorTime = System.currentTimeMillis() - startTimestamp;
            log.error("❌ [{}] ========== 时间线生成失败 ==========", taskId);
            log.error("❌ [{}] 时间线ID: {}", taskId, timelineId);
            log.error("❌ [{}] 失败原因: {}", taskId, e.getMessage());
            log.error("❌ [{}] 失败前耗时: {}ms", taskId, errorTime);
            log.error("❌ [{}] 异常堆栈:", taskId, e);
            log.error("❌ [{}] ========== 时间线生成失败结束 ==========", taskId);

            timelineService.failGeneration(timelineId, e.getMessage());
        }
    }

    /**
     * 使用动态提示词的时间线生成（新增方法）
     * 
     * @param timelineId 时间线ID
     * @param request    时间线生成请求
     */
    public void generateTimelineWithDynamicPrompt(Long timelineId, TimelineGenerateRequest request) {

        log.info("开始动态提示词时间线生成: name={}, description={}",
                request.getName(), request.getDescription());

        try {
            // 阶段1：动态提示词事件检索 (0-40%)
            timelineService.updateGenerationProgress(timelineId, 10, 0, 0, "正在生成动态提示词...");

            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);

            if (CollectionUtils.isEmpty(events)) {
                throw new RuntimeException("动态提示词未检索到相关事件");
            }

            timelineService.updateGenerationProgress(timelineId, 40, events.size(), 0,
                    String.format("检索到 %d 个事件，开始验证...", events.size()));

            // 阶段2：事件验证 (40-60%)
            List<EventData> validatedEvents = performEventValidation(timelineId, events);

            // 阶段3：事件存储 (60-75%)
            List<Long> storedEventIds = performEventStorage(timelineId, validatedEvents);

            // 阶段4：时间线编制 (75-90%)
            performTimelineCompilation(timelineId, storedEventIds, validatedEvents);

            // 阶段5：完成处理 (90-100%)
            finalizeTimelineGeneration(timelineId, storedEventIds.size(), validatedEvents.size());

            log.info("动态提示词时间线生成完成: name={}, eventCount={}, validatedCount={}",
                    request.getName(), events.size(), validatedEvents.size());

        } catch (Exception e) {
            log.error("动态提示词时间线生成失败", e);
            timelineService.failGeneration(timelineId, e.getMessage());
        }
    }

    /**
     * 构建时间线生成请求
     */
    private TimelineGenerateRequest buildTimelineRequest(Long timelineId, List<Long> regionIds,
            LocalDateTime startTime, LocalDateTime endTime) {
        log.info("🔍 [调试] 构建时间线请求: timelineId={}, regionIds={}, startTime={}, endTime={}",
                timelineId, regionIds, startTime, endTime);

        TimelineGenerateRequest request = new TimelineGenerateRequest();
        request.setRegionIds(regionIds);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        // 从数据库获取时间线基本信息
        try {
            log.info("🔍 [调试] 开始获取时间线信息: timelineId={}", timelineId);
            Map<String, Object> timelineDetail = timelineService.getTimelineDetail(timelineId);
            log.info("🔍 [调试] 获取到的时间线详情: {}", timelineDetail);

            if (timelineDetail != null && timelineDetail.get("timeline") != null) {
                // 从timeline键下获取实际的时间线信息
                @SuppressWarnings("unchecked")
                Map<String, Object> timeline = (Map<String, Object>) timelineDetail.get("timeline");

                String name = (String) timeline.get("name");
                String description = (String) timeline.get("description");

                log.info("🔍 [调试] 解析的时间线信息: name='{}', description='{}'", name, description);

                request.setName(name != null ? name : "时间线_" + timelineId);
                request.setDescription(description != null ? description : "基于地区和时间范围生成的时间线");

                log.info("🔍 [调试] 设置到请求中的信息: name='{}', description='{}'",
                        request.getName(), request.getDescription());
            } else {
                log.warn("🔍 [调试] 未找到时间线信息，使用默认值: timelineId={}", timelineId);
                request.setName("时间线_" + timelineId);
                request.setDescription("基于地区和时间范围生成的时间线");
            }
        } catch (Exception e) {
            log.error("🔍 [调试] 获取时间线信息失败，使用默认值: timelineId={}", timelineId, e);
            request.setName("默认时间线");
            request.setDescription("默认描述");
        }

        return request;
    }

    /**
     * 获取地区信息
     */
    private List<Region> getRegionsInfo(List<Long> regionIds) {
        List<Region> regions = new ArrayList<>();

        if (CollectionUtils.isEmpty(regionIds)) {
            log.warn("地区ID列表为空，使用默认地区");
            Region defaultRegion = getDefaultRegion();
            regions.add(defaultRegion);
            return regions;
        }

        for (Long regionId : regionIds) {
            Region region = regionMapper.selectById(regionId);
            if (region != null) {
                regions.add(region);
            } else {
                log.warn("未找到ID为{}的地区信息", regionId);
            }
        }

        // 如果没有找到任何有效地区，使用默认地区
        if (regions.isEmpty()) {
            log.warn("未找到任何有效的地区信息，将使用默认地区");
            Region defaultRegion = getDefaultRegion();
            regions.add(defaultRegion);
        }

        return regions;
    }

    /**
     * 获取默认地区
     */
    private Region getDefaultRegion() {
        // 尝试获取ID为1的地区作为默认地区
        Region defaultRegion = regionMapper.selectById(1L);
        if (defaultRegion != null) {
            log.info("使用默认地区: {}", defaultRegion.getName());
            return defaultRegion;
        }

        // 如果默认地区也不存在，则创建一个临时地区对象
        Region tempRegion = new Region();
        tempRegion.setId(1L);
        tempRegion.setName("全球");
        tempRegion.setType("CUSTOM");
        tempRegion.setCreatedAt(LocalDateTime.now());
        tempRegion.setUpdatedAt(LocalDateTime.now());
        log.info("使用临时地区: {}", tempRegion.getName());

        return tempRegion;
    }

    /**
     * 执行智能事件检索（集成时间段分割和批量API调用）
     */
    private List<EventData> performIntelligentEventRetrieval(Long timelineId, TimelineGenerateRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("🔍 [{}] 开始智能事件检索: timelineId={}, startTime={}, endTime={}",
                requestId, timelineId, request.getStartTime(), request.getEndTime());

        try {
            // 阶段1: 检查是否需要时间段分割
            timelineService.updateGenerationProgress(timelineId, 16, 0, 0, "正在分析时间跨度...");

            boolean needsSegmentation = timeSegmentationService.needsSegmentation(
                    request.getStartTime(), request.getEndTime());

            if (needsSegmentation) {
                log.info("🔍 [{}] 时间跨度较大，启用时间段分割处理", requestId);
                return performSegmentedEventRetrieval(timelineId, request, requestId);
            } else {
                log.info("🔍 [{}] 时间跨度适中，使用单次API调用", requestId);
                return performSingleEventRetrieval(timelineId, request, requestId);
            }

        } catch (Exception e) {
            log.error("🔍 [{}] 智能事件检索失败: {}", requestId, e.getMessage(), e);

            // 异常情况下的备用数据机制
            try {
                log.info("🔍 [{}] 启用异常情况备用数据机制", requestId);
                return generateFallbackEvents(timelineId, request, requestId);
            } catch (Exception fallbackException) {
                log.error("🔍 [{}] 备用数据生成也失败: {}", requestId, fallbackException.getMessage());
                throw new RuntimeException("智能事件检索和备用数据生成都失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 执行分段事件检索（大时间跨度）
     */
    private List<EventData> performSegmentedEventRetrieval(Long timelineId, TimelineGenerateRequest request,
            String requestId) {
        log.info("🔍 [{}] 开始分段事件检索", requestId);

        try {
            // 阶段1: 时间段分割
            timelineService.updateGenerationProgress(timelineId, 18, 0, 0, "正在分割时间段...");

            List<TimeSegment> timeSegments = timeSegmentationService.segmentTimeRange(
                    request.getStartTime(), request.getEndTime(), 30); // 默认30天为一个时间段

            log.info("🔍 [{}] 时间段分割完成，共分割为 {} 个时间段", requestId, timeSegments.size());

            // 阶段2: 批量获取事件（带进度更新）
            timelineService.updateGenerationProgress(timelineId, 20, 0, 0,
                    String.format("正在批量处理 %d 个时间段...", timeSegments.size()));

            List<EventData> allEvents = fetchEventsBatchWithProgress(timelineId, timeSegments, request, requestId);

            timelineService.updateGenerationProgress(timelineId, 30, allEvents.size(), 0,
                    String.format("批量检索完成，共获得 %d 个事件", allEvents.size()));

            // 阶段3: 事件合并和去重
            timelineService.updateGenerationProgress(timelineId, 32, allEvents.size(), 0, "正在合并和去重事件...");

            List<EventData> mergedEvents = mergeAndDeduplicateEvents(allEvents, requestId);

            // 阶段4: 智能备用数据机制
            List<EventData> enhancedEvents = ensureSufficientEvents(
                    timelineId, mergedEvents, request, requestId);

            // 阶段5: 最终去重和质量优化
            timelineService.updateGenerationProgress(timelineId, 35, enhancedEvents.size(), 0, "正在进行最终去重...");

            List<EventData> finalEvents = eventStorageService.deduplicateEvents(enhancedEvents);

            timelineService.updateGenerationProgress(timelineId, 40, finalEvents.size(), 0,
                    String.format("分段检索完成，最终获得 %d 个高质量事件", finalEvents.size()));

            log.info("🔍 [{}] 分段事件检索完成: 时间段数={}, 原始事件数={}, 合并后数={}, 最终数={}",
                    requestId, timeSegments.size(), allEvents.size(), mergedEvents.size(), finalEvents.size());

            return finalEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 分段事件检索失败，回退到单次调用: {}", requestId, e.getMessage());
            // 分段失败时回退到单次API调用
            return performSingleEventRetrieval(timelineId, request, requestId);
        }
    }

    /**
     * 批量获取事件并更新进度（增强版本，支持分段处理进度报告）
     */
    private List<EventData> fetchEventsBatchWithProgress(Long timelineId, List<TimeSegment> timeSegments,
            TimelineGenerateRequest request, String requestId) {
        log.info("🔍 [{}] 开始批量获取事件，时间段数: {}", requestId, timeSegments.size());

        try {
            List<EventData> allEvents = new ArrayList<>();
            int totalSegments = timeSegments.size();
            int processedSegments = 0;

            // 为每个时间段更新进度
            for (int i = 0; i < timeSegments.size(); i++) {
                TimeSegment segment = timeSegments.get(i);
                processedSegments++;

                // 计算当前进度（20-30%之间）
                int currentProgress = 20 + (int) ((double) processedSegments / totalSegments * 10);

                timelineService.updateGenerationProgress(timelineId, currentProgress, allEvents.size(), 0,
                        String.format("正在处理第 %d/%d 个时间段 (%s - %s)...",
                                processedSegments, totalSegments,
                                segment.getStartTime().toLocalDate(),
                                segment.getEndTime().toLocalDate()));

                log.info("🔍 [{}] 处理时间段 {}/{}: {} - {}",
                        requestId, processedSegments, totalSegments,
                        segment.getStartTime(), segment.getEndTime());

                try {
                    // 调用TimeSegmentationService获取单个时间段的事件
                    List<EventData> segmentEvents = timeSegmentationService.fetchEventsForSegment(segment, request);

                    if (segmentEvents != null && !segmentEvents.isEmpty()) {
                        allEvents.addAll(segmentEvents);
                        log.info("🔍 [{}] 时间段 {}/{} 获得 {} 个事件",
                                requestId, processedSegments, totalSegments, segmentEvents.size());
                    } else {
                        log.warn("🔍 [{}] 时间段 {}/{} 未获得任何事件",
                                requestId, processedSegments, totalSegments);
                    }

                } catch (Exception segmentException) {
                    log.error("🔍 [{}] 时间段 {}/{} 处理失败: {}",
                            requestId, processedSegments, totalSegments, segmentException.getMessage());
                    // 继续处理下一个时间段，不中断整个流程
                }

                // 短暂延迟以避免API调用过于频繁
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("🔍 [{}] 批量获取事件完成: 处理时间段数={}, 总事件数={}",
                    requestId, processedSegments, allEvents.size());

            return allEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 批量获取事件失败: {}", requestId, e.getMessage(), e);
            // 如果批量处理失败，回退到TimeSegmentationService的原始方法
            return timeSegmentationService.fetchEventsBatch(timeSegments, request);
        }
    }

    /**
     * 执行单次事件检索（小时间跨度）
     */
    private List<EventData> performSingleEventRetrieval(Long timelineId, TimelineGenerateRequest request,
            String requestId) {
        log.info("🔍 [{}] 开始单次事件检索", requestId);

        try {
            // 阶段1: 使用增强API管理器选择最优API
            timelineService.updateGenerationProgress(timelineId, 18, 0, 0, "正在选择最优API配置...");

            DynamicApiConfigManager.ApiConfig apiConfig = enhancedApiCallManager.selectOptimalApi(
                    request.getStartTime(), request.getEndTime());

            String apiType = apiConfig.isSupportsWebSearch() ? "火山引擎联网搜索API" : "DeepSeek官方API";
            log.info("🔍 [{}] 选择的API配置: type={}, model={}, supportsWebSearch={}",
                    requestId, apiType, apiConfig.getModel(), apiConfig.isSupportsWebSearch());

            // 阶段2: 构建智能提示词
            timelineService.updateGenerationProgress(timelineId, 20, 0, 0, "正在构建智能提示词...");
            String prompt = buildIntelligentPrompt(request);
            log.debug("🔍 [{}] 构建的提示词长度: {}", requestId, prompt.length());

            // 阶段3: 使用增强API管理器进行调用
            timelineService.updateGenerationProgress(timelineId, 22, 0, 0,
                    String.format("正在使用%s检索事件...", apiType));

            String apiResponse = enhancedApiCallManager.callWithFallback(
                    prompt, request.getStartTime(), request.getEndTime(), requestId);

            if (apiResponse == null || apiResponse.trim().isEmpty()) {
                log.warn("🔍 [{}] API调用返回空响应，启用备用数据机制", requestId);
                return handleEmptyApiResponse(timelineId, request, requestId);
            }

            log.info("🔍 [{}] API调用成功，响应长度: {}", requestId, apiResponse.length());

            // 阶段4: 使用多策略解析增强器解析响应
            timelineService.updateGenerationProgress(timelineId, 25, 0, 0, "正在解析API响应...");

            List<EventData> parsedEvents = parseEventsFromResponse(apiResponse, apiType, requestId);

            timelineService.updateGenerationProgress(timelineId, 30, parsedEvents.size(), 0,
                    String.format("解析出 %d 个候选事件", parsedEvents.size()));

            // 阶段5: 智能备用数据机制
            List<EventData> enhancedEvents = ensureSufficientEvents(
                    timelineId, parsedEvents, request, requestId);

            // 阶段6: 事件去重和质量优化
            timelineService.updateGenerationProgress(timelineId, 35, enhancedEvents.size(), 0, "正在进行事件去重...");

            List<EventData> deduplicatedEvents = eventStorageService.deduplicateEvents(enhancedEvents);

            timelineService.updateGenerationProgress(timelineId, 40, deduplicatedEvents.size(), 0,
                    String.format("单次检索完成，获得 %d 个高质量事件", deduplicatedEvents.size()));

            log.info("🔍 [{}] 单次事件检索完成: apiType={}, 原始解析数={}, 增强后数={}, 最终数={}",
                    requestId, apiType, parsedEvents.size(), enhancedEvents.size(), deduplicatedEvents.size());

            return deduplicatedEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 单次事件检索失败: {}", requestId, e.getMessage(), e);
            throw e; // 重新抛出异常，由上层处理
        }
    }

    /**
     * 合并和去重事件（用于分段检索结果）
     */
    private List<EventData> mergeAndDeduplicateEvents(List<EventData> allEvents, String requestId) {
        log.info("🔍 [{}] 开始合并和去重事件，原始事件数: {}", requestId, allEvents.size());

        try {
            // 按时间排序
            List<EventData> sortedEvents = allEvents.stream()
                    .sorted((e1, e2) -> {
                        if (e1.getEventTime() == null && e2.getEventTime() == null)
                            return 0;
                        if (e1.getEventTime() == null)
                            return 1;
                        if (e2.getEventTime() == null)
                            return -1;
                        return e1.getEventTime().compareTo(e2.getEventTime());
                    })
                    .collect(Collectors.toList());

            // 使用EventStorageService的去重功能
            List<EventData> deduplicatedEvents = eventStorageService.deduplicateEvents(sortedEvents);

            log.info("🔍 [{}] 事件合并和去重完成: 原始数={}, 去重后数={}",
                    requestId, allEvents.size(), deduplicatedEvents.size());

            return deduplicatedEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 事件合并和去重失败，返回原始事件列表: {}", requestId, e.getMessage());
            return allEvents;
        }
    }

    /**
     * 执行事件验证（已取消可信度判断）
     */
    private List<EventData> performEventValidation(Long timelineId, List<EventData> events) {
        log.info("开始事件验证: eventCount={}", events.size());

        try {
            timelineService.updateGenerationProgress(timelineId, 45, events.size(), 0, "正在处理事件数据...");

            // 直接返回所有事件，不进行可信度验证
            // 为所有事件设置默认的可信度评分
            events.forEach(event -> {
                if (event.getCredibilityScore() == null) {
                    event.setCredibilityScore(0.8); // 设置默认可信度
                }
            });

            timelineService.updateGenerationProgress(timelineId, 60, events.size(), 0,
                    String.format("处理完成 %d 个事件", events.size()));

            log.info("事件验证完成（已跳过可信度判断）: 事件数={}", events.size());

            return events;

        } catch (Exception e) {
            log.error("事件处理失败: {}", e.getMessage(), e);
            // 处理失败时仍返回原始事件列表
            events.forEach(event -> {
                if (event.getCredibilityScore() == null) {
                    event.setCredibilityScore(0.8); // 设置默认可信度
                }
            });
            return events;
        }
    }

    /**
     * 执行事件存储
     */
    private List<Long> performEventStorage(Long timelineId, List<EventData> validatedEvents) {
        log.info("开始事件存储: eventCount={}", validatedEvents.size());

        try {
            timelineService.updateGenerationProgress(timelineId, 65, validatedEvents.size(), 0, "正在存储验证事件...");

            // 批量存储事件
            List<Long> storedEventIds = eventStorageService.storeEventsBatch(validatedEvents);

            timelineService.updateGenerationProgress(timelineId, 75, storedEventIds.size(), 0,
                    String.format("成功存储 %d 个事件", storedEventIds.size()));

            log.info("事件存储完成: 验证事件数={}, 存储成功事件数={}",
                    validatedEvents.size(), storedEventIds.size());

            return storedEventIds;

        } catch (Exception e) {
            log.error("事件存储失败: {}", e.getMessage(), e);
            throw new RuntimeException("事件存储失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行时间线编制（支持基于时间的API动态选择）
     */
    private void performTimelineCompilation(Long timelineId, List<Long> eventIds, List<EventData> events) {
        log.info("开始时间线编制: timelineId={}, eventCount={}", timelineId, eventIds.size());

        try {
            // 获取时间范围信息用于API选择
            LocalDateTime startTime = getEarliestEventTime(events);
            LocalDateTime endTime = getLatestEventTime(events);

            boolean useWebSearch = dynamicDeepSeekService.shouldUseWebSearch(startTime, endTime);
            String apiType = useWebSearch ? "火山引擎联网搜索API" : "DeepSeek官方API";

            timelineService.updateGenerationProgress(timelineId, 80, eventIds.size(), 0,
                    String.format("正在使用%s分析事件关联关系...", apiType));

            // 分析事件关联关系（使用基于时间的API选择）
            List<Map<String, Object>> eventMaps = convertEventsToMaps(events);
            List<Map<String, Object>> relations = dynamicDeepSeekService.analyzeEventRelationsWithTimeBasedAPI(
                    eventMaps, startTime, endTime);

            timelineService.updateGenerationProgress(timelineId, 85, eventIds.size(), relations.size(),
                    String.format("使用%s组织时间线结构...", apiType));

            // 组织时间线（使用基于时间的API选择）
            List<Map<String, Object>> timelines = dynamicDeepSeekService.organizeTimelinesWithTimeBasedAPI(
                    eventMaps, relations, startTime, endTime);

            timelineService.updateGenerationProgress(timelineId, 90, eventIds.size(), relations.size(), "正在建立事件关联...");

            // 批量插入时间线事件关联
            if (!CollectionUtils.isEmpty(eventIds)) {
                log.info("🔗 开始批量插入时间线事件关联: timelineId={}, eventIds={}", timelineId, eventIds);
                try {
                    int insertedCount = timelineEventMapper.batchInsert(timelineId, eventIds);
                    log.info("✅ 时间线事件关联插入成功: timelineId={}, 插入数量={}", timelineId, insertedCount);
                } catch (Exception e) {
                    log.error("❌ 时间线事件关联插入失败: timelineId={}, eventIds={}, error={}",
                            timelineId, eventIds, e.getMessage(), e);
                    throw e;
                }
            } else {
                log.warn("⚠️  事件ID列表为空，无法建立时间线事件关联: timelineId={}", timelineId);
            }

            log.info("时间线编制完成: timelineId={}, apiType={}, eventCount={}, relationCount={}, timelineCount={}",
                    timelineId, apiType, eventIds.size(), relations.size(), timelines.size());

        } catch (Exception e) {
            log.error("时间线编制失败: {}", e.getMessage(), e);
            // 编制失败时仍然保存事件关联，但不建立复杂关系
            if (!CollectionUtils.isEmpty(eventIds)) {
                try {
                    timelineEventMapper.batchInsert(timelineId, eventIds);
                    log.info("已保存基础事件关联，但未建立复杂关系");
                } catch (Exception ex) {
                    log.error("保存基础事件关联也失败: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * 完成时间线生成
     */
    private void finalizeTimelineGeneration(Long timelineId, int eventCount, int validatedCount) {
        try {
            timelineService.updateGenerationProgress(timelineId, 95, eventCount, 0, "正在完成时间线生成...");

            // 模拟短暂延迟以显示进度
            simulateDelay(1);

            // 完成时间线生成
            timelineService.completeGeneration(timelineId, eventCount, validatedCount);

            timelineService.updateGenerationProgress(timelineId, 100, eventCount, 0, "时间线生成完成");

            log.info("时间线生成最终完成: timelineId={}, eventCount={}, validatedCount={}",
                    timelineId, eventCount, validatedCount);

        } catch (Exception e) {
            log.error("完成时间线生成时出错: {}", e.getMessage(), e);
            throw new RuntimeException("完成时间线生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将EventData列表转换为Map列表（兼容原有接口）
     */
    private List<Map<String, Object>> convertEventsToMaps(List<EventData> events) {
        return events.stream().map(event -> {
            Map<String, Object> eventMap = new java.util.HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("title", event.getTitle());
            eventMap.put("description", event.getDescription());
            eventMap.put("eventTime", event.getEventTime());
            eventMap.put("location", event.getLocation());
            eventMap.put("subject", event.getSubject());
            eventMap.put("object", event.getObject());
            eventMap.put("eventType", event.getEventType());
            eventMap.put("credibilityScore", event.getCredibilityScore());
            eventMap.put("keywords", event.getKeywords());
            eventMap.put("sources", event.getSources());
            return eventMap;
        }).collect(Collectors.toList());
    }

    /**
     * 获取事件列表中最早的事件时间
     */
    private LocalDateTime getEarliestEventTime(List<EventData> events) {
        if (CollectionUtils.isEmpty(events)) {
            return LocalDateTime.now().minusMonths(1);
        }

        return events.stream()
                .map(EventData::getEventTime)
                .filter(time -> time != null)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusMonths(1));
    }

    /**
     * 获取事件列表中最晚的事件时间
     */
    private LocalDateTime getLatestEventTime(List<EventData> events) {
        if (CollectionUtils.isEmpty(events)) {
            return LocalDateTime.now();
        }

        return events.stream()
                .map(EventData::getEventTime)
                .filter(time -> time != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    /**
     * 构建智能提示词
     */
    private String buildIntelligentPrompt(TimelineGenerateRequest request) {
        StringBuilder promptBuilder = new StringBuilder();

        // 获取地区信息
        List<Region> regions = getRegionsInfo(request.getRegionIds());
        String regionNames = regions.stream()
                .map(Region::getName)
                .collect(Collectors.joining("、"));

        // 构建详细的提示词
        promptBuilder.append("你是一个专业的历史事件分析师，请根据以下具体要求生成相关的历史事件数据：\n\n");

        // 时间线主题信息
        promptBuilder.append("【时间线主题】\n");
        if (request.getName() != null && !request.getName().isEmpty()) {
            promptBuilder.append("主题名称：").append(request.getName()).append("\n");
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            promptBuilder.append("主题描述：").append(request.getDescription()).append("\n");
        }

        // 时间范围
        promptBuilder.append("\n【时间范围】\n");
        if (request.getStartTime() != null && request.getEndTime() != null) {
            promptBuilder.append("起始时间：").append(request.getStartTime().toLocalDate()).append("\n");
            promptBuilder.append("结束时间：").append(request.getEndTime().toLocalDate()).append("\n");

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    request.getStartTime().toLocalDate(),
                    request.getEndTime().toLocalDate());
            promptBuilder.append("时间跨度：").append(daysBetween).append("天\n");
        }

        // 地区信息
        promptBuilder.append("\n【涉及地区】\n");
        if (!regionNames.isEmpty()) {
            promptBuilder.append("重点关注地区：").append(regionNames).append("\n");
        } else {
            promptBuilder.append("重点关注地区：全球范围\n");
        }

        // 具体要求
        promptBuilder.append("\n【生成要求】\n");
        promptBuilder.append("1. 请生成15-20个与上述主题高度相关的真实历史事件\n");
        promptBuilder.append("2. 所有事件必须发生在指定的时间范围内\n");
        promptBuilder.append("3. 事件应该与指定地区密切相关\n");
        promptBuilder.append("4. 事件内容要与时间线主题紧密关联，不要生成无关事件\n");
        promptBuilder.append("5. 每个事件都要包含详细的背景信息和具体细节\n");
        promptBuilder.append("6. 事件时间要精确到具体日期\n");
        promptBuilder.append("7. 事件地点要具体到城市或地区\n");
        promptBuilder.append("8. 必须提供准确的经纬度坐标（latitude和longitude字段）\n");
        promptBuilder.append("9. 经纬度必须与事件发生地点完全匹配\n");

        // 输出格式
        promptBuilder.append("\n【输出格式】\n");
        promptBuilder.append("请严格按照以下JSON数组格式返回，不要添加任何其他文字说明：\n");
        promptBuilder.append("[\n");
        promptBuilder.append("  {\n");
        promptBuilder.append("    \"title\": \"事件标题\",\n");
        promptBuilder.append("    \"description\": \"详细的事件描述，包含背景、过程、影响等\",\n");
        promptBuilder.append("    \"eventTime\": \"2024-01-15T10:30:00\",\n");
        promptBuilder.append("    \"location\": \"具体地点（城市、地区）\",\n");
        promptBuilder.append("    \"latitude\": 39.9042,\n");
        promptBuilder.append("    \"longitude\": 116.4074,\n");
        promptBuilder.append("    \"eventType\": \"事件类型\",\n");
        promptBuilder.append("    \"subject\": \"事件主体\",\n");
        promptBuilder.append("    \"object\": \"事件客体\",\n");
        promptBuilder.append("    \"sources\": [\"相关新闻来源或参考资料\"]\n");
        promptBuilder.append("  }\n");
        promptBuilder.append("]\n");

        // 特别强调
        promptBuilder.append("\n【特别注意】\n");
        promptBuilder.append("- 必须确保所有事件都与\"").append(request.getName() != null ? request.getName() : "指定主题")
                .append("\"直接相关\n");
        promptBuilder.append("- 事件时间必须在")
                .append(request.getStartTime() != null ? request.getStartTime().toLocalDate() : "指定时间范围").append("到")
                .append(request.getEndTime() != null ? request.getEndTime().toLocalDate() : "指定时间范围").append("之间\n");
        promptBuilder.append("- 重点关注").append(regionNames.isEmpty() ? "全球范围" : regionNames).append("发生的相关事件\n");
        promptBuilder.append("- 只返回JSON数组，不要包含任何解释性文字\n");

        return promptBuilder.toString();
    }

    /**
     * 使用多策略解析增强器解析API响应
     */
    private List<EventData> parseEventsFromResponse(String apiResponse, String apiType, String requestId) {
        log.info("🔍 [{}] 开始解析API响应，类型: {}, 响应长度: {}", requestId, apiType, apiResponse.length());

        try {
            // 使用事件解析增强器进行多策略解析
            String requestSummary = String.format("时间线事件检索 - API类型: %s", apiType);
            List<EventData> events = eventParsingEnhancer.parseWithMultipleStrategies(
                    apiResponse, apiType, requestSummary);

            log.info("🔍 [{}] 多策略解析完成，解析出 {} 个事件", requestId, events.size());

            // 为解析出的事件设置默认值
            for (EventData event : events) {
                if (event.getCredibilityScore() == null) {
                    event.setCredibilityScore(0.8);
                }
                if (event.getEventType() == null || event.getEventType().isEmpty()) {
                    event.setEventType("综合");
                }
            }

            return events;

        } catch (Exception e) {
            log.error("🔍 [{}] 解析API响应失败: {}", requestId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 处理API调用返回空响应的情况
     */
    private List<EventData> handleEmptyApiResponse(Long timelineId, TimelineGenerateRequest request, String requestId) {
        log.warn("🔍 [{}] 处理空API响应，启用备用数据机制", requestId);

        timelineService.updateGenerationProgress(timelineId, 22, 0, 0, "API响应为空，启用备用数据机制...");

        try {
            // 尝试从数据库获取相似事件
            List<EventData> similarEvents = fallbackDataGenerator.getSimilarEventsFromDatabase(
                    request.getDescription(), request.getRegionIds());

            if (!similarEvents.isEmpty()) {
                log.info("🔍 [{}] 从数据库获取到 {} 个相似事件", requestId, similarEvents.size());
                return similarEvents;
            }

            // 如果数据库也没有相似事件，生成默认事件
            List<EventData> defaultEvents = fallbackDataGenerator.generateDefaultEvents(
                    request.getRegionIds(), request.getStartTime(), request.getEndTime());

            log.info("🔍 [{}] 生成了 {} 个默认事件", requestId, defaultEvents.size());
            return defaultEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 备用数据机制也失败: {}", requestId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 确保有足够的事件数据（使用动态配置）
     */
    private List<EventData> ensureSufficientEvents(Long timelineId, List<EventData> parsedEvents,
            TimelineGenerateRequest request, String requestId) {
        // 从动态配置获取事件数量设置
        DynamicSystemConfig.EventCountConfig eventCountConfig = dynamicSystemConfig.getEventCount();
        final int MIN_EVENT_COUNT = eventCountConfig.getMinimumEventCount();
        final int TARGET_EVENT_COUNT = eventCountConfig.getTargetEventCount();
        final int MAX_EVENT_COUNT = eventCountConfig.getMaximumEventCount();

        log.info("🔍 [{}] 检查事件数量充足性，当前: {}, 最少需要: {}, 目标: {}, 最大: {}",
                requestId, parsedEvents.size(), MIN_EVENT_COUNT, TARGET_EVENT_COUNT, MAX_EVENT_COUNT);

        // 如果超过最大数量，进行裁剪
        if (parsedEvents.size() > MAX_EVENT_COUNT) {
            List<EventData> trimmedEvents = new ArrayList<>(parsedEvents.subList(0, MAX_EVENT_COUNT));
            log.info("🔍 [{}] 事件数量超过最大限制，已裁剪至: {}", requestId, MAX_EVENT_COUNT);
            return trimmedEvents;
        }

        // 如果达到最小数量要求，直接返回（不再强制补充到目标数量）
        if (parsedEvents.size() >= MIN_EVENT_COUNT) {
            log.info("🔍 [{}] 事件数量满足最小要求，无需补充。当前: {}, 最小要求: {}",
                    requestId, parsedEvents.size(), MIN_EVENT_COUNT);
            return parsedEvents;
        }

        // 只有在事件数量严重不足时才进行补充
        List<EventData> enhancedEvents = new ArrayList<>(parsedEvents);

        try {
            timelineService.updateGenerationProgress(timelineId, 32, parsedEvents.size(), 0,
                    "事件数量严重不足，正在最小化补充数据...");

            int needCount = MIN_EVENT_COUNT - parsedEvents.size();

            // 根据动态配置决定备用数据生成策略
            boolean enableSmartSupplement = eventCountConfig.isEnableSmartSupplement();
            double fallbackDataRatio = eventCountConfig.getFallbackDataRatio();

            log.warn("🔍 [{}] 事件数量严重不足（当前: {}, 最小要求: {}），启用最小化补充策略",
                    requestId, parsedEvents.size(), MIN_EVENT_COUNT);

            if (enableSmartSupplement && needCount > 0) {
                // 仅在启用智能补充时才生成少量备用数据
                int supplementCount = Math.min(needCount, (int) (needCount * fallbackDataRatio));
                if (supplementCount > 0) {
                    List<EventData> supplementEvents = fallbackDataGenerator.generateDefaultEvents(
                            request.getRegionIds(), request.getStartTime(), request.getEndTime());

                    // 只取需要的数量
                    List<EventData> limitedEvents = supplementEvents.stream()
                            .limit(supplementCount)
                            .collect(Collectors.toList());

                    enhancedEvents.addAll(limitedEvents);
                    log.info("🔍 [{}] 最小化补充了 {} 个事件", requestId, limitedEvents.size());
                }
            } else {
                log.info("🔍 [{}] 智能补充已禁用，保持原有事件数量: {}", requestId, parsedEvents.size());
            }

            timelineService.updateGenerationProgress(timelineId, 34, enhancedEvents.size(), 0,
                    String.format("数据补充完成，总计 %d 个事件", enhancedEvents.size()));

            log.info("🔍 [{}] 事件数量补充完成，原始: {}, 最终: {}",
                    requestId, parsedEvents.size(), enhancedEvents.size());

            return enhancedEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 事件数量补充失败: {}", requestId, e.getMessage(), e);
            return parsedEvents; // 返回原始事件列表
        }
    }

    /**
     * 生成备用事件数据（异常情况下使用）
     */
    private List<EventData> generateFallbackEvents(Long timelineId, TimelineGenerateRequest request, String requestId) {
        log.info("🔍 [{}] 生成异常情况备用事件数据", requestId);

        try {
            timelineService.updateGenerationProgress(timelineId, 25, 0, 0, "生成备用事件数据...");

            // 尝试多种备用数据生成策略
            List<EventData> fallbackEvents = new ArrayList<>();

            // 策略1: 生成默认事件
            List<EventData> defaultEvents = fallbackDataGenerator.generateDefaultEvents(
                    request.getRegionIds(), request.getStartTime(), request.getEndTime());
            fallbackEvents.addAll(defaultEvents);

            // 策略2: 生成通用事件
            if (fallbackEvents.size() < 10) {
                List<EventData> genericEvents = fallbackDataGenerator.generateGenericEvents(
                        10 - fallbackEvents.size());
                fallbackEvents.addAll(genericEvents);
            }

            // 策略3: 基于描述生成相关事件
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                List<EventData> relatedEvents = fallbackDataGenerator.getSimilarEventsFromDatabase(
                        request.getDescription(), request.getRegionIds());
                fallbackEvents.addAll(relatedEvents);
            }

            timelineService.updateGenerationProgress(timelineId, 35, fallbackEvents.size(), 0,
                    String.format("备用数据生成完成，共 %d 个事件", fallbackEvents.size()));

            log.info("🔍 [{}] 备用事件数据生成完成，共 {} 个事件", requestId, fallbackEvents.size());

            return fallbackEvents;

        } catch (Exception e) {
            log.error("🔍 [{}] 备用事件数据生成失败: {}", requestId, e.getMessage(), e);

            // 最后的保底措施：创建最基本的事件
            List<EventData> basicEvents = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                EventData event = new EventData();
                event.setTitle("系统生成事件 " + i);
                event.setDescription("这是系统在异常情况下生成的基础事件数据");
                event.setEventTime(LocalDateTime.now().minusDays(i));
                event.setEventType("系统");
                event.setCredibilityScore(0.5);
                basicEvents.add(event);
            }

            return basicEvents;
        }
    }

    /**
     * 模拟延迟
     * 
     * @param seconds 延迟秒数
     */
    private void simulateDelay(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}