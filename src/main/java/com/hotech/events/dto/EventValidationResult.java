package com.hotech.events.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件验证结果DTO
 * 
 * @author Kiro
 */
public class EventValidationResult {
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 是否验证通过
     */
    private Boolean isValid;
    
    /**
     * 可信度评分 (0.0-1.0)
     */
    private Double credibilityScore;
    
    /**
     * 验证问题列表
     */
    private List<String> issues;
    
    /**
     * 改进建议列表
     */
    private List<String> suggestions;
    
    /**
     * 验证类型
     */
    private String validationType;
    
    /**
     * 验证详情
     */
    private String validationDetails;
    
    /**
     * 验证时间
     */
    private LocalDateTime validatedAt;
    
    // 构造函数
    public EventValidationResult() {
        this.validatedAt = LocalDateTime.now();
    }
    
    public EventValidationResult(String eventId, Boolean isValid, Double credibilityScore) {
        this();
        this.eventId = eventId;
        this.isValid = isValid;
        this.credibilityScore = credibilityScore;
    }
    
    // Getter和Setter方法
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public Boolean getIsValid() {
        return isValid;
    }
    
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
    
    public Double getCredibilityScore() {
        return credibilityScore;
    }
    
    public void setCredibilityScore(Double credibilityScore) {
        this.credibilityScore = credibilityScore;
    }
    
    public List<String> getIssues() {
        return issues;
    }
    
    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    public String getValidationType() {
        return validationType;
    }
    
    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }
    
    public String getValidationDetails() {
        return validationDetails;
    }
    
    public void setValidationDetails(String validationDetails) {
        this.validationDetails = validationDetails;
    }
    
    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }
    
    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
    
    @Override
    public String toString() {
        return "EventValidationResult{" +
                "eventId=" + eventId +
                ", isValid=" + isValid +
                ", credibilityScore=" + credibilityScore +
                ", validationType='" + validationType + '\'' +
                ", validatedAt=" + validatedAt +
                '}';
    }
}