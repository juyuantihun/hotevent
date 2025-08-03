package com.hotech.events.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.SubjectObjectRelationDTO;
import com.hotech.events.dto.SubjectObjectRelationQueryDTO;

import java.util.List;

/**
 * 主体客体关系服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface SubjectObjectRelationService {

    /**
     * 分页查询主体客体关系列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<SubjectObjectRelationDTO> getRelationPage(SubjectObjectRelationQueryDTO queryDTO);

    /**
     * 根据ID获取主体客体关系详情
     * 
     * @param id 关系ID
     * @return 关系详情
     */
    SubjectObjectRelationDTO getRelationDetail(Long id);

    /**
     * 创建主体客体关系
     * 
     * @param relationDTO 关系信息
     * @return 创建的关系信息
     */
    SubjectObjectRelationDTO createRelation(SubjectObjectRelationDTO relationDTO);

    /**
     * 更新主体客体关系
     * 
     * @param relationDTO 关系信息
     * @return 更新后的关系信息
     */
    SubjectObjectRelationDTO updateRelation(SubjectObjectRelationDTO relationDTO);

    /**
     * 删除主体客体关系
     * 
     * @param id 关系ID
     * @return 是否删除成功
     */
    Boolean deleteRelation(Long id);

    /**
     * 批量删除主体客体关系
     * 
     * @param ids 关系ID列表
     * @return 删除成功的数量
     */
    Integer deleteRelationsBatch(List<Long> ids);

    /**
     * 根据主体编码查询关系
     * 
     * @param subjectCode 主体编码
     * @return 关系列表
     */
    List<SubjectObjectRelationDTO> getRelationsBySubject(String subjectCode);

    /**
     * 根据客体编码查询关系
     * 
     * @param objectCode 客体编码
     * @return 关系列表
     */
    List<SubjectObjectRelationDTO> getRelationsByObject(String objectCode);

    /**
     * 根据关系类型查询关系
     * 
     * @param relationType 关系类型
     * @return 关系列表
     */
    List<SubjectObjectRelationDTO> getRelationsByType(String relationType);

    /**
     * 根据主体和客体查询关系
     * 
     * @param subjectCode 主体编码
     * @param objectCode 客体编码
     * @return 关系列表
     */
    List<SubjectObjectRelationDTO> getRelationsBySubjectAndObject(String subjectCode, String objectCode);

    /**
     * 检查是否存在特定关系
     * 
     * @param subjectCode 主体编码
     * @param objectCode 客体编码
     * @param relationType 关系类型
     * @return 是否存在
     */
    Boolean existsRelation(String subjectCode, String objectCode, String relationType);
} 