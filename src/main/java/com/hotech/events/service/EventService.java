package com.hotech.events.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.dto.event.EventQueryDTO;
import com.hotech.events.dto.event.BatchEventRequestDTO;
import com.hotech.events.entity.Event;

import java.util.List;
import java.util.Map;

/**
 * 事件服务接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
public interface EventService {

    /**
     * 分页查询事件列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<EventDTO> getEventList(EventQueryDTO queryDTO);

    /**
     * 根据ID获取事件详情
     * 
     * @param id 事件ID
     * @return 事件详情
     */
    EventDTO getEventDetail(Long id);

    /**
     * 创建事件
     * 
     * @param eventDTO 事件信息
     * @return 创建的事件信息
     */
    EventDTO createEvent(EventDTO eventDTO);

    /**
     * 批量创建事件
     * 
     * @param eventDTOs 事件信息列表
     * @return 创建成功的数量
     */
    Integer createEventsBatch(List<EventDTO> eventDTOs);

    /**
     * 更新事件
     * 
     * @param eventDTO 事件信息
     * @return 更新后的事件信息
     */
    EventDTO updateEvent(EventDTO eventDTO);

    /**
     * 删除事件
     * 
     * @param id 事件ID
     * @return 是否删除成功
     */
    Boolean deleteEvent(Long id);

    /**
     * 批量删除事件
     * 
     * @param ids 事件ID列表
     * @return 删除成功的数量
     */
    Integer deleteEventsBatch(List<Long> ids);

    /**
     * 批量创建事件及其关联关系
     * 
     * @param batchRequest 批量事件请求
     * @return 创建结果统计
     */
    Map<String, Object> createEventsBatchWithRelations(BatchEventRequestDTO batchRequest);

    /**
     * 获取事件关联图谱数据
     * 
     * @param eventId 事件ID
     * @return 图谱数据
     */
    Object getEventGraph(Long eventId);

    /**
     * 导出所有事件数据
     * 
     * @return 所有事件数据
     */
    List<EventDTO> exportAllEvents();

    /**
     * 获取统计数据
     * 
     * @return 统计数据
     */
    Map<String, Object> getStats();

    /**
     * 获取地理分布统计数据
     * 
     * @return 地理分布统计数据
     */
    Map<String, Object> getGeographicStats();

    /**
     * 获取未关联到指定时间线的事件列表
     * 
     * @param timelineId 时间线ID
     * @param queryDTO 查询条件
     * @return 未关联的事件列表
     */
    Page<EventDTO> getUnlinkedEvents(Long timelineId, EventQueryDTO queryDTO);

    /**
     * 获取事件类型分布统计数据
     * 
     * @return 事件类型分布统计数据
     */
    Map<String, Object> getEventTypeStats();
} 