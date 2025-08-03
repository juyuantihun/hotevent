package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.EntityRelationshipDTO;
import com.hotech.events.entity.EntityRelationship;
import com.hotech.events.repository.mybatis.EntityRelationshipRepository;
import com.hotech.events.service.EntityRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EntityRelationshipServiceImpl implements EntityRelationshipService {

    @Autowired(required = false)
    private EntityRelationshipRepository repository;

    @Override
    @Transactional
    public EntityRelationshipDTO createRelationship(EntityRelationshipDTO dto) {
        log.info("创建实体关系：{}", dto);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，跳过实体关系创建");
            throw new RuntimeException("实体关系服务暂不可用");
        }
        
        // 检查关系是否已存在
        if (existsRelationship(dto.getSourceEntityType(), dto.getSourceEntityId(), 
                              dto.getTargetEntityType(), dto.getTargetEntityId(), 
                              dto.getRelationshipType())) {
            throw new RuntimeException("该关系已存在");
        }
        
        EntityRelationship entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setStatus(1);
        
        repository.insert(entity);
        
        log.info("实体关系创建成功，ID：{}", entity.getId());
        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public EntityRelationshipDTO updateRelationship(EntityRelationshipDTO dto) {
        log.info("更新实体关系：{}", dto);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，跳过实体关系更新");
            throw new RuntimeException("实体关系服务暂不可用");
        }
        
        EntityRelationship existing = repository.selectById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("实体关系不存在");
        }
        
        EntityRelationship entity = convertToEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        
        repository.updateById(entity);
        
        log.info("实体关系更新成功，ID：{}", entity.getId());
        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public Boolean deleteRelationship(Long id) {
        log.info("删除实体关系，ID：{}", id);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，跳过实体关系删除");
            return false;
        }
        
        EntityRelationship existing = repository.selectById(id);
        if (existing == null) {
            throw new RuntimeException("实体关系不存在");
        }
        
        // 软删除
        existing.setStatus(0);
        existing.setUpdatedAt(LocalDateTime.now());
        repository.updateById(existing);
        
        log.info("实体关系删除成功，ID：{}", id);
        return true;
    }

    @Override
    public EntityRelationshipDTO getRelationship(Long id) {
        log.info("获取实体关系，ID：{}", id);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，跳过实体关系查询");
            throw new RuntimeException("实体关系服务暂不可用");
        }
        
        EntityRelationship entity = repository.selectById(id);
        if (entity == null) {
            throw new RuntimeException("实体关系不存在");
        }
        
        return convertToDTO(entity);
    }

    @Override
    public Page<EntityRelationshipDTO> getRelationshipPage(int current, int size, String relationshipType, 
                                                          String sourceEntityType, String targetEntityType) {
        log.info("分页查询实体关系，current：{}，size：{}，relationshipType：{}，sourceEntityType：{}，targetEntityType：{}", 
                current, size, relationshipType, sourceEntityType, targetEntityType);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空分页结果");
            return new Page<>(current, size);
        }
        
        Page<EntityRelationship> page = new Page<>(current, size);
        QueryWrapper<EntityRelationship> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        
        if (relationshipType != null && !relationshipType.trim().isEmpty()) {
            wrapper.eq("relationship_type", relationshipType);
        }
        if (sourceEntityType != null && !sourceEntityType.trim().isEmpty()) {
            wrapper.eq("source_entity_type", sourceEntityType);
        }
        if (targetEntityType != null && !targetEntityType.trim().isEmpty()) {
            wrapper.eq("target_entity_type", targetEntityType);
        }
        
        wrapper.orderByDesc("created_at");
        
        Page<EntityRelationship> entityPage = repository.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<EntityRelationshipDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(entityPage, resultPage, "records");
        
        List<EntityRelationshipDTO> dtos = entityPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(dtos);
        
        log.info("分页查询实体关系完成，共{}条记录", resultPage.getTotal());
        return resultPage;
    }

    @Override
    public List<EntityRelationshipDTO> getRelationshipsByEntity(String entityType, Long entityId) {
        log.info("查询实体的所有关系，entityType：{}，entityId：{}", entityType, entityId);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空列表");
            return List.of();
        }
        
        List<EntityRelationship> entities = repository.findByEntity(entityType, entityId);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EntityRelationshipDTO> getRelationshipsBySourceEntity(String entityType, Long entityId) {
        log.info("查询源实体的关系，entityType：{}，entityId：{}", entityType, entityId);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空列表");
            return List.of();
        }
        
        List<EntityRelationship> entities = repository.findBySourceEntity(entityType, entityId);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EntityRelationshipDTO> getRelationshipsByTargetEntity(String entityType, Long entityId) {
        log.info("查询目标实体的关系，entityType：{}，entityId：{}", entityType, entityId);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空列表");
            return List.of();
        }
        
        List<EntityRelationship> entities = repository.findByTargetEntity(entityType, entityId);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean existsRelationship(String sourceType, Long sourceId, String targetType, Long targetId, String relationshipType) {
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回false");
            return false;
        }
        return repository.existsRelationship(sourceType, sourceId, targetType, targetId, relationshipType);
    }

    @Override
    public List<Map<String, Object>> getRelationshipGraphData() {
        log.info("获取实体关系图数据");
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空列表");
            return List.of();
        }
        
        return repository.findRelationshipGraphData();
    }

    @Override
    public List<EntityRelationshipDTO> getRelationshipsByType(String relationshipType) {
        log.info("根据关系类型查询，relationshipType：{}", relationshipType);
        
        if (repository == null) {
            log.warn("EntityRelationshipRepository 未初始化，返回空列表");
            return List.of();
        }
        
        List<EntityRelationship> entities = repository.findByRelationshipType(relationshipType);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EntityRelationshipDTO convertToDTO(EntityRelationship entity) {
        EntityRelationshipDTO dto = new EntityRelationshipDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private EntityRelationship convertToEntity(EntityRelationshipDTO dto) {
        EntityRelationship entity = new EntityRelationship();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
} 