package com.hotech.events.controller;

import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek联网搜索测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/deepseek/online-search")
public class DeepSeekOnlineSearchController {

    @Autowired
    @Qualifier("deepSeekOnlineSearchService")
    private DeepSeekService deepSeekOnlineSearchService;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Value("${app.deepseek.model}")
    private String model;

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.web-search.enabled}")
    private boolean webSearchEnabled;

    /**
     * 测试联网搜索API连接
     */
    @GetMapping("/test/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        log.info("开始测试DeepSeek联网搜索API连接");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 显示配置信息（隐藏敏感信息）
            result.put("apiUrl", apiUrl);
            result.put("model", model);
            result.put("apiKey", maskApiKey(apiKey));
            result.put("webSearchEnabled", webSearchEnabled);
            
            // 测试连接
            Boolean isConnected = deepSeekOnlineSearchService.checkConnection();
            result.put("connected", isConnected);
            result.put("status", isConnected ? "成功" : "失败");
            result.put("message", isConnected ? "DeepSeek联网搜索API连接正常" : "DeepSeek联网搜索API连接失败");
            result.put("searchType", "联网搜索");
            
            log.info("DeepSeek联网搜索API连接测试结果: {}", isConnected ? "成功" : "失败");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试DeepSeek联网搜索API连接时发生异常", e);
            result.put("connected", false);
            result.put("status", "异常");
            result.put("message", "测试连接时发生异常: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 测试获取最新事件（联网搜索）
     */
    @PostMapping("/test/latest-events")
    public ResponseEntity<Map<String, Object>> testLatestEvents(@RequestParam(defaultValue = "5") int limit) {
        log.info("开始测试获取最新事件（联网搜索）, limit={}", limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            List<EventDTO> events = deepSeekOnlineSearchService.fetchLatestEvents(limit);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("limit", limit);
            result.put("eventCount", events.size());
            result.put("responseTime", responseTime + "ms");
            result.put("events", events);
            result.put("searchType", "联网搜索");
            result.put("message", "成功获取最新事件");
            
            log.info("获取最新事件成功: eventCount={}, responseTime={}ms", events.size(), responseTime);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试获取最新事件失败", e);
            result.put("success", false);
            result.put("message", "获取最新事件失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 测试根据关键词获取事件（联网搜索）
     */
    @PostMapping("/test/events-by-keywords")
    public ResponseEntity<Map<String, Object>> testEventsByKeywords(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("开始测试根据关键词获取事件（联网搜索）, keywords={}, limit={}", keywords, limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            List<String> keywordList = List.of(keywords.split(","));
            List<EventDTO> events = deepSeekOnlineSearchService.fetchEventsByKeywords(keywordList, limit);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("keywords", keywordList);
            result.put("limit", limit);
            result.put("eventCount", events.size());
            result.put("responseTime", responseTime + "ms");
            result.put("events", events);
            result.put("searchType", "联网搜索");
            result.put("message", "成功根据关键词获取事件");
            
            log.info("根据关键词获取事件成功: keywords={}, eventCount={}, responseTime={}ms", 
                    keywordList, events.size(), responseTime);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试根据关键词获取事件失败", e);
            result.put("success", false);
            result.put("message", "根据关键词获取事件失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 测试根据日期范围获取事件（联网搜索）
     */
    @PostMapping("/test/events-by-date-range")
    public ResponseEntity<Map<String, Object>> testEventsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("开始测试根据日期范围获取事件（联网搜索）, startDate={}, endDate={}, limit={}", 
                startDate, endDate, limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            List<EventDTO> events = deepSeekOnlineSearchService.fetchEventsByDateRange(startDate, endDate, limit);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("limit", limit);
            result.put("eventCount", events.size());
            result.put("responseTime", responseTime + "ms");
            result.put("events", events);
            result.put("searchType", "联网搜索");
            result.put("message", "成功根据日期范围获取事件");
            
            log.info("根据日期范围获取事件成功: startDate={}, endDate={}, eventCount={}, responseTime={}ms", 
                    startDate, endDate, events.size(), responseTime);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试根据日期范围获取事件失败", e);
            result.put("success", false);
            result.put("message", "根据日期范围获取事件失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取联网搜索配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("apiUrl", apiUrl);
        config.put("model", model);
        config.put("apiKey", maskApiKey(apiKey));
        config.put("webSearchEnabled", webSearchEnabled);
        config.put("provider", "火山方舟 (Volcengine)");
        config.put("version", "v3");
        config.put("endpoint", "bots");
        config.put("searchType", "联网搜索");
        config.put("description", "已配置为使用火山方舟提供的DeepSeek联网搜索API服务");
        
        return ResponseEntity.ok(config);
    }

    /**
     * 测试简单对话（联网搜索）
     */
    @PostMapping("/test/chat")
    public ResponseEntity<Map<String, Object>> testChat(@RequestParam String query) {
        log.info("开始测试简单对话（联网搜索）, query={}", query);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 使用fetchLatestEvents方法来测试联网搜索功能
            List<EventDTO> events = deepSeekOnlineSearchService.fetchLatestEvents(3);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("success", true);
            result.put("query", query);
            result.put("responseTime", responseTime + "ms");
            result.put("searchType", "联网搜索");
            result.put("message", "联网搜索功能正常");
            result.put("eventCount", events.size());
            result.put("note", "通过获取最新事件来验证联网搜索功能");
            
            log.info("简单对话测试成功: query={}, responseTime={}ms", query, responseTime);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试简单对话失败", e);
            result.put("success", false);
            result.put("message", "简单对话测试失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 掩码API密钥，只显示前4位和后4位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "****" + suffix;
    }
}