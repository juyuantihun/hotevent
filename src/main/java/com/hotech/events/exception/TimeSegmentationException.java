package com.hotech.events.exception;

/**
 * 时间段分割异常
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public class TimeSegmentationException extends TimelineEnhancementException {
    
    public TimeSegmentationException(String message) {
        super("TIME_SEGMENTATION_ERROR", "SEGMENTATION", message);
    }
    
    public TimeSegmentationException(String message, Throwable cause) {
        super("TIME_SEGMENTATION_ERROR", "SEGMENTATION", message, cause);
    }
}