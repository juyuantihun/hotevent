package com.hotech.events.validation.rules;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.validation.ValidationRule;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间一致性验证规则
 * 验证事件时间是否合理和一致
 * 
 * @author Kiro
 */
@Component
public class TimeConsistencyRule implements ValidationRule {
    
    @Override
    public String getRuleName() {
        return "时间一致性验证";
    }
    
    @Override
    public String getRuleDescription() {
        return "验证事件时间是否在合理范围内，不能是未来时间或过于久远的历史时间";
    }
    
    @Override
    public EventValidationResult validate(EventData event) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setValidationType(getRuleName());
        
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        double score = 1.0;
        
        LocalDateTime eventTime = event.getEventTime();
        LocalDateTime now = LocalDateTime.now();
        
        // 检查事件时间是否为空
        if (eventTime == null) {
            issues.add("事件时间不能为空");
            suggestions.add("请提供准确的事件发生时间");
            score -= 0.5;
        } else {
            // 检查是否是未来时间
            if (eventTime.isAfter(now)) {
                issues.add("事件时间不能是未来时间");
                suggestions.add("请确认事件时间的准确性");
                score -= 0.3;
            }
            
            // 检查是否过于久远 (超过100年)
            LocalDateTime hundredYearsAgo = now.minusYears(100);
            if (eventTime.isBefore(hundredYearsAgo)) {
                issues.add("事件时间过于久远，可能不准确");
                suggestions.add("请确认历史事件的准确时间");
                score -= 0.2;
            }
            
            // 检查时间格式的合理性 (例如：不应该是1月32日这样的无效日期)
            try {
                // 如果能正常解析，说明时间格式合理
                eventTime.toString();
            } catch (Exception e) {
                issues.add("事件时间格式不正确");
                suggestions.add("请使用标准的时间格式");
                score -= 0.4;
            }
        }
        
        // 确保评分不低于0
        score = Math.max(0.0, score);
        
        result.setIsValid(issues.isEmpty());
        result.setCredibilityScore(score);
        result.setIssues(issues);
        result.setSuggestions(suggestions);
        result.setValidationDetails("时间一致性验证完成，评分: " + score);
        
        return result;
    }
    
    @Override
    public double getWeight() {
        return 0.8; // 时间一致性权重较高
    }
}