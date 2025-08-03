package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.TimelineCreationCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间线创建缓存Mapper
 */
@Mapper
public interface TimelineCreationCacheMapper extends BaseMapper<TimelineCreationCache> {
    
    /**
     * 根据请求指纹查找缓存记录
     * @param requestFingerprint 请求指纹
     * @return 缓存记录
     */
    @Select("SELECT * FROM timeline_creation_cache WHERE request_fingerprint = #{requestFingerprint} AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    TimelineCreationCache findByRequestFingerprint(@Param("requestFingerprint") String requestFingerprint);
    
    /**
     * 根据用户ID和时间窗口查找最近的创建记录
     * @param userId 用户ID
     * @param timeWindow 时间窗口（分钟）
     * @return 缓存记录列表
     */
    @Select("SELECT * FROM timeline_creation_cache WHERE user_id = #{userId} AND created_at >= DATE_SUB(NOW(), INTERVAL #{timeWindow} MINUTE) ORDER BY created_at DESC")
    List<TimelineCreationCache> findRecentByUserId(@Param("userId") String userId, @Param("timeWindow") int timeWindow);
    
    /**
     * 根据名称和时间范围查找相似的时间线
     * @param timelineName 时间线名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 缓存记录
     */
    @Select("SELECT * FROM timeline_creation_cache WHERE timeline_name = #{timelineName} AND start_time = #{startTime} AND end_time = #{endTime} AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    TimelineCreationCache findByNameAndTimeRange(@Param("timelineName") String timelineName, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    /**
     * 更新缓存记录状态
     * @param id 记录ID
     * @param status 新状态
     * @param timelineId 时间线ID
     * @return 更新行数
     */
    @Update("UPDATE timeline_creation_cache SET status = #{status}, timeline_id = #{timelineId}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("timelineId") Long timelineId);
    
    /**
     * 清理过期的缓存记录
     * @return 清理的记录数
     */
    @Update("DELETE FROM timeline_creation_cache WHERE expires_at < NOW()")
    int cleanExpiredRecords();
    
    /**
     * 根据时间线ID查找缓存记录
     * @param timelineId 时间线ID
     * @return 缓存记录
     */
    @Select("SELECT * FROM timeline_creation_cache WHERE timeline_id = #{timelineId} ORDER BY created_at DESC LIMIT 1")
    TimelineCreationCache findByTimelineId(@Param("timelineId") Long timelineId);
}