package com.hotech.events.validation.rules;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 重复事件检测规则
 * 检测事件是否可能是重复或相似的事件
 * 
 * @author Kiro
 */
@Component
public class DuplicateDetectionRule implements ValidationRule {
    
    @Override
    public String getRuleName() {
        return "重复事件检测";
    }
    
    @Override
    public String getRuleDescription() {
        return "检测事件是否存在重复或高度相似的内容，避免重复信息";
    }
    
    @Override
    public EventValidationResult validate(EventData event) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setValidationType(getRuleName());
        
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        double score = 1.0;
        
        // 检查标题和描述的重复性
        score = checkTitleDescriptionDuplication(event, issues, suggestions, score);
        
        // 检查关键信息的完整性
        score = checkKeyInformationCompleteness(event, issues, suggestions, score);
        
        // 检查内容的原创性指标
        score = checkContentOriginality(event, issues, suggestions, score);
        
        // 确保评分不低于0
        score = Math.max(0.0, score);
        
        result.setIsValid(score >= 0.7); // 重复检测的阈值稍高
        result.setCredibilityScore(score);
        result.setIssues(issues);
        result.setSuggestions(suggestions);
        result.setValidationDetails("重复事件检测完成，评分: " + score);
        
        return result;
    }
    
    /**
     * 检查标题和描述的重复性
     */
    private double checkTitleDescriptionDuplication(EventData event, List<String> issues, 
                                                  List<String> suggestions, double score) {
        String title = event.getTitle();
        String description = event.getDescription();
        
        if (StringUtils.hasText(title) && StringUtils.hasText(description)) {
            // 检查标题是否在描述中完全重复
            if (description.contains(title)) {
                issues.add("事件描述中包含完整的标题内容，可能存在重复");
                suggestions.add("建议简化描述，避免与标题重复");
                score -= 0.1;
            }
            
            // 检查是否有大量重复的词汇
            double similarity = calculateTextSimilarity(title, description);
            if (similarity > 0.8) {
                issues.add("标题和描述相似度过高，内容可能重复");
                suggestions.add("建议丰富描述内容，提供更多独特信息");
                score -= 0.2;
            }
        }
        
        return score;
    }
    
    /**
     * 检查关键信息的完整性
     */
    private double checkKeyInformationCompleteness(EventData event, List<String> issues, 
                                                 List<String> suggestions, double score) {
        // 检查是否缺少关键字段
        int missingFields = 0;
        
        if (!StringUtils.hasText(event.getSubject())) {
            missingFields++;
        }
        
        if (!StringUtils.hasText(event.getObject())) {
            missingFields++;
        }
        
        if (!StringUtils.hasText(event.getEventType())) {
            missingFields++;
        }
        
        if (event.getKeywords() == null || event.getKeywords().isEmpty()) {
            missingFields++;
        }
        
        if (missingFields > 2) {
            issues.add("事件缺少过多关键信息，可能是不完整的重复内容");
            suggestions.add("请补充完整的事件信息");
            score -= 0.3;
        } else if (missingFields > 0) {
            score -= missingFields * 0.1;
        }
        
        return score;
    }
    
    /**
     * 检查内容的原创性指标
     */
    private double checkContentOriginality(EventData event, List<String> issues, 
                                         List<String> suggestions, double score) {
        String description = event.getDescription();
        
        if (StringUtils.hasText(description)) {
            // 检查是否包含常见的复制粘贴标识
            List<String> copyIndicators = List.of(
                "转载", "来源", "摘自", "引用", "复制", "转发", "分享自"
            );
            
            for (String indicator : copyIndicators) {
                if (description.contains(indicator)) {
                    issues.add("描述中包含转载标识，可能是重复内容");
                    suggestions.add("如果是转载内容，请标明原始来源");
                    score -= 0.15;
                    break;
                }
            }
            
            // 检查描述的独特性（简单的启发式方法）
            if (isGenericDescription(description)) {
                issues.add("事件描述过于通用，缺乏独特性");
                suggestions.add("请提供更具体和独特的事件描述");
                score -= 0.1;
            }
        }
        
        return score;
    }
    
    /**
     * 计算文本相似度（简化版本）
     */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // 简单的字符级相似度计算
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0.0;
    }
    
    /**
     * 检查是否是通用描述
     */
    private boolean isGenericDescription(String description) {
        List<String> genericPhrases = List.of(
            "发生了", "出现了", "进行了", "举行了", "召开了", "宣布了",
            "据报道", "有消息", "最新消息", "重要事件"
        );
        
        int genericCount = 0;
        for (String phrase : genericPhrases) {
            if (description.contains(phrase)) {
                genericCount++;
            }
        }
        
        // 如果包含多个通用短语，认为是通用描述
        return genericCount >= 2;
    }
    
    @Override
    public double getWeight() {
        return 0.5; // 重复检测权重中等
    }
}