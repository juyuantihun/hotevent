package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.DictionaryDTO;
import com.hotech.events.dto.DictionaryQueryDTO;
import com.hotech.events.entity.Dictionary;
import com.hotech.events.mapper.DictionaryMapper;
import com.hotech.events.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.hotech.events.dto.CountryDTO;
import com.hotech.events.entity.Country;
import com.hotech.events.mapper.CountryMapper;
import com.hotech.events.service.OrganizationService;
import com.hotech.events.service.PersonService;

/**
 * 字典服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private PersonService personService;

    /**
     * 分页查询字典列表
     */
    @Override
    public Page<DictionaryDTO> getDictionaryList(DictionaryQueryDTO queryDTO) {
        log.info("查询字典列表，查询条件：{}", queryDTO);
        
        Page<Dictionary> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<Dictionary> wrapper = buildQueryWrapper(queryDTO);
        
        Page<Dictionary> dictionaryPage = dictionaryMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<DictionaryDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(dictionaryPage, resultPage, "records");
        
        List<DictionaryDTO> dictionaryDTOs = dictionaryPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(dictionaryDTOs);
        
        log.info("查询字典列表完成，共{}条记录", resultPage.getTotal());
        return resultPage;
    }

    /**
     * 获取字典树形结构
     */
    @Override
    public List<DictionaryDTO> getDictionaryTree(String dictType) {
        log.info("获取字典树形结构，字典类型：{}", dictType);
        
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq("dict_type", dictType);
        }
        wrapper.eq("status", 1);
        wrapper.orderByAsc("dict_type", "sort_order", "id");
        
        List<Dictionary> dictionaries = dictionaryMapper.selectList(wrapper);
        List<DictionaryDTO> dictionaryDTOs = dictionaries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        List<DictionaryDTO> tree = buildTree(dictionaryDTOs, 0L);
        
        log.info("获取字典树形结构完成，共{}个根节点", tree.size());
        return tree;
    }

    /**
     * 根据类型获取字典项
     */
    @Override
    public List<DictionaryDTO> getDictionaryByType(String dictType) {
        log.info("根据类型获取字典项，字典类型：{}", dictType);
        
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType);
        wrapper.eq("status", 1);
        wrapper.orderByAsc("sort_order", "id");
        
        List<Dictionary> dictionaries = dictionaryMapper.selectList(wrapper);
        List<DictionaryDTO> dictionaryDTOs = dictionaries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("根据类型获取字典项完成，共{}条记录", dictionaryDTOs.size());
        return dictionaryDTOs;
    }

    /**
     * 根据ID获取字典详情
     */
    @Override
    public DictionaryDTO getDictionaryDetail(Long id) {
        log.info("获取字典详情，ID：{}", id);
        
        Dictionary dictionary = dictionaryMapper.selectById(id);
        if (dictionary == null) {
            throw new RuntimeException("字典不存在，ID：" + id);
        }
        
        DictionaryDTO dictionaryDTO = convertToDTO(dictionary);
        
        log.info("获取字典详情完成，ID：{}", id);
        return dictionaryDTO;
    }

    /**
     * 创建字典项
     */
    @Override
    @Transactional
    public DictionaryDTO createDictionary(DictionaryDTO dictionaryDTO) {
        log.info("创建字典项，字典信息：{}", dictionaryDTO);
        
        // 检查编码唯一性
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictionaryDTO.getDictType());
        wrapper.eq("dict_code", dictionaryDTO.getDictCode());
        
        Dictionary existing = dictionaryMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("字典编码已存在：" + dictionaryDTO.getDictCode());
        }
        
        Dictionary dictionary = convertToEntity(dictionaryDTO);
        dictionary.setCreatedAt(LocalDateTime.now());
        dictionary.setUpdatedAt(LocalDateTime.now());
        
        if (dictionary.getStatus() == null) {
            dictionary.setStatus(1);
        }
        if (dictionary.getParentId() == null) {
            dictionary.setParentId(0L);
        }
        if (dictionary.getIsAutoAdded() == null) {
            dictionary.setIsAutoAdded(0);
        }
        
        // 自动生成排序号
        if (dictionary.getSortOrder() == null || dictionary.getSortOrder() == 0) {
            // 查询同类型、同父级下的最大排序号
            QueryWrapper<Dictionary> sortWrapper = new QueryWrapper<>();
            sortWrapper.eq("dict_type", dictionary.getDictType());
            sortWrapper.eq("parent_id", dictionary.getParentId());
            sortWrapper.orderByDesc("sort_order");
            sortWrapper.last("LIMIT 1");
            
            Dictionary maxSortDict = dictionaryMapper.selectOne(sortWrapper);
            if (maxSortDict != null && maxSortDict.getSortOrder() != null) {
                dictionary.setSortOrder(maxSortDict.getSortOrder() + 1);
            } else {
                // 如果没有同级字典，从1开始
                dictionary.setSortOrder(1);
            }
            
            log.info("自动生成排序号：dictType={}, parentId={}, sortOrder={}", 
                dictionary.getDictType(), dictionary.getParentId(), dictionary.getSortOrder());
        }
        
        dictionaryMapper.insert(dictionary);
        
        log.info("创建字典项完成，字典ID：{}", dictionary.getId());
        return convertToDTO(dictionary);
    }

    /**
     * 更新字典项
     */
    @Override
    @Transactional
    public DictionaryDTO updateDictionary(DictionaryDTO dictionaryDTO) {
        log.info("更新字典项，字典信息：{}", dictionaryDTO);
        
        Dictionary existingDictionary = dictionaryMapper.selectById(dictionaryDTO.getId());
        if (existingDictionary == null) {
            throw new RuntimeException("字典不存在，ID：" + dictionaryDTO.getId());
        }
        
        // 检查编码唯一性（排除自己）
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictionaryDTO.getDictType());
        wrapper.eq("dict_code", dictionaryDTO.getDictCode());
        wrapper.ne("id", dictionaryDTO.getId());
        
        Dictionary existing = dictionaryMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("字典编码已存在：" + dictionaryDTO.getDictCode());
        }
        
        Dictionary dictionary = convertToEntity(dictionaryDTO);
        dictionary.setUpdatedAt(LocalDateTime.now());
        
        dictionaryMapper.updateById(dictionary);
        
        log.info("更新字典项完成，字典ID：{}", dictionary.getId());
        return convertToDTO(dictionary);
    }

    /**
     * 删除字典项
     */
    @Override
    @Transactional
    public Boolean deleteDictionary(Long id) {
        log.info("删除字典项，ID：{}", id);
        
        Dictionary dictionary = dictionaryMapper.selectById(id);
        if (dictionary == null) {
            throw new RuntimeException("字典不存在，ID：" + id);
        }
        
        // 检查是否有子项
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        wrapper.eq("status", 1);
        
        List<Dictionary> children = dictionaryMapper.selectList(wrapper);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子级字典项，无法删除");
        }
        
        dictionaryMapper.deleteById(id);
        
        log.info("删除字典项完成，ID：{}", id);
        return true;
    }

    /**
     * 批量删除字典项
     */
    @Override
    @Transactional
    public Integer deleteDictionariesBatch(List<Long> ids) {
        log.info("批量删除字典项，IDs：{}", ids);
        
        int successCount = 0;
        for (Long id : ids) {
            try {
                deleteDictionary(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除字典项失败，ID：{}，错误：{}", id, e.getMessage(), e);
            }
        }
        
        log.info("批量删除字典项完成，成功：{}，失败：{}", successCount, ids.size() - successCount);
        return successCount;
    }

    /**
     * 获取字典类型列表
     */
    @Override
    public List<String> getDictionaryTypes() {
        log.info("获取字典类型列表");
        
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT dict_type");
        wrapper.eq("status", 1);
        wrapper.orderByAsc("dict_type");
        
        List<Dictionary> dictionaries = dictionaryMapper.selectList(wrapper);
        List<String> types = dictionaries.stream()
                .map(Dictionary::getDictType)
                .collect(Collectors.toList());
        
        log.info("获取字典类型列表完成，共{}种类型", types.size());
        return types;
    }

    /**
     * 获取实体详情
     */
    @Override
    public Object getEntityDetail(String entityType, Long entityId) {
        if (entityType == null || entityId == null) return null;
        switch (entityType) {
            case "country":
                Country country = countryMapper.selectById(entityId);
                if (country == null) return null;
                CountryDTO countryDTO = new CountryDTO();
                BeanUtils.copyProperties(country, countryDTO);
                return countryDTO;
            case "organization":
                return organizationService.getDetail(entityId);
            case "person":
                return personService.getDetail(entityId);
            default:
                return null;
        }
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<Dictionary> buildQueryWrapper(DictionaryQueryDTO queryDTO) {
        QueryWrapper<Dictionary> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getDictType())) {
            wrapper.eq("dict_type", queryDTO.getDictType());
        }
        
        if (StringUtils.hasText(queryDTO.getDictName())) {
            wrapper.like("dict_name", queryDTO.getDictName());
        }
        
        if (queryDTO.getParentId() != null) {
            wrapper.eq("parent_id", queryDTO.getParentId());
        }
        
        if (queryDTO.getIsAutoAdded() != null) {
            wrapper.eq("is_auto_added", queryDTO.getIsAutoAdded());
        }
        
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        
        // 先按字典类型排序，再按排序和ID升序
        wrapper.orderByAsc("dict_type", "sort_order", "id");
        
        return wrapper;
    }

    /**
     * 实体转DTO
     */
    private DictionaryDTO convertToDTO(Dictionary dictionary) {
        DictionaryDTO dictionaryDTO = new DictionaryDTO();
        BeanUtils.copyProperties(dictionary, dictionaryDTO);
        return dictionaryDTO;
    }

    /**
     * DTO转实体
     */
    private Dictionary convertToEntity(DictionaryDTO dictionaryDTO) {
        Dictionary dictionary = new Dictionary();
        BeanUtils.copyProperties(dictionaryDTO, dictionary);
        return dictionary;
    }

    /**
     * 构建树形结构
     */
    private List<DictionaryDTO> buildTree(List<DictionaryDTO> list, Long parentId) {
        return list.stream()
                .filter(item -> Objects.equals(item.getParentId(), parentId))
                .map(item -> {
                    List<DictionaryDTO> children = buildTree(list, item.getId());
                    if (!children.isEmpty()) {
                        item.setChildren(children);
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }
} 