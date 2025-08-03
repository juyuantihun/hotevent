package com.hotech.events.controller;

import com.hotech.events.config.TimelineEnhancementConfig;
import com.hotech.events.service.TimelineConfigurationService;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 时间线配置管理控制器
 * 提供时间线增强功能的配置管理和性能监控接口
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/timeline/config")
public class TimelineConfigurationController {
    
    @Autowired
    private TimelineConfigurationService configService;
    
    @Autowired
    private TimelinePerformanceMonitoringService monitoringService;
    
    /**
     * 获取当前时间线增强配置
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentConfig() {
        try {
            TimelineEnhancementConfig config = configService.getCurrentConfig();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", config);
            response.put("message", "获取配置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取当前配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新时间段分割配置
     */
    @PutMapping("/segmentation")
    public ResponseEntity<Map<String, Object>> updateSegmentationConfig(
            @RequestBody TimelineEnhancementConfig.SegmentationConfig config) {
        try {
            configService.updateSegmentationConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "时间段分割配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新时间段分割配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新地理信息处理配置
     */
    @PutMapping("/geographic")
    public ResponseEntity<Map<String, Object>> updateGeographicConfig(
            @RequestBody TimelineEnhancementConfig.GeographicConfig config) {
        try {
            configService.updateGeographicConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "地理信息处理配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新地理信息处理配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新API调用配置
     */
    @PutMapping("/api")
    public ResponseEntity<Map<String, Object>> updateApiConfig(
            @RequestBody TimelineEnhancementConfig.ApiConfig config) {
        try {
            configService.updateApiConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API调用配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新API调用配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新性能监控配置
     */
    @PutMapping("/monitoring")
    public ResponseEntity<Map<String, Object>> updateMonitoringConfig(
            @RequestBody TimelineEnhancementConfig.MonitoringConfig config) {
        try {
            configService.updateMonitoringConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "性能监控配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新性能监控配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新缓存配置
     */
    @PutMapping("/cache")
    public ResponseEntity<Map<String, Object>> updateCacheConfig(
            @RequestBody TimelineEnhancementConfig.CacheConfig config) {
        try {
            configService.updateCacheConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "缓存配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新缓存配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新前端UI配置
     */
    @PutMapping("/frontend")
    public ResponseEntity<Map<String, Object>> updateFrontendConfig(
            @RequestBody TimelineEnhancementConfig.FrontendConfig config) {
        try {
            configService.updateFrontendConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "前端UI配置更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新前端UI配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 验证配置有效性
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConfig(
            @RequestBody TimelineEnhancementConfig config) {
        try {
            boolean isValid = configService.validateConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("isValid", isValid));
            response.put("message", isValid ? "配置验证通过" : "配置验证失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置验证失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 重置配置为默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetToDefaults() {
        try {
            configService.resetToDefaults();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "配置已重置为默认值");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重置配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重置配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 重新加载配置文件
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadConfig() {
        try {
            boolean success = configService.reloadConfigFromFile();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "配置文件重新加载成功" : "配置文件重新加载失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重新加载配置文件失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重新加载配置文件失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 保存配置到文件
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveConfig() {
        try {
            boolean success = configService.saveConfigToFile();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "配置保存成功" : "配置保存失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("保存配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存配置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取配置变更历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getConfigChangeHistory() {
        try {
            List<TimelineConfigurationService.TimelineConfigChangeRecord> history = 
                configService.getConfigChangeHistory();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("message", "获取配置变更历史成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取配置变更历史失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取配置变更历史失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getConfigStatistics() {
        try {
            Map<String, Object> statistics = configService.getConfigStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "获取配置统计信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取配置统计信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 启用/禁用配置热更新
     */
    @PostMapping("/hot-reload/{enabled}")
    public ResponseEntity<Map<String, Object>> setHotReload(@PathVariable boolean enabled) {
        try {
            if (enabled) {
                configService.enableHotReload();
            } else {
                configService.disableHotReload();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", enabled ? "配置热更新已启用" : "配置热更新已禁用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置配置热更新状态失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置配置热更新状态失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取性能统计信息
     */
    @GetMapping("/performance/statistics")
    public ResponseEntity<Map<String, Object>> getPerformanceStatistics() {
        try {
            Map<String, Object> statistics = monitoringService.getPerformanceStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "获取性能统计信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取性能统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取性能统计信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 检查性能阈值
     */
    @GetMapping("/performance/thresholds/check")
    public ResponseEntity<Map<String, Object>> checkPerformanceThresholds() {
        try {
            Map<String, Object> thresholdResults = monitoringService.checkPerformanceThresholds();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", thresholdResults);
            response.put("message", "性能阈值检查完成");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查性能阈值失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "检查性能阈值失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 设置性能阈值
     */
    @PutMapping("/performance/thresholds")
    public ResponseEntity<Map<String, Object>> setPerformanceThresholds(
            @RequestBody Map<String, Object> thresholds) {
        try {
            monitoringService.setPerformanceThresholds(thresholds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "性能阈值设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置性能阈值失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置性能阈值失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 启用/禁用性能监控
     */
    @PostMapping("/performance/monitoring/{enabled}")
    public ResponseEntity<Map<String, Object>> setPerformanceMonitoring(@PathVariable boolean enabled) {
        try {
            if (enabled) {
                monitoringService.enableMonitoring();
            } else {
                monitoringService.disableMonitoring();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", enabled ? "性能监控已启用" : "性能监控已禁用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置性能监控状态失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置性能监控状态失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 重置性能统计数据
     */
    @PostMapping("/performance/statistics/reset")
    public ResponseEntity<Map<String, Object>> resetPerformanceStatistics() {
        try {
            monitoringService.resetStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "性能统计数据已重置");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重置性能统计数据失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重置性能统计数据失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 导出性能报告
     */
    @GetMapping("/performance/report/{format}")
    public ResponseEntity<Map<String, Object>> exportPerformanceReport(@PathVariable String format) {
        try {
            String report = monitoringService.exportPerformanceReport(format.toUpperCase());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("report", report, "format", format.toUpperCase()));
            response.put("message", "性能报告导出成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("导出性能报告失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "导出性能报告失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取系统资源使用情况
     */
    @GetMapping("/performance/system-resources")
    public ResponseEntity<Map<String, Object>> getSystemResourceUsage() {
        try {
            Map<String, Object> resourceUsage = monitoringService.getSystemResourceUsage();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resourceUsage);
            response.put("message", "获取系统资源使用情况成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取系统资源使用情况失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统资源使用情况失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}