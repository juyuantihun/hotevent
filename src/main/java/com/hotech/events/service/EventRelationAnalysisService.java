package com.hotech.events.service;

import com.hotech.events.entity.EventNode;
import com.hotech.events.entity.EventRelationship;

import java.util.List;
import java.util.Map;

/**
 * 事件关联分析服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface EventRelationAnalysisService {
    
    /**
     * 分析两个事件之间的关联关系
     * 
     * @param event1 事件1
     * @param event2 事件2
     * @return 关联关系，如果没有关联则返回null
     */
    EventRelationship analyzeEventRelation(EventNode event1, EventNode event2);
    
    /**
     * 批量分析事件之间的关联关系
     * 
     * @param events 事件列表
     * @return 关联关系列表
     */
    List<EventRelationship> analyzeEventRelations(List<EventNode> events);
    
    /**
     * 分析单个事件与其他所有事件的关联关系
     * 
     * @param targetEvent 目标事件
     * @param allEvents 所有事件列表
     * @return 关联关系列表
     */
    List<EventRelationship> analyzeEventRelationsForSingle(EventNode targetEvent, List<EventNode> allEvents);
    
    /**
     * 生成事件时间线
     * 
     * @param eventCode 起始事件编码
     * @return 时间线事件列表
     */
    List<EventNode> generateEventTimeline(String eventCode);
    
    /**
     * 查找事件集群
     * 
     * @param eventCode 中心事件编码
     * @param maxSize 最大集群大小
     * @return 事件集群
     */
    List<EventNode> findEventCluster(String eventCode, int maxSize);
    
    /**
     * 查找因果关系链
     * 
     * @param eventCode 起始事件编码
     * @return 因果关系链
     */
    List<EventNode> findCausalChain(String eventCode);
    
    /**
     * 获取事件关联统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getRelationStatistics();
} 