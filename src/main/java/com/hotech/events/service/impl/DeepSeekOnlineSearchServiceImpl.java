package com.hotech.events.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.entity.Region;
import com.hotech.events.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeepSeek联网搜索服务实现类
 * 基于火山方舟API，支持联网搜索功能
 */
@Slf4j
@Service("deepSeekOnlineSearchService")
public class DeepSeekOnlineSearchServiceImpl implements DeepSeekService {

    // 官方API配置（不支持联网搜索）
    @Value("${app.deepseek.official.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String officialApiUrl;

    @Value("${app.deepseek.official.api-key:}")
    private String officialApiKey;

    @Value("${app.deepseek.official.model:deepseek-chat}")
    private String officialModel;

    // 火山引擎API配置（支持联网搜索）
    @Value("${app.deepseek.volcengine.api-url:https://ark.cn-beijing.volces.com/api/v3/bots/chat/completions}")
    private String volcengineApiUrl;

    @Value("${app.deepseek.volcengine.api-key:}")
    private String volcengineApiKey;

    @Value("${app.deepseek.volcengine.model:bot-20250725163638-5gn4n}")
    private String volcengineModel;

    // 默认配置（向后兼容）
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
    private boolean webSearchEnabled;

    @Value("${app.deepseek.web-search.max-results:10}")
    private int webSearchMaxResults;

    @Value("${app.deepseek.web-search.search-timeout:30000}")
    private int webSearchTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Map<String, Object>> EVENT_CACHE = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    private com.hotech.events.mapper.EventMapper eventMapper;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.hotech.events.mapper.EventRelationMapper eventRelationMapper;

    @Override
    public Boolean checkConnection() {
        log.info("检查DeepSeek联网搜索API连接状态");

        try {
            String testPrompt = "请简单介绍一下你的功能，用中文回答。";
            String response = callDeepSeekAPI(testPrompt);

            boolean isConnected = response != null && !response.isEmpty();
            log.info("DeepSeek联网搜索API连接状态: {}", isConnected ? "正常" : "异常");
            return isConnected;
        } catch (Exception e) {
            log.error("检查DeepSeek联网搜索API连接状态时发生错误", e);
            return false;
        }
    }

    /**
     * 获取当前API配置
     */
    private ApiConfig getCurrentApiConfig() {
        if (webSearchEnabled) {
            // 启用联网搜索时使用火山引擎API
            return new ApiConfig(volcengineApiUrl, volcengineApiKey, volcengineModel, true);
        } else {
            // 不启用联网搜索时使用官方API
            return new ApiConfig(officialApiUrl, officialApiKey, officialModel, false);
        }
    }

    /**
     * API配置类
     */
    private static class ApiConfig {
        final String apiUrl;
        final String apiKey;
        final String model;
        final boolean supportsWebSearch;

        ApiConfig(String apiUrl, String apiKey, String model, boolean supportsWebSearch) {
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.model = model;
            this.supportsWebSearch = supportsWebSearch;
        }
    }

    /**
     * 调用DeepSeek联网搜索API
     * 
     * @param prompt 提示词
     * @return API响应内容
     */
    private String callDeepSeekAPI(String prompt) {
        try {
            // 获取当前API配置
            ApiConfig apiConfig = getCurrentApiConfig();
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", apiConfig.model);
            
            // 火山引擎API和官方API的参数略有不同
            if (apiConfig.supportsWebSearch) {
                // 火山引擎API - 不需要额外的web_search参数，默认支持联网搜索
                // 只需要基本参数
            } else {
                // 官方API - 添加标准参数
                requestBody.put("max_tokens", maxTokens);
                requestBody.put("temperature", temperature);
                requestBody.put("stream", false);
            }

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 火山引擎API只需要用户消息，不需要系统消息
            if (!apiConfig.supportsWebSearch) {
                // 官方API - 添加系统消息
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", "你是一个专业的事件分析助手，能够准确分析和处理各种国际事件数据。");
                messages.add(systemMessage);
            }
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiConfig.apiKey);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 创建RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            String apiType = apiConfig.supportsWebSearch ? "火山引擎联网搜索API" : "DeepSeek官方API";
            log.info("调用{}: apiUrl={}, model={}, webSearch={}, promptLength={}", 
                    apiType, apiConfig.apiUrl, apiConfig.model, apiConfig.supportsWebSearch, prompt.length());

            // 🔍 详细调试日志：打印完整的提示词内容
            log.info("🔍 [调试] {} 完整提示词内容:", apiType);
            log.info("🔍 [提示词开始] ==========================================");
            log.info("{}", prompt);
            log.info("🔍 [提示词结束] ==========================================");
            
            // 🔍 详细调试日志：打印请求体
            try {
                log.info("🔍 [调试] {} 请求体: {}", apiType, objectMapper.writeValueAsString(requestBody));
            } catch (Exception e) {
                log.warn("无法序列化请求体: {}", e.getMessage());
            }

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiConfig.apiUrl,
                    requestEntity,
                    String.class);

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 🔍 详细调试日志：打印完整的API响应
                log.info("🔍 [调试] {}响应状态: {}", apiType, response.getStatusCode());
                log.info("🔍 [调试] {}响应长度: {} 字符", apiType, response.getBody().length());
                log.info("🔍 [调试] {}完整响应内容:", apiType);
                log.info("🔍 [响应开始] ==========================================");
                log.info("{}", response.getBody());
                log.info("🔍 [响应结束] ==========================================");
                
                // 解析响应
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);

