package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.exception.ValidationException;

import java.util.List;

/**
 * 数据验证失败处理服务接口
 */
public interface DataValidationFailureHandler {
    
    /**
     * 处理事件数据验证失败
     * 
     * @param event 验证失败的事件
     * @param validationErrors 验证错误列表
     * @return 处理后的事件数据，如果无法修复则返回null
     */
    EventData handleEventValidationFailure(EventData event, List<String> validationErrors);
    
    /**
     * 处理批量事件验证失败
     * 
     * @param events 验证失败的事件列表
     * @param validationResults 验证结果
     * @return 处理后的有效事件列表
     */
    List<EventData> handleBatchValidationFailure(List<EventData> events, List<ValidationResult> validationResults);
    
    /**
     * 记录验证失败统计
     * 
     * @param operation 操作名称
     * @param failureType 失败类型
     * @param failureCount 失败数量
     */
    void recordValidationFailureStats(String operation, String failureType, int failureCount);
    
    /**
     * 获取验证失败统计
     * 
     * @return 验证失败统计信息
     */
    ValidationFailureStats getValidationFailureStats();
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private boolean isValid;
        private List<String> errors;
        private String suggestion;
        
        public ValidationResult(boolean isValid, List<String> errors, String suggestion) {
            this.isValid = isValid;
            this.errors = errors;
            this.suggestion = suggestion;
        }
        
        // Getters and Setters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
    
    /**
     * 验证失败统计
     */
    class ValidationFailureStats {
        private long totalValidations;
        private long failedValidations;
        private long fixedValidations;
        private double failureRate;
        private double fixRate;
        
        // Getters and Setters
        public long getTotalValidations() { return totalValidations; }
        public void setTotalValidations(long totalValidations) { this.totalValidations = totalValidations; }
        
        public long getFailedValidations() { return failedValidations; }
        public void setFailedValidations(long failedValidations) { this.failedValidations = failedValidations; }
        
        public long getFixedValidations() { return fixedValidations; }
        public void setFixedValidations(long fixedValidations) { this.fixedValidations = fixedValidations; }
        
        public double getFailureRate() { return failureRate; }
        public void setFailureRate(double failureRate) { this.failureRate = failureRate; }
        
        public double getFixRate() { return fixRate; }
        public void setFixRate(double fixRate) { this.fixRate = fixRate; }
    }
}