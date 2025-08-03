package com.hotech.events.validation;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;

/**
 * 事件验证规则接口
 * 
 * @author Kiro
 */
public interface ValidationRule {
    
    /**
     * 获取验证规则名称
     */
    String getRuleName();
    
    /**
     * 获取验证规则描述
     */
    String getRuleDescription();
    
    /**
     * 验证事件
     * 
     * @param event 待验证的事件
     * @return 验证结果
     */
    EventValidationResult validate(EventData event);
    
    /**
     * 获取规则权重 (用于综合评分计算)
     */
    default double getWeight() {
        return 1.0;
    }
    
    /**
     * 是否启用该规则
     */
    default boolean isEnabled() {
        return true;
    }
}