                // 获取生成的内容
                if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof List) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                        if (messageObj.containsKey("content")) {
                            String content = (String) messageObj.get("content");
                            log.info("DeepSeek API调用成功: 使用{}API, 响应长度: {}", 
                                    apiConfig.supportsWebSearch ? "火山引擎联网搜索" : "官方", content.length());
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
        log.info("使用联网搜索获取最新事件, limit={}", limit);

        try {
            String prompt = buildLatestEventsPrompt(limit);
            String response = callDeepSeekAPI(prompt);
            
            if (response != null && !response.isEmpty()) {
                return parseEventsFromResponse(response);
            } else {
                log.warn("联网搜索返回空数据，使用模拟数据作为备份");
                return simulateFetchLatestEvents(limit);
            }
        } catch (Exception e) {
            log.error("使用联网搜索获取最新事件失败", e);
            return simulateFetchLatestEvents(limit);
        }
    }

    @Override
    public List<EventDTO> fetchEventsByKeywords(List<String> keywords, int limit) {
        log.info("使用联网搜索根据关键词获取事件, keywords={}, limit={}", keywords, limit);

        try {
            String prompt = buildKeywordEventsPrompt(keywords, limit);
            String response = callDeepSeekAPI(prompt);
            
            if (response != null && !response.isEmpty()) {
                return parseEventsFromResponse(response);
            } else {
                log.warn("联网搜索返回空数据，使用模拟数据作为备份");
                return simulateFetchEventsByKeywords(keywords, limit);
            }
        } catch (Exception e) {
            log.error("使用联网搜索根据关键词获取事件失败", e);
            return simulateFetchEventsByKeywords(keywords, limit);
        }
    }

    @Override
    public List<EventDTO> fetchEventsByDateRange(String startDate, String endDate, int limit) {
        log.info("使用联网搜索根据日期范围获取事件, startDate={}, endDate={}, limit={}", startDate, endDate, limit);

        try {
            String prompt = buildDateRangeEventsPrompt(startDate, endDate, limit);
            String response = callDeepSeekAPI(prompt);
            
            if (response != null && !response.isEmpty()) {
                return parseEventsFromResponse(response);
            } else {
                log.warn("联网搜索返回空数据，使用模拟数据作为备份");
                return simulateFetchEventsByDateRange(LocalDateTime.parse(startDate + "T00:00:00"), 
                                                    LocalDateTime.parse(endDate + "T23:59:59"), limit);
            }
        } catch (Exception e) {
            log.error("使用联网搜索根据日期范围获取事件失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<EventDTO> parseGdeltData(String gdeltData) {
        log.info("解析GDELT数据, 数据长度={}", gdeltData.length());
        // 这个方法保持原有实现
        return simulateParseGdeltData(gdeltData);
    }

    @Override
    public Map<String, Object> generateEventAnalysis(List<Map<String, Object>> events, String prompt) {
        log.info("使用联网搜索生成事件分析, eventCount={}, promptLength={}", events.size(), prompt.length());

        try {
            String analysisPrompt = buildEventAnalysisPrompt(events, prompt);
            String response = callDeepSeekAPI(analysisPrompt);

            if (response == null || response.isEmpty()) {
                log.warn("DeepSeek联网搜索API返回空数据，返回默认分析结果");
                return createDefaultAnalysisResult(events, prompt);
            }

            try {
                // 尝试解析为JSON格式
                Map<String, Object> analysisResult = objectMapper.readValue(response, Map.class);
                log.info("成功生成事件分析结果");
                return analysisResult;
            } catch (JsonProcessingException e) {
                log.warn("API响应不是JSON格式，将作为文本处理");
                // 如果不是JSON格式，将响应作为分析内容返回
                Map<String, Object> result = new HashMap<>();
                result.put("analysis", response);
                result.put("eventCount", events.size());
                result.put("prompt", prompt);
                result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                return result;
            }
        } catch (Exception e) {
            log.error("生成事件分析失败", e);
            return createDefaultAnalysisResult(events, prompt);
        }
    }

    @Override
    public List<Map<String, Object>> fetchEvents(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("使用联网搜索从DeepSeek获取事件数据: regions={}, startTime={}, endTime={}", regions, startTime, endTime);

        EVENT_CACHE.clear();

        try {
            String prompt = buildEventFetchPrompt(regions, startTime, endTime);
            String response = callDeepSeekAPI(prompt);

            if (response == null || response.isEmpty()) {
                log.warn("DeepSeek联网搜索API返回空数据，使用数据库数据作为备份");
                List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
                return deduplicateEvents(events);
            }

            try {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

                if (!responseMap.containsKey("events")) {
                    log.error("DeepSeek联网搜索API返回数据格式错误: {}", response);
                    throw new RuntimeException("DeepSeek联网搜索API返回数据格式错误");
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> events = (List<Map<String, Object>>) responseMap.get("events");

                if (events == null || events.isEmpty()) {
                    log.warn("DeepSeek联网搜索API返回空事件列表，使用数据库数据作为备份");
                    events = simulateFetchEvents(regions, startTime, endTime);
                }

                return deduplicateEvents(events);
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek联网搜索API响应失败", e);
                log.warn("使用数据库数据作为备份");
                List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
                return deduplicateEvents(events);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek联网搜索API获取事件失败", e);
            log.warn("使用数据库数据作为备份");
            List<Map<String, Object>> events = simulateFetchEvents(regions, startTime, endTime);
            return deduplicateEvents(events);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeEventRelations(List<Map<String, Object>> events) {
        log.info("使用联网搜索分析事件关联关系: eventCount={}", events.size());

        return com.hotech.events.util.BatchProcessor.processBatch(
                events,
                100,
                this::analyzeEventRelationsBatch);
    }

    @Override
    public List<Map<String, Object>> organizeTimelines(List<Map<String, Object>> events,
            List<Map<String, Object>> relations) {
        log.info("使用联网搜索组织时间线: eventCount={}, relationCount={}", events.size(), relations.size());

        try {
            String prompt = buildTimelineOrganizePrompt(events, relations);
            String response = callDeepSeekAPI(prompt);

            if (response == null || response.isEmpty()) {
                log.warn("DeepSeek联网搜索API返回空数据，使用模拟数据作为备份");
                return simulateOrganizeTimelines(events, relations);
            }

            try {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

                if (!responseMap.containsKey("timelines")) {
                    log.error("DeepSeek联网搜索API返回数据格式错误: {}", response);
                    throw new RuntimeException("DeepSeek联网搜索API返回数据格式错误");
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> timelines = (List<Map<String, Object>>) responseMap.get("timelines");

                if (timelines == null || timelines.isEmpty()) {
                    log.warn("DeepSeek联网搜索API返回空时间线列表，使用模拟数据作为备份");
                    timelines = simulateOrganizeTimelines(events, relations);
                }

                return timelines;
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek联网搜索API响应失败", e);
                log.warn("使用模拟数据作为备份");
                return simulateOrganizeTimelines(events, relations);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek联网搜索API组织时间线失败", e);
            log.warn("使用模拟数据作为备份");
            return simulateOrganizeTimelines(events, relations);
        }
    }

    /**
     * 构建获取最新事件的提示词
     */
    private String buildLatestEventsPrompt(int limit) {
        return String.format(
            "请使用联网搜索功能，查找最新的%d个国际重要事件。\n" +
            "\n" +
            "【联网搜索要求】\n" +
            "- 请务必使用联网搜索功能获取最新、最准确的事件信息\n" +
            "- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）\n" +
            "- 确保事件信息的时效性和真实性\n" +
            "- 搜索时间范围：最近7天内的事件\n" +
            "\n" +
            "【返回格式要求】\n" +
            "请严格按照以下JSON格式返回，不要添加任何其他内容：\n" +
            "\n" +
            "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"title\": \"事件标题\",\n" +
            "      \"description\": \"详细描述\",\n" +
            "      \"eventTime\": \"2025-01-25T12:00:00\",\n" +
            "      \"location\": \"具体地点\",\n" +
            "      \"subject\": \"事件主体\",\n" +
            "      \"object\": \"事件客体\",\n" +
            "      \"eventType\": \"事件类型\",\n" +
            "      \"keywords\": [\"关键词1\", \"关键词2\"],\n" +
            "      \"sources\": [\"来源1\", \"来源2\"],\n" +
            "      \"credibilityScore\": 0.95\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "\n" +
            "如果没有找到相关事件，请返回：{\"events\": []}\n", limit);
    }

    /**
     * 构建根据关键词获取事件的提示词
     */
    private String buildKeywordEventsPrompt(List<String> keywords, int limit) {
        String keywordStr = String.join("、", keywords);
        return String.format(
            "请使用联网搜索功能，查找关于\"%s\"的最新%d个相关事件。\n" +
            "\n" +
            "【联网搜索要求】\n" +
            "- 请务必使用联网搜索功能获取最新、最准确的事件信息\n" +
            "- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）\n" +
            "- 确保事件信息的时效性和真实性\n" +
            "- 重点关注与关键词相关的事件\n" +
            "\n" +
            "【返回格式要求】\n" +
            "请严格按照以下JSON格式返回，不要添加任何其他内容：\n" +
            "\n" +
            "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"title\": \"事件标题\",\n" +
            "      \"description\": \"详细描述\",\n" +
            "      \"eventTime\": \"2025-01-25T12:00:00\",\n" +
            "      \"location\": \"具体地点\",\n" +
            "      \"subject\": \"事件主体\",\n" +
            "      \"object\": \"事件客体\",\n" +
            "      \"eventType\": \"事件类型\",\n" +
            "      \"keywords\": [\"关键词1\", \"关键词2\"],\n" +
            "      \"sources\": [\"来源1\", \"来源2\"],\n" +
            "      \"credibilityScore\": 0.95\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "\n" +
            "如果没有找到相关事件，请返回：{\"events\": []}\n", keywordStr, limit);
    }

    /**
     * 构建根据日期范围获取事件的提示词
     */
    private String buildDateRangeEventsPrompt(String startDate, String endDate, int limit) {
        return String.format(
            "请使用联网搜索功能，查找%s到%s期间的%d个重要国际事件。\n" +
            "\n" +
            "【联网搜索要求】\n" +
            "- 请务必使用联网搜索功能获取最新、最准确的事件信息\n" +
            "- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）\n" +
            "- 确保事件信息的时效性和真实性\n" +
            "- 严格按照指定的时间范围搜索\n" +
            "\n" +
            "【返回格式要求】\n" +
            "请严格按照以下JSON格式返回，不要添加任何其他内容：\n" +
            "\n" +
            "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"title\": \"事件标题\",\n" +
            "      \"description\": \"详细描述\",\n" +
            "      \"eventTime\": \"2025-01-25T12:00:00\",\n" +
            "      \"location\": \"具体地点\",\n" +
            "      \"subject\": \"事件主体\",\n" +
            "      \"object\": \"事件客体\",\n" +
            "      \"eventType\": \"事件类型\",\n" +
            "      \"keywords\": [\"关键词1\", \"关键词2\"],\n" +
            "      \"sources\": [\"来源1\", \"来源2\"],\n" +
            "      \"credibilityScore\": 0.95\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "\n" +
            "如果没有找到相关事件，请返回：{\"events\": []}\n", startDate, endDate, limit);
    }

    /**
     * 构建事件获取提示词
     */
    private String buildEventFetchPrompt(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请使用联网搜索功能，根据以下条件生成相关的国际事件数据：\n\n");

        // 添加地区信息
        prompt.append("地区：");
        if (regions != null && !regions.isEmpty()) {
            prompt.append(regions.stream().map(Region::getName).collect(java.util.stream.Collectors.joining(", ")));
        } else {
            prompt.append("全球");
        }
        prompt.append("\n");

        // 添加时间范围
        prompt.append("时间范围：").append(startTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .append(" 至 ").append(endTime.format(DateTimeFormatter.ISO_DATE_TIME)).append("\n\n");

        // 添加联网搜索要求
        prompt.append("【联网搜索要求】\n");
        prompt.append("- 请务必使用联网搜索功能获取最新、最准确的事件信息\n");
        prompt.append("- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）\n");
        prompt.append("- 确保事件信息的时效性和真实性\n");
        prompt.append("- 重点关注指定地区和时间范围内的事件\n\n");

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
        prompt.append("      \"keywords\": [\"关键词1\", \"关键词2\"],\n");
        prompt.append("      \"sources\": [\"来源1\", \"来源2\"],\n");
        prompt.append("      \"credibilityScore\": 0.95\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("请生成20-30个符合条件的事件，确保事件内容真实可信，时间在指定范围内，地点与指定地区相关。");

        return prompt.toString();
    }

    /**
     * 批量分析事件关联关系
     */
    private List<Map<String, Object>> analyzeEventRelationsBatch(List<Map<String, Object>> eventsBatch) {
        log.info("使用联网搜索批量分析事件关联关系: batchSize={}", eventsBatch.size());

        try {
            String prompt = buildRelationAnalysisPrompt(eventsBatch);
            String response = callDeepSeekAPI(prompt);

            if (response == null || response.isEmpty()) {
                log.warn("DeepSeek联网搜索API返回空数据，使用模拟数据作为备份");
                return simulateAnalyzeEventRelations(eventsBatch);
            }

            try {
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

                if (!responseMap.containsKey("relations")) {
                    log.error("DeepSeek联网搜索API返回数据格式错误: {}", response);
                    throw new RuntimeException("DeepSeek联网搜索API返回数据格式错误");
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> relations = (List<Map<String, Object>>) responseMap.get("relations");

                if (relations == null || relations.isEmpty()) {
                    log.warn("DeepSeek联网搜索API返回空关系列表，使用模拟数据作为备份");
                    relations = simulateAnalyzeEventRelations(eventsBatch);
                }

                return relations;
            } catch (JsonProcessingException e) {
                log.error("解析DeepSeek联网搜索API响应失败", e);
                log.warn("使用模拟数据作为备份");
                return simulateAnalyzeEventRelations(eventsBatch);
            }
        } catch (Exception e) {
            log.error("调用DeepSeek联网搜索API批量分析事件关联关系失败", e);
            log.warn("使用模拟数据作为备份");
            return simulateAnalyzeEventRelations(eventsBatch);
        }
    }

    /**
     * 构建关系分析提示词
     */
    private String buildRelationAnalysisPrompt(List<Map<String, Object>> events) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请使用联网搜索功能，分析以下事件之间的因果关系，运用逻辑推理找出事件间的关联：\n\n");

        for (int i = 0; i < events.size(); i++) {
            Map<String, Object> event = events.get(i);
            prompt.append(String.format("事件%d：\n", i + 1));
            
            // 尝试多种可能的字段名
            Object id = getEventField(event, "id", "eventId", "ID");
            Object subject = getEventField(event, "subject", "主体", "eventSubject");
            Object object = getEventField(event, "object", "客体", "eventObject");
            Object type = getEventField(event, "type", "eventType", "类型");
            Object time = getEventField(event, "time", "eventTime", "时间");
            Object location = getEventField(event, "location", "eventLocation", "地点");
            Object description = getEventField(event, "description", "eventDescription", "描述");
            Object title = getEventField(event, "title", "eventTitle", "标题");
            
            prompt.append(String.format("  ID: %s\n", id != null ? id : "未知"));
            prompt.append(String.format("  标题: %s\n", title != null ? title : "未知"));
            prompt.append(String.format("  主体: %s\n", subject != null ? subject : "未知"));
            prompt.append(String.format("  客体: %s\n", object != null ? object : "未知"));
            prompt.append(String.format("  类型: %s\n", type != null ? type : "未知"));
            prompt.append(String.format("  时间: %s\n", time != null ? time : "未知"));
            prompt.append(String.format("  地点: %s\n", location != null ? location : "未知"));
            prompt.append(String.format("  描述: %s\n", description != null ? description : "未知"));
            prompt.append("\n");
        }

        prompt.append("【联网搜索要求】\n");
        prompt.append("- 请使用联网搜索功能获取相关背景信息\n");
        prompt.append("- 查找事件之间的历史关联和因果关系\n");
        prompt.append("- 参考权威新闻源的分析报道\n\n");

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

    /**
     * 从事件Map中获取字段值，尝试多种可能的字段名
     */
    private Object getEventField(Map<String, Object> event, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = event.get(fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 构建时间线组织提示词
     */
    private String buildTimelineOrganizePrompt(List<Map<String, Object>> events, List<Map<String, Object>> relations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请使用联网搜索功能，根据以下事件和关系数据，组织成时间线：\n\n");

        // 添加事件信息
        prompt.append("事件数据：\n");
        for (int i = 0; i < Math.min(events.size(), 20); i++) {
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
        for (int i = 0; i < Math.min(relations.size(), 20); i++) {
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

        prompt.append("【联网搜索要求】\n");
        prompt.append("- 请使用联网搜索功能获取相关背景信息\n");
        prompt.append("- 查找事件的历史背景和发展脉络\n");
        prompt.append("- 参考权威新闻源的时间线报道\n\n");

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

    /**
     * 构建事件分析提示词
     */
    private String buildEventAnalysisPrompt(List<Map<String, Object>> events, String prompt) {
        StringBuilder analysisPrompt = new StringBuilder();
        analysisPrompt.append("请使用联网搜索功能，对以下事件进行深度分析：\n\n");

        // 添加用户的分析要求
        analysisPrompt.append("【分析要求】\n");
        analysisPrompt.append(prompt).append("\n\n");

        // 添加事件数据
        analysisPrompt.append("【事件数据】\n");
        for (int i = 0; i < Math.min(events.size(), 20); i++) {
            Map<String, Object> event = events.get(i);
            analysisPrompt.append(String.format("事件%d：\n", i + 1));
            analysisPrompt.append(String.format("  ID: %s\n", event.get("id")));
            analysisPrompt.append(String.format("  主体: %s\n", event.get("subject")));
            analysisPrompt.append(String.format("  客体: %s\n", event.get("object")));
            analysisPrompt.append(String.format("  类型: %s\n", event.get("type")));
            analysisPrompt.append(String.format("  时间: %s\n", event.get("time")));
            analysisPrompt.append(String.format("  地点: %s\n", event.get("location")));
            analysisPrompt.append(String.format("  描述: %s\n", event.get("description")));
            analysisPrompt.append("\n");
        }

        // 添加联网搜索要求
        analysisPrompt.append("【联网搜索要求】\n");
        analysisPrompt.append("- 请使用联网搜索功能获取相关背景信息和最新发展\n");
        analysisPrompt.append("- 查找权威新闻源的分析报道和专家观点\n");
        analysisPrompt.append("- 结合历史背景和当前形势进行综合分析\n");
        analysisPrompt.append("- 提供客观、准确、深入的分析结果\n\n");

        // 添加返回格式说明
        analysisPrompt.append("【返回格式】\n");
        analysisPrompt.append("请按照以下JSON格式返回分析结果：\n");
        analysisPrompt.append("{\n");
        analysisPrompt.append("  \"summary\": \"分析摘要\",\n");
        analysisPrompt.append("  \"keyFindings\": [\"关键发现1\", \"关键发现2\"],\n");
        analysisPrompt.append("  \"trends\": [\"趋势1\", \"趋势2\"],\n");
        analysisPrompt.append("  \"implications\": \"影响和意义\",\n");
        analysisPrompt.append("  \"recommendations\": [\"建议1\", \"建议2\"],\n");
        analysisPrompt.append("  \"confidence\": 0.85,\n");
        analysisPrompt.append("  \"sources\": [\"来源1\", \"来源2\"]\n");
        analysisPrompt.append("}\n\n");

        analysisPrompt.append("如果无法进行分析，请返回包含错误信息的JSON格式结果。");

        return analysisPrompt.toString();
    }

    /**
     * 创建默认分析结果
     */
    private Map<String, Object> createDefaultAnalysisResult(List<Map<String, Object>> events, String prompt) {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", "由于API调用失败，无法生成详细分析结果");
        result.put("keyFindings", Arrays.asList("API调用异常", "使用默认分析结果"));
        result.put("trends", Arrays.asList("无法获取最新趋势信息"));
        result.put("implications", "建议稍后重试或检查API配置");
        result.put("recommendations", Arrays.asList("检查网络连接", "验证API密钥", "稍后重试"));
        result.put("confidence", 0.0);
        result.put("sources", Arrays.asList("系统默认"));
        result.put("eventCount", events.size());
        result.put("prompt", prompt);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        result.put("status", "fallback");
        
        return result;
    }

    /**
     * 从API响应中解析事件
     */
    private List<EventDTO> parseEventsFromResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            if (!responseMap.containsKey("events")) {
                log.warn("响应中没有events字段");
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> eventMaps = (List<Map<String, Object>>) responseMap.get("events");
            
            List<EventDTO> events = new ArrayList<>();
            for (Map<String, Object> eventMap : eventMaps) {
                EventDTO event = new EventDTO();
                
                // 映射到EventDTO的实际字段
                event.setEventDescription((String) eventMap.get("description"));
                
                // 处理事件时间
                String eventTimeStr = (String) eventMap.get("eventTime");
                if (eventTimeStr != null) {
                    try {
                        event.setEventTime(LocalDateTime.parse(eventTimeStr));
                    } catch (Exception e) {
                        log.warn("解析事件时间失败: {}，使用当前时间作为默认值", eventTimeStr);
                        event.setEventTime(LocalDateTime.now());
                        event.setEventTime(LocalDateTime.now());
                    }
                } else {
                    event.setEventTime(LocalDateTime.now());
                }
                
                event.setEventLocation((String) eventMap.get("location"));
                event.setSubject((String) eventMap.get("subject"));
                event.setObject((String) eventMap.get("object"));
                event.setEventType((String) eventMap.get("eventType"));
                
                // 处理关键词
                @SuppressWarnings("unchecked")
                List<String> keywords = (List<String>) eventMap.get("keywords");
                event.setKeywords(keywords);
                
                // 设置默认值
                event.setSourceType(1); // 自动获取
                event.setStatus(1); // 启用
                event.setCreatedAt(LocalDateTime.now());
                event.setCreatedBy("DeepSeek联网搜索");
                
                // 生成事件编码
                String eventCode = "DS_" + System.currentTimeMillis() + "_" + events.size();
                event.setEventCode(eventCode);
                
                events.add(event);
            }
            
            log.info("成功解析 {} 个事件", events.size());
            return events;
        } catch (Exception e) {
            log.error("解析事件响应失败", e);
            return new ArrayList<>();
        }
    }

    // 以下是备用的模拟方法，保持原有实现
    private List<EventDTO> simulateFetchLatestEvents(int limit) {
        // 原有的模拟实现
        return new ArrayList<>();
    }

    private List<EventDTO> simulateFetchEventsByKeywords(List<String> keywords, int limit) {
        // 原有的模拟实现
        return new ArrayList<>();
    }

    private List<EventDTO> simulateFetchEventsByDateRange(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        // 原有的模拟实现
        return new ArrayList<>();
    }

    private List<EventDTO> simulateParseGdeltData(String gdeltData) {
        // 原有的模拟实现
        return new ArrayList<>();
    }

    private List<Map<String, Object>> simulateFetchEvents(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime) {
        // 从数据库获取事件数据的实现
        try {
            List<String> regionNames = new ArrayList<>();
            if (regions != null && !regions.isEmpty()) {
                for (Region region : regions) {
                    regionNames.add(region.getName());
                }
            }
            
            List<Map<String, Object>> events = eventMapper.findEventsByConditions(regionNames, startTime, endTime);
            
            if (events == null || events.isEmpty()) {
                log.warn("数据库中未找到符合条件的事件数据，将查询所有事件");
                events = eventMapper.findAllEvents();
            }
            
            if (events == null || events.isEmpty()) {
                log.warn("数据库中未找到任何事件数据");
                return new ArrayList<>();
            }
            
            // 处理事件数据，确保格式一致
            for (Map<String, Object> event : events) {
                if (!event.containsKey("id")) {
                    event.put("id", event.get("id"));
                }
                
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
                
                if (!event.containsKey("type")) {
                    event.put("type", event.get("event_type"));
                }
                
                if (!event.containsKey("description")) {
                    event.put("description", event.get("event_description"));
                }
                
                if (!event.containsKey("location")) {
                    event.put("location", event.get("event_location"));
                }
                
                if (!event.containsKey("subject")) {
                    event.put("subject", event.get("subject"));
                }
                if (!event.containsKey("object")) {
                    event.put("object", event.get("object"));
                }
                
                if (!event.containsKey("keywords")) {
                    event.put("keywords", Arrays.asList("数据库事件", "真实数据"));
                }
            }
            
            log.info("从数据库获取到 {} 条事件数据", events.size());
            return events;
        } catch (Exception e) {
            log.error("从数据库获取事件数据失败", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> simulateAnalyzeEventRelations(List<Map<String, Object>> events) {
        // 从数据库获取事件关联关系的实现
        try {
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
            
            if (eventIds.isEmpty()) {
                log.warn("没有有效的事件ID，无法查询关联关系");
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> relations = eventRelationMapper.findRelationsByEventIds(eventIds);
            
            if (relations == null || relations.isEmpty()) {
                log.warn("数据库中未找到事件关联关系，将查询所有关系");
                relations = eventRelationMapper.findAllRelations();
            }
            
            if (relations == null || relations.isEmpty()) {
                log.warn("数据库中未找到任何事件关联关系");
                return new ArrayList<>();
            }
            
            // 处理关系数据，确保格式一致
            for (Map<String, Object> relation : relations) {
                if (!relation.containsKey("id")) {
                    relation.put("id", relation.get("id"));
                }
                
                if (!relation.containsKey("sourceEventId")) {
                    relation.put("sourceEventId", relation.get("source_event_id"));
                }
                
                if (!relation.containsKey("targetEventId")) {
                    relation.put("targetEventId", relation.get("target_event_id"));
                }
                
                if (!relation.containsKey("type")) {
                    relation.put("type", relation.get("relation_type"));
                }
                
                if (!relation.containsKey("strength")) {
                    Object confidence = relation.get("confidence");
                    if (confidence != null) {
                        if (confidence instanceof Number) {
                            relation.put("strength", ((Number) confidence).intValue());
                        } else {
                            try {
                                relation.put("strength", Integer.valueOf(confidence.toString()));
                            } catch (NumberFormatException e) {
                                relation.put("strength", 3);
                            }
                        }
                    } else {
                        relation.put("strength", 3);
                    }
                }
                
                if (!relation.containsKey("description")) {
                    relation.put("description", relation.get("description"));
                }
            }
            
            log.info("从数据库获取到 {} 条事件关联关系", relations.size());
            return relations;
        } catch (Exception e) {
            log.error("从数据库获取事件关联关系失败", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> simulateOrganizeTimelines(List<Map<String, Object>> events, List<Map<String, Object>> relations) {
        // 简单的时间线组织实现
        List<Map<String, Object>> timelines = new ArrayList<>();
        
        Map<String, Object> timeline = new HashMap<>();
        timeline.put("id", 1);
        timeline.put("name", "默认时间线");
        timeline.put("description", "基于数据库数据的默认时间线");
        
        List<Object> eventIds = new ArrayList<>();
        for (Map<String, Object> event : events) {
            eventIds.add(event.get("id"));
        }
        timeline.put("events", eventIds);
        
        List<Object> relationIds = new ArrayList<>();
        for (Map<String, Object> relation : relations) {
            relationIds.add(relation.get("id"));
        }
        timeline.put("relations", relationIds);
        
        timelines.add(timeline);
        
        return timelines;
    }

    /**
     * 事件去重
     */
    private List<Map<String, Object>> deduplicateEvents(List<Map<String, Object>> events) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> event : events) {
            String key = buildEventKey(event);

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
}