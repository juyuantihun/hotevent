package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.TimelineRegion;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 时间线地区关联Mapper接口
 * 提供时间线与地区关联关系的数据库操作
 */
@Mapper
public interface TimelineRegionMapper extends BaseMapper<TimelineRegion> {
    
    /**
     * 查询时间线关联的地区列表（包含地区详细信息）
     * @param timelineId 时间线ID
     * @return 地区列表
     */
    @Select("SELECT r.*, tr.created_at as relation_created_at FROM region r " +
            "JOIN timeline_region tr ON r.id = tr.region_id " +
            "WHERE tr.timeline_id = #{timelineId} " +
            "ORDER BY tr.created_at ASC")
    List<Map<String, Object>> findRegionsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 查询时间线关联的地区ID列表
     * @param timelineId 时间线ID
     * @return 地区ID列表
     */
    @Select("SELECT region_id FROM timeline_region WHERE timeline_id = #{timelineId} ORDER BY created_at ASC")
    List<Long> findRegionIdsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 查询地区关联的时间线ID列表
     * @param regionId 地区ID
     * @return 时间线ID列表
     */
    @Select("SELECT timeline_id FROM timeline_region WHERE region_id = #{regionId}")
    List<Long> findTimelineIdsByRegionId(@Param("regionId") Long regionId);
    
    /**
     * 检查时间线和地区的关联关系是否存在
     * @param timelineId 时间线ID
     * @param regionId 地区ID
     * @return 关联记录数量
     */
    @Select("SELECT COUNT(*) FROM timeline_region WHERE timeline_id = #{timelineId} AND region_id = #{regionId}")
    int countByTimelineIdAndRegionId(@Param("timelineId") Long timelineId, @Param("regionId") Long regionId);
    
    /**
     * 批量插入时间线地区关联
     * @param timelineId 时间线ID
     * @param regionIds 地区ID列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO timeline_region (timeline_id, region_id, created_at) VALUES " +
            "<foreach collection='regionIds' item='regionId' separator=','>" +
            "(#{timelineId}, #{regionId}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("timelineId") Long timelineId, @Param("regionIds") List<Long> regionIds);
    
    /**
     * 批量删除时间线地区关联
     * @param timelineId 时间线ID
     * @param regionIds 地区ID列表
     * @return 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM timeline_region WHERE timeline_id = #{timelineId} " +
            "AND region_id IN " +
            "<foreach collection='regionIds' item='regionId' open='(' separator=',' close=')'>" +
            "#{regionId}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("timelineId") Long timelineId, @Param("regionIds") List<Long> regionIds);
    
    /**
     * 删除时间线的所有地区关联
     * @param timelineId 时间线ID
     * @return 影响行数
     */
    @Delete("DELETE FROM timeline_region WHERE timeline_id = #{timelineId}")
    int deleteByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 统计时间线的地区数量
     * @param timelineId 时间线ID
     * @return 地区数量
     */
    @Select("SELECT COUNT(*) FROM timeline_region WHERE timeline_id = #{timelineId}")
    int countRegionsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 查找无效的地区关联（地区不存在）
     * @return 无效关联列表
     */
    @Select("SELECT tr.id, tr.timeline_id, tr.region_id FROM timeline_region tr " +
            "LEFT JOIN region r ON tr.region_id = r.id " +
            "WHERE r.id IS NULL")
    List<Map<String, Object>> findInvalidRegionAssociations();
    
    /**
     * 查找无效的时间线地区关联（时间线不存在）
     * @return 无效关联列表
     */
    @Select("SELECT tr.id, tr.timeline_id, tr.region_id FROM timeline_region tr " +
            "LEFT JOIN timeline t ON tr.timeline_id = t.id " +
            "WHERE t.id IS NULL")
    List<Map<String, Object>> findInvalidTimelineRegionAssociations();
    
    /**
     * 查找重复的时间线地区关联
     * @return 重复关联列表
     */
    @Select("SELECT timeline_id, region_id, COUNT(*) as count " +
            "FROM timeline_region " +
            "GROUP BY timeline_id, region_id " +
            "HAVING COUNT(*) > 1")
    List<Map<String, Object>> findDuplicateRegionAssociations();
    
    /**
     * 统计每个时间线的实际地区数量
     * @return 时间线地区数量统计
     */
    @Select("SELECT timeline_id, COUNT(*) as actual_count " +
            "FROM timeline_region " +
            "GROUP BY timeline_id")
    List<Map<String, Object>> countRegionsByTimelines();
    
    /**
     * 查找没有任何地区关联的时间线
     * @return 没有地区关联的时间线列表
     */
    @Select("SELECT t.id, t.name FROM timeline t " +
            "LEFT JOIN timeline_region tr ON t.id = tr.timeline_id " +
            "WHERE tr.timeline_id IS NULL")
    List<Map<String, Object>> findTimelinesWithoutRegions();
}