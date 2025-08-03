package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.EventParsingRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件解析记录Mapper
 * 
 * @author Kiro
 */
@Mapper
public interface EventParsingRecordMapper extends BaseMapper<EventParsingRecord> {
    
    /**
     * 根据解析状态查询记录
     * 
     * @param status 解析状态
     * @return 解析记录列表
     */
    @Select("SELECT * FROM event_parsing_record WHERE parsing_status = #{status} ORDER BY parse_time DESC")
    List<EventParsingRecord> findByParsingStatus(@Param("status") String status);
    
    /**
     * 根据API类型查询记录
     * 
     * @param apiType API类型
     * @return 解析记录列表
     */
    @Select("SELECT * FROM event_parsing_record WHERE api_type = #{apiType} ORDER BY parse_time DESC")
    List<EventParsingRecord> findByApiType(@Param("apiType") String apiType);
    
    /**
     * 根据时间范围查询记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 解析记录列表
     */
    @Select("SELECT * FROM event_parsing_record WHERE parse_time BETWEEN #{startTime} AND #{endTime} ORDER BY parse_time DESC")
    List<EventParsingRecord> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计解析成功率
     * 
     * @param apiType API类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 成功率统计
     */
    @Select("SELECT COUNT(*) as total_count, " +
            "SUM(CASE WHEN parsing_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "AVG(CASE WHEN parsing_status = 'SUCCESS' THEN parsed_event_count ELSE 0 END) as avg_event_count " +
            "FROM event_parsing_record WHERE 1=1 " +
            "AND (#{apiType} IS NULL OR api_type = #{apiType}) " +
            "AND (#{startTime} IS NULL OR parse_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR parse_time <= #{endTime})")
    java.util.Map<String, Object> getParsingStats(@Param("apiType") String apiType,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 清理旧的解析记录
     * 
     * @param cutoffTime 截止时间
     * @return 清理的记录数
     */
    @Delete("DELETE FROM event_parsing_record WHERE created_at < #{cutoffTime}")
    int deleteOldRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
}