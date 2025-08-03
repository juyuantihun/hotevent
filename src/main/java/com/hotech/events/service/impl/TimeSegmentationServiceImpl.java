package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimeSegment;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.service.TimeSegmentationService;
import com.hotech.events.service.EnhancedApiCallManager;
import com.hotech.events.service.EventService;
import com.hotech.events.util.EventDeduplicator;
import com.hotech.events.config.DynamicApiConfigManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 时间段分割服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSegmentationServiceImpl implements TimeSegmentationService {

    private final EnhancedApiCallManager apiCallManager;
    private final EventService eventService;
    private final EventDeduplicator eventDeduplicator;

    // 配置参数
    @Value("${timeline.segmentation.max-span-days:7}")
    private int defaultMaxSpanDays;

    @Value("${timeline.segmentation.min-events-per-segment:5}")
    private int minEventsPerSegment;

    @Value("${timeline.segmentation.max-segments:10}")
    private int maxSegments;

    @Value("${timeline.segmentation.parallel-processing:true}")
    private boolean enableParallelProcessing;

    @Value("${timeline.segmentation.expected-events-per-day:10}")
    private int defaultExpectedEventsPerDay;

    @Value("${timeline.segmentation.max-events-per-segment:50}")
    private int defaultMaxEventsPerSegment;

    // 线程池用于并发处理
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public List<TimeSegment> segmentTimeRange(LocalDateTime startTime, LocalDateTime endTime, int maxSpanDays) {
        log.info("开始分割时间范围: {} 到 {}, 最大跨度: {}天", startTime, endTime, maxSpanDays);

        // 参数验证
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        if (maxSpanDays <= 0) {
            maxSpanDays = defaultMaxSpanDays;
        }

        List<TimeSegment> segments = new ArrayList<>();
        LocalDateTime currentStart = startTime;
        int segmentIndex = 0;

        while (currentStart.isBefore(endTime) && segmentIndex < maxSegments) {
            // 计算当前时间段的结束时间
            LocalDateTime currentEnd = currentStart.plusDays(maxSpanDays - 1)
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59);

            // 确保不超过总的结束时间
            if (currentEnd.isAfter(endTime)) {
                currentEnd = endTime;
            }

            // 创建时间段
            TimeSegment segment = TimeSegment.builder()
                    .startTime(currentStart)
                    .endTime(currentEnd)
                    .segmentIndex(segmentIndex)
                    .segmentId(generateSegmentId(segmentIndex, currentStart, currentEnd))
                    .expectedEventCount(calculateExpectedEventCount(currentStart, currentEnd))
                    .isLastSegment(currentEnd.equals(endTime) || currentEnd.isAfter(endTime.minusSeconds(1)))
                    .build();

            segment.calculateSpanDays();
            segments.add(segment);

            log.debug("创建时间段: {}", segment.getDescription());

            // 移动到下一个时间段
            currentStart = currentEnd.plusSeconds(1);
            segmentIndex++;
        }

        log.info("时间段分割完成，共生成 {} 个时间段", segments.size());
        return segments;
    }

    @Override
    public List<TimeSegment> intelligentSegmentTimeRange(LocalDateTime startTime, LocalDateTime endTime,
            int maxSpanDays, int expectedEventsPerDay,
            int maxEventsPerSegment) {
        log.info("开始智能分割时间范围: {} 到 {}", startTime, endTime);

        // 计算总天数
        long totalDays = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;

        // 计算预期总事件数
        int totalExpectedEvents = (int) (totalDays * expectedEventsPerDay);

        // 根据事件数量调整时间段大小
        int adjustedMaxSpanDays = maxSpanDays;
        if (totalExpectedEvents > maxEventsPerSegment) {
            // 如果预期事件数过多，减少时间段跨度
            adjustedMaxSpanDays = Math.max(1, maxEventsPerSegment / expectedEventsPerDay);
        }

        log.info("智能分割参数 - 总天数: {}, 预期总事件数: {}, 调整后最大跨度: {}天",
                totalDays, totalExpectedEvents, adjustedMaxSpanDays);

        return segmentTimeRange(startTime, endTime, adjustedMaxSpanDays);
    }

    @Override
    public List<EventData> fetchEventsBatch(List<TimeSegment> segments, TimelineGenerateRequest request) {
        log.info("开始批量获取 {} 个时间段的事件", segments.size());

        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }

        // 验证时间段
        if (!validateTimeSegments(segments)) {
            throw new IllegalArgumentException("时间段列表验证失败");
        }

        List<List<EventData>> eventLists = new ArrayList<>();

        if (enableParallelProcessing && segments.size() > 1) {
            // 并发处理
            eventLists = fetchEventsParallel(segments, request);
        } else {
            // 串行处理
            eventLists = fetchEventsSequential(segments, request);
        }

        // 合并事件列表
        List<EventData> mergedEvents = mergeEventLists(eventLists);

        log.info("批量获取完成，共获得 {} 个事件", mergedEvents.size());
        return mergedEvents;
    }

    @Override
    public boolean needsSegmentation(LocalDateTime startTime, LocalDateTime endTime) {
        return needsSegmentation(startTime, endTime, defaultMaxSpanDays);
    }

    @Override
    public boolean needsSegmentation(LocalDateTime startTime, LocalDateTime endTime, int maxSpanDays) {
        if (startTime == null || endTime == null) {
            return false;
        }

        long daysBetween = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;
        return daysBetween > maxSpanDays;
    }

    @Override
    public boolean validateTimeSegments(List<TimeSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return false;
        }

        // 检查每个时间段的有效性
        for (TimeSegment segment : segments) {
            if (!segment.isValid()) {
                log.warn("无效的时间段: {}", segment.getDescription());
                return false;
            }
        }

        // 检查时间段的连续性（按索引排序后检查）
        List<TimeSegment> sortedSegments = segments.stream()
                .sorted(Comparator.comparingInt(TimeSegment::getSegmentIndex))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedSegments.size() - 1; i++) {
            TimeSegment current = sortedSegments.get(i);
            TimeSegment next = sortedSegments.get(i + 1);

            // 检查时间段之间是否有间隙或重叠
            if (current.getEndTime().plusSeconds(1).isBefore(next.getStartTime()) ||
                    current.getEndTime().isAfter(next.getStartTime())) {
                log.warn("时间段不连续: {} 和 {}", current.getDescription(), next.getDescription());
                return false;
            }
        }

        return true;
    }

    @Override
    public List<EventData> mergeEventLists(List<List<EventData>> eventLists) {
        if (eventLists == null || eventLists.isEmpty()) {
            return new ArrayList<>();
        }

        // 合并所有事件
        List<EventData> allEvents = eventLists.stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 去重
        List<EventData> deduplicatedEvents = eventDeduplicator.deduplicateEvents(allEvents);

        // 按时间排序
        deduplicatedEvents.sort(Comparator.comparing(EventData::getEventTime));

        log.info("事件合并完成 - 原始: {}, 去重后: {}", allEvents.size(), deduplicatedEvents.size());

        return deduplicatedEvents;
    }

    @Override
    public int getDefaultMaxSpanDays() {
        return defaultMaxSpanDays;
    }

    @Override
    public String getSegmentationStats(List<TimeSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return "无时间段数据";
        }

        int totalSegments = segments.size();
        long totalDays = segments.stream()
                .mapToLong(TimeSegment::calculateSpanDays)
                .sum();
        int totalExpectedEvents = segments.stream()
                .mapToInt(TimeSegment::getExpectedEventCount)
                .sum();

        return String.format("分割统计 - 时间段数: %d, 总天数: %d, 预期事件数: %d",
                totalSegments, totalDays, totalExpectedEvents);
    }

    /**
     * 生成时间段ID
     */
    private String generateSegmentId(int index, LocalDateTime start, LocalDateTime end) {
        return String.format("segment_%d_%s_%s",
                index,
                start.toLocalDate().toString().replace("-", ""),
                end.toLocalDate().toString().replace("-", ""));
    }

    /**
     * 计算预期事件数量
     */
    private int calculateExpectedEventCount(LocalDateTime start, LocalDateTime end) {
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        return (int) (days * defaultExpectedEventsPerDay);
    }

    /**
     * 并发获取事件
     */
    private List<List<EventData>> fetchEventsParallel(List<TimeSegment> segments, TimelineGenerateRequest request) {
        log.info("使用并发模式获取事件");

        List<CompletableFuture<List<EventData>>> futures = segments.stream()
                .map(segment -> CompletableFuture.supplyAsync(() -> fetchEventsForSegment(segment, request),
                        executorService))
                .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.get(); // 等待完成

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("并发获取事件失败", e);
            throw new RuntimeException("并发获取事件失败", e);
        }
    }

    /**
     * 串行获取事件
     */
    private List<List<EventData>> fetchEventsSequential(List<TimeSegment> segments, TimelineGenerateRequest request) {
        log.info("使用串行模式获取事件");

        return segments.stream()
                .map(segment -> fetchEventsForSegment(segment, request))
                .collect(Collectors.toList());
    }

    /**
     * 为单个时间段获取事件
     */
    @Override
    public List<EventData> fetchEventsForSegment(TimeSegment segment, TimelineGenerateRequest request) {
        log.debug("获取时间段事件: {}", segment.getDescription());

        try {
            // 创建针对该时间段的请求
            TimelineGenerateRequest segmentRequest = createSegmentRequest(request, segment);

            // 选择最优API配置
            DynamicApiConfigManager.ApiConfig apiConfig = apiCallManager.selectOptimalApi(segment.getStartTime(), segment.getEndTime());

            // 构建事件获取提示词
            String prompt = buildEventFetchPrompt(segmentRequest, segment);

            // 使用大Token API调用获取更完整的事件数据
            String apiResponse = apiCallManager.callApiWithLargeTokens(
                    apiConfig,
                    prompt,
                    4000, // 使用4000 tokens以获取更多事件
                    segment);

            // 如果大Token调用失败，回退到普通调用
            if (apiResponse == null || apiResponse.trim().isEmpty()) {
                log.warn("大Token API调用失败，回退到普通调用: segmentId={}", segment.getSegmentId());
                apiResponse = apiCallManager.callWithFallback(
                        prompt,
                        segment.getStartTime(),
                        segment.getEndTime());
            }

            // 解析API响应为事件列表
            List<EventData> events = parseApiResponseToEvents(apiResponse, segment);

            // 验证事件数据质量
            events = validateAndFilterEvents(events, segment);

            log.debug("时间段 {} 获取到 {} 个有效事件", segment.getSegmentId(), events.size());
            return events;

        } catch (Exception e) {
            log.error("获取时间段 {} 的事件失败", segment.getSegmentId(), e);
            return new ArrayList<>(); // 返回空列表而不是抛出异常，保证其他时间段能正常处理
        }
    }

    /**
     * 为时间段创建请求对象
     */
    private TimelineGenerateRequest createSegmentRequest(TimelineGenerateRequest originalRequest, TimeSegment segment) {
        TimelineGenerateRequest segmentRequest = new TimelineGenerateRequest();
        segmentRequest.setName(originalRequest.getName() + "_" + segment.getSegmentId());
        segmentRequest.setDescription("时间段分割请求: " + segment.getDescription());
        segmentRequest.setRegionIds(originalRequest.getRegionIds());
        segmentRequest.setStartTime(segment.getStartTime());
        segmentRequest.setEndTime(segment.getEndTime());
        segmentRequest.setEnableDeduplication(originalRequest.getEnableDeduplication());
        segmentRequest.setEnableDictionary(originalRequest.getEnableDictionary());
        segmentRequest.setEnableRelationAnalysis(originalRequest.getEnableRelationAnalysis());

        return segmentRequest;
    }

    /**
     * 构建事件获取提示词
     */
    private String buildEventFetchPrompt(TimelineGenerateRequest request, TimeSegment segment) {
        StringBuilder prompt = new StringBuilder();

        // 添加时间线基本信息
        prompt.append("=== 时间线生成任务 ===\n");
        prompt.append("时间线名称：").append(request.getName() != null ? request.getName() : "未指定时间线名称").append("\n");
        prompt.append("时间线描述：").append(request.getDescription() != null ? request.getDescription() : "未指定时间线描述")
                .append("\n");
        prompt.append("\n");

        // 添加时间段信息
        prompt.append("=== 当前时间段信息 ===\n");
        prompt.append("时间范围：").append(segment.getStartTime().toLocalDate()).append(" 至 ")
                .append(segment.getEndTime().toLocalDate()).append("\n");
        prompt.append("具体时间：").append(segment.getStartTime()).append(" 至 ").append(segment.getEndTime()).append("\n");

        // 将地区ID转换为中文名称
        prompt.append("目标地区：");
        if (request.getRegionIds() != null && !request.getRegionIds().isEmpty()) {
            List<String> regionNames = convertRegionIdsToNames(request.getRegionIds());
            prompt.append(String.join("、", regionNames));
        } else {
            prompt.append("全球");
        }
        prompt.append("\n");

        prompt.append("预期事件数量：").append(segment.getExpectedEventCount()).append("个\n");
        prompt.append("\n");

        // 添加任务要求
        prompt.append("=== 任务要求 ===\n");
        prompt.append("请为上述时间线主题，在指定的时间段和地区范围内，搜索和获取相关的热点事件信息。\n");
        prompt.append("重点关注与时间线主题相关的重要事件、新闻、发展动态等。\n");
        prompt.append("\n");

        // 添加功能配置
        if (request.getEnableDeduplication() || request.getEnableDictionary() || request.getEnableRelationAnalysis()) {
            prompt.append("=== 功能配置 ===\n");
            if (request.getEnableDeduplication()) {
                prompt.append("- 启用事件去重：避免重复事件\n");
            }
            if (request.getEnableDictionary()) {
                prompt.append("- 启用字典管理：使用标准化术语\n");
            }
            if (request.getEnableRelationAnalysis()) {
                prompt.append("- 启用关系分析：分析事件间关联\n");
            }
            prompt.append("\n");
        }

        // 添加输出格式要求
        prompt.append("=== 输出格式要求 ===\n");
        prompt.append("请返回JSON格式的事件列表，每个事件必须包含以下字段：\n");
        prompt.append("- id: 事件唯一标识\n");
        prompt.append("- title: 事件标题（简洁明了）\n");
        prompt.append("- description: 事件详细描述\n");
        prompt.append("- eventTime: 事件发生时间（ISO格式）\n");
        prompt.append("- location: 事件发生地点（具体地名）\n");
        prompt.append("- subject: 事件主体（人物、组织等）\n");
        prompt.append("- object: 事件客体（受影响对象）\n");
        prompt.append("- eventType: 事件类型（政治、经济、社会、科技等）\n");
        prompt.append("- keywords: 关键词列表\n");
        prompt.append("- sources: 信息来源列表\n");
        prompt.append("- credibilityScore: 可信度评分（0.0-1.0）\n");
        prompt.append("\n");

        prompt.append("请确保返回的事件信息准确、及时、相关，并且符合指定的时间和地区范围。");

        return prompt.toString();
    }

    /**
     * 将地区ID转换为中文名称
     */
    private List<String> convertRegionIdsToNames(List<Long> regionIds) {
        List<String> regionNames = new ArrayList<>();

        // 这里需要注入RegionMapper来查询地区信息
        // 暂时使用硬编码的映射，实际应该从数据库查询
        Map<Long, String> regionMap = getRegionIdToNameMap();

        for (Long regionId : regionIds) {
            String regionName = regionMap.get(regionId);
            if (regionName != null) {
                regionNames.add(regionName);
            } else {
                // 如果找不到对应的地区名称，使用ID作为备选
                regionNames.add("地区ID_" + regionId);
                log.warn("未找到地区ID {} 对应的中文名称", regionId);
            }
        }

        return regionNames.isEmpty() ? Arrays.asList("全球") : regionNames;
    }

    /**
     * 获取地区ID到名称的映射
     * TODO: 这里应该从数据库动态获取，或者注入RegionMapper
     */
    private Map<Long, String> getRegionIdToNameMap() {
        Map<Long, String> regionMap = new HashMap<>();

        // 常见地区映射（应该从数据库获取）
        regionMap.put(1L, "全球");
        regionMap.put(2L, "中国");
        regionMap.put(3L, "美国");
        regionMap.put(4L, "欧洲");
        regionMap.put(5L, "亚洲");
        regionMap.put(6L, "非洲");
        regionMap.put(7L, "南美洲");
        regionMap.put(8L, "北美洲");
        regionMap.put(9L, "大洋洲");
        regionMap.put(10L, "中东");
        regionMap.put(11L, "东南亚");
        regionMap.put(12L, "东亚");
        regionMap.put(13L, "西欧");
        regionMap.put(14L, "东欧");
        regionMap.put(15L, "南亚");
        
        // 添加伊朗和以色列的地区映射
        regionMap.put(44L, "俄罗斯");
        regionMap.put(45L, "伊朗");
        regionMap.put(46L, "以色列");
        regionMap.put(47L, "土耳其");
        regionMap.put(48L, "沙特阿拉伯");
        regionMap.put(49L, "乌克兰");

        return regionMap;
    }

    /**
     * 解析API响应为事件列表
     */
    private List<EventData> parseApiResponseToEvents(String apiResponse, TimeSegment segment) {
        List<EventData> events = new ArrayList<>();

        if (apiResponse == null || apiResponse.trim().isEmpty()) {
            log.warn("时间段 {} 的API响应为空", segment.getSegmentId());
            return events;
        }

        try {
            log.info("=== 🔍 开始解析火山引擎API响应 ===");
            log.info("时间段: {}", segment.getSegmentId());
            log.info("响应长度: {}", apiResponse.length());
            
            // 只打印前500字符避免日志过长
            String truncatedResponse = apiResponse.length() > 500 ? 
                apiResponse.substring(0, 500) + "...[截断]" : apiResponse;
            log.info("原始响应内容: {}", truncatedResponse);

            // 尝试解析JSON响应
            log.info("🔍 尝试JSON解析...");
            events = parseJsonResponse(apiResponse, segment);
            log.info("JSON解析结果: {} 个事件", events.size());

            if (events.isEmpty()) {
                log.warn("⚠️ JSON解析未获得事件，尝试文本解析");
                events = parseTextResponse(apiResponse, segment);
                log.info("文本解析结果: {} 个事件", events.size());
            }

            log.info("=== ✅ API响应解析完成 ===");
            log.info("最终解析出事件数量: {}", events.size());

            // 打印解析出的事件标题用于调试
            for (int i = 0; i < Math.min(3, events.size()); i++) {
                EventData event = events.get(i);
                log.info("解析事件 {}: id={}, title={}, time={}", 
                    i + 1, event.getId(), event.getTitle(), event.getEventTime());
            }

            // 如果仍然没有解析出事件，创建少量模拟数据作为备用
            if (events.isEmpty()) {
                log.warn("⚠️ 所有解析方法都失败，创建备用事件数据");
                events = createFallbackEvents(segment);
                log.info("备用事件创建完成: {} 个事件", events.size());
            }

        } catch (Exception e) {
            log.error("解析时间段 {} 的API响应失败", segment.getSegmentId(), e);
            // 解析失败时创建备用事件
            events = createFallbackEvents(segment);
        }

        return events;
    }

    /**
     * 解析JSON格式的API响应
     */
    private List<EventData> parseJsonResponse(String apiResponse, TimeSegment segment) {
        List<EventData> events = new ArrayList<>();

        try {
            // 尝试提取JSON数组部分
            String jsonContent = extractJsonFromResponse(apiResponse);
            if (jsonContent == null) {
                return events;
            }

            log.info("提取的JSON内容: {}", jsonContent);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            // 尝试解析为JSON数组
            if (jsonContent.trim().startsWith("[")) {
                com.fasterxml.jackson.databind.JsonNode jsonArray = objectMapper.readTree(jsonContent);

                for (com.fasterxml.jackson.databind.JsonNode eventNode : jsonArray) {
                    EventData event = parseEventFromJsonNode(eventNode, segment);
                    if (event != null) {
                        events.add(event);
                    }
                }
            } else if (jsonContent.trim().startsWith("{")) {
                // 尝试解析为单个JSON对象
                com.fasterxml.jackson.databind.JsonNode eventNode = objectMapper.readTree(jsonContent);
                EventData event = parseEventFromJsonNode(eventNode, segment);
                if (event != null) {
                    events.add(event);
                }
            }

            log.info("JSON解析成功，获得 {} 个事件", events.size());

        } catch (Exception e) {
            log.warn("JSON解析失败: {}", e.getMessage());
        }

        return events;
    }

    /**
     * 从响应中提取JSON内容
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }

        // 查找JSON数组的开始和结束
        int arrayStart = response.indexOf('[');
        int arrayEnd = response.lastIndexOf(']');

        if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
            return response.substring(arrayStart, arrayEnd + 1);
        }

        // 查找JSON对象的开始和结束
        int objectStart = response.indexOf('{');
        int objectEnd = response.lastIndexOf('}');

        if (objectStart != -1 && objectEnd != -1 && objectEnd > objectStart) {
            return response.substring(objectStart, objectEnd + 1);
        }

        return null;
    }

    /**
     * 从JSON节点解析事件数据
     */
    private EventData parseEventFromJsonNode(com.fasterxml.jackson.databind.JsonNode eventNode, TimeSegment segment) {
        try {
            EventData event = new EventData();

            // 解析基本字段
            event.setId(getJsonStringValue(eventNode, "id", UUID.randomUUID().toString()));
            event.setTitle(getJsonStringValue(eventNode, "title", "未知事件"));
            event.setDescription(getJsonStringValue(eventNode, "description", ""));
            event.setLocation(getJsonStringValue(eventNode, "location", ""));
            event.setSubject(getJsonStringValue(eventNode, "subject", ""));
            event.setObject(getJsonStringValue(eventNode, "object", ""));
            event.setEventType(getJsonStringValue(eventNode, "eventType", "热点事件"));

            // 解析时间
            String eventTimeStr = getJsonStringValue(eventNode, "eventTime", null);
            if (eventTimeStr != null && !eventTimeStr.isEmpty()) {
                try {
                    event.setEventTime(LocalDateTime.parse(eventTimeStr.replace("Z", "")));
                } catch (Exception e) {
                    log.warn("解析事件时间失败: {}, 使用默认时间", eventTimeStr);
                    event.setEventTime(segment.getStartTime());
                }
            } else {
                event.setEventTime(segment.getStartTime());
            }

            // 解析关键词
            if (eventNode.has("keywords") && eventNode.get("keywords").isArray()) {
                List<String> keywords = new ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode keywordNode : eventNode.get("keywords")) {
                    keywords.add(keywordNode.asText());
                }
                event.setKeywords(keywords);
            } else {
                event.setKeywords(Arrays.asList("热点", "事件"));
            }

            // 解析来源
            if (eventNode.has("sources") && eventNode.get("sources").isArray()) {
                List<String> sources = new ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode sourceNode : eventNode.get("sources")) {
                    sources.add(sourceNode.asText());
                }
                event.setSources(sources);
            } else {
                event.setSources(Arrays.asList("火山引擎API"));
            }

            // 解析可信度评分
            if (eventNode.has("credibilityScore")) {
                event.setCredibilityScore(eventNode.get("credibilityScore").asDouble(0.8));
            } else {
                event.setCredibilityScore(0.8);
            }

            // 设置其他字段
            event.setFetchMethod("火山引擎API分段获取");
            event.setValidationStatus("待验证");
            event.setSource("API");
            // 注意：EventData类中没有setStatus方法，移除此行

            log.debug("成功解析事件: id={}, title={}", event.getId(), event.getTitle());
            return event;

        } catch (Exception e) {
            log.error("解析事件JSON节点失败", e);
            return null;
        }
    }

    /**
     * 从JSON节点获取字符串值
     */
    private String getJsonStringValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName,
            String defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }

    /**
     * 解析文本格式的API响应
     */
    private List<EventData> parseTextResponse(String apiResponse, TimeSegment segment) {
        List<EventData> events = new ArrayList<>();

        try {
            log.info("尝试文本解析API响应");

            // 简单的文本解析逻辑
            String[] lines = apiResponse.split("\n");
            EventData currentEvent = null;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // 检测事件标题（通常以数字开头或包含特定关键词）
                if (line.matches("^\\d+\\..*") || line.contains("事件") || line.contains("新闻")) {
                    if (currentEvent != null) {
                        events.add(currentEvent);
                    }

                    currentEvent = new EventData();
                    currentEvent.setId(UUID.randomUUID().toString());
                    currentEvent.setTitle(line.replaceAll("^\\d+\\.", "").trim());
                    currentEvent.setEventTime(segment.getStartTime());
                    currentEvent.setFetchMethod("火山引擎API文本解析");
                    currentEvent.setValidationStatus("待验证");
                    currentEvent.setSource("API");
                    currentEvent.setCredibilityScore(0.7);
                    currentEvent.setKeywords(Arrays.asList("热点", "事件"));
                    currentEvent.setSources(Arrays.asList("火山引擎API"));
                }

                // 添加到描述中
                if (currentEvent != null && !line.equals(currentEvent.getTitle())) {
                    String currentDesc = currentEvent.getDescription() != null ? currentEvent.getDescription() : "";
                    currentEvent.setDescription(currentDesc + "\n" + line);
                }
            }

            // 添加最后一个事件
            if (currentEvent != null) {
                events.add(currentEvent);
            }

            log.info("文本解析完成，获得 {} 个事件", events.size());

        } catch (Exception e) {
            log.error("文本解析失败", e);
        }

        return events;
    }

    /**
     * 创建备用事件数据
     */
    private List<EventData> createFallbackEvents(TimeSegment segment) {
        List<EventData> events = new ArrayList<>();

        try {
            // 创建少量备用事件
            for (int i = 0; i < Math.min(3, segment.getExpectedEventCount()); i++) {
                EventData event = new EventData();
                event.setId(segment.getSegmentId() + "_fallback_" + i);
                event.setTitle("备用事件 " + (i + 1));
                event.setDescription("API解析失败时的备用事件数据");
                event.setEventTime(segment.getStartTime().plusHours(i * 2));
                event.setLocation("未知地点");
                event.setSubject("未知主体");
                event.setObject("未知客体");
                event.setEventType("备用事件");
                event.setKeywords(Arrays.asList("备用", "事件"));
                event.setSources(Arrays.asList("系统生成"));
                event.setCredibilityScore(0.3);
                event.setFetchMethod("备用数据生成");
                event.setValidationStatus("待验证");
                event.setSource("SYSTEM");

                events.add(event);
            }

            log.info("创建了 {} 个备用事件", events.size());

        } catch (Exception e) {
            log.error("创建备用事件失败", e);
        }

        return events;
    }

    /**
     * 验证和过滤事件数据
     * 确保事件数据的质量和完整性
     */
    private List<EventData> validateAndFilterEvents(List<EventData> events, TimeSegment segment) {
        if (events == null || events.isEmpty()) {
            log.debug("时间段 {} 没有事件需要验证", segment.getSegmentId());
            return new ArrayList<>();
        }

        List<EventData> validEvents = new ArrayList<>();
        int invalidCount = 0;

        for (EventData event : events) {
            try {
                // 基本字段验证
                if (!isEventValid(event)) {
                    invalidCount++;
                    log.debug("事件验证失败，跳过: eventId={}, reason=基本字段缺失", event.getId());
                    continue;
                }

                // 时间范围验证
                if (!isEventTimeInRange(event, segment)) {
                    invalidCount++;
                    log.debug("事件验证失败，跳过: eventId={}, reason=时间超出范围", event.getId());
                    continue;
                }

                // 数据质量验证
                if (!isEventQualityAcceptable(event)) {
                    invalidCount++;
                    log.debug("事件验证失败，跳过: eventId={}, reason=数据质量不达标", event.getId());
                    continue;
                }

                // 增强事件数据
                enhanceEventData(event, segment);

                validEvents.add(event);

            } catch (Exception e) {
                invalidCount++;
                log.warn("验证事件时发生异常: eventId={}, error={}",
                        event.getId(), e.getMessage());
            }
        }

        log.debug("时间段 {} 事件验证完成: 总数={}, 有效={}, 无效={}",
                segment.getSegmentId(), events.size(), validEvents.size(), invalidCount);

        return validEvents;
    }

    /**
     * 检查事件基本字段是否有效
     */
    private boolean isEventValid(EventData event) {
        if (event == null) {
            return false;
        }

        // 检查必需字段
        if (event.getId() == null || event.getId().trim().isEmpty()) {
            return false;
        }

        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            return false;
        }

        if (event.getEventTime() == null) {
            return false;
        }

        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 检查事件时间是否在指定时间段范围内
     */
    private boolean isEventTimeInRange(EventData event, TimeSegment segment) {
        if (event.getEventTime() == null || segment == null) {
            return false;
        }

        LocalDateTime eventTime = event.getEventTime();
        LocalDateTime segmentStart = segment.getStartTime();
        LocalDateTime segmentEnd = segment.getEndTime();

        return !eventTime.isBefore(segmentStart) && !eventTime.isAfter(segmentEnd);
    }

    /**
     * 检查事件数据质量是否可接受
     */
    private boolean isEventQualityAcceptable(EventData event) {
        // 检查标题长度
        if (event.getTitle().length() < 5 || event.getTitle().length() > 200) {
            return false;
        }

        // 检查描述长度
        if (event.getDescription().length() < 10 || event.getDescription().length() > 1000) {
            return false;
        }

        // 检查可信度分数
        if (event.getCredibilityScore() != null &&
                (event.getCredibilityScore() < 0.0 || event.getCredibilityScore() > 1.0)) {
            return false;
        }

        // 检查是否包含明显的垃圾内容
        String title = event.getTitle().toLowerCase();
        String description = event.getDescription().toLowerCase();

        String[] spamKeywords = { "测试", "test", "example", "示例", "样例" };
        for (String keyword : spamKeywords) {
            if (title.contains(keyword) && description.contains(keyword)) {
                // 如果标题和描述都包含测试关键词，可能是测试数据
                return false;
            }
        }

        return true;
    }

    /**
     * 增强事件数据
     * 添加时间段相关的元数据
     */
    private void enhanceEventData(EventData event, TimeSegment segment) {
        // 设置获取方法
        if (event.getFetchMethod() == null || event.getFetchMethod().isEmpty()) {
            event.setFetchMethod("时间段分割API获取");
        }

        // 设置验证状态
        if (event.getValidationStatus() == null || event.getValidationStatus().isEmpty()) {
            event.setValidationStatus("已验证");
        }

        // 添加时间段信息到事件描述中（如果需要）
        if (event.getDescription() != null && !event.getDescription().contains("时间段")) {
            event.setDescription(event.getDescription() +
                    String.format(" [来源时间段: %s]", segment.getSegmentId()));
        }

        // 设置默认可信度分数
        if (event.getCredibilityScore() == null) {
            event.setCredibilityScore(0.7); // 默认可信度
        }

        // 确保关键词列表不为空
        if (event.getKeywords() == null || event.getKeywords().isEmpty()) {
            event.setKeywords(Arrays.asList("热点事件", "时间线"));
        }

        // 确保来源列表不为空
        if (event.getSources() == null || event.getSources().isEmpty()) {
            event.setSources(Arrays.asList("API获取"));
        }
    }
}