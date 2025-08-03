package com.hotech.events.exception;

import java.util.List;

/**
 * 数据验证异常
 */
public class ValidationException extends TimelineException {
    
    private final List<String> validationErrors;
    private final String fieldName;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = null;
        this.fieldName = null;
    }
    
    public ValidationException(String message, String fieldName) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = null;
        this.fieldName = fieldName;
    }
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
        this.fieldName = null;
    }
    
    public ValidationException(String message, List<String> validationErrors, String fieldName) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
        this.fieldName = fieldName;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}