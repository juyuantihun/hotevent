package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.RegionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 地区项目Mapper接口
 */
@Mapper
public interface RegionItemMapper extends BaseMapper<RegionItem> {
    
    /**
     * 查询地区包含的字典项
     * @param regionId 地区ID
     * @return 字典项列表
     */
    @Select("SELECT d.* FROM region_item ri " +
            "JOIN dictionary d ON ri.dictionary_id = d.id " +
            "WHERE ri.region_id = #{regionId}")
    List<Map<String, Object>> findDictionaryItemsByRegionId(@Param("regionId") Long regionId);
    
    /**
     * 查询地区包含的字典项ID列表
     * @param regionId 地区ID
     * @return 字典项ID列表
     */
    @Select("SELECT dictionary_id FROM region_item WHERE region_id = #{regionId}")
    List<Long> findDictionaryIdsByRegionId(@Param("regionId") Long regionId);
}