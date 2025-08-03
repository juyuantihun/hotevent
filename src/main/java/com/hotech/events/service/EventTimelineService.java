package com.hotech.events.service;

import com.hotech.events.entity.EventNode;
import com.hotech.events.entity.EventRelationship;

import java.util.List;
import java.util.Map;

/**
 * 事件时间线服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface EventTimelineService {
    
    /**
     * 同步MySQL事件到Neo4j
     * 
     * @param eventCodes 事件编码列表，如果为空则同步所有事件
     * @return 同步结果
     */
    Map<String, Object> syncEventsToNeo4j(List<String> eventCodes);
    
    /**
     * 分析并建立事件关联关系
     * 
     * @param eventCodes 事件编码列表
     * @return 分析结果
     */
    Map<String, Object> analyzeAndCreateRelations(List<String> eventCodes);
    
    /**
     * 获取事件时间线
     * 
     * @param eventCode 起始事件编码
     * @return 时间线数据
     */
    Map<String, Object> getEventTimeline(String eventCode);
    
    /**
     * 获取事件关联图数据
     * 
     * @param eventCode 中心事件编码
     * @param depth 关联深度
     * @return 图数据
     */
    Map<String, Object> getEventGraph(String eventCode, int depth);
    
    /**
     * 获取事件集群
     * 
     * @param eventCode 中心事件编码
     * @param maxSize 最大集群大小
     * @return 集群数据
     */
    Map<String, Object> getEventCluster(String eventCode, int maxSize);
    
    /**
     * 获取因果关系链
     * 
     * @param eventCode 起始事件编码
     * @return 因果链数据
     */
    Map<String, Object> getCausalChain(String eventCode);
    
    /**
     * 搜索相关事件
     * 
     * @param keyword 关键词
     * @param limit 结果数量限制
     * @return 搜索结果
     */
    Map<String, Object> searchRelatedEvents(String keyword, int limit);
    
    /**
     * 获取热点事件
     * 
     * @param limit 结果数量限制
     * @return 热点事件列表
     */
    List<EventNode> getHotEvents(int limit);
    
    /**
     * 获取事件关联统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getRelationStatistics();
    
    /**
     * 删除事件及其关联关系
     * 
     * @param eventCode 事件编码
     * @return 删除结果
     */
    Boolean deleteEventAndRelations(String eventCode);
} 