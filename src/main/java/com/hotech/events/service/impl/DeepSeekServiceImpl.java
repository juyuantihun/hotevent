package com.hotech.events.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.entity.Region;
import com.hotech.events.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DeepSeek服务实现类
 */
@Slf4j
@Service
@Primary
public class DeepSeekServiceImpl implements DeepSeekService {

    @Value("${app.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${app.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${app.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${app.deepseek.max-tokens:2000}")
    private int maxTokens;

    @Value("${app.deepseek.temperature:0.7}")
    private double temperature;

    @Value("${app.deepseek.web-search.enabled:true}")
    private boolean webSearchEnabled; // 是否启用联网搜索

    @Value("${app.deepseek.web-search.max-results:10}")
    private int webSearchMaxResults; // 联网搜索最大结果数

    @Value("${app.deepseek.web-search.search-timeout:30000}")
    private int webSearchTimeout; // 联网搜索超时时间

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 事件缓存，用于去重
    private static final Map<String, Map<String, Object>> EVENT_CACHE = new ConcurrentHashMap<>();
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.hotech.events.mapper.EventMapper eventMapper;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.hotech.events.mapper.EventRelationMapper eventRelationMapper;

    @Override
    public Boolean checkConnection() {
        log.info("检查DeepSeek API连接状态");

        try {
            // 发送一个简单的测试请求
            String testPrompt = "Hello, this is a test message to check connection.";
            String response = callDeepSeekAPI(testPrompt);

            // 如果能获取到响应，则连接正常
            boolean isConnected = response != null && !response.isEmpty();
            log.info("DeepSeek API连接状态: {}", isConnected ? "正常" : "异常");
            return isConnected;
        } catch (Exception e) {
            log.error("检查DeepSeek API连接状态时发生错误", e);
            return false;
        }
    }

    /**
     * 调用DeepSeek API
     * 
     * @param prompt 提示词
     * @return API响应内容
     */
    private String callDeepSeekAPI(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);

            // 火山方舟版本支持联网搜索功能
            if (webSearchEnabled) {
                log.debug("启用联网搜索: webSearch=true, maxResults={}, timeout={}ms", webSearchMaxResults, webSearchTimeout);
                // 注意：火山方舟的bot模型默认支持联网搜索，无需额外参数
            }
            
