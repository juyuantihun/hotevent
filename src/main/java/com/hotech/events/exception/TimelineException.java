package com.hotech.events.exception;

/**
 * 时间线相关异常基类
 */
public class TimelineException extends RuntimeException {
    
    private final String errorCode;
    private final String operation;
    
    public TimelineException(String message) {
        super(message);
        this.errorCode = "TIMELINE_ERROR";
        this.operation = "UNKNOWN";
    }
    
    public TimelineException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.operation = "UNKNOWN";
    }
    
    public TimelineException(String message, String errorCode, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public TimelineException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TIMELINE_ERROR";
        this.operation = "UNKNOWN";
    }
    
    public TimelineException(String message, Throwable cause, String errorCode, String operation) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getOperation() {
        return operation;
    }
}