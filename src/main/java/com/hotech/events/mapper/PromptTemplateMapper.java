package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提示词模板Mapper接口
 * 
 * @author system
 * @since 2025-01-24
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

    /**
     * 根据模板类型查询激活的模板
     * 
     * @param templateType 模板类型
     * @return 提示词模板列表
     */
    @Select("SELECT * FROM prompt_template WHERE template_type = #{templateType} AND is_active = 1 ORDER BY created_at DESC")
    List<PromptTemplate> selectActiveByType(@Param("templateType") String templateType);

    /**
     * 根据模板名称和类型查询模板
     * 
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @return 提示词模板
     */
    @Select("SELECT * FROM prompt_template WHERE template_name = #{templateName} AND template_type = #{templateType}")
    PromptTemplate selectByNameAndType(@Param("templateName") String templateName, @Param("templateType") String templateType);

    /**
     * 查询所有激活的模板
     * 
     * @return 提示词模板列表
     */
    @Select("SELECT * FROM prompt_template WHERE is_active = 1 ORDER BY template_type, created_at DESC")
    List<PromptTemplate> selectAllActive();
    
    /**
     * 根据模板类型查询激活的模板（单个）
     * 
     * @param templateType 模板类型
     * @return 提示词模板
     */
    @Select("SELECT * FROM prompt_template WHERE template_type = #{templateType} AND is_active = 1 ORDER BY created_at DESC LIMIT 1")
    PromptTemplate findActiveByType(@Param("templateType") String templateType);
    
    /**
     * 查询所有激活的模板
     * 
     * @return 提示词模板列表
     */
    @Select("SELECT * FROM prompt_template WHERE is_active = 1 ORDER BY template_type, created_at DESC")
    List<PromptTemplate> findAllActive();
    
    /**
     * 根据模板名称和类型查询模板
     * 
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @return 提示词模板
     */
    @Select("SELECT * FROM prompt_template WHERE template_name = #{templateName} AND template_type = #{templateType}")
    PromptTemplate findByNameAndType(@Param("templateName") String templateName, @Param("templateType") String templateType);
}