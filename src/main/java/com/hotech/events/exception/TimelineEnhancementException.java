package com.hotech.events.exception;

/**
 * 时间线增强异常
 * 用于时间线生成过程中的各种异常情况
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public class TimelineEnhancementException extends RuntimeException {
    
    private final String errorCode;
    private final String category;
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param category 错误类别
     * @param message 错误消息
     */
    public TimelineEnhancementException(String errorCode, String category, String message) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
    }
    
    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param category 错误类别
     * @param message 错误消息
     * @param cause 原因异常
     */
    public TimelineEnhancementException(String errorCode, String category, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误类别
     * 
     * @return 错误类别
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * 获取错误类型（与错误类别相同）
     * 
     * @return 错误类型
     */
    public String getErrorType() {
        return category;
    }
    
    @Override
    public String toString() {
        return String.format("TimelineEnhancementException[errorCode=%s, category=%s, message=%s]", 
                errorCode, category, getMessage());
    }
}