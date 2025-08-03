package com.hotech.events.repository.neo4j;

import com.hotech.events.entity.EventNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Neo4j事件节点Repository
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "spring.neo4j.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public interface EventNodeRepository extends Neo4jRepository<EventNode, String> {
    
    @Query("MATCH (e:Event) RETURN e ORDER BY e.eventTime DESC")
    List<EventNode> findAllEvents();

    /**
     * 根据事件编码查找事件
     */
    Optional<EventNode> findByEventCode(String eventCode);
    
    /**
     * 根据事件类型查找事件
     */
    List<EventNode> findByEventType(String eventType);
    
    /**
     * 根据时间范围查找事件
     */
    List<EventNode> findByEventTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找与指定事件相关的所有事件
     */
    @Query("MATCH (e:Event {eventCode: $eventCode})-[r:RELATES_TO]-(related:Event) " +
           "RETURN related, r")
    List<EventNode> findRelatedEvents(@Param("eventCode") String eventCode);
    
    /**
     * 查找事件的直接关联事件（一度关联）
     */
    @Query("MATCH (e:Event {eventCode: $eventCode})-[r:RELATES_TO]->(related:Event) " +
           "RETURN related, r " +
           "ORDER BY r.confidence DESC, r.strength DESC")
    List<EventNode> findDirectRelatedEvents(@Param("eventCode") String eventCode);
    
    /**
     * 查找事件的间接关联事件（二度关联）
     */
    @Query("MATCH (e:Event {eventCode: $eventCode})-[r1:RELATES_TO]->(intermediate:Event)-[r2:RELATES_TO]->(related:Event) " +
           "WHERE related.eventCode <> $eventCode " +
           "RETURN related, r1, r2, intermediate " +
           "ORDER BY r1.confidence * r2.confidence DESC")
    List<EventNode> findIndirectRelatedEvents(@Param("eventCode") String eventCode);
    
    /**
     * 查找事件时间线（按时间顺序的相关事件）
     */
    @Query("MATCH (e:Event {eventCode: $eventCode})-[r:RELATES_TO*1..3]-(related:Event) " +
           "WHERE r.relationType IN ['temporal', 'causal'] " +
           "RETURN DISTINCT related " +
           "ORDER BY related.eventTime ASC")
    List<EventNode> findEventTimeline(@Param("eventCode") String eventCode);
    
    /**
     * 查找事件集群（相似主题的事件）
     */
    @Query("MATCH (e:Event {eventCode: $eventCode})-[r:RELATES_TO]-(related:Event) " +
           "WHERE r.relationType = 'thematic' AND r.confidence > $minConfidence " +
           "RETURN related " +
           "ORDER BY r.confidence DESC, r.strength DESC " +
           "LIMIT $limit")
    List<EventNode> findEventCluster(@Param("eventCode") String eventCode, 
                                   @Param("minConfidence") Double minConfidence,
                                   @Param("limit") Integer limit);
    
    /**
     * 查找因果关系链
     */
    @Query("MATCH path = (start:Event {eventCode: $eventCode})-[r:RELATES_TO*1..5]->(end:Event) " +
           "WHERE ALL(rel in r WHERE rel.relationType = 'causal') " +
           "RETURN nodes(path) as events, relationships(path) as relations " +
           "ORDER BY length(path) ASC")
    List<EventNode> findCausalChain(@Param("eventCode") String eventCode);
    
    /**
     * 查找热点事件（关联度最高的事件）
     */
    @Query("MATCH (e:Event)-[r:RELATES_TO]-(related:Event) " +
           "RETURN e, COUNT(r) as relationCount " +
           "ORDER BY relationCount DESC " +
           "LIMIT $limit")
    List<EventNode> findHotEvents(@Param("limit") Integer limit);
    
    /**
     * 根据关键词搜索相关事件
     */
    @Query("MATCH (e:Event) " +
           "WHERE ANY(keyword IN e.keywords WHERE keyword CONTAINS $keyword) " +
           "   OR e.description CONTAINS $keyword " +
           "   OR e.title CONTAINS $keyword " +
           "RETURN e " +
           "ORDER BY e.eventTime DESC")
    List<EventNode> findEventsByKeyword(@Param("keyword") String keyword);
    
    /**
     * 获取事件统计信息
     */
    @Query("MATCH (e:Event) " +
           "OPTIONAL MATCH (e)-[r:RELATES_TO]-() " +
           "WITH e.eventType as eventType, COUNT(DISTINCT e) as eventCount, COUNT(r) as relationCount " +
           "RETURN eventType, eventCount, relationCount")
    List<EventStatistics> getEventStatistics();

    /**
     * 事件统计信息记录类
     */
    interface EventStatistics {
        String getEventType();
        Long getEventCount();
        Long getRelationCount();
    }
} 