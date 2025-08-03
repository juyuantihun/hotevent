package com.hotech.events.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.config.DynamicApiConfigManager;
import com.hotech.events.dto.ApiHealthStatus;
import com.hotech.events.dto.TimeSegment;
import com.hotech.events.mapper.ApiCallRecordMapper;
import com.hotech.events.service.ApiMonitoringService;
import com.hotech.events.service.EnhancedApiCallManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 增强的API调用管理器实现
 * 提供动态API选择、重试机制和健康检查功能
 */
@Slf4j
@Service
public class EnhancedApiCallManagerImpl implements EnhancedApiCallManager {

    @Autowired
    private DynamicApiConfigManager dynamicApiConfigManager;
    
    @Autowired
    private ApiMonitoringService apiMonitoringService;
    
    @Autowired
    private ApiCallRecordMapper apiCallRecordMapper;
    
    @Value("${app.deepseek.max-tokens:2000}")
    private int maxTokens;
    
    @Value("${app.deepseek.temperature:0.7}")
    private double temperature;
    
    @Value("${app.api.retry.base-delay:1000}")
    private long baseRetryDelay;
    
    @Value("${app.api.retry.max-delay:10000}")
    private long maxRetryDelay;
    
    @Value("${app.api.health-check.cache-duration:300000}")
    private long healthCacheDuration; // 5分钟
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // API健康状态缓存
    private final ConcurrentHashMap<String, ApiHealthCacheEntry> healthCache = new ConcurrentHashMap<>();
    
    // API调用统计
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    
    @Override
    public DynamicApiConfigManager.ApiConfig selectOptimalApi(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("选择最优API配置: startTime={}, endTime={}", startTime, endTime);
        
        try {
            // 判断是否需要使用联网搜索
            boolean shouldUseWebSearch = shouldUseWebSearchBasedOnTime(startTime, endTime);
            
            // 获取相应的API配置
            DynamicApiConfigManager.ApiConfig primaryConfig = dynamicApiConfigManager.getApiConfig(shouldUseWebSearch);
            
            // 检查主要API的健康状态
            if (isApiHealthy(primaryConfig)) {
                log.debug("选择主要API: {}", getApiTypeName(primaryConfig));
                return primaryConfig;
            }
            
            // 如果主要API不健康，尝试备用API
            DynamicApiConfigManager.ApiConfig fallbackConfig = shouldUseWebSearch 
                ? dynamicApiConfigManager.getOfficialApiConfig() 
                : dynamicApiConfigManager.getVolcengineApiConfig();
                
            if (isApiHealthy(fallbackConfig)) {
                log.warn("主要API不健康，切换到备用API: {}", getApiTypeName(fallbackConfig));
                return fallbackConfig;
            }
            
            // 如果所有API都不健康，返回主要配置（让调用方处理错误）
            log.error("所有API都不健康，返回主要配置");
            return primaryConfig;
            
        } catch (Exception e) {
            log.error("选择API配置时发生异常", e);
            // 发生异常时返回默认配置
            return dynamicApiConfigManager.getOfficialApiConfig();
        }
    }
    
    @Override
    public String callApiWithRetry(DynamicApiConfigManager.ApiConfig config, String prompt, int maxRetries) {
        return callApiWithRetry(config, prompt, maxRetries, UUID.randomUUID().toString());
    }
    
    @Override
    public String callApiWithRetry(DynamicApiConfigManager.ApiConfig config, String prompt, int maxRetries, String requestId) {
        totalCalls.incrementAndGet();
        
        Exception lastException = null;
        long delay = baseRetryDelay;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("API调用重试: attempt={}/{}, delay={}ms, requestId={}", 
                            attempt, maxRetries, delay, requestId);
                    Thread.sleep(delay);
                    retryCount.incrementAndGet();
                    
                    // 指数退避
                    delay = Math.min(delay * 2, maxRetryDelay);
                }
                
