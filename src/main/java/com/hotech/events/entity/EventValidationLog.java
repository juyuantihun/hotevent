package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 事件验证记录实体类
 * 
 * @author Kiro
 */
@TableName("event_validation_log")
public class EventValidationLog {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 事件ID
     */
    private Long eventId;
    
    /**
     * 验证类型
     */
    private String validationType;
    
    /**
     * 验证结果 (1-通过, 0-失败)
     */
    private Boolean validationResult;
    
    /**
     * 可信度评分
     */
    private BigDecimal credibilityScore;
    
    /**
     * 验证详情 (JSON格式)
     */
    private String validationDetails;
    
    /**
     * 验证时间
     */
    private LocalDateTime validatedAt;
    
    // 构造函数
    public EventValidationLog() {
        this.validatedAt = LocalDateTime.now();
    }
    
    public EventValidationLog(Long eventId, String validationType, Boolean validationResult, 
                            BigDecimal credibilityScore) {
        this();
        this.eventId = eventId;
        this.validationType = validationType;
        this.validationResult = validationResult;
        this.credibilityScore = credibilityScore;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public String getValidationType() {
        return validationType;
    }
    
    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }
    
    public Boolean getValidationResult() {
        return validationResult;
    }
    
    public void setValidationResult(Boolean validationResult) {
        this.validationResult = validationResult;
    }
    
    public BigDecimal getCredibilityScore() {
        return credibilityScore;
    }
    
    public void setCredibilityScore(BigDecimal credibilityScore) {
        this.credibilityScore = credibilityScore;
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
        return "EventValidationLog{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", validationType='" + validationType + '\'' +
                ", validationResult=" + validationResult +
                ", credibilityScore=" + credibilityScore +
                ", validatedAt=" + validatedAt +
                '}';
    }
}