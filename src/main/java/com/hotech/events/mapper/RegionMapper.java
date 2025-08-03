package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区Mapper接口
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {
    
    /**
     * 查询被时间线引用的地区ID列表
     * @return 地区ID列表
     */
    @Select("SELECT DISTINCT region_id FROM timeline_region")
    List<Long> findReferencedRegionIds();
    
    /**
     * 根据名称模糊查询地区
     * @param name 地区名称
     * @return 地区列表
     */
    @Select("SELECT id, name, type, created_at, updated_at FROM region WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Region> findByNameLike(@Param("name") String name);
}