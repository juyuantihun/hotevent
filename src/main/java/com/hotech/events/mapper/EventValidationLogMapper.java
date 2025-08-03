package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.EventValidationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件验证记录Mapper接口
 * 
 * @author Kiro
 */
@Mapper
public interface EventValidationLogMapper extends BaseMapper<EventValidationLog> {
    
    /**
     * 根据事件ID查询验证记录
     * 
     * @param eventId 事件ID
     * @return 验证记录列表
     */
    @Select("SELECT * FROM event_validation_log WHERE event_id = #{eventId} ORDER BY validated_at DESC")
    List<EventValidationLog> findByEventId(@Param("eventId") Long eventId);
    
    /**
     * 根据验证类型查询记录
     * 
     * @param validationType 验证类型
     * @return 验证记录列表
     */
    @Select("SELECT * FROM event_validation_log WHERE validation_type = #{validationType} ORDER BY validated_at DESC")
    List<EventValidationLog> findByValidationType(@Param("validationType") String validationType);
    
    /**
     * 查询指定时间范围内的验证统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN validation_result = 1 THEN 1 ELSE 0 END) as passed_count, " +
            "SUM(CASE WHEN validation_result = 0 THEN 1 ELSE 0 END) as failed_count, " +
            "AVG(credibility_score) as avg_score, " +
            "MAX(credibility_score) as max_score, " +
            "MIN(credibility_score) as min_score " +
            "FROM event_validation_log " +
            "WHERE validated_at BETWEEN #{startTime} AND #{endTime}")
    ValidationStatistics getValidationStatistics(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询最近的验证记录
     * 
     * @param limit 限制数量
     * @return 验证记录列表
     */
    @Select("SELECT * FROM event_validation_log ORDER BY validated_at DESC LIMIT #{limit}")
    List<EventValidationLog> findRecentValidations(@Param("limit") int limit);
    
    /**
     * 删除指定时间之前的验证记录
     * 
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    @Select("DELETE FROM event_validation_log WHERE validated_at < #{beforeTime}")
    int deleteOldValidations(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 验证统计结果内部类
     */
    class ValidationStatistics {
        private Long totalCount;
        private Long passedCount;
        private Long failedCount;
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        
        // Getter和Setter方法
        public Long getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(Long totalCount) {
            this.totalCount = totalCount;
        }
        
        public Long getPassedCount() {
            return passedCount;
        }
        
        public void setPassedCount(Long passedCount) {
            this.passedCount = passedCount;
        }
        
        public Long getFailedCount() {
            return failedCount;
        }
        
        public void setFailedCount(Long failedCount) {
            this.failedCount = failedCount;
        }
        
        public BigDecimal getAvgScore() {
            return avgScore;
        }
        
        public void setAvgScore(BigDecimal avgScore) {
            this.avgScore = avgScore;
        }
        
        public BigDecimal getMaxScore() {
            return maxScore;
        }
        
        public void setMaxScore(BigDecimal maxScore) {
            this.maxScore = maxScore;
        }
        
        public BigDecimal getMinScore() {
            return minScore;
        }
        
        public void setMinScore(BigDecimal minScore) {
            this.minScore = minScore;
        }
    }
}