package com.hotech.events.service.impl;

import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.entity.EventNode;
import com.hotech.events.entity.EventRelationship;
import com.hotech.events.repository.neo4j.EventNodeRepository;
import com.hotech.events.service.EventRelationAnalysisService;
import com.hotech.events.service.EventReasoningClient;
import com.hotech.events.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "spring.neo4j.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class EventRelationAnalysisServiceImpl implements EventRelationAnalysisService {
    
    @Autowired
    private EventNodeRepository eventNodeRepository;
    
    @Autowired
    private EventReasoningClient eventReasoningClient;

    @Autowired
    private EventService eventService;
    
    @Override
    public List<EventRelationship> analyzeEventRelations(List<EventNode> events) {
        try {
            // 将事件节点转换为DTO
            List<EventDTO> eventDTOs = events.stream()
                .map(event -> {
                    EventDTO dto = new EventDTO();
                    dto.setEventCode(event.getEventCode());
                    dto.setEventDescription(event.getDescription());
                    dto.setEventTime(event.getEventTime());
                    dto.setEventLocation(event.getEventLocation());
                    dto.setEventType(event.getEventType());
                    dto.setSubject(event.getSubject());
                    dto.setObject(event.getObject());
                    return dto;
                })
                .collect(Collectors.toList());

            // 调用event项目的分析接口
            Map<String, Object> result = eventReasoningClient.analyzeEventRelations(eventDTOs);
            
            // 解析结果并转换为EventRelationship列表
            List<EventRelationship> relationships = new ArrayList<>();
            if (result != null && result.containsKey("relations")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> relations = (List<Map<String, Object>>) result.get("relations");
                
                for (Map<String, Object> relation : relations) {
                    try {
                        EventRelationship relationship = new EventRelationship();
                        relationship.setRelationshipId(UUID.randomUUID().toString());
                        relationship.setRelationType((String) relation.get("relationType"));
                        relationship.setRelationDescription((String) relation.get("relationDescription"));
                        relationship.setConfidence(getDoubleValue(relation, "confidence", 0.0));
                        relationship.setStrength(getIntegerValue(relation, "strength", 1));
                        relationship.setDirection((String) relation.get("direction"));
                        relationship.setKeywords((String) relation.get("keywords"));
                        relationship.setAiAnalysis("Event项目分析：" + relation.get("relationDescription"));
                        relationship.setCreatedAt(LocalDateTime.now());
                        relationship.setUpdatedAt(LocalDateTime.now());
                        relationship.setCreatedBy("Event-Analysis-Service");
                        relationships.add(relationship);
                    } catch (Exception e) {
                        log.warn("解析关系数据失败: {}", relation, e);
                    }
                }
            }
            
            log.info("批量分析完成，发现{}个关联关系", relationships.size());
            return relationships;
        } catch (Exception e) {
            log.error("批量事件关联分析失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EventRelationship> analyzeEventRelationsForSingle(EventNode targetEvent, List<EventNode> allEvents) {
        List<EventNode> events = new ArrayList<>(allEvents);
        if (!events.contains(targetEvent)) {
            events.add(targetEvent);
        }
        return analyzeEventRelations(events);
    }

    @Override
    public EventRelationship analyzeEventRelation(EventNode event1, EventNode event2) {
        List<EventRelationship> relationships = analyzeEventRelations(Arrays.asList(event1, event2));
        return relationships.isEmpty() ? null : relationships.get(0);
    }
    
    @Override
    public List<EventNode> generateEventTimeline(String eventCode) {
        try {
            return eventNodeRepository.findEventTimeline(eventCode);
        } catch (Exception e) {
            log.error("生成事件时间线失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EventNode> findEventCluster(String eventCode, int maxSize) {
        try {
            return eventNodeRepository.findEventCluster(eventCode, 0.5, maxSize);
        } catch (Exception e) {
            log.error("查找事件集群失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EventNode> findCausalChain(String eventCode) {
        try {
            return eventNodeRepository.findCausalChain(eventCode);
        } catch (Exception e) {
            log.error("查找因果关系链失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, Object> getRelationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalEvents", eventNodeRepository.count());
            // 使用已有的统计方法
            List<EventNodeRepository.EventStatistics> eventStats = eventNodeRepository.getEventStatistics();
            int totalRelations = eventStats.stream()
                .mapToInt(stat -> stat.getRelationCount().intValue())
                .sum();
            stats.put("totalRelations", totalRelations);
            return stats;
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            stats.put("error", e.getMessage());
            return stats;
        }
    }
    
    /**
     * 从Map中获取Double值的辅助方法
     */
    private Double getDoubleValue(Map<String, Object> map, String key, Double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析数字: {}", value);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 从Map中获取Integer值的辅助方法
     */
    private Integer getIntegerValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析数字: {}", value);
                return defaultValue;
            }
        }
        return defaultValue;
    }
} 