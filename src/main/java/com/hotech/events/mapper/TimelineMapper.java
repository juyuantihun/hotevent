package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.Timeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时间线Mapper接口
 * 提供时间线相关的数据库操作
 */
@Mapper
public interface TimelineMapper extends BaseMapper<Timeline> {
    
    /**
     * 更新时间线状态
     * @param id 时间线ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE timeline SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 更新时间线进度
     * @param id 时间线ID
     * @param eventCount 事件数量
     * @param relationCount 关系数量
     * @return 影响行数
     */
    @Update("UPDATE timeline SET event_count = #{eventCount}, relation_count = #{relationCount}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateProgress(@Param("id") Long id, @Param("eventCount") Integer eventCount, 
                      @Param("relationCount") Integer relationCount);
    
    /**
     * 根据地区ID查询时间线列表
     * @param regionId 地区ID
     * @return 时间线列表
     */
    @Select("SELECT t.* FROM timeline t " +
            "JOIN timeline_region tr ON t.id = tr.timeline_id " +
            "WHERE tr.region_id = #{regionId} " +
            "ORDER BY t.created_at DESC")
    List<Timeline> findByRegionId(@Param("regionId") Long regionId);
    
    /**
     * 根据状态查询时间线列表
     * @param status 状态
     * @return 时间线列表
     */
    @Select("SELECT * FROM timeline WHERE status = #{status} ORDER BY created_at DESC")
    List<Timeline> findByStatus(@Param("status") String status);
    
    /**
     * 根据时间范围查询时间线列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间线列表
     */
    @Select("SELECT * FROM timeline WHERE " +
            "(start_time >= #{startTime} OR end_time <= #{endTime}) " +
            "ORDER BY created_at DESC")
    List<Timeline> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询时间线详情，包括地区和事件数量
     * @param id 时间线ID
     * @return 时间线详情
     */
    @Select("SELECT t.*, " +
            "(SELECT COUNT(*) FROM timeline_region WHERE timeline_id = t.id) AS region_count, " +
            "(SELECT COUNT(*) FROM timeline_event WHERE timeline_id = t.id) AS actual_event_count " +
            "FROM timeline t WHERE t.id = #{id}")
    Map<String, Object> findTimelineDetail(@Param("id") Long id);
    
    /**
     * 分页查询时间线列表
     * @param page 分页参数
     * @param name 时间线名称（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM timeline WHERE 1=1 " +
            "<if test='name != null and name != \"\"'>" +
            "AND name LIKE CONCAT('%', #{name}, '%') " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Timeline> selectTimelinePage(Page<Timeline> page, 
                                     @Param("name") String name, 
                                     @Param("status") String status);
}