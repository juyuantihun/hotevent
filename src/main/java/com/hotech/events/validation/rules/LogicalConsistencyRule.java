package com.hotech.events.validation.rules;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 逻辑一致性验证规则
 * 验证事件描述的逻辑性和合理性
 * 
 * @author Kiro
 */
@Component
public class LogicalConsistencyRule implements ValidationRule {
    
    // 可疑的逻辑关键词
    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
        "据说", "传言", "有人说", "可能", "也许", "大概", "估计", "听说", "谣传"
    );
    
    // 积极的确定性关键词
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
        "确认", "证实", "官方", "正式", "宣布", "发布", "公告", "声明", "报告"
    );
    
    @Override
    public String getRuleName() {
        return "逻辑一致性验证";
    }
    
    @Override
    public String getRuleDescription() {
        return "验证事件描述的逻辑性，检查是否存在矛盾或不合理的内容";
    }
    
    @Override
    public EventValidationResult validate(EventData event) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setValidationType(getRuleName());
        
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        double score = 1.0;
        
        // 验证标题和描述的一致性
        score = validateTitleDescriptionConsistency(event, issues, suggestions, score);
        
        // 验证描述的确定性
        score = validateDescriptionCertainty(event, issues, suggestions, score);
        
        // 验证主体客体关系的合理性
        score = validateSubjectObjectRelation(event, issues, suggestions, score);
        
        // 验证事件类型与描述的匹配度
        score = validateEventTypeConsistency(event, issues, suggestions, score);
        
        // 确保评分不低于0
        score = Math.max(0.0, score);
        
        result.setIsValid(issues.isEmpty() || score >= 0.6);
        result.setCredibilityScore(score);
        result.setIssues(issues);
        result.setSuggestions(suggestions);
        result.setValidationDetails("逻辑一致性验证完成，评分: " + score);
        
        return result;
    }
    
    /**
     * 验证标题和描述的一致性
     */
    private double validateTitleDescriptionConsistency(EventData event, List<String> issues, 
                                                     List<String> suggestions, double score) {
        String title = event.getTitle();
        String description = event.getDescription();
        
        if (!StringUtils.hasText(title)) {
            issues.add("事件标题缺失");
            suggestions.add("请提供简洁明确的事件标题");
            score -= 0.3;
        }
        
        if (!StringUtils.hasText(description)) {
            issues.add("事件描述缺失");
            suggestions.add("请提供详细的事件描述");
            score -= 0.4;
        }
        
        if (StringUtils.hasText(title) && StringUtils.hasText(description)) {
            // 检查标题是否过长
            if (title.length() > 100) {
                issues.add("事件标题过长，应该简洁明了");
                suggestions.add("建议将标题控制在100字符以内");
                score -= 0.1;
            }
            
            // 检查描述是否过短
            if (description.length() < 20) {
                issues.add("事件描述过于简短，缺乏必要信息");
                suggestions.add("请提供更详细的事件描述");
                score -= 0.2;
            }
        }
        
        return score;
    }
    
    /**
     * 验证描述的确定性
     */
    private double validateDescriptionCertainty(EventData event, List<String> issues, 
                                              List<String> suggestions, double score) {
        String description = event.getDescription();
        
        if (StringUtils.hasText(description)) {
            // 检查可疑关键词
            int suspiciousCount = 0;
            for (String keyword : SUSPICIOUS_KEYWORDS) {
                if (description.contains(keyword)) {
                    suspiciousCount++;
                }
            }
            
            if (suspiciousCount > 0) {
                issues.add("描述中包含不确定性词汇，可能影响事件可信度");
                suggestions.add("建议使用更确定的表述方式");
                score -= suspiciousCount * 0.1;
            }
            
            // 检查积极关键词
            boolean hasPositiveKeywords = POSITIVE_KEYWORDS.stream()
                    .anyMatch(description::contains);
            
            if (hasPositiveKeywords) {
                score += 0.1; // 有官方确认的表述，增加可信度
            }
        }
        
        return score;
    }
    
    /**
     * 验证主体客体关系的合理性
     */
    private double validateSubjectObjectRelation(EventData event, List<String> issues, 
                                               List<String> suggestions, double score) {
        String subject = event.getSubject();
        String object = event.getObject();
        
        if (!StringUtils.hasText(subject)) {
            issues.add("事件主体信息缺失");
            suggestions.add("请明确事件的主要参与者或发起方");
            score -= 0.2;
        }
        
        if (StringUtils.hasText(subject) && StringUtils.hasText(object)) {
            // 检查主体和客体是否相同
            if (subject.equals(object)) {
                issues.add("事件主体和客体相同，逻辑可能存在问题");
                suggestions.add("请确认事件的主体和客体关系");
                score -= 0.2;
            }
        }
        
        return score;
    }
    
    /**
     * 验证事件类型与描述的匹配度
     */
    private double validateEventTypeConsistency(EventData event, List<String> issues, 
                                              List<String> suggestions, double score) {
        String eventType = event.getEventType();
        String description = event.getDescription();
        
        if (!StringUtils.hasText(eventType)) {
            issues.add("事件类型信息缺失");
            suggestions.add("请为事件指定合适的类型分类");
            score -= 0.1;
        }
        
        // 这里可以根据具体的事件类型和描述进行更复杂的匹配验证
        // 目前只做基础检查
        
        return score;
    }
    
    @Override
    public double getWeight() {
        return 0.7; // 逻辑一致性权重较高
    }
}