package com.hotech.events.service.impl;

import com.hotech.events.dto.EventGraphDTO;
import com.hotech.events.entity.Event;
import com.hotech.events.entity.EventNode;
import com.hotech.events.entity.EventRelationship;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.repository.neo4j.EventNodeRepository;
import com.hotech.events.service.EventRelationAnalysisService;
import com.hotech.events.service.EventTimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件时间线服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.neo4j.enabled", havingValue = "true", matchIfMissing = false)
public class EventTimelineServiceImpl implements EventTimelineService {
    
    @Autowired
    private EventMapper eventMapper;
    
    @Autowired(required = false)
    private EventNodeRepository eventNodeRepository;
    
    @Autowired(required = false)
    private EventRelationAnalysisService relationAnalysisService;
    
    @Override
    @Transactional
    public Map<String, Object> syncEventsToNeo4j(List<String> eventCodes) {
        log.info("开始同步MySQL事件到Neo4j，事件编码：{}", eventCodes);
        
        Map<String, Object> result = new HashMap<>();
        
        if (eventNodeRepository == null) {
            result.put("success", false);
            result.put("message", "Neo4j服务不可用");
            return result;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        try {
            List<Event> events;
            if (eventCodes == null || eventCodes.isEmpty()) {
                // 同步所有事件
                events = eventMapper.selectList(null);
            } else {
                // 同步指定事件
                events = eventMapper.selectBatchIds(eventCodes.stream()
                    .map(code -> eventMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Event>()
                            .eq("event_code", code)
                    ))
                    .flatMap(List::stream)
                    .map(Event::getId)
                    .collect(Collectors.toList()));
            }
            
            for (Event event : events) {
                try {
                    EventNode eventNode = convertToEventNode(event);
                    eventNodeRepository.save(eventNode);
                    successCount++;
                } catch (Exception e) {
                    log.error("同步事件失败：{}", event.getEventCode(), e);
                    failCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "同步完成");
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", events.size());
            
        } catch (Exception e) {
            log.error("同步事件到Neo4j失败", e);
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> analyzeAndCreateRelations(List<String> eventCodes) {
        log.info("开始分析并建立事件关联关系，事件编码：{}", eventCodes);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EventNode> events = new ArrayList<>();
            
            if (eventCodes == null || eventCodes.isEmpty()) {
                try {
                    events = eventNodeRepository.findAllEvents();
                    if (events.isEmpty()) {
                        result.put("success", true);
                        result.put("message", "数据库中没有事件节点");
                        return result;
                    }
                } catch (Exception e) {
                    log.error("获取所有事件节点失败", e);
                    result.put("success", false);
                    result.put("message", "获取事件节点失败：" + e.getMessage());
                    return result;
                }
            } else {
                for (String eventCode : eventCodes) {
                    try {
                        Optional<EventNode> eventNode = eventNodeRepository.findByEventCode(eventCode);
                        eventNode.ifPresent(events::add);
                    } catch (Exception e) {
                        log.error("获取事件节点失败：{}", eventCode, e);
                    }
                }
                
                if (events.isEmpty()) {
                    result.put("success", true);
                    result.put("message", "未找到指定的事件节点");
                    return result;
                }
            }
            
            // 分析关联关系
            List<EventRelationship> relationships = relationAnalysisService.analyzeEventRelations(events);
            
            // 保存关联关系到Neo4j
            int savedCount = 0;
            for (EventRelationship relationship : relationships) {
                try {
                    String sourceEventCode = findSourceEventCode(relationship, events);
                    if (sourceEventCode != null) {
                        EventNode sourceEvent = eventNodeRepository.findByEventCode(sourceEventCode).orElse(null);
                        if (sourceEvent != null) {
                            sourceEvent.initializeRelationships();
                            sourceEvent.getRelatedEvents().add(relationship);
                            eventNodeRepository.save(sourceEvent);
                            savedCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("保存关联关系失败", e);
                }
            }
            
            result.put("success", true);
            result.put("message", "关联分析完成");
            result.put("relationCount", relationships.size());
            result.put("savedCount", savedCount);
            result.put("eventCount", events.size());
            
        } catch (Exception e) {
            log.error("分析事件关联失败", e);
            result.put("success", false);
            result.put("message", "分析失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getEventTimeline(String eventCode) {
        log.info("获取事件时间线：{}", eventCode);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EventNode> timelineEvents = relationAnalysisService.generateEventTimeline(eventCode);
            EventGraphDTO graphData = convertToTimelineGraph(timelineEvents, eventCode);
            
            result.put("success", true);
            result.put("data", graphData);
            result.put("eventCount", timelineEvents.size());
            
        } catch (Exception e) {
            log.error("获取事件时间线失败", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getEventGraph(String eventCode, int depth) {
        log.info("获取事件关联图：{}，深度：{}", eventCode, depth);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取中心事件
            EventNode centerEvent = eventNodeRepository.findByEventCode(eventCode).orElse(null);
            if (centerEvent == null) {
                result.put("success", false);
                result.put("message", "事件不存在");
                return result;
            }
            
            // 获取关联事件
            List<EventNode> relatedEvents = new ArrayList<>();
            relatedEvents.add(centerEvent);
            
            // 根据深度获取关联事件
            Set<String> processedEvents = new HashSet<>();
            Queue<String> eventQueue = new LinkedList<>();
            eventQueue.offer(eventCode);
            processedEvents.add(eventCode);
            
            for (int currentDepth = 0; currentDepth < depth && !eventQueue.isEmpty(); currentDepth++) {
                int queueSize = eventQueue.size();
                for (int i = 0; i < queueSize; i++) {
                    String currentEventCode = eventQueue.poll();
                    List<EventNode> directRelated = eventNodeRepository.findDirectRelatedEvents(currentEventCode);
                    
                    for (EventNode related : directRelated) {
                        if (!processedEvents.contains(related.getEventCode())) {
                            relatedEvents.add(related);
                            eventQueue.offer(related.getEventCode());
                            processedEvents.add(related.getEventCode());
                        }
                    }
                }
            }
            
            EventGraphDTO graphData = convertToGraph(relatedEvents, eventCode, "general");
            
            result.put("success", true);
            result.put("data", graphData);
            result.put("eventCount", relatedEvents.size());
            
        } catch (Exception e) {
            log.error("获取事件关联图失败", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getEventCluster(String eventCode, int maxSize) {
        log.info("获取事件集群：{}，最大大小：{}", eventCode, maxSize);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EventNode> clusterEvents = relationAnalysisService.findEventCluster(eventCode, maxSize);
            EventGraphDTO graphData = convertToGraph(clusterEvents, eventCode, "cluster");
            
            result.put("success", true);
            result.put("data", graphData);
            result.put("eventCount", clusterEvents.size());
            
        } catch (Exception e) {
            log.error("获取事件集群失败", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getCausalChain(String eventCode) {
        log.info("获取因果关系链：{}", eventCode);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EventNode> causalChain = relationAnalysisService.findCausalChain(eventCode);
            EventGraphDTO graphData = convertToGraph(causalChain, eventCode, "causal");
            
            result.put("success", true);
            result.put("data", graphData);
            result.put("eventCount", causalChain.size());
            
        } catch (Exception e) {
            log.error("获取因果关系链失败", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> searchRelatedEvents(String keyword, int limit) {
        log.info("搜索相关事件：{}，限制：{}", keyword, limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EventNode> events = eventNodeRepository.findEventsByKeyword(keyword);
            if (events.size() > limit) {
                events = events.subList(0, limit);
            }
            
            EventGraphDTO graphData = convertToGraph(events, null, "search");
            
            result.put("success", true);
            result.put("data", graphData);
            result.put("eventCount", events.size());
            result.put("keyword", keyword);
            
        } catch (Exception e) {
            log.error("搜索相关事件失败", e);
            result.put("success", false);
            result.put("message", "搜索失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<EventNode> getHotEvents(int limit) {
        log.info("获取热点事件，限制：{}", limit);
        
        try {
            return eventNodeRepository.findHotEvents(limit);
        } catch (Exception e) {
            log.error("获取热点事件失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getRelationStatistics() {
        log.info("获取事件关联统计信息");
        
        return relationAnalysisService.getRelationStatistics();
    }
    
    @Override
    @Transactional
    public Boolean deleteEventAndRelations(String eventCode) {
        log.info("删除事件及其关联关系：{}", eventCode);
        
        try {
            eventNodeRepository.deleteById(eventCode);
            return true;
        } catch (Exception e) {
            log.error("删除事件失败", e);
            return false;
        }
    }
    
    /**
     * 转换MySQL事件为Neo4j事件节点
     */
    private EventNode convertToEventNode(Event event) {
        EventNode eventNode = new EventNode();
        eventNode.setEventCode(event.getEventCode());
        eventNode.setTitle(event.getEventDescription() != null && event.getEventDescription().length() > 50 
            ? event.getEventDescription().substring(0, 50) + "..." 
            : event.getEventDescription());
        eventNode.setDescription(event.getEventDescription());
        eventNode.setEventTime(event.getEventTime());
        eventNode.setEventLocation(event.getEventLocation());
        eventNode.setEventType(event.getEventType());
        eventNode.setSubject(event.getSubject());
        eventNode.setObject(event.getObject());
        eventNode.setSourceType(event.getSourceType());
        eventNode.setIntensityLevel(event.getIntensityLevel());
        eventNode.setLongitude(event.getLongitude() != null ? event.getLongitude().doubleValue() : null);
        eventNode.setLatitude(event.getLatitude() != null ? event.getLatitude().doubleValue() : null);
        eventNode.setCreatedAt(event.getCreatedAt());
        eventNode.setUpdatedAt(event.getUpdatedAt());
        
        // 设置关键词（这里需要从关键词表获取）
        eventNode.setKeywords(new ArrayList<>());
        
        return eventNode;
    }
    
    /**
     * 转换为时间线图数据
     */
    private EventGraphDTO convertToTimelineGraph(List<EventNode> events, String centerEventCode) {
        EventGraphDTO graphData = new EventGraphDTO();
        graphData.setGraphType("timeline");
        graphData.setCenterNode(centerEventCode);
        
        List<EventGraphDTO.Node> nodes = new ArrayList<>();
        List<EventGraphDTO.Edge> edges = new ArrayList<>();
        
        // 按时间排序
        events.sort(Comparator.comparing(EventNode::getEventTime));
        
        // 创建节点
        for (int i = 0; i < events.size(); i++) {
            EventNode event = events.get(i);
            EventGraphDTO.Node node = new EventGraphDTO.Node();
            node.setId(event.getEventCode());
            node.setLabel(event.getTitle());
            node.setType(event.getEventType());
            node.setDescription(event.getDescription());
            node.setTime(event.getEventTime());
            node.setLocation(event.getEventLocation());
            node.setSubject(event.getSubject());
            node.setObject(event.getObject());
            node.setKeywords(event.getKeywords());
            node.setSize(event.getEventCode().equals(centerEventCode) ? 20 : 15);
            node.setColor(getNodeColor(event.getEventType()));
            node.setShape("circle");
            node.setX((double) i * 100);
            node.setY(0.0);
            nodes.add(node);
            
            // 创建时间线边
            if (i > 0) {
                EventGraphDTO.Edge edge = new EventGraphDTO.Edge();
                edge.setId("timeline_" + i);
                edge.setSource(events.get(i - 1).getEventCode());
                edge.setTarget(event.getEventCode());
                edge.setLabel("时间顺序");
                edge.setType("temporal");
                edge.setColor("#999999");
                edge.setWidth(2);
                edge.setStyle("solid");
                edges.add(edge);
            }
        }
        
        graphData.setNodes(nodes);
        graphData.setEdges(edges);
        graphData.setTotalNodes(nodes.size());
        graphData.setTotalEdges(edges.size());
        
        return graphData;
    }
    
    /**
     * 转换为通用图数据
     */
    private EventGraphDTO convertToGraph(List<EventNode> events, String centerEventCode, String graphType) {
        EventGraphDTO graphData = new EventGraphDTO();
        graphData.setGraphType(graphType);
        graphData.setCenterNode(centerEventCode);
        
        List<EventGraphDTO.Node> nodes = new ArrayList<>();
        List<EventGraphDTO.Edge> edges = new ArrayList<>();
        
        // 创建节点
        for (EventNode event : events) {
            EventGraphDTO.Node node = new EventGraphDTO.Node();
            node.setId(event.getEventCode());
            node.setLabel(event.getTitle());
            node.setType(event.getEventType());
            node.setDescription(event.getDescription());
            node.setTime(event.getEventTime());
            node.setLocation(event.getEventLocation());
            node.setSubject(event.getSubject());
            node.setObject(event.getObject());
            node.setKeywords(event.getKeywords());
            node.setSize(event.getEventCode().equals(centerEventCode) ? 20 : 15);
            node.setColor(getNodeColor(event.getEventType()));
            node.setShape("circle");
            nodes.add(node);
        }
        
        // 创建边（基于事件的关联关系）
        for (EventNode event : events) {
            if (event.getRelatedEvents() != null) {
                for (EventRelationship relationship : event.getRelatedEvents()) {
                    EventGraphDTO.Edge edge = new EventGraphDTO.Edge();
                    edge.setId(relationship.getRelationshipId());
                    edge.setSource(event.getEventCode());
                    edge.setTarget(relationship.getTargetEvent().getEventCode());
                    edge.setLabel(relationship.getRelationType());
                    edge.setType(relationship.getRelationType());
                    edge.setDescription(relationship.getRelationDescription());
                    edge.setConfidence(relationship.getConfidence());
                    edge.setStrength(relationship.getStrength());
                    edge.setDirection(relationship.getDirection());
                    edge.setColor(getEdgeColor(relationship.getRelationType()));
                    edge.setWidth(Math.max(1, relationship.getStrength() / 2));
                    edge.setStyle("solid");
                    edges.add(edge);
                }
            }
        }
        
        graphData.setNodes(nodes);
        graphData.setEdges(edges);
        graphData.setTotalNodes(nodes.size());
        graphData.setTotalEdges(edges.size());
        
        return graphData;
    }
    
    /**
     * 获取节点颜色
     */
    private String getNodeColor(String eventType) {
        if (eventType == null) return "#666666";
        
        switch (eventType) {
            case "军事冲突": return "#FF4444";
            case "外交会谈": return "#4444FF";
            case "经济制裁": return "#FF8844";
            case "导弹试验": return "#FF44FF";
            case "政治事件": return "#44FF44";
            default: return "#666666";
        }
    }
    
    /**
     * 获取边颜色
     */
    private String getEdgeColor(String relationType) {
        if (relationType == null) return "#999999";
        
        switch (relationType) {
            case "causal": return "#FF6666";
            case "temporal": return "#6666FF";
            case "thematic": return "#66FF66";
            case "geographic": return "#FFFF66";
            case "actor": return "#FF66FF";
            default: return "#999999";
        }
    }
    
    /**
     * 查找关系的源事件编码
     */
    private String findSourceEventCode(EventRelationship relationship, List<EventNode> events) {
        // 这里需要根据具体的关系查找源事件
        // 简化处理，返回第一个事件的编码
        return events.isEmpty() ? null : events.get(0).getEventCode();
    }
} 