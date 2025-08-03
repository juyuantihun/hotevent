package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek原始测试服务
 * 用于直接测试API调用和获取原始响应
 */
@Slf4j
@Service
public class DeepSeekRawTestService {

    @Value("${app.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${app.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${app.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${app.deepseek.web-search.enabled:true}")
    private boolean webSearchEnabled;

    @Value("${app.deepseek.web-search.max-results:10}")
    private int webSearchMaxResults;

    @Value("${app.deepseek.web-search.search-timeout:30000}")
    private int webSearchTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 直接测试DeepSeek API调用
     */
    public Map<String, Object> testRawApiCall(String query) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始原始API测试: query={}", query);
            
            // 构建提示词
            String prompt = buildTestPrompt(query);
            log.info("构建的提示词: {}", prompt);
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 调用API
            String rawResponse = callDeepSeekAPI(prompt);
            
            // 记录结束时间
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 构建结果
            result.put("success", true);
            result.put("query", query);
            result.put("prompt", prompt);
            result.put("promptLength", prompt.length());
            result.put("responseTime", responseTime);
            result.put("rawResponse", rawResponse);
            result.put("responseLength", rawResponse != null ? rawResponse.length() : 0);
            result.put("hasResponse", rawResponse != null && !rawResponse.trim().isEmpty());
            
            // 分析响应
            if (rawResponse != null && !rawResponse.trim().isEmpty()) {
                result.put("analysis", analyzeResponse(rawResponse));
            } else {
                result.put("analysis", "响应为空");
            }
            
            log.info("原始API测试完成: responseTime={}ms, responseLength={}", 
                    responseTime, rawResponse != null ? rawResponse.length() : 0);
            
        } catch (Exception e) {
            log.error("原始API测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 构建测试提示词
     */
    private String buildTestPrompt(String query) {
        return String.format(
            "请使用联网搜索功能，查找关于\"%s\"的最新事件信息。\n" +
            "\n" +
            "【联网搜索要求】\n" +
            "- 请务必使用联网搜索功能获取最新、最准确的事件信息\n" +
            "- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）\n" +
            "- 确保事件信息的时效性和真实性\n" +
            "\n" +
            "【返回格式要求】\n" +
            "请严格按照以下JSON格式返回，不要添加任何其他内容：\n" +
            "\n" +
            "{\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"title\": \"事件标题\",\n" +
            "      \"description\": \"详细描述\",\n" +
            "      \"eventTime\": \"2024-01-01T12:00:00\",\n" +
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
            "如果没有找到相关事件，请返回：{\"events\": []}\n", query);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            // 添加联网搜索配置 - 使用正确的DeepSeek API格式
            if (webSearchEnabled) {
                // 方法1: 尝试官方文档格式
                requestBody.put("web_search", true);
                
                // 方法2: 尝试详细配置格式
                Map<String, Object> webSearchConfig = new HashMap<>();
                webSearchConfig.put("enabled", true);
                webSearchConfig.put("max_results", webSearchMaxResults);
                webSearchConfig.put("timeout_ms", webSearchTimeout);
                requestBody.put("web_search_config", webSearchConfig);
                
                // 方法3: 尝试tools格式
                List<Map<String, Object>> tools = new ArrayList<>();
                Map<String, Object> webSearchTool = new HashMap<>();
                webSearchTool.put("type", "web_search");
                Map<String, Object> webSearchFunction = new HashMap<>();
                webSearchFunction.put("name", "web_search");
                webSearchFunction.put("description", "Search the web for current information");
                webSearchTool.put("function", webSearchFunction);
                tools.add(webSearchTool);
                requestBody.put("tools", tools);
                
                log.info("启用联网搜索: webSearch=true, maxResults={}, timeout={}ms", webSearchMaxResults, webSearchTimeout);
                log.info("请求体包含联网搜索配置: web_search=true, web_search_config={}, tools={}", 
                        true, webSearchConfig, tools.size());
            }

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            // 记录请求信息
            log.info("API请求信息: url={}, model={}, webSearch={}", deepseekApiUrl, model, webSearchEnabled);
            log.debug("请求体: {}", objectMapper.writeValueAsString(requestBody));

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepseekApiUrl, requestEntity, String.class);

            // 记录响应信息
            log.info("API响应状态: {}", response.getStatusCode());
            log.info("响应头: {}", response.getHeaders());

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("原始响应长度: {}", responseBody.length());
                log.info("原始响应内容: {}", responseBody.length() > 1000 ? 
                    responseBody.substring(0, 1000) + "..." : responseBody);

                // 解析响应获取content
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                    
                    if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof List) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                        if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                            if (messageObj.containsKey("content")) {
                                String content = (String) messageObj.get("content");
                                log.info("提取的内容长度: {}", content.length());
                                log.info("提取的内容: {}", content);
                                return content;
                            }
                        }
                    }
                    
                    log.warn("响应格式不符合预期: {}", responseBody);
                    return responseBody; // 返回原始响应
                    
                } catch (Exception e) {
                    log.error("解析响应JSON失败", e);
                    return responseBody; // 返回原始响应
                }
            } else {
                log.error("API调用失败: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                return null;
            }

        } catch (Exception e) {
            log.error("调用DeepSeek API异常", e);
            throw new RuntimeException("API调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分析响应内容
     */
    private Map<String, Object> analyzeResponse(String response) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (response == null || response.trim().isEmpty()) {
            analysis.put("type", "empty");
            analysis.put("description", "响应为空");
            return analysis;
        }
        
        String trimmed = response.trim();
        
        // 检查是否是JSON格式
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            analysis.put("type", "json_object");
            analysis.put("description", "JSON对象格式");
            
            try {
                Map<String, Object> jsonObj = objectMapper.readValue(trimmed, Map.class);
                analysis.put("valid_json", true);
                analysis.put("keys", jsonObj.keySet());
                
                if (jsonObj.containsKey("events")) {
                    Object events = jsonObj.get("events");
                    if (events instanceof List) {
                        analysis.put("has_events", true);
                        analysis.put("events_count", ((List<?>) events).size());
                    } else {
                        analysis.put("has_events", false);
                        analysis.put("events_type", events != null ? events.getClass().getSimpleName() : "null");
                    }
                } else {
                    analysis.put("has_events", false);
                }
                
            } catch (Exception e) {
                analysis.put("valid_json", false);
                analysis.put("json_error", e.getMessage());
            }
            
        } else if (trimmed.contains("```json")) {
            analysis.put("type", "markdown_json");
            analysis.put("description", "Markdown代码块中的JSON");
            
        } else if (trimmed.contains("```")) {
            analysis.put("type", "markdown_code");
            analysis.put("description", "Markdown代码块");
            
        } else if (trimmed.contains("事件") || trimmed.contains("Event")) {
            analysis.put("type", "text_with_events");
            analysis.put("description", "包含事件信息的文本");
            
        } else {
            analysis.put("type", "plain_text");
            analysis.put("description", "纯文本响应");
        }
        
        // 统计信息
        analysis.put("length", response.length());
        analysis.put("lines", response.split("\n").length);
        analysis.put("contains_events", response.contains("事件") || response.contains("Event"));
        analysis.put("contains_json", response.contains("{") && response.contains("}"));
        analysis.put("contains_markdown", response.contains("```"));
        
        return analysis;
    }
}