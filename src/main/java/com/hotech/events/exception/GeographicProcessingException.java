package com.hotech.events.exception;

/**
 * 地理信息处理异常
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public class GeographicProcessingException extends TimelineEnhancementException {
    
    public GeographicProcessingException(String message) {
        super("GEOGRAPHIC_PROCESSING_ERROR", "GEOGRAPHIC", message);
    }
    
    public GeographicProcessingException(String message, Throwable cause) {
        super("GEOGRAPHIC_PROCESSING_ERROR", "GEOGRAPHIC", message, cause);
    }
}