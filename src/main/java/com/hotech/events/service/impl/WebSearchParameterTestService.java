package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 联网搜索参数测试服务
 * 用于测试不同的DeepSeek联网搜索参数格式
 */
@Slf4j
@Service
public class WebSearchParameterTestService {

    @Value("${app.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${app.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${app.deepseek.model:deepseek-chat}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 测试不同的联网搜索参数格式
     */
    public Map<String, Object> testDifferentWebSearchFormats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> testResults = new ArrayList<>();
        
        String testQuery = "2025年7月最新国际新闻";
        
        // 测试格式1: 简单布尔值
        testResults.add(testWebSearchFormat("format1_boolean", testQuery, this::addBooleanWebSearch));
        
        // 测试格式2: 对象格式 - enable
        testResults.add(testWebSearchFormat("format2_object_enable", testQuery, this::addObjectWebSearchEnable));
        
        // 测试格式3: 对象格式 - enabled
        testResults.add(testWebSearchFormat("format3_object_enabled", testQuery, this::addObjectWebSearchEnabled));
        
        // 测试格式4: tools格式
        testResults.add(testWebSearchFormat("format4_tools", testQuery, this::addToolsWebSearch));
        
        // 测试格式5: 无联网搜索（对照组）
        testResults.add(testWebSearchFormat("format5_no_websearch", testQuery, null));
        
        result.put("testQuery", testQuery);
        result.put("testTime", LocalDateTime.now());
        result.put("totalTests", testResults.size());
        result.put("results", testResults);
        
        // 分析结果
        result.put("analysis", analyzeTestResults(testResults));
        
        return result;
    }

