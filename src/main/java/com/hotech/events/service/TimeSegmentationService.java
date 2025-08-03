package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimeSegment;
import com.hotech.events.dto.TimelineGenerateRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间段分割服务接口
 * 负责将大时间跨度分割成多个子时间段，并管理批量API调用
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface TimeSegmentationService {
    
    /**
     * 分割时间段
     * 根据配置的最大时间跨度将大时间范围分割成多个子时间段
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param maxSpanDays 最大时间跨度（天）
     * @return 时间段列表
     */
    List<TimeSegment> segmentTimeRange(LocalDateTime startTime, LocalDateTime endTime, int maxSpanDays);
    
    /**
     * 智能分割时间段
     * 根据时间跨度和预期事件数量进行智能分割
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param maxSpanDays 最大时间跨度（天）
     * @param expectedEventsPerDay 每天预期事件数量
     * @param maxEventsPerSegment 每个时间段最大事件数量
     * @return 时间段列表
     */
    List<TimeSegment> intelligentSegmentTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                 int maxSpanDays, int expectedEventsPerDay, 
                                                 int maxEventsPerSegment);
    
    /**
     * 批量获取事件
     * 对多个时间段并发调用API获取事件，并合并结果
     * 
     * @param segments 时间段列表
     * @param request 时间线生成请求
     * @return 合并后的事件列表
     */
    List<EventData> fetchEventsBatch(List<TimeSegment> segments, TimelineGenerateRequest request);
    
    /**
     * 检查是否需要分割
     * 根据时间跨度判断是否需要进行时间段分割
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否需要分割
     */
    boolean needsSegmentation(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检查是否需要分割（带配置参数）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param maxSpanDays 最大时间跨度（天）
     * @return 是否需要分割
     */
    boolean needsSegmentation(LocalDateTime startTime, LocalDateTime endTime, int maxSpanDays);
    
    /**
     * 验证时间段列表
     * 检查时间段列表的有效性和连续性
     * 
     * @param segments 时间段列表
     * @return 验证结果
     */
    boolean validateTimeSegments(List<TimeSegment> segments);
    
    /**
     * 合并事件列表
     * 将多个时间段的事件合并，去重并按时间排序
     * 
     * @param eventLists 事件列表的列表
     * @return 合并后的事件列表
     */
    List<EventData> mergeEventLists(List<List<EventData>> eventLists);
    
    /**
     * 获取默认配置
     * 
     * @return 默认的最大时间跨度（天）
     */
    int getDefaultMaxSpanDays();
    
    /**
     * 获取分割统计信息
     * 
     * @param segments 时间段列表
     * @return 统计信息
     */
    String getSegmentationStats(List<TimeSegment> segments);
    
    /**
     * 为单个时间段获取事件
     * 
     * @param segment 时间段
     * @param request 时间线生成请求
     * @return 事件列表
     */
    List<EventData> fetchEventsForSegment(TimeSegment segment, TimelineGenerateRequest request);
}