package com.hotech.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间段模型
 * 用于时间段分割处理
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSegment {
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 段索引
     */
    private int segmentIndex;
    
    /**
     * 段ID
     */
    private String segmentId;
    
    /**
     * 预期事件数量
     */
    private int expectedEventCount;
    
    /**
     * 实际事件数量
     */
    private int actualEventCount;
    
    /**
     * 处理状态
     */
    private SegmentStatus status;
    
    /**
     * 处理开始时间
     */
    private LocalDateTime processingStartTime;
    
    /**
     * 处理结束时间
     */
    private LocalDateTime processingEndTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 构造函数 - 基本信息
     */
    public TimeSegment(LocalDateTime startTime, LocalDateTime endTime, int segmentIndex, String segmentId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.segmentIndex = segmentIndex;
        this.segmentId = segmentId;
        this.status = SegmentStatus.PENDING;
        this.expectedEventCount = 0;
        this.actualEventCount = 0;
    }
    
    /**
     * 构造函数 - 包含预期事件数量
     */
    public TimeSegment(LocalDateTime startTime, LocalDateTime endTime, int segmentIndex, 
                      String segmentId, int expectedEventCount) {
        this(startTime, endTime, segmentIndex, segmentId);
        this.expectedEventCount = expectedEventCount;
    }
    
    /**
     * 获取时间段持续时间（天数）
     */
    public long getDurationInDays() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startTime, endTime);
    }
    
    /**
     * 获取时间段持续时间（小时数）
     */
    public long getDurationInHours() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startTime, endTime);
    }
    
    /**
     * 获取处理耗时（毫秒）
     */
    public long getProcessingDurationMs() {
        if (processingStartTime == null || processingEndTime == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(processingStartTime, processingEndTime);
    }
    
    /**
     * 检查时间段是否有效
     */
    public boolean isValid() {
        return startTime != null && endTime != null && 
               startTime.isBefore(endTime) && 
               segmentId != null && !segmentId.trim().isEmpty();
    }
    
    /**
     * 检查时间段是否重叠
     */
    public boolean overlapsWith(TimeSegment other) {
        if (other == null || !this.isValid() || !other.isValid()) {
            return false;
        }
        
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
    
    /**
     * 开始处理
     */
    public void startProcessing() {
        this.status = SegmentStatus.PROCESSING;
        this.processingStartTime = LocalDateTime.now();
        this.errorMessage = null;
    }
    
    /**
     * 完成处理
     */
    public void completeProcessing(int actualEventCount) {
        this.status = SegmentStatus.COMPLETED;
        this.processingEndTime = LocalDateTime.now();
        this.actualEventCount = actualEventCount;
    }
    
    /**
     * 处理失败
     */
    public void failProcessing(String errorMessage) {
        this.status = SegmentStatus.FAILED;
        this.processingEndTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
    
    /**
     * 重置处理状态
     */
    public void resetProcessing() {
        this.status = SegmentStatus.PENDING;
        this.processingStartTime = null;
        this.processingEndTime = null;
        this.actualEventCount = 0;
        this.errorMessage = null;
    }
    
    /**
     * 获取格式化的时间范围字符串
     */
    public String getFormattedTimeRange() {
        if (startTime == null || endTime == null) {
            return "无效时间范围";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("%s 至 %s", 
                           startTime.format(formatter), 
                           endTime.format(formatter));
    }
    
    /**
     * 获取处理进度百分比
     */
    public double getProgressPercentage() {
        if (expectedEventCount <= 0) {
            return status == SegmentStatus.COMPLETED ? 100.0 : 0.0;
        }
        
        return Math.min(100.0, (double) actualEventCount / expectedEventCount * 100.0);
    }
    
    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        return String.format("TimeSegment{id='%s', index=%d, range='%s', status=%s, events=%d/%d}",
                           segmentId, segmentIndex, getFormattedTimeRange(), 
                           status, actualEventCount, expectedEventCount);
    }
    
    /**
     * 创建子时间段
     */
    public TimeSegment createSubSegment(LocalDateTime subStart, LocalDateTime subEnd, int subIndex) {
        String subSegmentId = this.segmentId + "-sub-" + subIndex;
        return new TimeSegment(subStart, subEnd, subIndex, subSegmentId);
    }
    
    /**
     * 时间段处理状态枚举
     */
    public enum SegmentStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("处理失败"),
        CANCELLED("已取消");
        
        private final String description;
        
        SegmentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}