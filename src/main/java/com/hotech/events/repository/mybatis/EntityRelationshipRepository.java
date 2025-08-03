package com.hotech.events.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.EntityRelationship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface EntityRelationshipRepository extends BaseMapper<EntityRelationship> {

    /**
     * 根据源实体查询关系
     */
    @Select("SELECT * FROM entity_relationship WHERE source_entity_type = #{entityType} AND source_entity_id = #{entityId} AND status = 1")
    List<EntityRelationship> findBySourceEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * 根据目标实体查询关系
     */
    @Select("SELECT * FROM entity_relationship WHERE target_entity_type = #{entityType} AND target_entity_id = #{entityId} AND status = 1")
    List<EntityRelationship> findByTargetEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * 查询实体的所有关系（包括作为源实体和目标实体）
     */
    @Select("SELECT * FROM entity_relationship WHERE ((source_entity_type = #{entityType} AND source_entity_id = #{entityId}) OR (target_entity_type = #{entityType} AND target_entity_id = #{entityId})) AND status = 1")
    List<EntityRelationship> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * 检查两个实体之间是否存在特定关系
     */
    @Select("SELECT COUNT(*) > 0 FROM entity_relationship WHERE source_entity_type = #{sourceType} AND source_entity_id = #{sourceId} AND target_entity_type = #{targetType} AND target_entity_id = #{targetId} AND relationship_type = #{relationshipType} AND status = 1")
    boolean existsRelationship(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId, 
                              @Param("targetType") String targetType, @Param("targetId") Long targetId, 
                              @Param("relationshipType") String relationshipType);

    /**
     * 根据关系类型查询
     */
    @Select("SELECT * FROM entity_relationship WHERE relationship_type = #{relationshipType} AND status = 1")
    List<EntityRelationship> findByRelationshipType(@Param("relationshipType") String relationshipType);

    /**
     * 查询实体关系图数据
     */
    @Select("SELECT r.*, " +
           "CASE r.source_entity_type " +
           "  WHEN 'country' THEN (SELECT name FROM country WHERE id = r.source_entity_id) " +
           "  WHEN 'organization' THEN (SELECT name FROM organization WHERE id = r.source_entity_id) " +
           "  WHEN 'person' THEN (SELECT name FROM person WHERE id = r.source_entity_id) " +
           "END as source_entity_name, " +
           "CASE r.target_entity_type " +
           "  WHEN 'country' THEN (SELECT name FROM country WHERE id = r.target_entity_id) " +
           "  WHEN 'organization' THEN (SELECT name FROM organization WHERE id = r.target_entity_id) " +
           "  WHEN 'person' THEN (SELECT name FROM person WHERE id = r.target_entity_id) " +
           "END as target_entity_name " +
           "FROM entity_relationship r WHERE r.status = 1")
    List<Map<String, Object>> findRelationshipGraphData();
} 