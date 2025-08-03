package com.hotech.events.config;

import com.hotech.events.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 配置验证器
 * 负责验证配置值的有效性和完整性
 * 
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Component
public class ConfigValidator {
    
    /**
     * 验证配置对象
     * 
     * @param config 配置对象
     * @return 验证结果
     */
    public ValidationResult validateConfig(SystemConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null) {
            result.addError("配置对象不能为空");
            return result;
        }
        
        // 验证配置键
        validateConfigKey(config.getConfigKey(), result);
        
        // 验证配置类型
        validateConfigType(config.getConfigType(), result);
        
        // 验证配置分组
        validateConfigGroup(config.getConfigGroup(), result);
        
        // 验证配置值
        validateConfigValue(config, result);
        
        // 验证验证规则
        validateValidationRule(config.getValidationRule(), result);
        
        return result;
    }
    
    /**
     * 验证配置键
     */
    private void validateConfigKey(String configKey, ValidationResult result) {
        if (!StringUtils.hasText(configKey)) {
            result.addError("配置键不能为空");
            return;
        }
        
        // 配置键格式验证：只允许字母、数字、点号、连字符和下划线
        if (!Pattern.matches("^[a-zA-Z0-9._-]+$", configKey)) {
            result.addError("配置键格式无效，只允许字母、数字、点号、连字符和下划线");
        }
        
        // 配置键长度验证
        if (configKey.length() > 100) {
            result.addError("配置键长度不能超过100个字符");
        }
    }
    
    /**
     * 验证配置类型
     */
    private void validateConfigType(String configType, ValidationResult result) {
        if (!StringUtils.hasText(configType)) {
            result.addWarning("配置类型为空，将使用默认类型STRING");
            return;
        }
        
        try {
            SystemConfig.ConfigType.fromCode(configType);
        } catch (IllegalArgumentException e) {
            result.addError("无效的配置类型: " + configType);
        }
    }
    
    /**
     * 验证配置分组
     */
    private void validateConfigGroup(String configGroup, ValidationResult result) {
        if (!StringUtils.hasText(configGroup)) {
            result.addError("配置分组不能为空");
            return;
        }
        
        try {
            SystemConfig.ConfigGroup.fromCode(configGroup);
        } catch (IllegalArgumentException e) {
            result.addError("无效的配置分组: " + configGroup);
        }
    }
    
    /**
     * 验证配置值
     */
    private void validateConfigValue(SystemConfig config, ValidationResult result) {
        String configValue = config.getConfigValue();
        String configType = config.getConfigType();
        
        // 必需配置值不能为空
        if (Boolean.TRUE.equals(config.getIsRequired()) && !StringUtils.hasText(configValue)) {
            result.addError("必需配置的值不能为空");
            return;
        }
        
        if (!StringUtils.hasText(configValue)) {
            return; // 非必需配置可以为空
        }
        
        // 根据配置类型验证值格式
        if (StringUtils.hasText(configType)) {
            validateValueByType(configValue, configType, result);
        }
        
        // 根据验证规则验证值
        if (StringUtils.hasText(config.getValidationRule())) {
            validateValueByRule(configValue, config.getValidationRule(), result);
        }
    }
    
    /**
     * 根据类型验证配置值
     */
    private void validateValueByType(String value, String type, ValidationResult result) {
        try {
            SystemConfig.ConfigType configType = SystemConfig.ConfigType.fromCode(type);
            
            switch (configType) {
                case NUMBER:
                    try {
                        Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        result.addError("配置值不是有效的数字: " + value);
                    }
                    break;
                    
                case BOOLEAN:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        result.addError("配置值不是有效的布尔值: " + value);
                    }
                    break;
                    
                case JSON:
                    // 简单的JSON格式验证
                    if (!isValidJson(value)) {
                        result.addError("配置值不是有效的JSON格式: " + value);
                    }
                    break;
                    
                case STRING:
                default:
                    // 字符串类型不需要特殊验证
                    break;
            }
        } catch (IllegalArgumentException e) {
            result.addError("无效的配置类型: " + type);
        }
    }
    
    /**
     * 根据验证规则验证配置值
     */
    private void validateValueByRule(String value, String rule, ValidationResult result) {
        try {
            Pattern pattern = Pattern.compile(rule);
            if (!pattern.matcher(value).matches()) {
                result.addError("配置值不符合验证规则: " + rule);
            }
        } catch (Exception e) {
            result.addError("验证规则格式错误: " + rule);
        }
    }
    
    /**
     * 验证验证规则
     */
    private void validateValidationRule(String rule, ValidationResult result) {
        if (!StringUtils.hasText(rule)) {
            return; // 验证规则可以为空
        }
        
        try {
            Pattern.compile(rule);
        } catch (Exception e) {
            result.addError("验证规则格式错误: " + rule + ", 错误信息: " + e.getMessage());
        }
    }
    
    /**
     * 简单的JSON格式验证
     */
    private boolean isValidJson(String json) {
        if (!StringUtils.hasText(json)) {
            return false;
        }
        
        json = json.trim();
        return (json.startsWith("{") && json.endsWith("}")) || 
               (json.startsWith("[") && json.endsWith("]"));
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("valid=").append(isValid());
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }
            sb.append("}");
            return sb.toString();
        }
    }
}