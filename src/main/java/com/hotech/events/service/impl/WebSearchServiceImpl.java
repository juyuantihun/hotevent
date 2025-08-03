package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.service.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 联网搜索服务实现类
 */
@Slf4j
@Service
public class WebSearchServiceImpl implements WebSearchService {

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

    @Value("${app.deepseek.web-search.enabled:true}")
    private boolean webSearchEnabled;

    @Value("${app.deepseek.web-search.max-results:10}")
    private int webSearchMaxResults;

    @Value("${app.deepseek.web-search.search-timeout:30000}")
    private int webSearchTimeout;

    @Autowired
    private com.hotech.events.config.DynamicApiConfigManager dynamicApiConfigManager;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // 统计信息
    private final AtomicLong totalSearchRequests = new AtomicLong(0);
    private final AtomicLong successfulSearchRequests = new AtomicLong(0);
    private final AtomicLong failedSearchRequests = new AtomicLong(0);
    private final AtomicLong totalSearchTime = new AtomicLong(0);

    // 搜索缓存
    private final Map<String, Object> searchCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private final long cacheExpirationTime = 300000; // 5分钟缓存

    @Override
    public boolean isWebSearchAvailable() {
        return webSearchEnabled && deepseekApiKey != null && !deepseekApiKey.isEmpty();
    }

    @Override
    public void enableWebSearch() {
        this.webSearchEnabled = true;
        log.info("联网搜索已启用");
    }

    @Override
    public void disableWebSearch() {
        this.webSearchEnabled = false;
        log.info("联网搜索已禁用");
    }

    @Override
    public Map<String, Object> getWebSearchConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", webSearchEnabled);
        config.put("maxResults", webSearchMaxResults);
        config.put("searchTimeout", webSearchTimeout);
        config.put("apiUrl", deepseekApiUrl);
        config.put("model", model);
        config.put("hasApiKey", deepseekApiKey != null && !deepseekApiKey.isEmpty());
        return config;
    }

    @Override
    public void updateWebSearchConfig(Map<String, Object> config) {
        if (config.containsKey("enabled")) {
            this.webSearchEnabled = (Boolean) config.get("enabled");
        }
        if (config.containsKey("maxResults")) {
            this.webSearchMaxResults = ((Number) config.get("maxResults")).intValue();
        }
        if (config.containsKey("searchTimeout")) {
            this.webSearchTimeout = ((Number) config.get("searchTimeout")).intValue();
        }
        
        log.info("联网搜索配置已更新: enabled={}, maxResults={}, timeout={}ms", 
                webSearchEnabled, webSearchMaxResults, webSearchTimeout);
    }

    @Override
    public Map<String, Object> testWebSearch(String query) {
        Map<String, Object> result = new HashMap<>();
        result.put("testTime", LocalDateTime.now());
        result.put("query", query);

        if (!isWebSearchAvailable()) {
            result.put("success", false);
            result.put("error", "联网搜索功能不可用");
            return result;
        }

        try {
            long startTime = System.currentTimeMillis();
            
            // 构建测试提示词
            String testPrompt = "请使用联网搜索功能查找关于 \"" + query + "\" 的最新信息，并简要总结。";
            
            // 调用API
            String response = callDeepSeekAPIWithWebSearch(testPrompt);
            
            long responseTime = System.currentTimeMillis() - startTime;

            if (response != null && !response.isEmpty()) {
                result.put("success", true);
                result.put("responseTime", responseTime);
                result.put("response", response);
                result.put("responseLength", response.length());
            } else {
                result.put("success", false);
                result.put("error", "API返回空响应");
                result.put("responseTime", responseTime);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("测试联网搜索功能失败", e);
        }

        return result;
    }

    @Override
    public Map<String, Object> getWebSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", totalSearchRequests.get());
        stats.put("successfulRequests", successfulSearchRequests.get());
        stats.put("failedRequests", failedSearchRequests.get());
        stats.put("totalSearchTime", totalSearchTime.get());
        
        long totalReq = totalSearchRequests.get();
        if (totalReq > 0) {
            stats.put("averageResponseTime", (double) totalSearchTime.get() / totalReq);
            stats.put("successRate", (double) successfulSearchRequests.get() / totalReq * 100);
        } else {
            stats.put("averageResponseTime", 0.0);
            stats.put("successRate", 0.0);
        }
        
        stats.put("cacheSize", searchCache.size());
        stats.put("isEnabled", webSearchEnabled);
        stats.put("isAvailable", isWebSearchAvailable());
        
        return stats;
    }

    @Override
    public void clearWebSearchCache() {
        searchCache.clear();
        cacheTimestamps.clear();
        log.info("联网搜索缓存已清空");
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
     * 调用DeepSeek API并根据配置启用联网搜索
     */
    private String callDeepSeekAPIWithWebSearch(String prompt) {
        long startTime = System.currentTimeMillis();
        totalSearchRequests.incrementAndGet();

        try {
            // 获取当前API配置
            ApiConfig apiConfig = getCurrentApiConfig();
            
            // 检查缓存
            String cacheKey = generateCacheKey(prompt);
            Object cachedResult = getCachedResult(cacheKey);
            if (cachedResult != null) {
                return (String) cachedResult;
            }

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", apiConfig.model);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            // 如果支持联网搜索且启用了联网搜索，则添加web_search参数
            if (apiConfig.supportsWebSearch && webSearchEnabled) {
                Map<String, Object> webSearch = new HashMap<>();
                webSearch.put("enable", true);
                webSearch.put("max_results", webSearchMaxResults);
                webSearch.put("timeout", webSearchTimeout);
                requestBody.put("web_search", webSearch);
            }

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiConfig.apiKey);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.debug("调用DeepSeek API: apiUrl={}, model={}, webSearch={}, promptLength={}", 
                    apiConfig.apiUrl, apiConfig.model, apiConfig.supportsWebSearch && webSearchEnabled, prompt.length());

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiConfig.apiUrl, requestEntity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            totalSearchTime.addAndGet(responseTime);

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
                            
                            // 缓存结果
                            cacheResult(cacheKey, content);
                            
                            successfulSearchRequests.incrementAndGet();
                            log.info("联网搜索API调用成功: responseTime={}ms, responseLength={}", 
                                    responseTime, content.length());
                            return content;
                        }
                    }
                }
            }

            failedSearchRequests.incrementAndGet();
            log.error("联网搜索API调用失败: status={}, body={}", 
                    response.getStatusCode(), response.getBody());

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            totalSearchTime.addAndGet(responseTime);
            failedSearchRequests.incrementAndGet();
            log.error("调用联网搜索API异常", e);
            throw new RuntimeException("联网搜索API调用失败: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String prompt) {
        return "websearch_" + prompt.hashCode();
    }

    /**
     * 获取缓存结果
     */
    private Object getCachedResult(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp != null && System.currentTimeMillis() - timestamp < cacheExpirationTime) {
            return searchCache.get(cacheKey);
        } else {
            // 清除过期缓存
            searchCache.remove(cacheKey);
            cacheTimestamps.remove(cacheKey);
            return null;
        }
    }

    /**
     * 缓存结果
     */
    private void cacheResult(String cacheKey, Object result) {
        searchCache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
    }
}