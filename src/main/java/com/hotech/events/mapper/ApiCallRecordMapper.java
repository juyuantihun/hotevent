package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.ApiCallRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API调用记录Mapper
 */
@Mapper
public interface ApiCallRecordMapper extends BaseMapper<ApiCallRecord> {
    
    /**
     * 获取指定时间范围内的API调用统计
     */
    @Select("SELECT api_type, COUNT(*) as call_count, " +
            "SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "AVG(response_time) as avg_response_time, " +
            "SUM(COALESCE(token_usage, 0)) as total_tokens " +
            "FROM api_call_record " +
            "WHERE call_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY api_type")
    List<Map<String, Object>> getApiStatsByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取API调用成功率统计
     */
    @Select("SELECT api_type, " +
            "COUNT(*) as total_calls, " +
            "SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_calls, " +
            "ROUND(SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM api_call_record " +
            "WHERE call_time >= #{since} " +
            "GROUP BY api_type")
    List<Map<String, Object>> getSuccessRateStats(@Param("since") LocalDateTime since);
    
    /**
     * 获取最近的API调用记录
     */
    @Select("SELECT * FROM api_call_record " +
            "ORDER BY call_time DESC " +
            "LIMIT #{limit}")
    List<ApiCallRecord> getRecentCalls(@Param("limit") int limit);
    
    /**
     * 获取API性能统计
     */
    @Select("SELECT api_type, " +
            "AVG(response_time) as avg_response_time, " +
            "MIN(response_time) as min_response_time, " +
            "MAX(response_time) as max_response_time, " +
            "PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY response_time) as p95_response_time " +
            "FROM api_call_record " +
            "WHERE call_time >= #{since} AND response_status = 'SUCCESS' " +
            "GROUP BY api_type")
    List<Map<String, Object>> getPerformanceStats(@Param("since") LocalDateTime since);
    
    /**
     * 清理过期记录
     */
    @Select("DELETE FROM api_call_record WHERE call_time < #{beforeTime}")
    int cleanupOldRecords(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 获取错误统计
     */
    @Select("SELECT api_type, error_message, COUNT(*) as error_count " +
            "FROM api_call_record " +
            "WHERE call_time >= #{since} AND response_status != 'SUCCESS' " +
            "GROUP BY api_type, error_message " +
            "ORDER BY error_count DESC")
    List<Map<String, Object>> getErrorStats(@Param("since") LocalDateTime since);
    
    /**
     * 获取按小时分组的统计数据
     */
    @Select("SELECT " +
            "DATE_FORMAT(call_time, '%Y-%m-%d %H:00:00') as hour, " +
            "api_type, " +
            "COUNT(*) as call_count, " +
            "SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "ROUND(SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate, " +
            "AVG(response_time) as avg_response_time, " +
            "SUM(COALESCE(token_usage, 0)) as total_tokens " +
            "FROM api_call_record " +
            "WHERE call_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(call_time, '%Y-%m-%d %H:00:00'), api_type " +
            "ORDER BY hour, api_type")
    List<Map<String, Object>> getHourlyStats(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
}