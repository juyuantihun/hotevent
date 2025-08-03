package com.hotech.events.controller;

import com.hotech.events.service.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API测试控制器
 * 用于测试动态API切换功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class ApiTestController {

    @Autowired(required = false)
    private WebSearchService webSearchService;

    @Value("${app.deepseek.web-search.enabled:true}")
    private boolean webSearchEnabled;

    @Value("${app.deepseek.official.api-url:}")
    private String officialApiUrl;

    @Value("${app.deepseek.volcengine.api-url:}")
    private String volcengineApiUrl;

    @Value("${app.deepseek.official.model:}")
    private String officialModel;

    @Value("${app.deepseek.volcengine.model:}")
    private String volcengineModel;

    /**
     * 获取当前API配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> getApiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("webSearchEnabled", webSearchEnabled);
        config.put("currentApiType", webSearchEnabled ? "volcengine" : "official");
        
        Map<String, Object> officialConfig = new HashMap<>();
        officialConfig.put("apiUrl", officialApiUrl);
        officialConfig.put("model", officialModel);
        officialConfig.put("supportsWebSearch", false);
        
        Map<String, Object> volcengineConfig = new HashMap<>();
        volcengineConfig.put("apiUrl", volcengineApiUrl);
        volcengineConfig.put("model", volcengineModel);
        volcengineConfig.put("supportsWebSearch", true);
        
        config.put("official", officialConfig);
        config.put("volcengine", volcengineConfig);
        
        return config;
    }

    /**
     * 测试联网搜索功能
     */
    @PostMapping("/websearch")
    public Map<String, Object> testWebSearch(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "杭州今天天气");
        
        log.info("测试联网搜索: query={}, webSearchEnabled={}", query, webSearchEnabled);
        
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("webSearchEnabled", webSearchEnabled);
        result.put("apiType", webSearchEnabled ? "volcengine" : "official");
        
        if (webSearchService == null) {
            result.put("success", false);
            result.put("error", "WebSearchService 服务暂不可用");
            result.put("message", "服务未初始化");
            return result;
        }
        
        try {
            Map<String, Object> response = webSearchService.testWebSearch(query);
            result.put("success", true);
            result.put("response", response);
            result.put("message", "API调用成功");
        } catch (Exception e) {
            log.error("联网搜索测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "API调用失败");
        }
        
        return result;
    }

    /**
     * 切换联网搜索开关（仅用于测试）
     */
    @PostMapping("/toggle-websearch")
    public Map<String, Object> toggleWebSearch(@RequestBody Map<String, Boolean> request) {
        Boolean enabled = request.get("enabled");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "注意：这只是显示当前配置，实际切换需要重启应用或使用配置管理接口");
        result.put("currentWebSearchEnabled", webSearchEnabled);
        result.put("requestedEnabled", enabled);
        result.put("currentApiType", webSearchEnabled ? "volcengine" : "official");
        result.put("requestedApiType", enabled ? "volcengine" : "official");
        
        return result;
    }
}