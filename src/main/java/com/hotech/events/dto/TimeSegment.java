package com.hotech.events.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间段数据模型
 * 用于时间线生成时的时间段分割处理
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSegment {
    
    /**
     * 时间段开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 时间段结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 时间段索引（从0开始）
     */
    private int segmentIndex;
    
    /**
     * 时间段唯一标识符
     */
    private String segmentId;
    
    /**
     * 预期事件数量
     */
    private int expectedEventCount;
    
    /**
     * 时间段跨度（天数）
     */
    private long spanDays;
    
    /**
     * 是否为最后一个时间段
     */
    private boolean isLastSegment;
    
    /**
     * 计算时间段跨度天数
     * 
     * @return 跨度天数
     */
    public long calculateSpanDays() {
        if (startTime != null && endTime != null) {
            this.spanDays = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;
        }
        return this.spanDays;
    }
    
    /**
     * 验证时间段是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return startTime != null && 
               endTime != null && 
               !startTime.isAfter(endTime) &&
               segmentId != null && 
               !segmentId.trim().isEmpty();
    }
    
    /**
     * 获取时间段描述
     * 
     * @return 时间段描述
     */
    public String getDescription() {
        return String.format("时间段[%d]: %s 至 %s (跨度: %d天)", 
                           segmentIndex, 
                           startTime != null ? startTime.toString() : "未设置",
                           endTime != null ? endTime.toString() : "未设置",
                           calculateSpanDays());
    }
    
    /**
     * 检查时间段是否包含指定时间点
     * 
     * @param dateTime 指定时间点
     * @return 是否包含
     */
    public boolean contains(LocalDateTime dateTime) {
        if (dateTime == null || startTime == null || endTime == null) {
            return false;
        }
        return !dateTime.isBefore(startTime) && !dateTime.isAfter(endTime);
    }
    
    /**
     * 检查与另一个时间段是否有重叠
     * 
     * @param other 另一个时间段
     * @return 是否有重叠
     */
    public boolean overlaps(TimeSegment other) {
        if (other == null || !this.isValid() || !other.isValid()) {
            return false;
        }
        
        return !this.endTime.isBefore(other.startTime) && 
               !this.startTime.isAfter(other.endTime);
    }
}