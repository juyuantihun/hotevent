package com.hotech.events.exception;

/**
 * API调用异常
 */
public class ApiException extends TimelineException {
    
    private final int statusCode;
    private final String apiUrl;
    
    public ApiException(String message, int statusCode) {
        super(message, "API_ERROR");
        this.statusCode = statusCode;
        this.apiUrl = null;
    }
    
    public ApiException(String message, int statusCode, String apiUrl) {
        super(message, "API_ERROR");
        this.statusCode = statusCode;
        this.apiUrl = apiUrl;
    }
    
    public ApiException(String message, Throwable cause, int statusCode, String apiUrl) {
        super(message, cause, "API_ERROR", "API_CALL");
        this.statusCode = statusCode;
        this.apiUrl = apiUrl;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
}