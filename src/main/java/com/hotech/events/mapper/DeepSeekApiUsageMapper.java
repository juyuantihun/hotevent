package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.DeepSeekApiUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API使用记录Mapper
 */
@Mapper
public interface DeepSeekApiUsageMapper extends BaseMapper<DeepSeekApiUsage> {
    
    /**
     * 获取API使用统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as totalRequests, " +
            "SUM(CASE WHEN response_status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulRequests, " +
            "SUM(CASE WHEN response_status = 'FAILED' THEN 1 ELSE 0 END) as failedRequests, " +
            "AVG(response_time_ms) as averageResponseTime, " +
            "SUM(COALESCE(token_usage, 0)) as totalTokenUsage " +
            "FROM deepseek_api_usage " +
            "WHERE created_at BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getUsageStatistics(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取今日使用统计
     * 
     * @param today 今日日期
     * @return 今日统计
     */
    @Select("SELECT " +
            "COUNT(*) as todayRequests, " +
            "SUM(COALESCE(token_usage, 0)) as todayTokenUsage " +
            "FROM deepseek_api_usage " +
            "WHERE DATE(created_at) = DATE(#{today})")
    Map<String, Object> getTodayUsageStatistics(@Param("today") LocalDateTime today);
    
    /**
     * 获取请求类型统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 请求类型统计列表
     */
    @Select("SELECT " +
            "request_type, " +
            "COUNT(*) as requestCount, " +
            "AVG(response_time_ms) as avgResponseTime " +
            "FROM deepseek_api_usage " +
            "WHERE created_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY request_type " +
            "ORDER BY requestCount DESC")
    List<Map<String, Object>> getRequestTypeStatistics(@Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * 清理过期记录
     * 
     * @param beforeTime 清理此时间之前的记录
     * @return 清理的记录数
     */
    @Select("DELETE FROM deepseek_api_usage WHERE created_at < #{beforeTime}")
    int cleanupOldRecords(@Param("beforeTime") LocalDateTime beforeTime);
}