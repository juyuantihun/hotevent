package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.TimelineEvent;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时间线事件关联Mapper接口
 * 提供时间线与事件关联关系的数据库操作
 */
@Mapper
public interface TimelineEventMapper extends BaseMapper<TimelineEvent> {
    
    /**
     * 查询时间线关联的事件列表（包含事件详细信息）
     * @param timelineId 时间线ID
     * @return 事件列表
     */
    @Select("SELECT e.id, e.event_description, e.event_time, e.event_location, " +
            "e.source_type, e.intensity_level, e.created_at, e.updated_at, " +
            "te.created_at as relation_created_at FROM event e " +
            "JOIN timeline_event te ON e.id = te.event_id " +
            "WHERE te.timeline_id = #{timelineId} " +
            "ORDER BY e.event_time ASC")
    List<Map<String, Object>> findEventsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 分页查询时间线关联的事件列表（包含事件详细信息）
     * @param timelineId 时间线ID
     * @param page 分页参数
     * @param includeDetails 是否包含详细信息
     * @return 事件分页列表
     */
    @Select("<script>" +
            "SELECT " +
            "<if test='includeDetails'>" +
            "e.*, te.created_at as relation_created_at " +
            "</if>" +
            "<if test='!includeDetails'>" +
            "e.id, e.event_description, e.event_time, e.event_location, " +
            "e.source_type, e.intensity_level, te.created_at as relation_created_at " +
            "</if>" +
            "FROM event e " +
            "JOIN timeline_event te ON e.id = te.event_id " +
            "WHERE te.timeline_id = #{timelineId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (e.event_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.event_location LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.subject LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.object LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='nodeType != null and nodeType != \"\"'>" +
            "AND e.node_type = #{nodeType} " +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"title\"'>e.event_description</when>" +
            "<when test='sortBy == \"location\"'>e.event_location</when>" +
            "<when test='sortBy == \"nodeType\"'>e.node_type</when>" +
            "<when test='sortBy == \"importanceScore\"'>e.intensity_level</when>" +
            "<otherwise>e.event_time</otherwise>" +
            "</choose>" +
            "<choose>" +
            "<when test='sortOrder == \"desc\"'> DESC</when>" +
            "<otherwise> ASC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<Map<String, Object>> findEventsByTimelineIdWithPagination(
            @Param("timelineId") Long timelineId, 
            Page<Map<String, Object>> page, 
            @Param("includeDetails") Boolean includeDetails,
            @Param("keyword") String keyword,
            @Param("nodeType") String nodeType,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder);
    
    /**
     * 查询时间线关联的事件ID列表
     * @param timelineId 时间线ID
     * @return 事件ID列表
     */
    @Select("SELECT event_id FROM timeline_event WHERE timeline_id = #{timelineId} ORDER BY created_at ASC")
    List<Long> findEventIdsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 查询事件关联的时间线ID列表
     * @param eventId 事件ID
     * @return 时间线ID列表
     */
    @Select("SELECT timeline_id FROM timeline_event WHERE event_id = #{eventId}")
    List<Long> findTimelineIdsByEventId(@Param("eventId") Long eventId);
    
    /**
     * 检查时间线和事件的关联关系是否存在
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @return 关联记录数量
     */
    @Select("SELECT COUNT(*) FROM timeline_event WHERE timeline_id = #{timelineId} AND event_id = #{eventId}")
    int countByTimelineIdAndEventId(@Param("timelineId") Long timelineId, @Param("eventId") Long eventId);
    
    /**
     * 批量插入时间线事件关联
     * @param timelineId 时间线ID
     * @param eventIds 事件ID列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO timeline_event (timeline_id, event_id, created_at) VALUES " +
            "<foreach collection='eventIds' item='eventId' separator=','>" +
            "(#{timelineId}, #{eventId}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("timelineId") Long timelineId, @Param("eventIds") List<Long> eventIds);
    
    /**
     * 批量删除时间线事件关联
     * @param timelineId 时间线ID
     * @param eventIds 事件ID列表
     * @return 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM timeline_event WHERE timeline_id = #{timelineId} " +
            "AND event_id IN " +
            "<foreach collection='eventIds' item='eventId' open='(' separator=',' close=')'>" +
            "#{eventId}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("timelineId") Long timelineId, @Param("eventIds") List<Long> eventIds);
    
    /**
     * 删除时间线的所有事件关联
     * @param timelineId 时间线ID
     * @return 影响行数
     */
    @Delete("DELETE FROM timeline_event WHERE timeline_id = #{timelineId}")
    int deleteByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 统计时间线的事件数量
     * @param timelineId 时间线ID
     * @return 事件数量
     */
    @Select("SELECT COUNT(*) FROM timeline_event WHERE timeline_id = #{timelineId}")
    int countEventsByTimelineId(@Param("timelineId") Long timelineId);
    
    /**
     * 查找无效的事件关联（事件不存在）
     * @return 无效关联列表
     */
    @Select("SELECT te.id, te.timeline_id, te.event_id FROM timeline_event te " +
            "LEFT JOIN event e ON te.event_id = e.id " +
            "WHERE e.id IS NULL")
    List<Map<String, Object>> findInvalidEventAssociations();
    
    /**
     * 查找无效的时间线关联（时间线不存在）
     * @return 无效关联列表
     */
    @Select("SELECT te.id, te.timeline_id, te.event_id FROM timeline_event te " +
            "LEFT JOIN timeline t ON te.timeline_id = t.id " +
            "WHERE t.id IS NULL")
    List<Map<String, Object>> findInvalidTimelineAssociations();
    
    /**
     * 查找重复的时间线事件关联
     * @return 重复关联列表
     */
    @Select("SELECT timeline_id, event_id, COUNT(*) as count " +
            "FROM timeline_event " +
            "GROUP BY timeline_id, event_id " +
            "HAVING COUNT(*) > 1")
    List<Map<String, Object>> findDuplicateAssociations();
    
    /**
     * 统计每个时间线的实际事件数量
     * @return 时间线事件数量统计
     */
    @Select("SELECT timeline_id, COUNT(*) as actual_count " +
            "FROM timeline_event " +
            "GROUP BY timeline_id")
    List<Map<String, Object>> countEventsByTimelines();
    
    /**
     * 查找没有任何事件关联的时间线
     * @return 空时间线列表
     */
    @Select("SELECT t.id, t.name, t.event_count FROM timeline t " +
            "LEFT JOIN timeline_event te ON t.id = te.timeline_id " +
            "WHERE te.timeline_id IS NULL AND t.event_count > 0")
    List<Map<String, Object>> findTimelinesWithoutEvents();
}