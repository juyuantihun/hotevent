package com.hotech.events.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.EntityRelationshipDTO;
import com.hotech.events.entity.EntityRelationship;

import java.util.List;
import java.util.Map;

/**
 * 实体关系服务接口
 */
public interface EntityRelationshipService {

    /**
     * 创建实体关系
     */
    EntityRelationshipDTO createRelationship(EntityRelationshipDTO dto);

    /**
     * 更新实体关系
     */
    EntityRelationshipDTO updateRelationship(EntityRelationshipDTO dto);

    /**
     * 删除实体关系
     */
    Boolean deleteRelationship(Long id);

    /**
     * 根据ID获取实体关系
     */
    EntityRelationshipDTO getRelationship(Long id);

    /**
     * 分页查询实体关系
     */
    Page<EntityRelationshipDTO> getRelationshipPage(int current, int size, String relationshipType, String sourceEntityType, String targetEntityType);

    /**
     * 根据实体查询所有关系
     */
    List<EntityRelationshipDTO> getRelationshipsByEntity(String entityType, Long entityId);

    /**
     * 根据源实体查询关系
     */
    List<EntityRelationshipDTO> getRelationshipsBySourceEntity(String entityType, Long entityId);

    /**
     * 根据目标实体查询关系
     */
    List<EntityRelationshipDTO> getRelationshipsByTargetEntity(String entityType, Long entityId);

    /**
     * 检查两个实体之间是否存在特定关系
     */
    Boolean existsRelationship(String sourceType, Long sourceId, String targetType, Long targetId, String relationshipType);

    /**
     * 获取实体关系图数据
     */
    List<Map<String, Object>> getRelationshipGraphData();

    /**
     * 根据关系类型查询
     */
    List<EntityRelationshipDTO> getRelationshipsByType(String relationshipType);
} 