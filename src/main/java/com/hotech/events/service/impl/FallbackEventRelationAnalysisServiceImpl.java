package com.hotech.events.service.impl;

import com.hotech.events.entity.EventNode;
import com.hotech.events.entity.EventRelationship;
import com.hotech.events.service.EventRelationAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件关系分析服务备用实现类
 * 当Neo4j不可用时使用此实现
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.neo4j.enabled", havingValue = "false", matchIfMissing = true)
public class FallbackEventRelationAnalysisServiceImpl implements EventRelationAnalysisService {

    @Override
    public EventRelationship analyzeEventRelation(EventNode event1, EventNode event2) {
        log.warn("Neo4j服务不可用，无法分析事件关系");
        return null;
    }

    @Override
    public List<EventRelationship> analyzeEventRelations(List<EventNode> events) {
        log.warn("Neo4j服务不可用，无法分析事件关系");
        return new ArrayList<>();
    }

    @Override
    public List<EventRelationship> analyzeEventRelationsForSingle(EventNode targetEvent, List<EventNode> allEvents) {
        log.warn("Neo4j服务不可用，无法分析单个事件关系");
        return new ArrayList<>();
    }

    @Override
    public List<EventNode> generateEventTimeline(String eventCode) {
        log.warn("Neo4j服务不可用，无法生成事件时间线");
        return new ArrayList<>();
    }

    @Override
    public List<EventNode> findEventCluster(String eventCode, int maxSize) {
        log.warn("Neo4j服务不可用，无法查找事件集群");
        return new ArrayList<>();
    }

    @Override
    public List<EventNode> findCausalChain(String eventCode) {
        log.warn("Neo4j服务不可用，无法查找因果关系链");
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getRelationStatistics() {
        log.warn("Neo4j服务不可用，返回空的关系统计");
        Map<String, Object> stats = new HashMap<>();
        stats.put("success", false);
        stats.put("message", "Neo4j服务不可用");
        return stats;
    }
}