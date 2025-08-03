package com.hotech.events.controller;

import com.hotech.events.config.ConfigHotReloadListener;
import com.hotech.events.config.ConfigValidator;
import com.hotech.events.entity.ConfigChangeLog;
import com.hotech.events.entity.SystemConfig;
import com.hotech.events.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理控制器
 * 提供配置的CRUD操作和管理功能
 * 
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private ConfigValidator configValidator;
    
    @Autowired
    private ConfigHotReloadListener configHotReloadListener;
    
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", configs);
            result.put("total", configs.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取所有配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取配置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @GetMapping("/group/{group}")
    public ResponseEntity<Map<String, Object>> getConfigsByGroup(@PathVariable String group) {
        try {
            Map<String, String> configs = systemConfigService.getConfigsByGroup(group);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", configs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("根据分组获取配置失败: group={}", group, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取配置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> getConfig(@PathVariable String key) {
        try {
            String value = systemConfigService.getConfigValue(key);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of("key", key, "value", value != null ? value : ""));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取配置失败: key={}", key, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取配置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody SystemConfig config) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证配置
            ConfigValidator.ValidationResult validationResult = configValidator.validateConfig(config);
            if (!validationResult.isValid()) {
                result.put("success", false);
                result.put("message", "配置验证失败");
                result.put("errors", validationResult.getErrors());
                return ResponseEntity.ok(result);
            }
            
            // 保存配置
            boolean success = systemConfigService.saveOrUpdateConfig(config);
            result.put("success", success);
            result.put("message", success ? "配置保存成功" : "配置保存失败");
            
            if (validationResult.hasWarnings()) {
                result.put("warnings", validationResult.getWarnings());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("保存配置失败: key={}", config.getConfigKey(), e);
            result.put("success", false);
            result.put("message", "保存配置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = systemConfigService.deleteConfig(key);
            result.put("success", success);
            result.put("message", success ? "配置删除成功" : "配置删除失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除配置失败: key={}", key, e);
            result.put("success", false);
            result.put("message", "删除配置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            systemConfigService.refreshCache();
            result.put("success", true);
            result.put("message", "配置缓存刷新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            result.put("success", false);
            result.put("message", "刷新缓存失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConfigStats() {
        try {
            Map<String, Object> stats = systemConfigService.getConfigStats();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}