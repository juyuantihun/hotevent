package com.hotech.events.service;

import com.hotech.events.dto.EventData;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 备用数据生成器接口
 * 当API调用失败或解析失败时，提供备用的事件数据
 * 
 * @author Kiro
 */
public interface FallbackDataGenerator {
    
    /**
     * 生成基于时间和地区的默认事件
     * 
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 生成的事件列表
     */
    List<EventData> generateDefaultEvents(List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 从数据库获取相似事件
     * 
     * @param description 描述关键词
     * @param regionIds 地区ID列表
     * @return 相似事件列表
     */
    List<EventData> getSimilarEventsFromDatabase(String description, List<Long> regionIds);
    
    /**
     * 生成测试事件数据
     * 
     * @param theme 主题
     * @param count 数量
     * @return 测试事件列表
     */
    List<EventData> generateTestEvents(String theme, int count);
    
    /**
     * 生成基于历史数据的事件
     * 
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param minCount 最小事件数量
     * @return 历史事件列表
     */
    List<EventData> generateHistoricalEvents(List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime, int minCount);
    
    /**
     * 生成通用事件模板
     * 
     * @param count 数量
     * @return 通用事件列表
     */
    List<EventData> generateGenericEvents(int count);
}