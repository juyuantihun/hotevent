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
 * 来源可信度验证规则
 * 验证事件信息来源的可靠性
 * 
 * @author Kiro
 */
@Component
public class SourceCredibilityRule implements ValidationRule {
    
    // 高可信度来源关键词
    private static final List<String> HIGH_CREDIBILITY_SOURCES = Arrays.asList(
        "新华社", "人民日报", "央视", "BBC", "CNN", "Reuters", "AP", "官方", "政府",
        "xinhua", "reuters", "bbc", "cnn", "官网", "gov.cn", "gov.uk", "gov.us"
    );
    
    // 中等可信度来源关键词
    private static final List<String> MEDIUM_CREDIBILITY_SOURCES = Arrays.asList(
        "新浪", "搜狐", "网易", "腾讯", "凤凰", "澎湃", "界面", "财新", "第一财经",
        "sina", "sohu", "163", "qq", "ifeng", "thepaper", "jiemian", "caixin"
    );
    
    // 低可信度来源关键词
    private static final List<String> LOW_CREDIBILITY_SOURCES = Arrays.asList(
        "自媒体", "博客", "论坛", "贴吧", "微博", "朋友圈", "群聊", "传言", "小道消息",
        "blog", "forum", "weibo", "wechat", "rumor", "gossip"
    );
    
    @Override
    public String getRuleName() {
        return "来源可信度验证";
    }
    
    @Override
    public String getRuleDescription() {
        return "评估事件信息来源的可信度，优先信任权威媒体和官方渠道";
    }
    
    @Override
    public EventValidationResult validate(EventData event) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setValidationType(getRuleName());
        
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        double score = 0.5; // 基础分数
        
        // 检查是否有来源信息
        List<String> sources = event.getSources();
        if (sources == null || sources.isEmpty()) {
            issues.add("缺少事件来源信息");
            suggestions.add("请提供可靠的信息来源");
            score = 0.3;
        } else {
            // 分析来源可信度
            score = analyzeSourceCredibility(sources, issues, suggestions);
        }
        
        // 检查来源数量
        if (sources != null && sources.size() > 1) {
            score += 0.1; // 多个来源增加可信度
        }
        
        // 确保评分在合理范围内
        score = Math.max(0.0, Math.min(1.0, score));
        
        result.setIsValid(score >= 0.5);
        result.setCredibilityScore(score);
        result.setIssues(issues);
        result.setSuggestions(suggestions);
        result.setValidationDetails("来源可信度验证完成，评分: " + score);
        
        return result;
    }
    
    /**
     * 分析来源可信度
     */
    private double analyzeSourceCredibility(List<String> sources, List<String> issues, 
                                          List<String> suggestions) {
        double maxScore = 0.5;
        boolean hasHighCredibility = false;
        boolean hasMediumCredibility = false;
        boolean hasLowCredibility = false;
        
        for (String source : sources) {
            if (!StringUtils.hasText(source)) {
                continue;
            }
            
            String lowerSource = source.toLowerCase();
            
            // 检查高可信度来源
            for (String keyword : HIGH_CREDIBILITY_SOURCES) {
                if (lowerSource.contains(keyword.toLowerCase())) {
                    hasHighCredibility = true;
                    maxScore = Math.max(maxScore, 0.9);
                    break;
                }
            }
            
            // 检查中等可信度来源
            if (!hasHighCredibility) {
                for (String keyword : MEDIUM_CREDIBILITY_SOURCES) {
                    if (lowerSource.contains(keyword.toLowerCase())) {
                        hasMediumCredibility = true;
                        maxScore = Math.max(maxScore, 0.7);
                        break;
                    }
                }
            }
            
            // 检查低可信度来源
            for (String keyword : LOW_CREDIBILITY_SOURCES) {
                if (lowerSource.contains(keyword.toLowerCase())) {
                    hasLowCredibility = true;
                    break;
                }
            }
        }
        
        // 根据来源类型给出建议
        if (hasHighCredibility) {
            // 有权威来源，无需额外建议
        } else if (hasMediumCredibility) {
            suggestions.add("建议补充更权威的信息来源");
        } else {
            issues.add("缺乏权威信息来源");
            suggestions.add("请提供官方或主流媒体的报道作为来源");
        }
        
        if (hasLowCredibility) {
            issues.add("包含可信度较低的信息来源");
            suggestions.add("建议验证低可信度来源的信息准确性");
            maxScore -= 0.2;
        }
        
        return maxScore;
    }
    
    @Override
    public double getWeight() {
        return 0.9; // 来源可信度权重最高
    }
}