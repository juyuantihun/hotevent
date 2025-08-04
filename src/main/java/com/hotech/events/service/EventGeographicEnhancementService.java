package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.entity.Event;

import java.util.List;

/**
 * 事件地理信息增强服务接口
 * 专门用于为缺少经纬度信息的事件补充地理坐标
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface EventGeographicEnhancementService {
    
    /**
     * 为单个事件增强地理信息
     * 
     * @param event 事件对象
     * @return 增强后的事件对象
     */
    Event enhanceEventGeographicInfo(Event event);
    
    /**
     * 为事件数据增强地理信息
     * 
     * @param eventData 事件数据对象
     * @return 增强后的事件数据对象
     */
    EventData enhanceEventDataGeographicInfo(EventData eventData);
    
    /**
     * 批量为事件增强地理信息
     * 
     * @param events 事件列表
     * @return 增强后的事件列表
     */
    List<Event> enhanceEventsGeographicInfo(List<Event> events);
    
    /**
     * 批量为事件数据增强地理信息
     * 
     * @param eventDataList 事件数据列表
     * @return 增强后的事件数据列表
     */
    List<EventData> enhanceEventDataListGeographicInfo(List<EventData> eventDataList);
    
    /**
     * 检查事件是否需要地理信息增强
     * 
     * @param event 事件对象
     * @return 是否需要增强
     */
    boolean needsGeographicEnhancement(Event event);
    
    /**
     * 检查事件数据是否需要地理信息增强
     * 
     * @param eventData 事件数据对象
     * @return 是否需要增强
     */
    boolean needsGeographicEnhancement(EventData eventData);
    
    /**
     * 根据地点名称获取经纬度坐标
     * 
     * @param locationName 地点名称
     * @return 经纬度数组 [纬度, 经度]，如果无法获取则返回null
     */
    double[] getCoordinatesByLocation(String locationName);
    
    /**
     * 智能解析事件描述中的地理位置信息
     * 
     * @param description 事件描述
     * @return 解析出的地点名称，如果无法解析则返回null
     */
    String extractLocationFromDescription(String description);
    
    /**
     * 获取地理信息增强统计
     * 
     * @return 统计信息
     */
    String getEnhancementStatistics();
}