            // 火山方舟版本说明 - 使用bot模型支持联网搜索
            log.debug("使用火山方舟DeepSeek API，模型: {}，支持联网搜索: {}", model, webSearchEnabled);

            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息（火山方舟版本支持）
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的事件分析助手，能够准确分析和处理各种国际事件数据。");
            messages.add(systemMessage);
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 创建RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            log.info("调用DeepSeek API, 模型: {}, 提示词长度: {}", model, prompt.length());

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepseekApiUrl,
                    requestEntity,
                    String.class);

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);

                // 获取生成的内容
                if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof List) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                        if (messageObj.containsKey("content")) {
                            String content = (String) messageObj.get("content");
                            log.info("DeepSeek API调用成功, 响应长度: {}", content.length());
                            return content;
                        }
                    }
                }

                log.error("DeepSeek API响应格式错误: {}", response.getBody());
                return null;
            } else {
                log.error("DeepSeek API调用失败, 状态码: {}, 响应: {}",
                        response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API异常", e);
            return null;
        }
    }

    @Override
    public List<EventDTO> fetchLatestEvents(int limit) {
        log.info("获取最新事件, limit={}", limit);

        try {
            // 在实际项目中，这里应该调用DeepSeek API获取最新事件
            // 这里使用模拟数据
            return simulateFetchLatestEvents(limit);
        } catch (Exception e) {
            log.error("获取最新事件失败", e);
            throw new RuntimeException("获取最新事件失败: " + e.getMessage());
        }
    }

    @Override
    public List<EventDTO> fetchEventsByKeywords(List<String> keywords, int limit) {
        log.info("根据关键词获取事件, keywords={}, limit={}", keywords, limit);

        try {
            // 在实际项目中，这里应该调用DeepSeek API根据关键词获取事件
            // 这里使用模拟数据
            return simulateFetchEventsByKeywords(keywords, limit);
        } catch (Exception e) {
            log.error("根据关键词获取事件失败", e);
            throw new RuntimeException("根据关键词获取事件失败: " + e.getMessage());
        }
    }

    @Override
    public List<EventDTO> fetchEventsByDateRange(String startDate, String endDate, int limit) {
        log.info("根据日期范围获取事件, startDate={}, endDate={}, limit={}", startDate, endDate, limit);

        try {
            // 解析日期字符串
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 在实际项目中，这里应该调用DeepSeek API根据日期范围获取事件
            // 这里使用模拟数据
            return simulateFetchEventsByDateRange(start, end, limit);
        } catch (DateTimeParseException e) {
            log.error("日期格式错误", e);
            throw new RuntimeException("日期格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("根据日期范围获取事件失败", e);
            throw new RuntimeException("根据日期范围获取事件失败: " + e.getMessage());
        }
    }

    @Override
    public List<EventDTO> parseGdeltData(String gdeltData) {
        log.info("解析GDELT数据, 数据长度={}", gdeltData.length());

        try {
            // 在实际项目中，这里应该解析GDELT格式的数据
            // 这里使用模拟数据
            return simulateParseGdeltData(gdeltData);
        } catch (Exception e) {
            log.error("解析GDELT数据失败", e);
            throw new RuntimeException("解析GDELT数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> fetchEvents(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("从DeepSeek获取事件数据: regions={}, startTime={}, endTime={}", regions, startTime, endTime);

        // 清除缓存
        EVENT_CACHE.clear();

        try {
            // 构建提示词
            String prompt = buildEventFetchPrompt(regions, startTime, endTime);

            // 调用DeepSeek API
            String responseContent = callDeepSeekAPI(prompt);

            // 如果API调用失败，使用模拟数据作为备份
            if (responseContent == null || responseContent.isEmpty()) {
                log.warn("DeepSeek API返回空数据，使用模拟数据作为备份");
                List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
                return deduplicateEvents(events);
            }

            // 解析JSON响应
            try {
                Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

                // 检查响应数据格式
                if (!responseMap.containsKey("events")) {
                    log.error("DeepSeek API返回数据格式错误: {}", responseContent);
                    throw new RuntimeException("DeepSeek API返回数据格式错误");
                }

                // 获取事件列表
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> events = (List<Map<String, Object>>) responseMap.get("events");

                // 如果API返回空数据，使用模拟数据作为备份
                if (events == null || events.isEmpty()) {
                    log.warn("DeepSeek API返回空事件列表，使用模拟数据作为备份");
                    events = simulateFetchEvents(regions, startTime, endTime);
                }

                // 事件去重
                return deduplicateEvents(events);
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek API响应失败", e);
                // 发生异常时，使用模拟数据作为备份
                log.warn("使用模拟数据作为备份");
                List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
                return deduplicateEvents(events);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API获取事件失败", e);

            // 发生异常时，使用模拟数据作为备份
            log.warn("使用模拟数据作为备份");
            List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
            return deduplicateEvents(events);
        }
    }

    /**
     * 构建事件获取提示词
     */
    private String buildEventFetchPrompt(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下条件，生成相关的国际事件数据：\n\n");

        // 添加地区信息
        prompt.append("地区：");
        if (regions != null && !regions.isEmpty()) {
            prompt.append(regions.stream().map(Region::getName).collect(Collectors.joining(", ")));
        } else {
            prompt.append("全球");
        }
        prompt.append("\n");

        // 添加时间范围
        prompt.append("时间范围：").append(startTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .append(" 至 ").append(endTime.format(DateTimeFormatter.ISO_DATE_TIME)).append("\n\n");

        // 添加返回格式说明
        prompt.append("请返回JSON格式的事件数据，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"events\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"事件ID\",\n");
        prompt.append("      \"subject\": \"事件主体\",\n");
        prompt.append("      \"object\": \"事件客体\",\n");
        prompt.append("      \"type\": \"事件类型\",\n");
        prompt.append("      \"time\": \"事件时间（ISO格式）\",\n");
        prompt.append("      \"location\": \"事件地点\",\n");
        prompt.append("      \"description\": \"事件描述\",\n");
        prompt.append("      \"keywords\": [\"关键词1\", \"关键词2\"]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("请生成20-30个符合条件的事件，确保事件内容真实可信，时间在指定范围内，地点与指定地区相关。");

        return prompt.toString();
    }

    @Override
    public List<Map<String, Object>> analyzeEventRelations(List<Map<String, Object>> events) {
        log.info("分析事件关联关系: eventCount={}", events.size());

        // 使用批量处理工具类处理事件
        return com.hotech.events.util.BatchProcessor.processBatch(
                events,
                100, // 每批次处理100个事件
                this::analyzeEventRelationsBatch);
    }

    /**
     * 批量分析事件关联关系
     * 
     * @param eventsBatch 事件批次
     * @return 事件关联关系列表
     */
    private List<Map<String, Object>> analyzeEventRelationsBatch(List<Map<String, Object>> eventsBatch) {
        log.info("批量分析事件关联关系: batchSize={}", eventsBatch.size());

        try {
            // 构建提示词
            String prompt = buildRelationAnalysisPrompt(eventsBatch);

            // 调用DeepSeek API
            String responseContent = callDeepSeekAPI(prompt);

            // 如果API调用失败，使用模拟数据作为备份
            if (responseContent == null || responseContent.isEmpty()) {
                log.warn("DeepSeek API返回空数据，使用模拟数据作为备份");
                return simulateAnalyzeEventRelations(eventsBatch);
            }

            // 解析JSON响应
            try {
                Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

                // 检查响应数据格式
                if (!responseMap.containsKey("relations")) {
                    log.error("DeepSeek API返回数据格式错误: {}", responseContent);
                    throw new RuntimeException("DeepSeek API返回数据格式错误");
                }

                // 获取关系列表
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> relations = (List<Map<String, Object>>) responseMap.get("relations");

                // 如果API返回空数据，使用模拟数据作为备份
                if (relations == null || relations.isEmpty()) {
                    log.warn("DeepSeek API返回空关系列表，使用模拟数据作为备份");
                    relations = simulateAnalyzeEventRelations(eventsBatch);
                }

                return relations;
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek API响应失败", e);
                // 发生异常时，使用模拟数据作为备份
                log.warn("使用模拟数据作为备份");
                return simulateAnalyzeEventRelations(eventsBatch);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API批量分析事件关联关系失败", e);

            // 发生异常时，使用模拟数据作为备份
            log.warn("使用模拟数据作为备份");
            return simulateAnalyzeEventRelations(eventsBatch);
        }
    }

    /**
     * 构建关系分析提示词
     */
    private String buildRelationAnalysisPrompt(List<Map<String, Object>> events) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下事件之间的因果关系，运用逻辑推理找出事件间的关联：\n\n");

        for (int i = 0; i < events.size(); i++) {
            Map<String, Object> event = events.get(i);
            prompt.append(String.format("事件%d：\n", i + 1));
            prompt.append(String.format("  ID: %s\n", event.get("id")));
            prompt.append(String.format("  主体: %s\n", event.get("subject")));
            prompt.append(String.format("  客体: %s\n", event.get("object")));
            prompt.append(String.format("  类型: %s\n", event.get("type")));
            prompt.append(String.format("  时间: %s\n", event.get("time")));
            prompt.append(String.format("  地点: %s\n", event.get("location")));
            prompt.append(String.format("  描述: %s\n", event.get("description")));
            prompt.append("\n");
        }

        prompt.append("请仔细分析：\n");
        prompt.append("1. 事件之间是否存在因果关系？\n");
        prompt.append("2. 哪个事件是原因，哪个是结果？\n");
        prompt.append("3. 关系的类型是什么？\n");
        prompt.append("4. 关系的强度如何（1-5）？\n\n");

        prompt.append("**重要：必须严格按照以下JSON格式返回结果，不要添加任何其他文字说明**\n\n");
        prompt.append("JSON格式示例：\n");
        prompt.append("{\n");
        prompt.append("  \"relations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": 1,\n");
        prompt.append("      \"sourceEventId\": 1,\n");
        prompt.append("      \"targetEventId\": 2,\n");
        prompt.append("      \"type\": \"导致\",\n");
        prompt.append("      \"strength\": 3,\n");
        prompt.append("      \"description\": \"关系描述\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("**注意：**\n");
        prompt.append("- 如果没有发现关系，返回：{\"relations\": []}\n");
        prompt.append("- 只返回JSON，不要有其他解释文字");

        return prompt.toString();
    }

    @Override
    public List<Map<String, Object>> organizeTimelines(List<Map<String, Object>> events,
            List<Map<String, Object>> relations) {
        log.info("组织时间线: eventCount={}, relationCount={}", events.size(), relations.size());

        try {
            // 构建提示词
            String prompt = buildTimelineOrganizePrompt(events, relations);

            // 调用DeepSeek API
            String responseContent = callDeepSeekAPI(prompt);

            // 如果API调用失败，使用模拟数据作为备份
            if (responseContent == null || responseContent.isEmpty()) {
                log.warn("DeepSeek API返回空数据，使用模拟数据作为备份");
                return simulateOrganizeTimelines(events, relations);
            }

            // 解析JSON响应
            try {
                Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

                // 检查响应数据格式
                if (!responseMap.containsKey("timelines")) {
                    log.error("DeepSeek API返回数据格式错误: {}", responseContent);
                    throw new RuntimeException("DeepSeek API返回数据格式错误");
                }

                // 获取时间线列表
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> timelines = (List<Map<String, Object>>) responseMap.get("timelines");

                // 如果API返回空数据，使用模拟数据作为备份
                if (timelines == null || timelines.isEmpty()) {
                    log.warn("DeepSeek API返回空时间线列表，使用模拟数据作为备份");
                    timelines = simulateOrganizeTimelines(events, relations);
                }

                return timelines;
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek API响应失败", e);
                // 发生异常时，使用模拟数据作为备份
                log.warn("使用模拟数据作为备份");
                return simulateOrganizeTimelines(events, relations);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API组织时间线失败", e);

            // 发生异常时，使用模拟数据作为备份
            log.warn("使用模拟数据作为备份");
            return simulateOrganizeTimelines(events, relations);
        }
    }

    /**
     * 构建时间线组织提示词
     */
    private String buildTimelineOrganizePrompt(List<Map<String, Object>> events, List<Map<String, Object>> relations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下事件和关系数据，组织成时间线：\n\n");

        // 添加事件信息
        prompt.append("事件数据：\n");
        for (int i = 0; i < Math.min(events.size(), 20); i++) { // 限制事件数量，避免提示词过长
            Map<String, Object> event = events.get(i);
            prompt.append(String.format("事件%d：\n", i + 1));
            prompt.append(String.format("  ID: %s\n", event.get("id")));
            prompt.append(String.format("  主体: %s\n", event.get("subject")));
            prompt.append(String.format("  客体: %s\n", event.get("object")));
            prompt.append(String.format("  类型: %s\n", event.get("type")));
            prompt.append(String.format("  时间: %s\n", event.get("time")));
            prompt.append(String.format("  地点: %s\n", event.get("location")));
            prompt.append(String.format("  描述: %s\n", event.get("description")));
            prompt.append("\n");
        }

        // 添加关系信息
        prompt.append("关系数据：\n");
        for (int i = 0; i < Math.min(relations.size(), 20); i++) { // 限制关系数量，避免提示词过长
            Map<String, Object> relation = relations.get(i);
            prompt.append(String.format("关系%d：\n", i + 1));
            prompt.append(String.format("  ID: %s\n", relation.get("id")));
            prompt.append(String.format("  源事件ID: %s\n", relation.get("sourceEventId")));
            prompt.append(String.format("  目标事件ID: %s\n", relation.get("targetEventId")));
            prompt.append(String.format("  类型: %s\n", relation.get("type")));
            prompt.append(String.format("  强度: %s\n", relation.get("strength")));
            prompt.append(String.format("  描述: %s\n", relation.get("description")));
            prompt.append("\n");
        }

        // 添加返回格式说明
        prompt.append("请根据以上数据，组织成一个或多个时间线，并按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"timelines\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": 1,\n");
        prompt.append("      \"name\": \"时间线名称\",\n");
        prompt.append("      \"description\": \"时间线描述\",\n");
        prompt.append("      \"events\": [事件ID列表],\n");
        prompt.append("      \"relations\": [关系ID列表]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("请确保返回的是有效的JSON格式，不要添加任何其他文字说明。");

        return prompt.toString();
    }

    // 已删除createHttpEntity方法，使用callDeepSeekAPI方法替代

    /**
     * 事件去重
     * 
     * @param events 事件列表
     * @return 去重后的事件列表
     */
    private List<Map<String, Object>> deduplicateEvents(List<Map<String, Object>> events) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> event : events) {
            // 构建事件唯一标识
            String key = buildEventKey(event);

            // 如果缓存中不存在，则添加到结果列表
            if (!EVENT_CACHE.containsKey(key)) {
                EVENT_CACHE.put(key, event);
                result.add(event);
            }
        }

        log.info("事件去重: 原始数量={}, 去重后数量={}", events.size(), result.size());
        return result;
    }

    /**
     * 构建事件唯一标识
     * 
     * @param event 事件
     * @return 唯一标识
     */
    private String buildEventKey(Map<String, Object> event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getOrDefault("subject", ""))
                .append("_")
                .append(event.getOrDefault("object", ""))
                .append("_")
                .append(event.getOrDefault("type", ""))
                .append("_")
                .append(event.getOrDefault("time", ""))
                .append("_")
                .append(event.getOrDefault("location", ""));
        return sb.toString();
    }

    /**
     * 从数据库获取事件数据
     * 
     * @param regions   地区列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 事件列表
     */
    private List<Map<String, Object>> simulateFetchEvents(List<Region> regions, LocalDateTime startTime,
            LocalDateTime endTime) {
        log.info("从数据库获取事件数据: regions={}, startTime={}, endTime={}", regions, startTime, endTime);
        
        try {
            // 构建查询条件
            List<String> regionNames = new ArrayList<>();
            if (regions != null && !regions.isEmpty()) {
                for (Region region : regions) {
                    regionNames.add(region.getName());
                }
            }
            
            // 从数据库查询事件
            List<Map<String, Object>> events = eventMapper.findEventsByConditions(regionNames, startTime, endTime);
            
            // 如果没有找到事件，使用所有事件作为备份
            if (events == null || events.isEmpty()) {
                log.warn("数据库中未找到符合条件的事件数据，将查询所有事件");
                events = eventMapper.findAllEvents();
            }
            
            // 如果仍然没有找到事件，返回空列表
            if (events == null || events.isEmpty()) {
                log.warn("数据库中未找到任何事件数据");
                return new ArrayList<>();
            }
            
            // 处理事件数据，确保格式一致
            for (Map<String, Object> event : events) {
                // 确保事件有ID
                if (!event.containsKey("id")) {
                    event.put("id", event.get("id"));
                }
                
                // 确保事件有时间
                if (!event.containsKey("time")) {
                    Object eventTime = event.get("event_time");
                    if (eventTime != null) {
                        if (eventTime instanceof java.sql.Timestamp) {
                            event.put("time", new java.sql.Timestamp(((java.sql.Timestamp) eventTime).getTime()).toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME));
                        } else if (eventTime instanceof LocalDateTime) {
                            event.put("time", ((LocalDateTime) eventTime).format(DateTimeFormatter.ISO_DATE_TIME));
                        } else {
                            event.put("time", eventTime.toString());
                        }
                    }
                }
                
                // 确保事件有类型
                if (!event.containsKey("type")) {
                    event.put("type", event.get("event_type"));
                }
                
                // 确保事件有描述
                if (!event.containsKey("description")) {
                    event.put("description", event.get("event_description"));
                }
                
                // 确保事件有地点
                if (!event.containsKey("location")) {
                    event.put("location", event.get("event_location"));
                }
                
                // 确保事件有主体和客体
                if (!event.containsKey("subject")) {
                    event.put("subject", event.get("subject"));
                }
                if (!event.containsKey("object")) {
                    event.put("object", event.get("object"));
                }
                
                // 添加关键词
                if (!event.containsKey("keywords")) {
                    event.put("keywords", Arrays.asList("数据库事件", "真实数据"));
                }
            }
            
            log.info("从数据库获取到 {} 条事件数据", events.size());
            return events;
        } catch (Exception e) {
            log.error("从数据库获取事件数据失败", e);
            
            // 发生异常时，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 从数据库获取事件关联关系
     * 
     * @param events 事件列表
     * @return 事件关联关系列表
     */
    private List<Map<String, Object>> simulateAnalyzeEventRelations(List<Map<String, Object>> events) {
        log.info("从数据库获取事件关联关系: eventCount={}", events.size());
        
        try {
            // 提取事件ID列表
            List<Long> eventIds = new ArrayList<>();
            for (Map<String, Object> event : events) {
                Object idObj = event.get("id");
                if (idObj != null) {
                    if (idObj instanceof Number) {
                        eventIds.add(((Number) idObj).longValue());
                    } else {
                        eventIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }
            
            // 如果没有事件ID，返回空列表
            if (eventIds.isEmpty()) {
                log.warn("没有有效的事件ID，无法查询关联关系");
                return new ArrayList<>();
            }
            
            // 从数据库查询事件关系
            List<Map<String, Object>> relations = eventRelationMapper.findRelationsByEventIds(eventIds);
            
            // 如果没有找到关系，查询所有关系
            if (relations == null || relations.isEmpty()) {
                log.warn("数据库中未找到事件关联关系，将查询所有关系");
                relations = eventRelationMapper.findAllRelations();
            }
            
            // 如果仍然没有找到关系，返回空列表
            if (relations == null || relations.isEmpty()) {
                log.warn("数据库中未找到任何事件关联关系");
                return new ArrayList<>();
            }
            
            // 处理关系数据，确保格式一致
            for (Map<String, Object> relation : relations) {
                // 确保关系有ID
                if (!relation.containsKey("id")) {
                    relation.put("id", relation.get("id"));
                }
                
                // 确保关系有源事件ID
                if (!relation.containsKey("sourceEventId")) {
                    relation.put("sourceEventId", relation.get("source_event_id"));
                }
                
                // 确保关系有目标事件ID
                if (!relation.containsKey("targetEventId")) {
                    relation.put("targetEventId", relation.get("target_event_id"));
                }
                
                // 确保关系有类型
                if (!relation.containsKey("type")) {
                    relation.put("type", relation.get("relation_type"));
                }
                
                // 确保关系有强度
                if (!relation.containsKey("strength")) {
                    Object confidence = relation.get("confidence");
                    if (confidence != null) {
                        if (confidence instanceof Number) {
                            relation.put("strength", ((Number) confidence).intValue());
                        } else {
                            try {
                                relation.put("strength", Integer.valueOf(confidence.toString()));
                            } catch (NumberFormatException e) {
                                relation.put("strength", 3); // 默认强度
                            }
                        }
                    } else {
                        relation.put("strength", 3); // 默认强度
                    }
                }
                
                // 确保关系有描述
                if (!relation.containsKey("description")) {
                    relation.put("description", relation.get("relation_description"));
                }
            }
            
            log.info("从数据库获取到 {} 条事件关联关系", relations.size());
            return relations;
        } catch (Exception e) {
            log.error("从数据库获取事件关联关系失败", e);
            
            // 发生异常时，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 组织时间线
     * 
     * @param events    事件列表
     * @param relations 事件关联关系列表
     * @return 时间线列表
     */
    private List<Map<String, Object>> simulateOrganizeTimelines(List<Map<String, Object>> events,
            List<Map<String, Object>> relations) {
        log.info("组织时间线: eventCount={}, relationCount={}", events.size(), relations.size());
        
        List<Map<String, Object>> timelines = new ArrayList<>();

        try {
            // 按时间排序事件
            events.sort((e1, e2) -> {
                String time1 = (String) e1.get("time");
                String time2 = (String) e2.get("time");
                if (time1 == null || time2 == null) {
                    return 0;
                }
                return time1.compareTo(time2);
            });
            
            // 提取事件ID列表
            List<Object> eventIds = new ArrayList<>();
            for (Map<String, Object> event : events) {
                eventIds.add(event.get("id"));
            }
            
            // 提取关系ID列表
            List<Object> relationIds = new ArrayList<>();
            for (Map<String, Object> relation : relations) {
                relationIds.add(relation.get("id"));
            }
            
            // 创建时间线
            Map<String, Object> timeline = new HashMap<>();
            timeline.put("id", 1);
            timeline.put("name", "事件时间线");
            timeline.put("description", "根据数据库中的真实事件数据生成的时间线");
            timeline.put("events", eventIds);
            timeline.put("relations", relationIds);
            
            timelines.add(timeline);
        } catch (Exception e) {
            log.error("组织时间线失败", e);
            
            // 发生异常时，创建一个简单的时间线
            Map<String, Object> timeline = new HashMap<>();
            timeline.put("id", 1);
            timeline.put("name", "事件时间线");
            timeline.put("description", "事件时间线");
            timeline.put("events", new ArrayList<>());
            timeline.put("relations", new ArrayList<>());
            
            timelines.add(timeline);
        }
        
        return timelines;
    }

    /**
     * 模拟获取最新事件
     * 
     * @param limit 限制数量
     * @return 事件列表
     */
    private List<EventDTO> simulateFetchLatestEvents(int limit) {
        List<EventDTO> events = new ArrayList<>();

        // 模拟数据
        String[] subjects = { "以色列", "伊朗", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] objects = { "伊朗", "以色列", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] types = { "袭击", "支持", "谴责", "会谈", "签署协议", "宣布制裁", "提供援助" };
        String[] locations = { "耶路撒冷", "德黑兰", "贝鲁特", "华盛顿", "莫斯科", "北京", "基辅" };

        // 生成随机事件
        Random random = new Random();
        for (int i = 0; i < Math.min(limit, 20); i++) {
            EventDTO event = new EventDTO();
            event.setId((long) (i + 1));
            event.setSubject(subjects[random.nextInt(subjects.length)]);
            event.setObject(objects[random.nextInt(objects.length)]);
            event.setRelationType(types[random.nextInt(types.length)]);
            event.setEventLocation(locations[random.nextInt(locations.length)]);

            // 设置事件时间（最近7天内）
            LocalDateTime eventTime = LocalDateTime.now().minusDays(random.nextInt(7));
            event.setEventTime(eventTime);

            // 生成描述
            String description = event.getSubject() + " " + event.getRelationType() + " " + event.getObject();
            event.setEventDescription(description);

            // 设置事件类型
            event.setEventType("国际事件");

            // 设置来源类型（1-自动获取）
            event.setSourceType(1);

            // 设置状态（1-启用）
            event.setStatus(1);

            // 设置关键词
            event.setKeywords(Arrays.asList("中东", "冲突", "国际关系"));

            // 设置创建和更新时间
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            events.add(event);
        }

        return events;
    }

    /**
     * 模拟根据关键词获取事件
     * 
     * @param keywords 关键词列表
     * @param limit    限制数量
     * @return 事件列表
     */
    private List<EventDTO> simulateFetchEventsByKeywords(List<String> keywords, int limit) {
        List<EventDTO> events = new ArrayList<>();

        // 模拟数据
        String[] subjects = { "以色列", "伊朗", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] objects = { "伊朗", "以色列", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] types = { "袭击", "支持", "谴责", "会谈", "签署协议", "宣布制裁", "提供援助" };
        String[] locations = { "耶路撒冷", "德黑兰", "贝鲁特", "华盛顿", "莫斯科", "北京", "基辅" };

        // 生成随机事件
        Random random = new Random();
        for (int i = 0; i < Math.min(limit, 20); i++) {
            EventDTO event = new EventDTO();
            event.setId((long) (i + 1));

            // 根据关键词选择主体和客体
            if (!keywords.isEmpty()) {
                String keyword = keywords.get(random.nextInt(keywords.size()));
                // 模拟根据关键词匹配
                if (Arrays.asList(subjects).contains(keyword)) {
                    event.setSubject(keyword);
                    event.setObject(objects[random.nextInt(objects.length)]);
                } else if (Arrays.asList(objects).contains(keyword)) {
                    event.setSubject(subjects[random.nextInt(subjects.length)]);
                    event.setObject(keyword);
                } else {
                    event.setSubject(subjects[random.nextInt(subjects.length)]);
                    event.setObject(objects[random.nextInt(objects.length)]);
                }
            } else {
                event.setSubject(subjects[random.nextInt(subjects.length)]);
                event.setObject(objects[random.nextInt(objects.length)]);
            }

            event.setRelationType(types[random.nextInt(types.length)]);
            event.setEventLocation(locations[random.nextInt(locations.length)]);

            // 设置事件时间（最近30天内）
            LocalDateTime eventTime = LocalDateTime.now().minusDays(random.nextInt(30));
            event.setEventTime(eventTime);

            // 生成描述
            String description = event.getSubject() + " " + event.getRelationType() + " " + event.getObject();
            event.setEventDescription(description);

            // 设置事件类型
            event.setEventType("国际事件");

            // 设置来源类型（1-自动获取）
            event.setSourceType(1);

            // 设置状态（1-启用）
            event.setStatus(1);

            // 设置关键词
            event.setKeywords(keywords);

            // 设置创建和更新时间
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            events.add(event);
        }

        return events;
    }

    /**
     * 模拟根据日期范围获取事件
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 事件列表
     */
    private List<EventDTO> simulateFetchEventsByDateRange(LocalDate startDate, LocalDate endDate, int limit) {
        List<EventDTO> events = new ArrayList<>();

        // 模拟数据
        String[] subjects = { "以色列", "伊朗", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] objects = { "伊朗", "以色列", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] types = { "袭击", "支持", "谴责", "会谈", "签署协议", "宣布制裁", "提供援助" };
        String[] locations = { "耶路撒冷", "德黑兰", "贝鲁特", "华盛顿", "莫斯科", "北京", "基辅" };

        // 计算日期范围内的天数
        long daysBetween = endDate.toEpochDay() - startDate.toEpochDay() + 1;

        // 生成随机事件
        Random random = new Random();
        for (int i = 0; i < Math.min(limit, 20); i++) {
            EventDTO event = new EventDTO();
            event.setId((long) (i + 1));
            event.setSubject(subjects[random.nextInt(subjects.length)]);
            event.setObject(objects[random.nextInt(objects.length)]);
            event.setRelationType(types[random.nextInt(types.length)]);
            event.setEventLocation(locations[random.nextInt(locations.length)]);

            // 设置事件时间（在指定日期范围内）
            LocalDateTime eventTime = startDate.plusDays(random.nextInt((int) daysBetween)).atTime(
                    random.nextInt(24), random.nextInt(60), random.nextInt(60));
            event.setEventTime(eventTime);

            // 生成描述
            String description = event.getSubject() + " " + event.getRelationType() + " " + event.getObject();
            event.setEventDescription(description);

            // 设置事件类型
            event.setEventType("国际事件");

            // 设置来源类型（1-自动获取）
            event.setSourceType(1);

            // 设置状态（1-启用）
            event.setStatus(1);

            // 设置关键词
            event.setKeywords(Arrays.asList("中东", "冲突", "国际关系"));

            // 设置创建和更新时间
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            events.add(event);
        }

        return events;
    }

    @Override
    public Map<String, Object> generateEventAnalysis(List<Map<String, Object>> events, String prompt) {
        log.info("使用DeepSeek聊天API生成事件分析: eventCount={}, prompt={}", events.size(), prompt);

        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            // 构建消息数组
            ArrayNode messages = objectMapper.createArrayNode();

            // 系统消息
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的国际事件分析专家，擅长分析国际事件之间的关联和影响。请根据提供的事件数据进行分析，提供洞察和总结。");
            messages.add(systemMessage);

            // 用户消息
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");

            // 构建用户消息内容
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("以下是一系列国际事件数据，请进行分析：\n\n");

            // 添加事件数据
            for (int i = 0; i < Math.min(events.size(), 20); i++) {
                Map<String, Object> event = events.get(i);
                contentBuilder.append("事件").append(i + 1).append("：\n");
                contentBuilder.append("- 主体：").append(event.getOrDefault("subject", "")).append("\n");
                contentBuilder.append("- 客体：").append(event.getOrDefault("object", "")).append("\n");
                contentBuilder.append("- 类型：").append(event.getOrDefault("type", "")).append("\n");
                contentBuilder.append("- 时间：").append(event.getOrDefault("time", "")).append("\n");
                contentBuilder.append("- 地点：").append(event.getOrDefault("location", "")).append("\n");
                contentBuilder.append("- 描述：").append(event.getOrDefault("description", "")).append("\n\n");
            }

            // 添加分析提示
            contentBuilder.append("请根据以上事件数据，").append(prompt).append("\n");
            contentBuilder.append("请提供详细的分析，包括事件之间的关联、影响、趋势和可能的发展方向。");

            userMessage.put("content", contentBuilder.toString());
            messages.add(userMessage);

            // 添加消息数组到请求体
            requestBody.set("messages", messages);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 创建HTTP请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // 创建RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // 调用DeepSeek聊天API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    deepseekApiUrl,
                    requestEntity,
                    Map.class);

            // 检查响应状态
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("DeepSeek聊天API返回错误: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("DeepSeek聊天API返回错误: " + response.getStatusCode());
            }

            // 解析响应数据
            Map<String, Object> responseBody = response.getBody();

            // 检查响应数据格式
            if (!responseBody.containsKey("choices")) {
                log.error("DeepSeek聊天API返回数据格式错误: {}", responseBody);
                throw new RuntimeException("DeepSeek聊天API返回数据格式错误");
            }

            // 获取生成的文本
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

            if (choices == null || choices.isEmpty()) {
                log.error("DeepSeek聊天API返回空数据");
                throw new RuntimeException("DeepSeek聊天API返回空数据");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            if (message == null || !message.containsKey("content")) {
                log.error("DeepSeek聊天API返回数据格式错误: {}", choices);
                throw new RuntimeException("DeepSeek聊天API返回数据格式错误");
            }

            String analysisText = (String) message.get("content");

            // 构建分析结果
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", analysisText);
            result.put("eventCount", events.size());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return result;
        } catch (Exception e) {
            log.error("调用DeepSeek聊天API生成事件分析失败", e);

            // 发生异常时，返回简单的分析结果
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", "由于技术原因，无法生成详细分析。请稍后再试。");
            result.put("eventCount", events.size());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            result.put("error", e.getMessage());

            return result;
        }
    }

    /**
     * 模拟解析GDELT数据
     * 
     * @param gdeltData GDELT格式数据
     * @return 事件列表
     */
    private List<EventDTO> simulateParseGdeltData(String gdeltData) {
        List<EventDTO> events = new ArrayList<>();

        // 模拟数据
        String[] subjects = { "以色列", "伊朗", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] objects = { "伊朗", "以色列", "黎巴嫩", "哈马斯", "美国", "俄罗斯", "中国", "乌克兰" };
        String[] types = { "袭击", "支持", "谴责", "会谈", "签署协议", "宣布制裁", "提供援助" };
        String[] locations = { "耶路撒冷", "德黑兰", "贝鲁特", "华盛顿", "莫斯科", "北京", "基辅" };

        // 根据输入数据长度生成一定数量的事件
        int eventCount = Math.min(gdeltData.length() / 100, 20);

        // 生成随机事件
        Random random = new Random();
        for (int i = 0; i < eventCount; i++) {
            EventDTO event = new EventDTO();
            event.setId((long) (i + 1));
            event.setSubject(subjects[random.nextInt(subjects.length)]);
            event.setObject(objects[random.nextInt(objects.length)]);
            event.setRelationType(types[random.nextInt(types.length)]);
            event.setEventLocation(locations[random.nextInt(locations.length)]);

            // 设置事件时间（最近90天内）
            LocalDateTime eventTime = LocalDateTime.now().minusDays(random.nextInt(90));
            event.setEventTime(eventTime);

            // 生成描述
            String description = event.getSubject() + " " + event.getRelationType() + " " + event.getObject();
            event.setEventDescription(description);

            // 设置事件类型
            event.setEventType("GDELT事件");

            // 设置来源类型（1-自动获取）
            event.setSourceType(1);

            // 设置状态（1-启用）
            event.setStatus(1);

            // 设置关键词
            event.setKeywords(Arrays.asList("GDELT", "国际关系", "事件数据"));

            // 设置创建和更新时间
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            events.add(event);
        }

        return events;
    }
}