    /**
     * 测试特定的联网搜索格式
     */
    private Map<String, Object> testWebSearchFormat(String formatName, String query, WebSearchConfigurer configurer) {
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("format", formatName);
        testResult.put("query", query);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            // 添加联网搜索配置
            if (configurer != null) {
                configurer.configure(requestBody);
            }
            
            // 构建消息
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", String.format(
                "请告诉我关于'%s'的信息。如果你能访问互联网，请提供最新的信息并明确说明信息来源和时间。" +
                "如果你无法访问互联网，请明确说明你只能提供训练数据中的信息。", query));
            messages.add(message);
            requestBody.put("messages", messages);
            
            // 记录请求配置
            testResult.put("requestConfig", getRequestConfigSummary(requestBody));
            
            // 发送请求
            String response = callAPI(requestBody);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 分析响应
            testResult.put("success", true);
            testResult.put("responseTime", responseTime);
            testResult.put("response", response);
            testResult.put("responseLength", response != null ? response.length() : 0);
            testResult.put("analysis", analyzeResponse(response));
            
        } catch (Exception e) {
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            log.error("测试格式 {} 失败", formatName, e);
        }
        
        return testResult;
    }

    /**
     * 调用API
     */
    private String callAPI(Map<String, Object> requestBody) {
        try {
            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.debug("发送请求: {}", objectMapper.writeValueAsString(requestBody));

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    deepseekApiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应获取content
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                
                if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof List) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                        if (messageObj.containsKey("content")) {
                            return (String) messageObj.get("content");
                        }
                    }
                }
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("API调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分析响应内容
     */
    private Map<String, Object> analyzeResponse(String response) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (response == null || response.trim().isEmpty()) {
            analysis.put("hasWebSearch", false);
            analysis.put("reason", "响应为空");
            return analysis;
        }
        
        String lowerResponse = response.toLowerCase();
        
        // 检查是否提到了2025年的信息
        boolean mentions2025 = lowerResponse.contains("2025");
        analysis.put("mentions2025", mentions2025);
        
        // 检查是否提到了最新信息
        boolean mentionsLatest = lowerResponse.contains("最新") || lowerResponse.contains("latest") || 
                                lowerResponse.contains("recent") || lowerResponse.contains("current");
        analysis.put("mentionsLatest", mentionsLatest);
        
        // 检查是否提到了信息来源
        boolean mentionsSources = lowerResponse.contains("来源") || lowerResponse.contains("source") || 
                                 lowerResponse.contains("根据") || lowerResponse.contains("according");
        analysis.put("mentionsSources", mentionsSources);
        
        // 检查是否明确说明无法访问互联网
        boolean mentionsNoInternet = lowerResponse.contains("无法访问") || lowerResponse.contains("cannot access") ||
                                    lowerResponse.contains("训练数据") || lowerResponse.contains("training data") ||
                                    lowerResponse.contains("知识截止") || lowerResponse.contains("knowledge cutoff");
        analysis.put("mentionsNoInternet", mentionsNoInternet);
        
        // 综合判断是否使用了联网搜索
        boolean likelyUsedWebSearch = mentions2025 && mentionsLatest && mentionsSources && !mentionsNoInternet;
        analysis.put("likelyUsedWebSearch", likelyUsedWebSearch);
        
        // 置信度评分
        int score = 0;
        if (mentions2025) score += 30;
        if (mentionsLatest) score += 20;
        if (mentionsSources) score += 25;
        if (!mentionsNoInternet) score += 25;
        
        analysis.put("webSearchConfidence", score);
        
        return analysis;
    }

    /**
     * 分析所有测试结果
     */
    private Map<String, Object> analyzeTestResults(List<Map<String, Object>> testResults) {
        Map<String, Object> analysis = new HashMap<>();
        
        List<String> successfulFormats = new ArrayList<>();
        List<String> webSearchFormats = new ArrayList<>();
        String bestFormat = null;
        int bestScore = 0;
        
        for (Map<String, Object> result : testResults) {
            String format = (String) result.get("format");
            Boolean success = (Boolean) result.get("success");
            
            if (Boolean.TRUE.equals(success)) {
                successfulFormats.add(format);
                
                Map<String, Object> responseAnalysis = (Map<String, Object>) result.get("analysis");
                if (responseAnalysis != null) {
                    Boolean likelyUsedWebSearch = (Boolean) responseAnalysis.get("likelyUsedWebSearch");
                    Integer confidence = (Integer) responseAnalysis.get("webSearchConfidence");
                    
                    if (Boolean.TRUE.equals(likelyUsedWebSearch)) {
                        webSearchFormats.add(format);
                    }
                    
                    if (confidence != null && confidence > bestScore) {
                        bestScore = confidence;
                        bestFormat = format;
                    }
                }
            }
        }
        
        analysis.put("successfulFormats", successfulFormats);
        analysis.put("webSearchFormats", webSearchFormats);
        analysis.put("bestFormat", bestFormat);
        analysis.put("bestScore", bestScore);
        analysis.put("recommendation", generateRecommendation(webSearchFormats, bestFormat, bestScore));
        
        return analysis;
    }

    /**
     * 生成建议
     */
    private String generateRecommendation(List<String> webSearchFormats, String bestFormat, int bestScore) {
        if (webSearchFormats.isEmpty()) {
            return "所有格式都没有成功启用联网搜索，建议检查API密钥权限或模型支持";
        } else if (webSearchFormats.size() == 1) {
            return String.format("建议使用格式: %s (置信度: %d%%)", bestFormat, bestScore);
        } else {
            return String.format("多个格式可用，推荐使用: %s (置信度: %d%%)，备选: %s", 
                    bestFormat, bestScore, String.join(", ", webSearchFormats));
        }
    }

    /**
     * 获取请求配置摘要
     */
    private Map<String, Object> getRequestConfigSummary(Map<String, Object> requestBody) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("model", requestBody.get("model"));
        summary.put("hasWebSearch", requestBody.containsKey("web_search"));
        summary.put("hasWebSearchConfig", requestBody.containsKey("web_search_config"));
        summary.put("hasTools", requestBody.containsKey("tools"));
        return summary;
    }

    // 不同的联网搜索配置方法
    private void addBooleanWebSearch(Map<String, Object> requestBody) {
        requestBody.put("web_search", true);
    }

    private void addObjectWebSearchEnable(Map<String, Object> requestBody) {
        Map<String, Object> webSearch = new HashMap<>();
        webSearch.put("enable", true);
        webSearch.put("max_results", 10);
        webSearch.put("timeout", 30000);
        requestBody.put("web_search", webSearch);
    }

    private void addObjectWebSearchEnabled(Map<String, Object> requestBody) {
        Map<String, Object> webSearch = new HashMap<>();
        webSearch.put("enabled", true);
        webSearch.put("max_results", 10);
        webSearch.put("timeout_ms", 30000);
        requestBody.put("web_search", webSearch);
    }

    private void addToolsWebSearch(Map<String, Object> requestBody) {
        List<Map<String, Object>> tools = new ArrayList<>();
        Map<String, Object> webSearchTool = new HashMap<>();
        webSearchTool.put("type", "web_search");
        tools.add(webSearchTool);
        requestBody.put("tools", tools);
    }

    @FunctionalInterface
    private interface WebSearchConfigurer {
        void configure(Map<String, Object> requestBody);
    }
}