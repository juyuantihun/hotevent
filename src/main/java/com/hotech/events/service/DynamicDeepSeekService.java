package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Region;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 动态DeepSeek服务接口
 * 根据时间范围动态选择使用官方API还是火山引擎联网搜索API
 */
public interface DynamicDeepSeekService {
    
    /**
     * 根据时间范围动态获取事件
     * - 2024年以前：使用官方DeepSeek API
     * - 2024年及以后：使用火山引擎联网搜索API
     * 
     * @param request 时间线生成请求
     * @return 事件列表
     */
    List<EventData> fetchEventsWithTimeBasedAPI(TimelineGenerateRequest request);
    
    /**
     * 根据时间范围动态分析事件关系
     * 
     * @param events 事件列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件关系列表
     */
    List<Map<String, Object>> analyzeEventRelationsWithTimeBasedAPI(
            List<Map<String, Object>> events, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据时间范围动态组织时间线
     * 
     * @param events 事件列表
     * @param relations 事件关系列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间线列表
     */
    List<Map<String, Object>> organizeTimelinesWithTimeBasedAPI(
            List<Map<String, Object>> events, List<Map<String, Object>> relations,
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 判断是否应该使用联网搜索
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return true表示使用联网搜索（火山引擎），false表示使用官方API
     */
    boolean shouldUseWebSearch(LocalDateTime startTime, LocalDateTime endTime);
}