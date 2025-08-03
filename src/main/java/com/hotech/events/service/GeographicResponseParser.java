package com.hotech.events.service;

import com.hotech.events.dto.EnhancedEventData;
import com.hotech.events.dto.GeographicCoordinate;

import java.util.List;
import java.util.Map;

/**
 * 地理信息响应解析器接口
 * 负责解析API响应中的地理坐标信息
 */
public interface GeographicResponseParser {
    
    /**
     * 解析API响应中的事件地理信息
     * 
     * @param apiResponse API响应内容
     * @return 解析后的增强事件数据列表
     */
    List<EnhancedEventData> parseEventsWithGeographicInfo(String apiResponse);
    
    /**
     * 解析单个事件的地理坐标信息
     * 
     * @param eventMap 事件数据Map
     * @return 解析后的增强事件数据
     */
    EnhancedEventData parseEventGeographicInfo(Map<String, Object> eventMap);
    
    /**
     * 解析地理坐标对象
     * 
     * @param coordinateMap 坐标数据Map
     * @return 地理坐标对象
     */
    GeographicCoordinate parseGeographicCoordinate(Map<String, Object> coordinateMap);
    
    /**
     * 验证并标准化地理坐标
     * 
     * @param coordinate 原始坐标
     * @return 标准化后的坐标
     */
    GeographicCoordinate validateAndNormalizeCoordinate(GeographicCoordinate coordinate);
    
    /**
     * 从事件描述中提取地理信息
     * 
     * @param eventData 事件数据
     * @return 是否成功提取地理信息
     */
    boolean extractGeographicInfoFromDescription(EnhancedEventData eventData);
    
    /**
     * 获取解析统计信息
     * 
     * @return 解析统计
     */
    Map<String, Object> getParsingStatistics();
}