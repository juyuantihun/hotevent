package com.hotech.events.exception;

/**
 * API调用异常
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public class ApiCallException extends TimelineEnhancementException {
    
    private final String apiType;
    private final int statusCode;
    
    public ApiCallException(String apiType, String message) {
        super("API_CALL_ERROR", "API", message);
        this.apiType = apiType;
        this.statusCode = -1;
    }
    
    public ApiCallException(String apiType, String message, Throwable cause) {
        super("API_CALL_ERROR", "API", message, cause);
        this.apiType = apiType;
        this.statusCode = -1;
    }
    
    public ApiCallException(String apiType, int statusCode, String message) {
        super("API_CALL_ERROR", "API", message);
        this.apiType = apiType;
        this.statusCode = statusCode;
    }
    
    public ApiCallException(String apiType, int statusCode, String message, Throwable cause) {
        super("API_CALL_ERROR", "API", message, cause);
        this.apiType = apiType;
        this.statusCode = statusCode;
    }
    
    public String getApiType() {
        return apiType;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}