                String response = callApiInternal(config, prompt, requestId, attempt);
                if (response != null && !response.trim().isEmpty()) {
                    successfulCalls.incrementAndGet();
                    log.debug("API调用成功: attempt={}, requestId={}", attempt, requestId);
                    return response;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("API调用被中断: requestId={}", requestId);
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("API调用失败: attempt={}/{}, error={}, requestId={}", 
                        attempt, maxRetries, e.getMessage(), requestId);
                
                // 如果是认证错误或配置错误，不需要重试
                if (isNonRetryableError(e)) {
                    log.error("遇到不可重试的错误，停止重试: {}", e.getMessage());
                    break;
                }
            }
        }
        
        failedCalls.incrementAndGet();
        log.error("API调用最终失败: maxRetries={}, requestId={}, lastError={}", 
                maxRetries, requestId, lastException != null ? lastException.getMessage() : "unknown");
        
        return null;
    }
    
    @Override
    public boolean isApiHealthy(DynamicApiConfigManager.ApiConfig config) {
        String cacheKey = getApiTypeName(config);
        ApiHealthCacheEntry cacheEntry = healthCache.get(cacheKey);
        
        // 检查缓存是否有效
        if (cacheEntry != null && 
            System.currentTimeMillis() - cacheEntry.timestamp < healthCacheDuration) {
            return cacheEntry.isHealthy;
        }
        
        // 执行健康检查
        boolean isHealthy = performHealthCheck(config);
        
        // 更新缓存
        healthCache.put(cacheKey, new ApiHealthCacheEntry(isHealthy, System.currentTimeMillis()));
        
        return isHealthy;
    }
    
    @Override
    public ApiHealthStatus getApiHealthStatus(DynamicApiConfigManager.ApiConfig config) {
        ApiHealthStatus status = new ApiHealthStatus();
        status.setCheckTime(LocalDateTime.now());
        
        try {
            long startTime = System.currentTimeMillis();
            boolean isHealthy = performHealthCheck(config);
            long responseTime = System.currentTimeMillis() - startTime;
            
            status.setIsHealthy(isHealthy);
            status.setResponseTime(responseTime);
            status.setStatusCode(isHealthy ? 200 : 500);
            
            if (!isHealthy) {
                status.setErrorMessage("API健康检查失败");
            }
            
        } catch (Exception e) {
            status.setIsHealthy(false);
            status.setErrorMessage(e.getMessage());
            status.setStatusCode(500);
        }
        
        return status;
    }
    
    @Override
    public String callWithFallback(String prompt, LocalDateTime startTime, LocalDateTime endTime) {
        return callWithFallback(prompt, startTime, endTime, UUID.randomUUID().toString());
    }
    
    @Override
    public String callWithFallback(String prompt, LocalDateTime startTime, LocalDateTime endTime, String requestId) {
        log.info("开始带备用的API调用: requestId={}", requestId);
        
        // 选择最优API
        DynamicApiConfigManager.ApiConfig primaryConfig = selectOptimalApi(startTime, endTime);
        
        // 尝试主要API
        String response = callApiWithRetry(primaryConfig, prompt, 2, requestId);
        if (response != null) {
            return response;
        }
        
        // 主要API失败，尝试备用API
        log.warn("主要API调用失败，尝试备用API: requestId={}", requestId);
        
        boolean primaryIsWebSearch = primaryConfig.isSupportsWebSearch();
        DynamicApiConfigManager.ApiConfig fallbackConfig = primaryIsWebSearch 
            ? dynamicApiConfigManager.getOfficialApiConfig() 
            : dynamicApiConfigManager.getVolcengineApiConfig();
        
        response = callApiWithRetry(fallbackConfig, prompt, 1, requestId);
        if (response != null) {
            log.info("备用API调用成功: requestId={}", requestId);
            return response;
        }
        
        log.error("所有API调用都失败: requestId={}", requestId);
        return null;
    }
    
    @Override
    public Map<String, Object> getApiCallStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", totalCalls.get());
        stats.put("successfulCalls", successfulCalls.get());
        stats.put("failedCalls", failedCalls.get());
        stats.put("retryCount", retryCount.get());
        
        long total = totalCalls.get();
        if (total > 0) {
            stats.put("successRate", (double) successfulCalls.get() / total * 100);
        } else {
            stats.put("successRate", 0.0);
        }
        
        // 获取最近的API统计
        try {
            LocalDateTime since = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            List<Map<String, Object>> recentStats = apiCallRecordMapper.getApiStatsByTimeRange(since, LocalDateTime.now());
            stats.put("recentApiStats", recentStats);
        } catch (Exception e) {
            log.warn("获取最近API统计失败", e);
        }
        
        return stats;
    }
    
    @Override
    public void resetHealthCache() {
        healthCache.clear();
        log.info("API健康状态缓存已清空");
    }
    
    @Override
    public String getCurrentSelectionStrategy() {
        return "基于时间范围的动态API选择策略：" +
               "近期事件使用联网搜索API，历史事件使用官方API，" +
               "结合健康检查和备用机制确保可用性";
    }
    
    @Override
    public String callApiWithLargeTokens(DynamicApiConfigManager.ApiConfig config, String prompt, int maxTokens, TimeSegment timeSegment) {
        log.info("调用大Token API: maxTokens={}, timeSegment={}", maxTokens, timeSegment.getSegmentId());
        
        String requestId = "large_tokens_" + timeSegment.getSegmentId();
        totalCalls.incrementAndGet();
        
        Exception lastException = null;
        long delay = baseRetryDelay;
        
        for (int attempt = 0; attempt <= 2; attempt++) { // 最多重试2次
            try {
                if (attempt > 0) {
                    log.info("大Token API调用重试: attempt={}, delay={}ms, requestId={}", 
                            attempt, delay, requestId);
                    Thread.sleep(delay);
                    retryCount.incrementAndGet();
                    delay = Math.min(delay * 2, maxRetryDelay);
                }
                
                String response = callApiInternalWithLargeTokens(config, prompt, maxTokens, requestId, attempt);
                if (response != null && !response.trim().isEmpty()) {
                    // 检查响应完整性
                    if (isResponseComplete(response, timeSegment.getExpectedEventCount())) {
                        successfulCalls.incrementAndGet();
                        log.debug("大Token API调用成功: attempt={}, requestId={}", attempt, requestId);
                        return response;
                    } else {
                        log.warn("API响应不完整，尝试重试: requestId={}", requestId);
                        continue;
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("大Token API调用被中断: requestId={}", requestId);
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("大Token API调用失败: attempt={}, error={}, requestId={}", 
                        attempt, e.getMessage(), requestId);
                
                if (isNonRetryableError(e)) {
                    log.error("遇到不可重试的错误，停止重试: {}", e.getMessage());
                    break;
                }
            }
        }
        
        failedCalls.incrementAndGet();
        log.error("大Token API调用最终失败: requestId={}, lastError={}", 
                requestId, lastException != null ? lastException.getMessage() : "unknown");
        
        return null;
    }
    
    @Override
    public boolean isResponseComplete(String response, int expectedEventCount) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 检查响应是否包含JSON格式的事件数据
            if (!response.contains("[") || !response.contains("]")) {
                log.debug("响应不包含JSON数组格式");
                return false;
            }
            
            // 检查是否包含关键字段
            String[] requiredFields = {"id", "title", "eventTime", "description"};
            for (String field : requiredFields) {
                if (!response.contains("\"" + field + "\"")) {
                    log.debug("响应缺少必需字段: {}", field);
                    return false;
                }
            }
            
            // 简单估算事件数量（通过计算"id"字段出现次数）
            int eventCount = countOccurrences(response, "\"id\":");
            if (eventCount < Math.min(expectedEventCount * 0.5, 3)) { // 至少应该有预期数量的50%或3个事件
                log.debug("事件数量不足: actual={}, expected={}", eventCount, expectedEventCount);
                return false;
            }
            
            // 检查响应是否被截断
            if (response.endsWith("...") || response.contains("truncated") || 
                response.contains("省略") || response.contains("继续")) {
                log.debug("响应可能被截断");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("检查响应完整性时发生异常", e);
            return false;
        }
    }
    
    @Override
    public List<String> callApiBatch(List<TimeSegment> segments, String prompt) {
        log.info("开始批量并发API调用: segments={}", segments.size());
        
        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 创建线程池用于并发调用
        ExecutorService executorService = Executors.newFixedThreadPool(
                Math.min(segments.size(), 5)); // 最多5个并发线程
        
        try {
            List<CompletableFuture<String>> futures = segments.stream()
                    .map(segment -> CompletableFuture.supplyAsync(() -> {
                        try {
                            // 为每个时间段选择最优API
                            DynamicApiConfigManager.ApiConfig config = selectOptimalApi(
                                    segment.getStartTime(), segment.getEndTime());
                            
                            // 构建针对该时间段的提示词
                            String segmentPrompt = buildSegmentPrompt(prompt, segment);
                            
                            // 调用API（使用大Token支持）
                            return callApiWithLargeTokens(config, segmentPrompt, 4000, segment);
                            
                        } catch (Exception e) {
                            log.error("批量API调用失败: segmentId={}", segment.getSegmentId(), e);
                            return null;
                        }
                    }, executorService))
                    .collect(Collectors.toList());
            
            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            // 设置超时时间（每个时间段30秒）
            allFutures.get(segments.size() * 30, TimeUnit.SECONDS);
            
            // 收集结果
            List<String> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            
            long successCount = results.stream().filter(Objects::nonNull).count();
            log.info("批量API调用完成: total={}, success={}", segments.size(), successCount);
            
            return results;
            
        } catch (TimeoutException e) {
            log.error("批量API调用超时", e);
            return segments.stream().map(s -> (String) null).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量API调用异常", e);
            return segments.stream().map(s -> (String) null).collect(Collectors.toList());
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // 私有辅助方法
    
    /**
     * 内部API调用方法
     */
    private String callApiInternal(DynamicApiConfigManager.ApiConfig config, String prompt, String requestId, int attempt) {
        long startTime = System.currentTimeMillis();
        String apiType = getApiTypeName(config);
        String responseStatus = "FAILED";
        Integer tokenUsage = 0;
        String errorMessage = null;
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(config, prompt);
            
            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());
            
            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            log.debug("调用{}API: attempt={}, requestId={}, promptLength={}", 
                    apiType, attempt, requestId, prompt.length());
            
            // 如果是火山引擎API，打印详细的请求信息
            if (config.isSupportsWebSearch()) {
                log.info("=== 火山引擎API调用详情 ===");
                log.info("请求URL: {}", config.getApiUrl());
                log.info("请求ID: {}", requestId);
                log.info("尝试次数: {}", attempt);
                log.info("提示词内容: {}", prompt);
                try {
                    String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                    log.info("请求体JSON: {}", requestBodyJson);
                } catch (Exception e) {
                    log.warn("无法序列化请求体", e);
                }
                log.info("=== 开始发送请求 ===");
            }
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(), requestEntity, String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 如果是火山引擎API，打印详细的响应信息
            if (config.isSupportsWebSearch()) {
                log.info("=== 火山引擎API响应详情 ===");
                log.info("响应状态码: {}", response.getStatusCode());
                log.info("响应时间: {}ms", responseTime);
                log.info("响应体JSON: {}", response.getBody());
                log.info("=== 响应处理完成 ===");
            }
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                
                // 更新Token使用统计
                if (responseMap.containsKey("usage")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
                    if (usage.containsKey("total_tokens")) {
                        tokenUsage = ((Number) usage.get("total_tokens")).intValue();
                    }
                }
                
                // 获取生成的内容
                String content = extractContentFromResponse(responseMap);
                if (content != null && !content.trim().isEmpty()) {
                    responseStatus = "SUCCESS";
                    
                    // 记录成功的API调用
                    recordApiCall(apiType, requestBody, responseStatus, tokenUsage, 
                                (int) responseTime, null, requestId, attempt);
                    
                    return content;
                }
            }
            
            errorMessage = "API返回格式错误: status=" + response.getStatusCode();
            
        } catch (JsonProcessingException e) {
            errorMessage = "JSON解析错误: " + e.getMessage();
            log.error("JSON解析失败", e);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("API调用异常", e);
        } finally {
            // 记录API调用
            long responseTime = System.currentTimeMillis() - startTime;
            recordApiCall(apiType, null, responseStatus, tokenUsage, 
                        (int) responseTime, errorMessage, requestId, attempt);
        }
        
        return null;
    }
    
    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(DynamicApiConfigManager.ApiConfig config, String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        // 如果支持联网搜索，添加相关配置
        if (config.isSupportsWebSearch()) {
            Map<String, Object> webSearchConfig = new HashMap<>();
            webSearchConfig.put("enable", true);
            webSearchConfig.put("max_results", 10);
            webSearchConfig.put("timeout", 30000);
            requestBody.put("web_search", webSearchConfig);
        }
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);
        
        return requestBody;
    }
    
    /**
     * 从响应中提取内容
     */
    private String extractContentFromResponse(Map<String, Object> responseMap) {
        if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                if (messageObj.containsKey("content")) {
                    return (String) messageObj.get("content");
                }
            }
        }
        return null;
    }
    
    /**
     * 执行健康检查
     */
    private boolean performHealthCheck(DynamicApiConfigManager.ApiConfig config) {
        try {
            // 检查API密钥是否有效
            if (config.getApiKey() == null || config.getApiKey().isEmpty() ||
                config.getApiKey().equals("sk-your-api-key-here") ||
                config.getApiKey().equals("sk-test-key-placeholder")) {
                return false;
            }
            
            String testPrompt = "Hello, this is a health check.";
            String response = callApiInternal(config, testPrompt, "health-check", 0);
            return response != null && !response.trim().isEmpty();
            
        } catch (Exception e) {
            log.debug("API健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 判断是否应该使用联网搜索
     */
    private boolean shouldUseWebSearchBasedOnTime(LocalDateTime startTime, LocalDateTime endTime) {
        // 强制使用联网搜索（火山引擎API）
        // 因为联网搜索能提供更准确和最新的信息
        log.info("强制启用联网搜索功能，使用火山引擎API");
        return true;
        
        // 原有的时间判断逻辑（已禁用）
        /*
        if (startTime == null || endTime == null) {
            return true; // 默认使用联网搜索
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minus(1, ChronoUnit.MONTHS);
        
        // 如果时间范围包含最近一个月的内容，使用联网搜索
        return endTime.isAfter(oneMonthAgo);
        */
    }
    
    /**
     * 获取API类型名称
     */
    private String getApiTypeName(DynamicApiConfigManager.ApiConfig config) {
        if (config.isSupportsWebSearch()) {
            return "VOLCENGINE_WEB";
        } else {
            return "DEEPSEEK_OFFICIAL";
        }
    }
    
    /**
     * 判断是否为不可重试的错误
     */
    private boolean isNonRetryableError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        return message.contains("401") || 
               message.contains("403") || 
               message.contains("Invalid API key") ||
               message.contains("Authentication failed");
    }
    
    /**
     * 记录API调用
     */
    private void recordApiCall(String apiType, Map<String, Object> requestBody, String responseStatus, 
                             Integer tokenUsage, Integer responseTime, String errorMessage, 
                             String requestId, int attempt) {
        try {
            apiMonitoringService.recordApiCall(apiType, requestBody, responseStatus, 
                                             tokenUsage, responseTime, errorMessage, requestId, attempt);
        } catch (Exception e) {
            log.warn("记录API调用失败", e);
        }
    }
    
    /**
     * 内部API调用方法（支持大Token）
     */
    private String callApiInternalWithLargeTokens(DynamicApiConfigManager.ApiConfig config, String prompt, 
                                                 int maxTokens, String requestId, int attempt) {
        long startTime = System.currentTimeMillis();
        String apiType = getApiTypeName(config);
        String responseStatus = "FAILED";
        Integer tokenUsage = 0;
        String errorMessage = null;
        
        try {
            // 构建请求体（使用指定的maxTokens）
            Map<String, Object> requestBody = buildRequestBodyWithTokens(config, prompt, maxTokens);
            
            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());
            
            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            log.debug("调用大Token {}API: attempt={}, requestId={}, promptLength={}, maxTokens={}", 
                    apiType, attempt, requestId, prompt.length(), maxTokens);
            
            // 如果是火山引擎API，打印详细的大Token请求信息
            if (config.isSupportsWebSearch()) {
                log.info("=== 火山引擎大Token API调用详情 ===");
                log.info("请求URL: {}", config.getApiUrl());
                log.info("请求ID: {}", requestId);
                log.info("尝试次数: {}", attempt);
                log.info("最大Token数: {}", maxTokens);
                log.info("提示词长度: {}", prompt.length());
                log.info("提示词内容: {}", prompt);
                try {
                    String requestBodyJson = objectMapper.writeValueAsString(requestBody);
                    log.info("大Token请求体JSON: {}", requestBodyJson);
                } catch (Exception e) {
                    log.warn("无法序列化大Token请求体", e);
                }
                log.info("=== 开始发送大Token请求 ===");
            }
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(), requestEntity, String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 如果是火山引擎API，打印详细的大Token响应信息
            if (config.isSupportsWebSearch()) {
                log.info("=== 火山引擎大Token API响应详情 ===");
                log.info("响应状态码: {}", response.getStatusCode());
                log.info("响应时间: {}ms", responseTime);
                log.info("响应体长度: {}", response.getBody() != null ? response.getBody().length() : 0);
                log.info("大Token响应体JSON: {}", response.getBody());
                log.info("=== 大Token响应处理完成 ===");
            }
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                
                // 更新Token使用统计
                if (responseMap.containsKey("usage")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
                    if (usage.containsKey("total_tokens")) {
                        tokenUsage = ((Number) usage.get("total_tokens")).intValue();
                    }
                }
                
                // 获取生成的内容
                String content = extractContentFromResponse(responseMap);
                if (content != null && !content.trim().isEmpty()) {
                    responseStatus = "SUCCESS";
                    
                    // 记录成功的API调用
                    recordApiCall(apiType, requestBody, responseStatus, tokenUsage, 
                                (int) responseTime, null, requestId, attempt);
                    
                    return content;
                }
            }
            
            errorMessage = "API返回格式错误: status=" + response.getStatusCode();
            
        } catch (JsonProcessingException e) {
            errorMessage = "JSON解析错误: " + e.getMessage();
            log.error("JSON解析失败", e);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("大Token API调用异常", e);
        } finally {
            // 记录API调用
            long responseTime = System.currentTimeMillis() - startTime;
            recordApiCall(apiType, null, responseStatus, tokenUsage, 
                        (int) responseTime, errorMessage, requestId, attempt);
        }
        
        return null;
    }
    
    /**
     * 构建请求体（支持自定义Token数量）
     */
    private Map<String, Object> buildRequestBodyWithTokens(DynamicApiConfigManager.ApiConfig config, String prompt, int maxTokens) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("max_tokens", maxTokens); // 使用传入的maxTokens
        requestBody.put("temperature", temperature);
        
        // 如果支持联网搜索，添加相关配置
        if (config.isSupportsWebSearch()) {
            Map<String, Object> webSearchConfig = new HashMap<>();
            webSearchConfig.put("enable", true);
            webSearchConfig.put("max_results", 15); // 增加搜索结果数量
            webSearchConfig.put("timeout", 45000); // 增加超时时间
            requestBody.put("web_search", webSearchConfig);
        }
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);
        
        return requestBody;
    }
    
    /**
     * 构建时间段特定的提示词
     */
    private String buildSegmentPrompt(String basePrompt, TimeSegment segment) {
        StringBuilder segmentPrompt = new StringBuilder();
        segmentPrompt.append("【时间段分割请求】\n");
        segmentPrompt.append("时间段ID: ").append(segment.getSegmentId()).append("\n");
        segmentPrompt.append("时间范围: ").append(segment.getStartTime()).append(" 至 ").append(segment.getEndTime()).append("\n");
        segmentPrompt.append("预期事件数量: ").append(segment.getExpectedEventCount()).append("\n");
        segmentPrompt.append("时间跨度: ").append(segment.calculateSpanDays()).append("天\n\n");
        
        segmentPrompt.append("原始请求:\n");
        segmentPrompt.append(basePrompt);
        
        segmentPrompt.append("\n\n请特别关注以下要求：");
        segmentPrompt.append("\n1. 只返回指定时间范围内的事件");
        segmentPrompt.append("\n2. 确保返回完整的JSON格式数据");
        segmentPrompt.append("\n3. 每个事件必须包含：id, title, description, eventTime, location, subject, object等字段");
        segmentPrompt.append("\n4. 事件时间必须在 ").append(segment.getStartTime()).append(" 到 ").append(segment.getEndTime()).append(" 之间");
        segmentPrompt.append("\n5. 尽量返回 ").append(segment.getExpectedEventCount()).append(" 个左右的事件");
        
        return segmentPrompt.toString();
    }
    
    /**
     * 计算字符串中子字符串出现的次数
     */
    private int countOccurrences(String text, String substring) {
        if (text == null || substring == null || text.isEmpty() || substring.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
    
    /**
     * API健康状态缓存条目
     */
    private static class ApiHealthCacheEntry {
        final boolean isHealthy;
        final long timestamp;
        
        ApiHealthCacheEntry(boolean isHealthy, long timestamp) {
            this.isHealthy = isHealthy;
            this.timestamp = timestamp;
        }
    }
}