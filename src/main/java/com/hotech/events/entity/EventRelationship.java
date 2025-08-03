package com.hotech.events.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

/**
 * Neo4j事件关系实体
 * 用于表示事件之间的关联关系
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RelationshipProperties
public class EventRelationship {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("relationshipId")
    private String relationshipId;
    
    @Property("relationType")
    private String relationType; // 关系类型：因果关系、时间关系、主题关系等
    
    @Property("relationDescription")
    private String relationDescription; // 关系描述
    
    @Property("confidence")
    private Double confidence; // 关系置信度 (0-1)
    
    @Property("strength")
    private Integer strength; // 关系强度 (1-10)
    
    @Property("direction")
    private String direction; // 关系方向：forward, backward, bidirectional
    
    @Property("keywords")
    private String keywords; // 关系关键词
    
    @Property("aiAnalysis")
    private String aiAnalysis; // AI分析结果
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("updatedAt")
    private LocalDateTime updatedAt;
    
    @Property("createdBy")
    private String createdBy; // 创建者（AI或人工）
    
    @TargetNode
    private EventNode targetEvent;
} 