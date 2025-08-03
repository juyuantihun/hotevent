package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.GeographicCoordinate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地理坐标数据访问层
 */
@Mapper
public interface GeographicCoordinateMapper extends BaseMapper<GeographicCoordinate> {
    
    /**
     * 根据地点名称查找坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE location_name = #{locationName} AND is_deleted = 0 LIMIT 1")
    GeographicCoordinate findByLocationName(@Param("locationName") String locationName);
    
    /**
     * 根据地点名称和类型查找坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE location_name = #{locationName} AND location_type = #{locationType} AND is_deleted = 0 LIMIT 1")
    GeographicCoordinate findByLocationNameAndType(@Param("locationName") String locationName, 
                                                  @Param("locationType") String locationType);
    
    /**
     * 查找所有国家首都坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE location_type = 'COUNTRY' AND is_default = 1 AND is_deleted = 0")
    List<GeographicCoordinate> findAllCapitals();
    
    /**
     * 查找所有地区首府坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE location_type = 'REGION' AND is_default = 1 AND is_deleted = 0")
    List<GeographicCoordinate> findAllRegionCapitals();
    
    /**
     * 根据国家代码查找坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE country_code = #{countryCode} AND is_deleted = 0")
    List<GeographicCoordinate> findByCountryCode(@Param("countryCode") String countryCode);
    
    /**
     * 模糊查找地点名称
     */
    @Select("SELECT * FROM geographic_coordinates WHERE location_name LIKE CONCAT('%', #{keyword}, '%') AND is_deleted = 0 LIMIT 10")
    List<GeographicCoordinate> findByLocationNameLike(@Param("keyword") String keyword);
    
    /**
     * 查找指定范围内的坐标
     */
    @Select("SELECT * FROM geographic_coordinates WHERE latitude BETWEEN #{minLat} AND #{maxLat} " +
            "AND longitude BETWEEN #{minLon} AND #{maxLon} AND is_deleted = 0")
    List<GeographicCoordinate> findByCoordinateRange(@Param("minLat") Double minLat, 
                                                    @Param("maxLat") Double maxLat,
                                                    @Param("minLon") Double minLon, 
                                                    @Param("maxLon") Double maxLon);
    
    /**
     * 统计坐标数据总数
     */
    @Select("SELECT COUNT(*) FROM geographic_coordinates WHERE is_deleted = 0")
    Long countTotal();
    
    /**
     * 按类型统计坐标数据
     */
    @Select("SELECT location_type, COUNT(*) as count FROM geographic_coordinates WHERE is_deleted = 0 GROUP BY location_type")
    List<java.util.Map<String, Object>> countByType();
}