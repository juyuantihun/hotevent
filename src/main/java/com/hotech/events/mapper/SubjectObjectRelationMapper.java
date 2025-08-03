package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.SubjectObjectRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 主体客体关系Mapper
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Mapper
public interface SubjectObjectRelationMapper extends BaseMapper<SubjectObjectRelation> {

    /**
     * 根据主体编码查询关系
     * 
     * @param subjectCode 主体编码
     * @return 关系列表
     */
    @Select("SELECT * FROM subject_object_relation WHERE subject_code = #{subjectCode} AND status = 1 ORDER BY created_at DESC")
    List<SubjectObjectRelation> selectBySubjectCode(@Param("subjectCode") String subjectCode);

    /**
     * 根据客体编码查询关系
     * 
     * @param objectCode 客体编码
     * @return 关系列表
     */
    @Select("SELECT * FROM subject_object_relation WHERE object_code = #{objectCode} AND status = 1 ORDER BY created_at DESC")
    List<SubjectObjectRelation> selectByObjectCode(@Param("objectCode") String objectCode);

    /**
     * 根据关系类型查询关系
     * 
     * @param relationType 关系类型
     * @return 关系列表
     */
    @Select("SELECT * FROM subject_object_relation WHERE relation_type = #{relationType} AND status = 1 ORDER BY created_at DESC")
    List<SubjectObjectRelation> selectByRelationType(@Param("relationType") String relationType);

    /**
     * 根据主体和客体编码查询关系
     * 
     * @param subjectCode 主体编码
     * @param objectCode 客体编码
     * @return 关系列表
     */
    @Select("SELECT * FROM subject_object_relation WHERE subject_code = #{subjectCode} AND object_code = #{objectCode} AND status = 1 ORDER BY created_at DESC")
    List<SubjectObjectRelation> selectBySubjectAndObject(@Param("subjectCode") String subjectCode, @Param("objectCode") String objectCode);

    /**
     * 检查是否存在特定关系
     * 
     * @param subjectCode 主体编码
     * @param objectCode 客体编码
     * @param relationType 关系类型
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM subject_object_relation WHERE subject_code = #{subjectCode} AND object_code = #{objectCode} AND relation_type = #{relationType} AND status = 1")
    boolean existsRelation(@Param("subjectCode") String subjectCode, @Param("objectCode") String objectCode, @Param("relationType") String relationType);

    /**
     * 根据强度级别查询关系
     * 
     * @param intensityLevel 强度级别
     * @return 关系列表
     */
    @Select("SELECT * FROM subject_object_relation WHERE intensity_level = #{intensityLevel} AND status = 1 ORDER BY created_at DESC")
    List<SubjectObjectRelation> selectByIntensityLevel(@Param("intensityLevel") Integer intensityLevel);
} 