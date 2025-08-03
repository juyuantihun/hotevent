package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.ValidationStats;
import com.hotech.events.service.EventValidationService;
import com.hotech.events.validation.ValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 事件验证服务实现类
 * 
 * @author Kiro
 */
@Service
public class EventValidationServiceImpl implements EventValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventValidationServiceImpl.class);
    
    // 验证规则映射
    private final Map<String, ValidationRule> validationRules = new ConcurrentHashMap<>();
    
    // 规则启用状态
    private final Map<String, Boolean> ruleEnabledStatus = new ConcurrentHashMap<>();
    
    // 验证统计
    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong passedValidations = new AtomicLong(0);
    private final AtomicLong failedValidations = new AtomicLong(0);
    private volatile LocalDateTime statsStartTime = LocalDateTime.now();
    
    // 可信度阈值
    @Value("${timeline.validation.credibility-threshold:0.7}")
    private double credibilityThreshold;
    
    /**
     * 自动注入所有验证规则
     */
    @Autowired
    public EventValidationServiceImpl(List<ValidationRule> rules) {
        for (ValidationRule rule : rules) {
            addValidationRule(rule);
        }
        logger.info("初始化事件验证服务，加载了 {} 个验证规则", rules.size());
    }
    
    @Override
    public EventValidationResult validateEvent(EventData event) {
        if (event == null) {
            throw new IllegalArgumentException("待验证事件不能为空");
        }
        
        logger.debug("开始验证事件: {}", event.getTitle());
        
        List<EventValidationResult> ruleResults = new ArrayList<>();
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        // 执行所有启用的验证规则
        for (ValidationRule rule : getEnabledRules()) {
            try {
                EventValidationResult ruleResult = rule.validate(event);
                ruleResults.add(ruleResult);
                
                double weight = rule.getWeight();
                totalScore += ruleResult.getCredibilityScore() * weight;
                totalWeight += weight;
                
                logger.debug("规则 {} 验证完成，评分: {}, 权重: {}", 
                           rule.getRuleName(), ruleResult.getCredibilityScore(), weight);
                
            } catch (Exception e) {
                logger.error("验证规则 {} 执行失败: {}", rule.getRuleName(), e.getMessage(), e);
            }
        }
        
        // 计算综合评分
        double finalScore = totalWeight > 0 ? totalScore / totalWeight : 0.0;
        
        // 合并验证结果
        EventValidationResult finalResult = mergeValidationResults(event, ruleResults, finalScore);
        
        // 更新统计信息
        updateStats(finalResult);
        
        logger.debug("事件验证完成: {}, 最终评分: {}, 是否通过: {}", 
                   event.getTitle(), finalScore, finalResult.getIsValid());
        
        return finalResult;
    }
    
    @Override
    public List<EventValidationResult> validateEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.info("开始批量验证 {} 个事件", events.size());
        
        List<EventValidationResult> results = events.parallelStream()
                .map(this::validateEvent)
                .collect(Collectors.toList());
        
        logger.info("批量验证完成，通过: {}, 失败: {}", 
                   results.stream().mapToLong(r -> r.getIsValid() ? 1 : 0).sum(),
                   results.stream().mapToLong(r -> r.getIsValid() ? 0 : 1).sum());
        
        return results;
    }
    
    @Override
    public void addValidationRule(ValidationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("验证规则不能为空");
        }
        
        validationRules.put(rule.getRuleName(), rule);
        ruleEnabledStatus.put(rule.getRuleName(), rule.isEnabled());
        
        logger.info("添加验证规则: {}", rule.getRuleName());
    }
    
    @Override
    public void removeValidationRule(String ruleName) {
        if (ruleName == null) {
            return;
        }
        
        validationRules.remove(ruleName);
        ruleEnabledStatus.remove(ruleName);
        
        logger.info("移除验证规则: {}", ruleName);
    }
    
    @Override
    public List<ValidationRule> getAllValidationRules() {
        return new ArrayList<>(validationRules.values());
    }
    
    @Override
    public void setRuleEnabled(String ruleName, boolean enabled) {
        if (validationRules.containsKey(ruleName)) {
            ruleEnabledStatus.put(ruleName, enabled);
            logger.info("设置验证规则 {} 状态为: {}", ruleName, enabled ? "启用" : "禁用");
        }
    }
    
    @Override
    public ValidationStats getValidationStats() {
        ValidationStats stats = new ValidationStats();
        stats.setTotalValidations(totalValidations.get());
        stats.setPassedValidations(passedValidations.get());
        stats.setFailedValidations(failedValidations.get());
        stats.setStartTime(statsStartTime);
        stats.setEndTime(LocalDateTime.now());
        
        // 计算平均可信度评分（这里简化处理，实际应该记录所有评分）
        if (stats.getTotalValidations() > 0) {
            double passRate = (double) stats.getPassedValidations() / stats.getTotalValidations();
            stats.setAverageCredibilityScore(passRate * credibilityThreshold + (1 - passRate) * (credibilityThreshold - 0.2));
        }
        
        return stats;
    }
    
    @Override
    public void resetValidationStats() {
        totalValidations.set(0);
        passedValidations.set(0);
        failedValidations.set(0);
        statsStartTime = LocalDateTime.now();
        
        logger.info("重置验证统计信息");
    }
    
    @Override
    public void setCredibilityThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("可信度阈值必须在0.0到1.0之间");
        }
        
        this.credibilityThreshold = threshold;
        logger.info("设置可信度阈值为: {}", threshold);
    }
    
    @Override
    public double getCredibilityThreshold() {
        return credibilityThreshold;
    }
    
    /**
     * 获取启用的验证规则
     */
    private List<ValidationRule> getEnabledRules() {
        return validationRules.entrySet().stream()
                .filter(entry -> ruleEnabledStatus.getOrDefault(entry.getKey(), true))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    
    /**
     * 合并验证结果
     */
    private EventValidationResult mergeValidationResults(EventData event, 
                                                       List<EventValidationResult> ruleResults, 
                                                       double finalScore) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setCredibilityScore(finalScore);
        result.setIsValid(finalScore >= credibilityThreshold);
        result.setValidationType("综合验证");
        
        // 合并所有问题和建议
        List<String> allIssues = new ArrayList<>();
        List<String> allSuggestions = new ArrayList<>();
        
        for (EventValidationResult ruleResult : ruleResults) {
            if (ruleResult.getIssues() != null) {
                allIssues.addAll(ruleResult.getIssues());
            }
            if (ruleResult.getSuggestions() != null) {
                allSuggestions.addAll(ruleResult.getSuggestions());
            }
        }
        
        result.setIssues(allIssues);
        result.setSuggestions(allSuggestions);
        
        // 生成验证详情
        StringBuilder details = new StringBuilder();
        details.append("综合验证完成，最终评分: ").append(String.format("%.2f", finalScore));
        details.append("，阈值: ").append(credibilityThreshold);
        details.append("，执行规则数: ").append(ruleResults.size());
        
        result.setValidationDetails(details.toString());
        
        return result;
    }
    
    /**
     * 更新统计信息
     */
    private void updateStats(EventValidationResult result) {
        totalValidations.incrementAndGet();
        
        if (result.getIsValid()) {
            passedValidations.incrementAndGet();
        } else {
            failedValidations.incrementAndGet();
        }
    }
}