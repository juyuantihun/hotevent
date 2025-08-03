package com.hotech.events.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Neo4j事件节点实体
 * 用于在图数据库中存储事件信息
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Node("Event")
public class EventNode {
    
    @Id
    private String eventCode;
    
    @Property("title")
    private String title;
    
    @Property("description")
    private String description;
    
    @Property("eventTime")
    private LocalDateTime eventTime;
    
    @Property("eventLocation")
    private String eventLocation;
    
    @Property("eventType")
    private String eventType;
    
    @Property("subject")
    private String subject;
    
    @Property("object")
    private String object;
    
    @Property("sourceType")
    private Integer sourceType;
    
    @Property("intensityLevel")
    private Integer intensityLevel;
    
    @Property("keywords")
    private List<String> keywords;
    
    @Property("longitude")
    private Double longitude;
    
    @Property("latitude")
    private Double latitude;
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("updatedAt")
    private LocalDateTime updatedAt;
    
    // 关联到其他事件的关系
    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.OUTGOING)
    private List<EventRelationship> relatedEvents;
    
    // 被其他事件关联的关系
    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.INCOMING)
    private List<EventRelationship> relatedByEvents;

    /**
     * 初始化关系集合
     */
    public void initializeRelationships() {
        if (relatedEvents == null) {
            relatedEvents = new ArrayList<>();
        }
        if (relatedByEvents == null) {
            relatedByEvents = new ArrayList<>();
        }
    }
} 