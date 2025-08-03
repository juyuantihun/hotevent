package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.ValidationStats;
import com.hotech.events.validation.ValidationRule;

import java.util.List;

/**
 * 事件验证服务接口
 * 
 * @author Kiro
 */
public interface EventValidationService {
    
    /**
     * 验证单个事件
     * 
     * @param event 待验证的事件
     * @return 验证结果
     */
    EventValidationResult validateEvent(EventData event);
    
    /**
     * 批量验证事件
     * 
     * @param events 待验证的事件列表
     * @return 验证结果列表
     */
    List<EventValidationResult> validateEvents(List<EventData> events);
    
    /**
     * 添加自定义验证规则
     * 
     * @param rule 验证规则
     */
    void addValidationRule(ValidationRule rule);
    
    /**
     * 移除验证规则
     * 
     * @param ruleName 规则名称
     */
    void removeValidationRule(String ruleName);
    
    /**
     * 获取所有验证规则
     * 
     * @return 验证规则列表
     */
    List<ValidationRule> getAllValidationRules();
    
    /**
     * 启用或禁用验证规则
     * 
     * @param ruleName 规则名称
     * @param enabled 是否启用
     */
    void setRuleEnabled(String ruleName, boolean enabled);
    
    /**
     * 获取验证统计信息
     * 
     * @return 验证统计
     */
    ValidationStats getValidationStats();
    
    /**
     * 重置验证统计信息
     */
    void resetValidationStats();
    
    /**
     * 设置可信度阈值
     * 
     * @param threshold 阈值 (0.0-1.0)
     */
    void setCredibilityThreshold(double threshold);
    
    /**
     * 获取可信度阈值
     * 
     * @return 当前阈值
     */
    double getCredibilityThreshold();
}