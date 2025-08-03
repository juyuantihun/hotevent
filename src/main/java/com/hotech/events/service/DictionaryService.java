package com.hotech.events.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.DictionaryDTO;
import com.hotech.events.dto.DictionaryQueryDTO;
import com.hotech.events.entity.Dictionary;

import java.util.List;

/**
 * 字典服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface DictionaryService {

    /**
     * 分页查询字典列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<DictionaryDTO> getDictionaryList(DictionaryQueryDTO queryDTO);

    /**
     * 获取字典树形结构
     * 
     * @param dictType 字典类型，可选
     * @return 字典树
     */
    List<DictionaryDTO> getDictionaryTree(String dictType);

    /**
     * 根据类型获取字典项
     * 
     * @param dictType 字典类型
     * @return 字典项列表
     */
    List<DictionaryDTO> getDictionaryByType(String dictType);

    /**
     * 根据ID获取字典详情
     * 
     * @param id 字典ID
     * @return 字典详情
     */
    DictionaryDTO getDictionaryDetail(Long id);

    /**
     * 创建字典项
     * 
     * @param dictionaryDTO 字典信息
     * @return 创建的字典信息
     */
    DictionaryDTO createDictionary(DictionaryDTO dictionaryDTO);

    /**
     * 更新字典项
     * 
     * @param dictionaryDTO 字典信息
     * @return 更新后的字典信息
     */
    DictionaryDTO updateDictionary(DictionaryDTO dictionaryDTO);

    /**
     * 删除字典项
     * 
     * @param id 字典ID
     * @return 是否删除成功
     */
    Boolean deleteDictionary(Long id);

    /**
     * 批量删除字典项
     * 
     * @param ids 字典ID列表
     * @return 删除成功的数量
     */
    Integer deleteDictionariesBatch(List<Long> ids);

    /**
     * 获取字典类型列表
     * 
     * @return 字典类型列表
     */
    List<String> getDictionaryTypes();

    /**
     * 根据字典项的entityType和entityId获取实体详情
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return Object 详情DTO
     */
    Object getEntityDetail(String entityType, Long entityId);
} 