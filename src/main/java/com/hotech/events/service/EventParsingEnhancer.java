package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.entity.EventParsingRecord;

import java.util.List;

/**
 * 事件解析增强器接口
 * 提供多种解析策略来处理不同格式的API响应
 * 
 * @author Kiro
 */
public interface EventParsingEnhancer {
    
    /**
     * 使用多种策略解析事件
     * 
     * @param response API响应内容
     * @param apiType API类型
     * @param requestSummary 请求摘要
     * @return 解析出的事件列表
     */
    List<EventData> parseWithMultipleStrategies(String response, String apiType, String requestSummary);
    
    /**
     * 增强的JSON提取方法
     * 
     * @param response 原始响应
     * @return 提取的JSON字符串
     */
    String extractJsonWithAdvancedMethods(String response);
    
    /**
     * 从文本中解析事件
     * 
     * @param text 文本内容
     * @return 解析出的事件列表
     */
    List<EventData> parseEventsFromText(String text);
    
    /**
     * 验证解析结果
     * 
     * @param events 解析出的事件列表
     * @return 是否验证通过
     */
    boolean validateParsedEvents(List<EventData> events);
    
    /**
     * 获取解析统计信息
     * 
     * @param apiType API类型（可选）
     * @param hours 统计时间范围（小时）
     * @return 统计信息
     */
    java.util.Map<String, Object> getParsingStats(String apiType, int hours);
    
    /**
     * 清理旧的解析记录
     * 
     * @param daysOld 保留天数
     * @return 清理的记录数
     */
    int cleanupOldParsingRecords(int daysOld);
}