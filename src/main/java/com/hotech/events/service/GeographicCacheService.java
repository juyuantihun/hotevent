package com.hotech.events.service;

import com.hotech.events.dto.GeographicCoordinate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 地理信息缓存服务接口
 * 用于提高地理坐标查询的响应速度
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface GeographicCacheService {
    
    /**
     * 获取地理坐标（优先从缓存获取）
     * 
     * @param locationName 地点名称
     * @param locationType 地点类型
     * @return 地理坐标，如果不存在则返回Optional.empty()
     */
    Optional<GeographicCoordinate> getCoordinate(String locationName, String locationType);
    
    /**
     * 批量获取地理坐标
     * 
     * @param locationNames 地点名称列表
     * @param locationType 地点类型
     * @return 地点名称到坐标的映射
     */
    Map<String, GeographicCoordinate> getCoordinatesBatch(List<String> locationNames, String locationType);
    
    /**
     * 缓存地理坐标
     * 
     * @param locationName 地点名称
     * @param locationType 地点类型
     * @param coordinate 地理坐标
     */
    void cacheCoordinate(String locationName, String locationType, GeographicCoordinate coordinate);
    
    /**
     * 批量缓存地理坐标
     * 
     * @param coordinates 地点名称到坐标的映射
     * @param locationType 地点类型
     */
    void cacheCoordinatesBatch(Map<String, GeographicCoordinate> coordinates, String locationType);
    
    /**
     * 预加载常用地理坐标
     */
    void preloadCommonCoordinates();
    
    /**
     * 清理过期缓存
     */
    void cleanupExpiredCache();
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    Map<String, Object> getCacheStatistics();
    
    /**
     * 清空所有缓存
     */
    void clearAllCache();
    
    /**
     * 设置缓存TTL（生存时间）
     * 
     * @param ttlSeconds TTL秒数
     */
    void setCacheTTL(long ttlSeconds);
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存中的条目数量
     */
    int getCacheSize();
    
    /**
     * 检查缓存中是否存在指定地点
     * 
     * @param locationName 地点名称
     * @param locationType 地点类型
     * @return 是否存在
     */
    boolean containsCoordinate(String locationName, String locationType);
    
    /**
     * 从缓存中移除指定地点
     * 
     * @param locationName 地点名称
     * @param locationType 地点类型
     * @return 是否成功移除
     */
    boolean removeCoordinate(String locationName, String locationType);
    
    /**
     * 获取缓存命中率
     * 
     * @return 命中率（0.0-1.0）
     */
    double getCacheHitRate();
    
    /**
     * 重置缓存统计信息
     */
    void resetCacheStatistics();
}