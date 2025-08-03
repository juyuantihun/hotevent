package com.hotech.events.validation.rules;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 地理位置验证规则
 * 验证事件地点信息的准确性和合理性
 * 
 * @author Kiro
 */
@Component
public class LocationValidationRule implements ValidationRule {
    
    // 常见的无效地点关键词
    private static final List<String> INVALID_LOCATION_KEYWORDS = Arrays.asList(
        "未知", "不明", "网络", "虚拟", "线上", "在线", "数字", "电子"
    );
    
    // 地点名称的基本格式验证
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z\\s\\-,，。]+$");
    
    @Override
    public String getRuleName() {
        return "地理位置验证";
    }
    
    @Override
    public String getRuleDescription() {
        return "验证事件发生地点的真实性和准确性，排除虚假或不合理的地点信息";
    }
    
    @Override
    public EventValidationResult validate(EventData event) {
        EventValidationResult result = new EventValidationResult();
        result.setEventId(event.getId());
        result.setValidationType(getRuleName());
        
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        double score = 1.0;
        
        String location = event.getLocation();
        
        // 检查地点是否为空
        if (!StringUtils.hasText(location)) {
            issues.add("事件地点信息缺失");
            suggestions.add("请提供具体的事件发生地点");
            score -= 0.4;
        } else {
            // 检查地点长度是否合理
            if (location.length() < 2) {
                issues.add("地点信息过于简短，可能不准确");
                suggestions.add("请提供更详细的地点信息");
                score -= 0.2;
            }
            
            if (location.length() > 200) {
                issues.add("地点信息过长，可能包含无关内容");
                suggestions.add("请简化地点描述，保留核心信息");
                score -= 0.1;
            }
            
            // 检查是否包含无效关键词
            for (String keyword : INVALID_LOCATION_KEYWORDS) {
                if (location.contains(keyword)) {
                    issues.add("地点包含可疑关键词: " + keyword);
                    suggestions.add("请确认地点的真实性");
                    score -= 0.3;
                    break;
                }
            }
            
            // 检查地点格式是否合理
            if (!LOCATION_PATTERN.matcher(location).matches()) {
                issues.add("地点格式不规范，包含特殊字符");
                suggestions.add("请使用标准的地名格式");
                score -= 0.2;
            }
            
            // 检查是否包含常见的地理标识词
            boolean hasGeoIndicator = containsGeoIndicator(location);
            if (!hasGeoIndicator && location.length() > 10) {
                issues.add("地点缺乏明确的地理标识");
                suggestions.add("建议包含国家、省份、城市等地理标识");
                score -= 0.1;
            }
        }
        
        // 确保评分不低于0
        score = Math.max(0.0, score);
        
        result.setIsValid(issues.isEmpty());
        result.setCredibilityScore(score);
        result.setIssues(issues);
        result.setSuggestions(suggestions);
        result.setValidationDetails("地理位置验证完成，评分: " + score);
        
        return result;
    }
    
    /**
     * 检查地点是否包含地理标识词
     */
    private boolean containsGeoIndicator(String location) {
        List<String> geoIndicators = Arrays.asList(
            "国", "省", "市", "县", "区", "镇", "村", "街", "路", "大道", "广场",
            "State", "City", "County", "Street", "Avenue", "Square", "District"
        );
        
        return geoIndicators.stream().anyMatch(location::contains);
    }
    
    @Override
    public double getWeight() {
        return 0.6; // 地理位置验证权重中等
    }
}