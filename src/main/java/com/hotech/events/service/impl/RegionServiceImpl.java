package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.Region;
import com.hotech.events.entity.RegionItem;
import com.hotech.events.mapper.RegionItemMapper;
import com.hotech.events.mapper.RegionMapper;
import com.hotech.events.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 地区服务实现类
 */
@Slf4j
@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;
    
    @Autowired
    private RegionItemMapper regionItemMapper;
    
    @Override
    @Transactional
    public Region createRegion(Region region, List<Long> dictionaryIds) {
        log.info("创建地区: {}, 字典项: {}", region, dictionaryIds);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        region.setCreatedAt(now);
        region.setUpdatedAt(now);
        
        // 保存地区信息
        regionMapper.insert(region);
        
        // 保存地区项目关联
        if (dictionaryIds != null && !dictionaryIds.isEmpty()) {
            for (Long dictionaryId : dictionaryIds) {
                RegionItem item = new RegionItem();
                item.setRegionId(region.getId());
                item.setDictionaryId(dictionaryId);
                item.setCreatedAt(now);
                regionItemMapper.insert(item);
            }
        }
        
        return region;
    }
    
    @Override
    @Transactional
    public Region updateRegion(Region region, List<Long> dictionaryIds) {
        log.info("更新地区: {}, 字典项: {}", region, dictionaryIds);
        
        // 设置更新时间
        region.setUpdatedAt(LocalDateTime.now());
        
        // 更新地区信息
        regionMapper.updateById(region);
        
        // 更新地区项目关联
        if (dictionaryIds != null) {
            // 获取当前地区的字典项ID列表
            List<Long> currentDictionaryIds = regionItemMapper.findDictionaryIdsByRegionId(region.getId());
            
            // 需要添加的字典项
            List<Long> toAdd = dictionaryIds.stream()
                    .filter(id -> !currentDictionaryIds.contains(id))
                    .collect(Collectors.toList());
            
            // 需要删除的字典项
            List<Long> toRemove = currentDictionaryIds.stream()
                    .filter(id -> !dictionaryIds.contains(id))
                    .collect(Collectors.toList());
            
            // 添加新的字典项
            LocalDateTime now = LocalDateTime.now();
            for (Long dictionaryId : toAdd) {
                RegionItem item = new RegionItem();
                item.setRegionId(region.getId());
                item.setDictionaryId(dictionaryId);
                item.setCreatedAt(now);
                regionItemMapper.insert(item);
            }
            
            // 删除不再需要的字典项
            if (!toRemove.isEmpty()) {
                LambdaQueryWrapper<RegionItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RegionItem::getRegionId, region.getId())
                        .in(RegionItem::getDictionaryId, toRemove);
                regionItemMapper.delete(wrapper);
            }
        }
        
        return region;
    }
    
    @Override
    @Transactional
    public boolean deleteRegion(Long id) {
        log.info("删除地区: {}", id);
        
        // 检查地区是否被引用
        if (isRegionReferenced(id)) {
            log.warn("地区 {} 被引用，无法删除", id);
            return false;
        }
        
        // 删除地区项目关联
        LambdaQueryWrapper<RegionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionItem::getRegionId, id);
        regionItemMapper.delete(wrapper);
        
        // 删除地区
        int result = regionMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public Map<String, Object> getRegionDetail(Long id) {
        log.info("获取地区详情: {}", id);
        
        // 获取地区基本信息
        Region region = regionMapper.selectById(id);
        if (region == null) {
            return null;
        }
        
        // 获取地区包含的字典项
        List<Map<String, Object>> dictionaryItems = regionItemMapper.findDictionaryItemsByRegionId(id);
        
        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("region", region);
        result.put("dictionaryItems", dictionaryItems);
        
        return result;
    }
    
    @Override
    public IPage<Region> listRegions(Page<Region> page, String name, String type) {
        log.info("分页查询地区列表: page={}, name={}, type={}", page, name, type);
        
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(name)) {
            wrapper.like(Region::getName, name);
        }
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Region::getType, type);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Region::getCreatedAt);
        
        return regionMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional
    public boolean addDictionaryItem(Long regionId, Long dictionaryId) {
        log.info("添加字典项到地区: regionId={}, dictionaryId={}", regionId, dictionaryId);
        
        // 检查地区是否存在
        Region region = regionMapper.selectById(regionId);
        if (region == null) {
            log.warn("地区 {} 不存在", regionId);
            return false;
        }
        
        // 检查是否已经存在关联
        LambdaQueryWrapper<RegionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionItem::getRegionId, regionId)
                .eq(RegionItem::getDictionaryId, dictionaryId);
        
        if (regionItemMapper.selectCount(wrapper) > 0) {
            log.info("地区 {} 已包含字典项 {}", regionId, dictionaryId);
            return true;
        }
        
        // 添加关联
        RegionItem item = new RegionItem();
        item.setRegionId(regionId);
        item.setDictionaryId(dictionaryId);
        item.setCreatedAt(LocalDateTime.now());
        
        int result = regionItemMapper.insert(item);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean removeDictionaryItem(Long regionId, Long dictionaryId) {
        log.info("从地区移除字典项: regionId={}, dictionaryId={}", regionId, dictionaryId);
        
        LambdaQueryWrapper<RegionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionItem::getRegionId, regionId)
                .eq(RegionItem::getDictionaryId, dictionaryId);
        
        int result = regionItemMapper.delete(wrapper);
        return result > 0;
    }
    
    @Override
    public List<Map<String, Object>> getRegionDictionaryItems(Long regionId) {
        log.info("获取地区包含的字典项: {}", regionId);
        return regionItemMapper.findDictionaryItemsByRegionId(regionId);
    }
    
    @Override
    public boolean isRegionReferenced(Long regionId) {
        log.info("检查地区是否被引用: {}", regionId);
        
        // 获取被时间线引用的地区ID列表
        List<Long> referencedIds = regionMapper.findReferencedRegionIds();
        
        return referencedIds.contains(regionId);
    }
    
    @Override
    public List<Region> getRegionTree() {
        log.info("获取地区树形结构");
        
        // 获取所有地区
        List<Region> allRegions = regionMapper.selectList(null);
        
        // 构建树形结构
        return buildRegionTree(allRegions, null);
    }
    
    @Override
    public Region getById(Long id) {
        log.info("根据ID获取地区信息: {}", id);
        return regionMapper.selectById(id);
    }
    
    @Override
    public List<Region> getRegionChildren(Long parentId) {
        log.info("获取地区的子地区: parentId={}", parentId);
        
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getParentId, parentId)
                .orderBy(true, true, Region::getName);
        
        return regionMapper.selectList(wrapper);
    }
    
    @Override
    public List<Region> getRegionAncestors(Long regionId) {
        log.info("获取地区的祖先地区: regionId={}", regionId);
        
        List<Region> ancestors = new java.util.ArrayList<>();
        Long currentId = regionId;
        
        while (currentId != null) {
            Region region = regionMapper.selectById(currentId);
            if (region == null || region.getParentId() == null) {
                break;
            }
            
            Region parent = regionMapper.selectById(region.getParentId());
            if (parent != null) {
                ancestors.add(0, parent); // 添加到列表开头，保持从根到父级的顺序
                currentId = parent.getParentId();
            } else {
                break;
            }
        }
        
        return ancestors;
    }
    
    @Override
    public IPage<Region> searchRegions(Page<Region> page, String keyword) {
        log.info("搜索地区: keyword={}", keyword);
        
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Region::getName, keyword);
        }
        
        // 按名称排序
        wrapper.orderBy(true, true, Region::getName);
        
        return regionMapper.selectPage(page, wrapper);
    }
    
    /**
     * 构建地区树形结构
     * @param allRegions 所有地区列表
     * @param parentId 父地区ID
     * @return 树形结构
     */
    private List<Region> buildRegionTree(List<Region> allRegions, Long parentId) {
        return allRegions.stream()
                .filter(region -> {
                    if (parentId == null) {
                        return region.getParentId() == null;
                    } else {
                        return parentId.equals(region.getParentId());
                    }
                })
                .peek(region -> {
                    // 递归设置子地区
                    List<Region> children = buildRegionTree(allRegions, region.getId());
                    region.setChildren(children);
                })
                .sorted((r1, r2) -> r1.getName().compareTo(r2.getName()))
                .collect(Collectors.toList());
    }
}