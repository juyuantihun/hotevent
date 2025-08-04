package com.hotech.events.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.Timeline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时间线服务接口
 */
public interface TimelineService {
    
    /**
     * 创建时间线
     * @param timeline 时间线信息
     * @param regionIds 地区ID列表
     * @return 创建的时间线
     */
    Timeline createTimeline(Timeline timeline, List<Long> regionIds);
    
    /**
     * 更新时间线
     * @param timeline 时间线信息
     * @param regionIds 地区ID列表
     * @return 更新后的时间线
     */
    Timeline updateTimeline(Timeline timeline, List<Long> regionIds);
    
    /**
     * 删除时间线
     * @param id 时间线ID
     * @return 是否删除成功
     */
    boolean deleteTimeline(Long id);
    
    /**
     * 获取时间线详情
     * @param id 时间线ID
     * @return 时间线详情
     */
    Map<String, Object> getTimelineDetail(Long id);
    
    /**
     * 分页查询时间线列表
     * @param page 分页参数
     * @param name 时间线名称（可选）
     * @param status 时间线状态（可选）
     * @param regionId 地区ID（可选）
     * @return 时间线分页列表
     */
    IPage<Timeline> listTimelines(Page<Timeline> page, String name, String status, Long regionId);
    
    /**
     * 异步生成时间线
     * @param name 时间线名称
     * @param description 时间线描述
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 生成的时间线ID
     */
    Long generateTimelineAsync(String name, String description, List<Long> regionIds, 
                              LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取时间线生成进度
     * @param id 时间线ID
     * @return 生成进度信息
     */
    Map<String, Object> getGenerationProgress(Long id);
    
    /**
     * 取消时间线生成
     * @param id 时间线ID
     * @return 是否取消成功
     */
    boolean cancelGeneration(Long id);
    
    /**
     * 获取时间线包含的地区
     * @param timelineId 时间线ID
     * @return 地区列表
     */
    List<Map<String, Object>> getTimelineRegions(Long timelineId);
    
    /**
     * 获取时间线包含的事件
     * @param timelineId 时间线ID
     * @return 事件列表
     */
    List<Map<String, Object>> getTimelineEvents(Long timelineId);
    
    /**
     * 分页获取时间线包含的事件（支持搜索和排序）
     * @param timelineId 时间线ID
     * @param page 分页参数
     * @param includeDetails 是否包含详细信息
     * @param keyword 搜索关键词
     * @param nodeType 事件类型
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 事件分页列表
     */
    IPage<Map<String, Object>> getTimelineEventsWithPagination(Long timelineId, Page<Map<String, Object>> page, 
            Boolean includeDetails, String keyword, String nodeType, String sortBy, String sortOrder);
    
    /**
     * 获取时间线图形数据
     * @param timelineId 时间线ID
     * @return 图形数据
     */
    Map<String, Object> getTimelineGraph(Long timelineId);
    
    /**
     * 关联地区到时间线
     * @param timelineId 时间线ID
     * @param regionIds 地区ID列表
     */
    void associateRegions(Long timelineId, List<Long> regionIds);
    
    /**
     * 关联事件到时间线
     * @param timelineId 时间线ID
     * @param eventIds 事件ID列表
     */
    void associateEvents(Long timelineId, List<Long> eventIds);
    
    /**
     * 添加单个事件到时间线
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @return 是否添加成功
     */
    boolean addEventToTimeline(Long timelineId, Long eventId);
    
    /**
     * 从时间线中移除事件
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @return 是否移除成功
     */
    boolean removeEventFromTimeline(Long timelineId, Long eventId);
    
    /**
     * 保存时间线
     * @param timeline 时间线对象
     * @return 保存的时间线
     */
    Timeline save(Timeline timeline);
    
    /**
     * 根据ID获取时间线
     * @param id 时间线ID
     * @return 时间线对象
     */
    Timeline getById(Long id);
    
    /**
     * 根据ID更新时间线
     * @param timeline 时间线对象
     * @return 是否更新成功
     */
    boolean updateById(Timeline timeline);
    
    /**
     * 获取未关联到指定时间线的事件
     * @param timelineId 时间线ID
     * @param page 分页参数
     * @param eventType 事件类型（可选）
     * @param subject 事件主体（可选）
     * @param object 事件客体（可选）
     * @param sourceType 来源类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 未关联事件分页列表
     */
    IPage<Map<String, Object>> getAvailableEvents(Long timelineId, Page<Map<String, Object>> page,
            String eventType, String subject, String object, Integer sourceType, 
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取所有事件数量（调试用）
     * @return 事件总数
     */
    int countAllEvents();
    
    /**
     * 获取指定时间线关联的事件数量（调试用）
     * @param timelineId 时间线ID
     * @return 关联事件数量
     */
    int countAssociatedEvents(Long timelineId);
    
    /**
     * 调试方法：获取所有事件（包含关联状态）
     * @param timelineId 时间线ID
     * @param page 分页参数
     * @param eventType 事件类型（可选）
     * @param subject 事件主体（可选）
     * @param object 事件客体（可选）
     * @param sourceType 来源类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 所有事件分页列表（包含关联状态）
     */
    IPage<Map<String, Object>> debugAllEvents(Long timelineId, Page<Map<String, Object>> page,
            String eventType, String subject, String object, Integer sourceType, 
            LocalDateTime startTime, LocalDateTime endTime);
}