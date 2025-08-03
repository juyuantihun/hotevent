package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.dto.GeographicCoordinate.LocationType;

import java.util.List;
import java.util.Optional;

/**
 * 地理信息处理服务接口
 * 负责处理事件的地理位置信息，提供坐标解析和标准化功能
 */
public interface GeographicInfoService {
    
    /**
     * 解析地理位置坐标
     * @param locationName 地点名称
     * @param locationType 地点类型（国家/地区/城市）
     * @return 地理坐标信息
     */
    Optional<GeographicCoordinate> parseLocationCoordinate(String locationName, LocationType locationType);
    
    /**
     * 批量处理事件地理信息
     * @param events 事件列表
     * @return 处理后的事件列表
     */
    List<EventData> enhanceEventsWithGeographicInfo(List<EventData> events);
    
    /**
     * 获取默认坐标（首都/首府）
     * @param locationName 地点名称
     * @param locationType 地点类型
     * @return 默认坐标
     */
    Optional<GeographicCoordinate> getDefaultCoordinate(String locationName, LocationType locationType);
    
    /**
     * 根据地点名称智能识别地点类型和坐标
     * @param locationName 地点名称
     * @return 地理坐标信息
     */
    Optional<GeographicCoordinate> smartParseLocation(String locationName);
    
    /**
     * 缓存地理坐标信息
     * @param locationName 地点名称
     * @param coordinate 坐标信息
     */
    void cacheCoordinate(String locationName, GeographicCoordinate coordinate);
    
    /**
     * 从缓存获取地理坐标
     * @param locationName 地点名称
     * @return 缓存的坐标信息
     */
    Optional<GeographicCoordinate> getCachedCoordinate(String locationName);
    
    /**
     * 清除地理坐标缓存
     */
    void clearCoordinateCache();
    
    /**
     * 获取国家首都坐标
     * @param countryName 国家名称
     * @return 首都坐标
     */
    Optional<GeographicCoordinate> getCapitalCoordinate(String countryName);
    
    /**
     * 获取地区首府坐标
     * @param regionName 地区名称
     * @param countryName 所属国家名称
     * @return 首府坐标
     */
    Optional<GeographicCoordinate> getRegionCapitalCoordinate(String regionName, String countryName);
    
    /**
     * 验证坐标有效性
     * @param coordinate 坐标信息
     * @return 是否有效
     */
    boolean validateCoordinate(GeographicCoordinate coordinate);
    
    /**
     * 计算两个坐标之间的距离（公里）
     * @param coord1 坐标1
     * @param coord2 坐标2
     * @return 距离（公里）
     */
    double calculateDistance(GeographicCoordinate coord1, GeographicCoordinate coord2);
    
    /**
     * 初始化默认地理数据
     */
    void initializeDefaultGeographicData();
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    String getCacheStatistics();
}