package com.hotech.events.controller;

import com.hotech.events.service.WebSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 联网搜索管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/web-search")
@Tag(name = "联网搜索管理", description = "DeepSeek联网搜索功能的配置和管理")
public class WebSearchController {

    @Autowired
    private WebSearchService webSearchService;

    @GetMapping("/status")
    @Operation(summary = "获取联网搜索状态", description = "检查联网搜索功能是否可用")
    public ResponseEntity<Map<String, Object>> getWebSearchStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("available", webSearchService.isWebSearchAvailable());
            status.put("config", webSearchService.getWebSearchConfig());
            status.put("stats", webSearchService.getWebSearchStats());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取联网搜索状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取联网搜索状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/enable")
    @Operation(summary = "启用联网搜索", description = "启用DeepSeek联网搜索功能")
    public ResponseEntity<Map<String, Object>> enableWebSearch() {
        try {
            webSearchService.enableWebSearch();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "联网搜索已启用");
            result.put("available", webSearchService.isWebSearchAvailable());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("启用联网搜索失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "启用联网搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/disable")
    @Operation(summary = "禁用联网搜索", description = "禁用DeepSeek联网搜索功能")
    public ResponseEntity<Map<String, Object>> disableWebSearch() {
        try {
            webSearchService.disableWebSearch();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "联网搜索已禁用");
            result.put("available", webSearchService.isWebSearchAvailable());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("禁用联网搜索失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "禁用联网搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/config")
    @Operation(summary = "获取联网搜索配置", description = "获取当前联网搜索配置信息")
    public ResponseEntity<Map<String, Object>> getWebSearchConfig() {
        try {
            Map<String, Object> config = webSearchService.getWebSearchConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取联网搜索配置失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取联网搜索配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/config")
    @Operation(summary = "更新联网搜索配置", description = "更新联网搜索配置参数")
    public ResponseEntity<Map<String, Object>> updateWebSearchConfig(
            @RequestBody @Parameter(description = "新的配置参数") Map<String, Object> config) {
        try {
            webSearchService.updateWebSearchConfig(config);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "联网搜索配置已更新");
            result.put("config", webSearchService.getWebSearchConfig());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新联网搜索配置失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "更新联网搜索配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/test")
    @Operation(summary = "测试联网搜索", description = "测试联网搜索功能是否正常工作")
    public ResponseEntity<Map<String, Object>> testWebSearch(
            @RequestParam(defaultValue = "最新科技新闻") 
            @Parameter(description = "测试查询内容") String query) {
        try {
            Map<String, Object> testResult = webSearchService.testWebSearch(query);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            log.error("测试联网搜索失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "测试联网搜索失败: " + e.getMessage());
            error.put("query", query);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取联网搜索统计", description = "获取联网搜索使用统计信息")
    public ResponseEntity<Map<String, Object>> getWebSearchStats() {
        try {
            Map<String, Object> stats = webSearchService.getWebSearchStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取联网搜索统计失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取联网搜索统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/clear-cache")
    @Operation(summary = "清除联网搜索缓存", description = "清除联网搜索结果缓存")
    public ResponseEntity<Map<String, Object>> clearWebSearchCache() {
        try {
            webSearchService.clearWebSearchCache();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "联网搜索缓存已清空");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("清除联网搜索缓存失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "清除联网搜索缓存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}