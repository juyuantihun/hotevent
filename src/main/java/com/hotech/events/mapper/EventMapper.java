package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件Mapper接口
 */
@Mapper
public interface EventMapper extends BaseMapper<Event> {
    
    /**
     * 根据条件查询事件
     * @param regionNames 地区名称列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    @Select({
        "<script>",
        "SELECT e.* FROM event e",
        "<where>",
        "   <if test='startTime != null'>",
        "       AND e.event_time &gt;= #{startTime}",
        "   </if>",
        "   <if test='endTime != null'>",
        "       AND e.event_time &lt;= #{endTime}",
        "   </if>",
        "   <if test='regionNames != null and regionNames.size() > 0'>",
        "       AND (",
        "           <foreach collection='regionNames' item='region' separator=' OR '>",
        "               e.event_location LIKE CONCAT('%', #{region}, '%')",
        "           </foreach>",
        "       )",
        "   </if>",
        "   AND e.status = 1",
        "</where>",
        "ORDER BY e.event_time DESC",
        "LIMIT 50",
        "</script>"
    })
    List<Map<String, Object>> findEventsByConditions(
            @Param("regionNames") List<String> regionNames,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
            
    /**
     * 查询所有事件
     * @return 事件列表
     */
    @Select("SELECT * FROM event WHERE status = 1 ORDER BY event_time DESC LIMIT 50")
    List<Map<String, Object>> findAllEvents();
    
    /**
     * 根据标题相似度查找事件
     * @param title 标题
     * @return 相似事件列表
     */
    @Select("SELECT * FROM event WHERE event_title LIKE CONCAT('%', #{title}, '%') AND status = 1 LIMIT 10")
    List<Event> findByTitleSimilar(@Param("title") String title);
    
    /**
     * 根据关键字段查找事件
     * @param subject 主体
     * @param object 客体
     * @param eventType 事件类型
     * @return 匹配的事件列表
     */
    @Select("SELECT * FROM event WHERE subject = #{subject} AND object = #{object} AND event_type = #{eventType} AND status = 1 LIMIT 10")
    List<Event> findByKeyFields(@Param("subject") String subject, 
                               @Param("object") String object, 
                               @Param("eventType") String eventType);
    
    /**
     * 根据时间和地点查找事件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param location 地点
     * @return 匹配的事件列表
     */
    @Select("SELECT * FROM event WHERE event_time BETWEEN #{startTime} AND #{endTime} AND event_location LIKE CONCAT('%', #{location}, '%') AND status = 1 LIMIT 10")
    List<Event> findByTimeAndLocation(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime, 
                                     @Param("location") String location);
    
    /**
     * 删除旧的验证记录
     * @param cutoffTime 截止时间
     * @return 删除的记录数
     */
    @Select("DELETE FROM event_validation_log WHERE validated_at < #{cutoffTime}")
    int deleteOldValidationRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 统计各种状态的事件数量
     * @return 状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM event GROUP BY status")
    List<Map<String, Object>> countEventsByStatus();
    
    /**
     * 查找禁用状态的事件
     * @return 禁用事件列表
     */
    @Select("SELECT id, event_code, event_title, event_description FROM event WHERE status = 0")
    List<Map<String, Object>> findDisabledEvents();
    
    /**
     * 查找状态为空的事件
     * @return 状态为空的事件列表
     */
    @Select("SELECT id, event_code, event_title, event_description FROM event WHERE status IS NULL")
    List<Map<String, Object>> findEventsWithNullStatus();
    
    /**
     * 查找没有关联到任何时间线的事件
     * @return 孤立事件列表
     */
    @Select("SELECT e.id, e.event_code, e.event_title, e.event_description " +
            "FROM event e " +
            "LEFT JOIN timeline_event te ON e.id = te.event_id " +
            "WHERE te.event_id IS NULL")
    List<Map<String, Object>> findOrphanedEvents();
    
    /**
     * 根据状态查询事件数量
     * @param status 事件状态
     * @return 事件数量
     */
    @Select("SELECT COUNT(*) FROM event WHERE status = #{status}")
    int countEventsByStatus(@Param("status") Integer status);
    
    /**
     * 查找创建时间异常的事件（创建时间为空或未来时间）
     * @return 异常事件列表
     */
    @Select("SELECT id, event_code, event_title, created_at " +
            "FROM event " +
            "WHERE created_at IS NULL OR created_at > NOW()")
    List<Map<String, Object>> findEventsWithAbnormalCreationTime();
    
    /**
     * 查找事件时间异常的事件（事件时间为空或未来很久的时间）
     * @return 异常事件列表
     */
    @Select("SELECT id, event_code, event_title, event_time " +
            "FROM event " +
            "WHERE event_time IS NULL OR event_time > DATE_ADD(NOW(), INTERVAL 1 YEAR)")
    List<Map<String, Object>> findEventsWithAbnormalEventTime();
}