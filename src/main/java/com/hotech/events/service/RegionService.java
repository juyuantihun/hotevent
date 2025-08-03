package com.hotech.events.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.Region;

import java.util.List;
import java.util.Map;

/**
 * 地区服务接口
 */
public interface RegionService {
    
    /**
     * 创建地区
     * @param region 地区信息
     * @param dictionaryIds 字典项ID列表
     * @return 创建的地区
     */
    Region createRegion(Region region, List<Long> dictionaryIds);
    
    /**
     * 更新地区
     * @param region 地区信息
     * @param dictionaryIds 字典项ID列表
     * @return 更新后的地区
     */
    Region updateRegion(Region region, List<Long> dictionaryIds);
    
    /**
     * 删除地区
     * @param id 地区ID
     * @return 是否删除成功
     */
    boolean deleteRegion(Long id);
    
    /**
     * 获取地区详情
     * @param id 地区ID
     * @return 地区详情
     */
    Map<String, Object> getRegionDetail(Long id);
    
    /**
     * 分页查询地区列表
     * @param page 分页参数
     * @param name 地区名称（可选）
     * @param type 地区类型（可选）
     * @return 地区分页列表
     */
    IPage<Region> listRegions(Page<Region> page, String name, String type);
    
    /**
     * 添加字典项到地区
     * @param regionId 地区ID
     * @param dictionaryId 字典项ID
     * @return 是否添加成功
     */
    boolean addDictionaryItem(Long regionId, Long dictionaryId);
    
    /**
     * 从地区移除字典项
     * @param regionId 地区ID
     * @param dictionaryId 字典项ID
     * @return 是否移除成功
     */
    boolean removeDictionaryItem(Long regionId, Long dictionaryId);
    
    /**
     * 获取地区包含的字典项
     * @param regionId 地区ID
     * @return 字典项列表
     */
    List<Map<String, Object>> getRegionDictionaryItems(Long regionId);
    
    /**
     * 检查地区是否被引用
     * @param regionId 地区ID
     * @return 是否被引用
     */
    boolean isRegionReferenced(Long regionId);
    
    /**
     * 获取地区树形结构
     * @return 地区树形结构
     */
    List<Region> getRegionTree();
    
    /**
     * 根据ID获取地区信息
     * @param id 地区ID
     * @return 地区信息
     */
    Region getById(Long id);
    
    /**
     * 获取地区的子地区
     * @param parentId 父地区ID
     * @return 子地区列表
     */
    List<Region> getRegionChildren(Long parentId);
    
    /**
     * 获取地区的祖先地区
     * @param regionId 地区ID
     * @return 祖先地区列表（从根到父级）
     */
    List<Region> getRegionAncestors(Long regionId);
    
    /**
     * 搜索地区
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    IPage<Region> searchRegions(Page<Region> page, String keyword);
}