package com.hotech.events.controller;

import com.hotech.events.config.DynamicSystemConfig;
import com.hotech.events.service.ConfigurationManagementService;
import com.hotech.events.service.SystemHealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 * 提供配置管理和健康检查的API接口
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Tag(name = "系统管理", description = "配置管理和健康检查接口")
public class SystemManagementController {
    
    @Autowired
    private ConfigurationManagementService configurationManagementService;
    
    @Autowired
    private SystemHealthCheckService systemHealthCheckService;
    
    /**
     * 获取当前系统配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取当前系统配置", description = "返回当前的动态系统配置信息")
    public ResponseEntity<DynamicSystemConfig> getCurrentConfig() {
        log.info("获取当前系统配置");
        
        try {
            DynamicSystemConfig config = configurationManagementService.getCurrentConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 更新API选择策略配置
     */
    @PutMapping("/config/api-selection")
    @Operation(summary = "更新API选择策略配置", description = "动态调整API选择和切换策略")
    public ResponseEntity<Map<String, Object>> updateApiSelectionConfig(
            @RequestBody DynamicSystemConfig.ApiSelectionConfig config) {
        log.info("更新API选择策略配置: {}", config);
        
        try {
            configurationManagementService.updateApiSelectionConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API选择策略配置更新成功");
            response.put("config", config);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("API选择策略配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置验证失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("更新API选择策略配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置更新失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新重复检测配置
     */
    @PutMapping("/config/duplication-detection")
    @Operation(summary = "更新重复检测配置", description = "动态调整重复检测阈值和策略")
    public ResponseEntity<Map<String, Object>> updateDuplicationDetectionConfig(
            @RequestBody DynamicSystemConfig.DuplicationDetectionConfig config) {
        log.info("更新重复检测配置: {}", config);
        
        try {
            configurationManagementService.updateDuplicationDetectionConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "重复检测配置更新成功");
            response.put("config", config);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("重复检测配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置验证失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("更新重复检测配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置更新失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新事件数量配置
     */
    @PutMapping("/config/event-count")
    @Operation(summary = "更新事件数量配置", description = "动态调整事件数量的最小值、目标值和最大值")
    public ResponseEntity<Map<String, Object>> updateEventCountConfig(
            @RequestBody DynamicSystemConfig.EventCountConfig config) {
        log.info("更新事件数量配置: {}", config);
        
        try {
            configurationManagementService.updateEventCountConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "事件数量配置更新成功");
            response.put("config", config);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("事件数量配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置验证失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("更新事件数量配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置更新失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新健康检查配置
     */
    @PutMapping("/config/health-check")
    @Operation(summary = "更新健康检查配置", description = "动态调整健康检查间隔和阈值")
    public ResponseEntity<Map<String, Object>> updateHealthCheckConfig(
            @RequestBody DynamicSystemConfig.HealthCheckConfig config) {
        log.info("更新健康检查配置: {}", config);
        
        try {
            configurationManagementService.updateHealthCheckConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "健康检查配置更新成功");
            response.put("config", config);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("健康检查配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置验证失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("更新健康检查配置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置更新失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 重置配置为默认值
     */
    @PostMapping("/config/reset")
    @Operation(summary = "重置配置为默认值", description = "将所有配置重置为系统默认值")
    public ResponseEntity<Map<String, Object>> resetConfigToDefaults() {
        log.info("重置配置为默认值");
        
        try {
            configurationManagementService.resetToDefaults();
            
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
     * 获取配置变更历史
     */
    @GetMapping("/config/history")
    @Operation(summary = "获取配置变更历史", description = "返回最近的配置变更记录")
    public ResponseEntity<List<ConfigurationManagementService.ConfigChangeRecord>> getConfigChangeHistory() {
        log.info("获取配置变更历史");
        
        try {
            List<ConfigurationManagementService.ConfigChangeRecord> history = 
                    configurationManagementService.getConfigChangeHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取配置变更历史失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 执行系统健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "执行系统健康检查", description = "返回完整的系统健康状态报告")
    public ResponseEntity<SystemHealthCheckService.SystemHealthReport> performHealthCheck() {
        log.info("执行系统健康检查");
        
        try {
            SystemHealthCheckService.SystemHealthReport report = 
                    systemHealthCheckService.performFullHealthCheck();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 检查API健康状态
     */
    @GetMapping("/health/api")
    @Operation(summary = "检查API健康状态", description = "返回API服务的健康状态")
    public ResponseEntity<SystemHealthCheckService.ApiHealthStatus> checkApiHealth() {
        log.info("检查API健康状态");
        
        try {
            SystemHealthCheckService.ApiHealthStatus status = 
                    systemHealthCheckService.checkApiHealth();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("API健康检查失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 检查数据库健康状态
     */
    @GetMapping("/health/database")
    @Operation(summary = "检查数据库健康状态", description = "返回数据库连接的健康状态")
    public ResponseEntity<SystemHealthCheckService.DatabaseHealthStatus> checkDatabaseHealth() {
        log.info("检查数据库健康状态");
        
        try {
            SystemHealthCheckService.DatabaseHealthStatus status = 
                    systemHealthCheckService.checkDatabaseHealth();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取系统性能指标
     */
    @GetMapping("/performance")
    @Operation(summary = "获取系统性能指标", description = "返回当前的系统性能指标")
    public ResponseEntity<SystemHealthCheckService.SystemPerformanceMetrics> getPerformanceMetrics() {
        log.info("获取系统性能指标");
        
        try {
            SystemHealthCheckService.SystemPerformanceMetrics metrics = 
                    systemHealthCheckService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取健康检查历史
     */
    @GetMapping("/health/history")
    @Operation(summary = "获取健康检查历史", description = "返回最近的健康检查记录")
    public ResponseEntity<List<SystemHealthCheckService.HealthCheckRecord>> getHealthCheckHistory(
            @RequestParam(defaultValue = "50") int limit) {
        log.info("获取健康检查历史，限制数量: {}", limit);
        
        try {
            List<SystemHealthCheckService.HealthCheckRecord> history = 
                    systemHealthCheckService.getHealthCheckHistory(limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取健康检查历史失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 验证配置有效性
     */
    @PostMapping("/config/validate")
    @Operation(summary = "验证配置有效性", description = "验证提供的配置是否有效")
    public ResponseEntity<Map<String, Object>> validateConfig(
            @RequestBody DynamicSystemConfig config) {
        log.info("验证配置有效性");
        
        try {
            boolean isValid = configurationManagementService.validateConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "配置验证通过" : "配置验证失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("配置验证失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "配置验证异常: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}