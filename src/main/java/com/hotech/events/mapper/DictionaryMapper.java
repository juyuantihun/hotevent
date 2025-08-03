package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.Dictionary;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典Mapper接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Repository
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    /**
     * 根据类型查询字典列表
     * 
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<Dictionary> selectByType(@Param("dictType") String dictType);

    /**
     * 根据类型和编码查询字典
     * 
     * @param dictType 字典类型
     * @param dictCode 字典编码
     * @return 字典对象
     */
    Dictionary selectByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 根据类型和名称查询字典
     * 
     * @param dictType 字典类型
     * @param dictName 字典名称
     * @return 字典对象
     */
    Dictionary selectByTypeAndName(@Param("dictType") String dictType, @Param("dictName") String dictName);

    /**
     * 自动添加字典项
     * 
     * @param dictType 字典类型
     * @param dictName 字典名称
     * @return 新增的字典对象
     */
    Dictionary insertAutoDict(@Param("dictType") String dictType, @Param("dictName") String dictName);

    /**
     * 查询字典树形结构
     * 
     * @param dictType 字典类型
     * @param parentId 父级ID
     * @return 字典树形列表
     */
    List<Dictionary> selectTreeByType(@Param("dictType") String dictType, @Param("parentId") Long parentId);
    
    /**
     * 根据类型和值查询字典
     * 
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典对象
     */
    Dictionary findByTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);
} 