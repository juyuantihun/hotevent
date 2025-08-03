package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.SubjectObjectRelationDTO;
import com.hotech.events.dto.SubjectObjectRelationQueryDTO;
import com.hotech.events.entity.SubjectObjectRelation;
import com.hotech.events.entity.Dictionary;
import com.hotech.events.mapper.SubjectObjectRelationMapper;
import com.hotech.events.mapper.DictionaryMapper;
import com.hotech.events.service.SubjectObjectRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主体客体关系服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SubjectObjectRelationServiceImpl implements SubjectObjectRelationService {

    @Autowired
    private SubjectObjectRelationMapper relationMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Override
    public Page<SubjectObjectRelationDTO> getRelationPage(SubjectObjectRelationQueryDTO queryDTO) {
        log.info("分页查询主体客体关系，查询条件：{}", queryDTO);
        
        Page<SubjectObjectRelation> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<SubjectObjectRelation> wrapper = buildQueryWrapper(queryDTO);
        
        Page<SubjectObjectRelation> relationPage = relationMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<SubjectObjectRelationDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(relationPage, resultPage, "records");
        
        List<SubjectObjectRelationDTO> relationDTOs = relationPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(relationDTOs);
        
        log.info("分页查询主体客体关系完成，共{}条记录", resultPage.getTotal());
        return resultPage;
    }

    @Override
    public SubjectObjectRelationDTO getRelationDetail(Long id) {
        log.info("获取主体客体关系详情，ID：{}", id);
        
        SubjectObjectRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            throw new RuntimeException("关系不存在，ID：" + id);
        }
        
        SubjectObjectRelationDTO relationDTO = convertToDTO(relation);
        
        log.info("获取主体客体关系详情完成，ID：{}", id);
        return relationDTO;
    }

    @Override
    @Transactional
    public SubjectObjectRelationDTO createRelation(SubjectObjectRelationDTO relationDTO) {
        log.info("创建主体客体关系，关系信息：{}", relationDTO);
        
        // 检查是否已存在相同关系
        if (relationMapper.existsRelation(relationDTO.getSubjectCode(), 
                relationDTO.getObjectCode(), relationDTO.getRelationType())) {
            throw new RuntimeException("该关系已存在");
        }
        
        SubjectObjectRelation relation = convertToEntity(relationDTO);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());
        
        if (relation.getStatus() == null) {
            relation.setStatus(1);
        }
        
        relationMapper.insert(relation);
        
        log.info("创建主体客体关系完成，关系ID：{}", relation.getId());
        return convertToDTO(relation);
    }

    @Override
    @Transactional
    public SubjectObjectRelationDTO updateRelation(SubjectObjectRelationDTO relationDTO) {
        log.info("更新主体客体关系，关系信息：{}", relationDTO);
        
        SubjectObjectRelation existingRelation = relationMapper.selectById(relationDTO.getId());
        if (existingRelation == null) {
            throw new RuntimeException("关系不存在，ID：" + relationDTO.getId());
        }
        
        // 检查是否已存在相同关系（排除自己）
        QueryWrapper<SubjectObjectRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_code", relationDTO.getSubjectCode())
                .eq("object_code", relationDTO.getObjectCode())
                .eq("relation_type", relationDTO.getRelationType())
                .ne("id", relationDTO.getId());
        
        SubjectObjectRelation existing = relationMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("该关系已存在");
        }
        
        SubjectObjectRelation relation = convertToEntity(relationDTO);
        relation.setUpdatedAt(LocalDateTime.now());
        
        relationMapper.updateById(relation);
        
        log.info("更新主体客体关系完成，关系ID：{}", relation.getId());
        return convertToDTO(relation);
    }

    @Override
    @Transactional
    public Boolean deleteRelation(Long id) {
        log.info("删除主体客体关系，ID：{}", id);
        
        SubjectObjectRelation relation = relationMapper.selectById(id);
        if (relation == null) {
            throw new RuntimeException("关系不存在，ID：" + id);
        }
        
        relationMapper.deleteById(id);
        
        log.info("删除主体客体关系完成，ID：{}", id);
        return true;
    }

    @Override
    @Transactional
    public Integer deleteRelationsBatch(List<Long> ids) {
        log.info("批量删除主体客体关系，IDs：{}", ids);
        
        int successCount = 0;
        for (Long id : ids) {
            try {
                deleteRelation(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除主体客体关系失败，ID：{}，错误：{}", id, e.getMessage(), e);
            }
        }
        
        log.info("批量删除主体客体关系完成，成功：{}，失败：{}", successCount, ids.size() - successCount);
        return successCount;
    }

    @Override
    public List<SubjectObjectRelationDTO> getRelationsBySubject(String subjectCode) {
        log.info("根据主体编码查询关系，主体编码：{}", subjectCode);
        
        List<SubjectObjectRelation> relations = relationMapper.selectBySubjectCode(subjectCode);
        
        return relations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectObjectRelationDTO> getRelationsByObject(String objectCode) {
        log.info("根据客体编码查询关系，客体编码：{}", objectCode);
        
        List<SubjectObjectRelation> relations = relationMapper.selectByObjectCode(objectCode);
        
        return relations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectObjectRelationDTO> getRelationsByType(String relationType) {
        log.info("根据关系类型查询关系，关系类型：{}", relationType);
        
        List<SubjectObjectRelation> relations = relationMapper.selectByRelationType(relationType);
        
        return relations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectObjectRelationDTO> getRelationsBySubjectAndObject(String subjectCode, String objectCode) {
        log.info("根据主体和客体查询关系，主体编码：{}，客体编码：{}", subjectCode, objectCode);
        
        List<SubjectObjectRelation> relations = relationMapper.selectBySubjectAndObject(subjectCode, objectCode);
        
        return relations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean existsRelation(String subjectCode, String objectCode, String relationType) {
        log.info("检查是否存在特定关系，主体编码：{}，客体编码：{}，关系类型：{}", subjectCode, objectCode, relationType);
        
        return relationMapper.existsRelation(subjectCode, objectCode, relationType);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<SubjectObjectRelation> buildQueryWrapper(SubjectObjectRelationQueryDTO queryDTO) {
        QueryWrapper<SubjectObjectRelation> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getSubjectCode())) {
            wrapper.eq("subject_code", queryDTO.getSubjectCode());
        }
        
        if (StringUtils.hasText(queryDTO.getObjectCode())) {
            wrapper.eq("object_code", queryDTO.getObjectCode());
        }
        
        if (StringUtils.hasText(queryDTO.getRelationType())) {
            wrapper.eq("relation_type", queryDTO.getRelationType());
        }
        
        if (StringUtils.hasText(queryDTO.getRelationName())) {
            wrapper.like("relation_name", queryDTO.getRelationName());
        }
        
        if (queryDTO.getIntensityLevel() != null) {
            wrapper.eq("intensity_level", queryDTO.getIntensityLevel());
        }
        
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc("created_at");
        
        return wrapper;
    }

    /**
     * 实体转DTO
     */
    private SubjectObjectRelationDTO convertToDTO(SubjectObjectRelation relation) {
        SubjectObjectRelationDTO relationDTO = new SubjectObjectRelationDTO();
        BeanUtils.copyProperties(relation, relationDTO);
        
        // 填充关联字典名称
        fillDictionaryNames(relationDTO);
        
        return relationDTO;
    }

    /**
     * DTO转实体
     */
    private SubjectObjectRelation convertToEntity(SubjectObjectRelationDTO relationDTO) {
        SubjectObjectRelation relation = new SubjectObjectRelation();
        BeanUtils.copyProperties(relationDTO, relation);
        return relation;
    }

    /**
     * 填充字典名称
     */
    private void fillDictionaryNames(SubjectObjectRelationDTO relationDTO) {
        // 填充主体名称
        if (StringUtils.hasText(relationDTO.getSubjectCode())) {
            Dictionary subject = getDictionaryByCode("事件主体", relationDTO.getSubjectCode());
            if (subject != null) {
                relationDTO.setSubjectName(subject.getDictName());
            }
        }
        
        // 填充客体名称
        if (StringUtils.hasText(relationDTO.getObjectCode())) {
            Dictionary object = getDictionaryByCode("事件客体", relationDTO.getObjectCode());
            if (object != null) {
                relationDTO.setObjectName(object.getDictName());
            }
        }
        
        // 填充关系类型名称
        if (StringUtils.hasText(relationDTO.getRelationType())) {
            Dictionary relationType = getDictionaryByCode("关系类型", relationDTO.getRelationType());
            if (relationType != null) {
                relationDTO.setRelationTypeName(relationType.getDictName());
            }
        }
    }

    /**
     * 根据字典类型和编码获取字典
     */
    private Dictionary getDictionaryByCode(String dictType, String dictCode) {
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType)
                .eq("dict_code", dictCode)
                .eq("status", 1);
        return dictionaryMapper.selectOne(wrapper);
    }
} 