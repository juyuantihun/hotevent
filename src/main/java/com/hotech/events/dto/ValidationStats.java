package com.hotech.events.dto;

import java.time.LocalDateTime;

/**
 * 验证统计信息DTO
 * 
 * @author Kiro
 */
public class ValidationStats {
    
    /**
     * 总验证次数
     */
    private Long totalValidations;
    
    /**
     * 验证通过次数
     */
    private Long passedValidations;
    
    /**
     * 验证失败次数
     */
    private Long failedValidations;
    
    /**
     * 平均可信度评分
     */
    private Double averageCredibilityScore;
    
    /**
     * 最高可信度评分
     */
    private Double maxCredibilityScore;
    
    /**
     * 最低可信度评分
     */
    private Double minCredibilityScore;
    
    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 验证通过率
     */
    private Double passRate;
    
    // 构造函数
    public ValidationStats() {
        this.endTime = LocalDateTime.now();
    }
    
    // 计算验证通过率
    public void calculatePassRate() {
        if (totalValidations != null && totalValidations > 0) {
            this.passRate = (double) passedValidations / totalValidations;
        } else {
            this.passRate = 0.0;
        }
    }
    
    // Getter和Setter方法
    public Long getTotalValidations() {
        return totalValidations;
    }
    
    public void setTotalValidations(Long totalValidations) {
        this.totalValidations = totalValidations;
        calculatePassRate();
    }
    
    public Long getPassedValidations() {
        return passedValidations;
    }
    
    public void setPassedValidations(Long passedValidations) {
        this.passedValidations = passedValidations;
        calculatePassRate();
    }
    
    public Long getFailedValidations() {
        return failedValidations;
    }
    
    public void setFailedValidations(Long failedValidations) {
        this.failedValidations = failedValidations;
    }
    
    public Double getAverageCredibilityScore() {
        return averageCredibilityScore;
    }
    
    public void setAverageCredibilityScore(Double averageCredibilityScore) {
        this.averageCredibilityScore = averageCredibilityScore;
    }
    
    public Double getMaxCredibilityScore() {
        return maxCredibilityScore;
    }
    
    public void setMaxCredibilityScore(Double maxCredibilityScore) {
        this.maxCredibilityScore = maxCredibilityScore;
    }
    
    public Double getMinCredibilityScore() {
        return minCredibilityScore;
    }
    
    public void setMinCredibilityScore(Double minCredibilityScore) {
        this.minCredibilityScore = minCredibilityScore;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Double getPassRate() {
        return passRate;
    }
    
    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }
    
    @Override
    public String toString() {
        return "ValidationStats{" +
                "totalValidations=" + totalValidations +
                ", passedValidations=" + passedValidations +
                ", failedValidations=" + failedValidations +
                ", averageCredibilityScore=" + averageCredibilityScore +
                ", passRate=" + passRate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}