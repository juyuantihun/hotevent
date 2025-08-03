package com.hotech.events.controller;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.ValidationStats;
import com.hotech.events.service.EventValidationService;
import com.hotech.events.validation.ValidationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件验证控制器
 * 
 * @author Kiro
 */
@RestController
@RequestMapping("/api/validation")
@Tag(name = "事件验证", description = "事件真实性验证相关接口")
public class EventValidationController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventValidationController.class);
    
    @Autowired
    private EventValidationService eventValidationService;
    
    /**
     * 验证单个事件
     */
    @PostMapping("/validate-event")
    @Operation(summary = "验证单个事件", description = "对单个事件进行真实性验证")
    public ResponseEntity<EventValidationResult> validateEvent(
            @RequestBody @Parameter(description = "待验证的事件数据") EventData event) {
        
        EventValidationResult result = eventValidationService.validateEvent(event);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量验证事件
     */
    @PostMapping("/validate-events")
    @Operation(summary = "批量验证事件", description = "对多个事件进行批量真实性验证")
    public ResponseEntity<List<EventValidationResult>> validateEvents(
            @RequestBody @Parameter(description = "待验证的事件列表") List<EventData> events) {
        
        List<EventValidationResult> results = eventValidationService.validateEvents(events);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取所有验证规则
     */
    @GetMapping("/rules")
    @Operation(summary = "获取验证规则", description = "获取所有可用的验证规则列表")
    public ResponseEntity<List<Map<String, Object>>> getValidationRules() {
        
        List<ValidationRule> rules = eventValidationService.getAllValidationRules();
        List<Map<String, Object>> ruleInfos = rules.stream()
                .map(rule -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", rule.getRuleName());
                    info.put("description", rule.getRuleDescription());
                    info.put("weight", rule.getWeight());
                    info.put("enabled", rule.isEnabled());
                    return info;
                })
                .toList();
        
        return ResponseEntity.ok(ruleInfos);
    }
    
    /**
     * 启用或禁用验证规则
     */
    @PutMapping("/rules/{ruleName}/enabled")
    @Operation(summary = "设置规则状态", description = "启用或禁用指定的验证规则")
    public ResponseEntity<Map<String, Object>> setRuleEnabled(
            @PathVariable @Parameter(description = "规则名称") String ruleName,
            @RequestParam @Parameter(description = "是否启用") boolean enabled) {
        
        eventValidationService.setRuleEnabled(ruleName, enabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ruleName", ruleName);
        response.put("enabled", enabled);
        response.put("message", "规则状态更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取验证统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取验证统计", description = "获取事件验证的统计信息")
    public ResponseEntity<ValidationStats> getValidationStats() {
        
        ValidationStats stats = eventValidationService.getValidationStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 重置验证统计信息
     */
    @PostMapping("/stats/reset")
    @Operation(summary = "重置验证统计", description = "重置验证统计信息")
    public ResponseEntity<Map<String, String>> resetValidationStats() {
        
        eventValidationService.resetValidationStats();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "验证统计信息已重置");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设置可信度阈值
     */
    @PutMapping("/threshold")
    @Operation(summary = "设置可信度阈值", description = "设置事件验证的可信度阈值")
    public ResponseEntity<Map<String, Object>> setCredibilityThreshold(
            @RequestParam @Parameter(description = "可信度阈值 (0.0-1.0)") double threshold) {
        
        eventValidationService.setCredibilityThreshold(threshold);
        
        Map<String, Object> response = new HashMap<>();
        response.put("threshold", threshold);
        response.put("message", "可信度阈值设置成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前可信度阈值
     */
    @GetMapping("/threshold")
    @Operation(summary = "获取可信度阈值", description = "获取当前的可信度阈值设置")
    public ResponseEntity<Map<String, Object>> getCredibilityThreshold() {
        
        double threshold = eventValidationService.getCredibilityThreshold();
        
        Map<String, Object> response = new HashMap<>();
        response.put("threshold", threshold);
        
        return ResponseEntity.ok(response);
    }
}