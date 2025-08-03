package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.StorageStats;
import com.hotech.events.entity.Event;

import java.util.List;

/**
 * 事件存储服务接口
 * 
 * @author Kiro
 */
public interface EventStorageService {
    
    /**
     * 存储验证后的事件
     * 
     * @param eventData 事件数据
     * @return 存储后的事件ID
     */
    Long storeValidatedEvent(EventData eventData);
    
    /**
     * 批量存储事件
     * 
     * @param events 事件列表
     * @return 存储后的事件ID列表
     */
    List<Long> storeEventsBatch(List<EventData> events);
    
    /**
     * 更新字典表
     * 
     * @param eventData 事件数据
     */
    void updateDictionaries(EventData eventData);
    
    /**
     * 事件去重
     * 
     * @param events 事件列表
     * @return 去重后的事件列表
     */
    List<EventData> deduplicateEvents(List<EventData> events);
    
    /**
     * 检查事件是否已存在
     * 
     * @param eventData 事件数据
     * @return 如果存在返回现有事件，否则返回null
     */
    Event findExistingEvent(EventData eventData);
    
    /**
     * 更新现有事件
     * 
     * @param existingEvent 现有事件
     * @param newEventData 新事件数据
     * @return 更新后的事件
     */
    Event updateExistingEvent(Event existingEvent, EventData newEventData);
    
    /**
     * 创建新事件
     * 
     * @param eventData 事件数据
     * @return 创建的事件
     */
    Event createNewEvent(EventData eventData);
    
    /**
     * 获取存储统计信息
     * 
     * @return 统计信息
     */
    StorageStats getStorageStats();
    
    /**
     * 清理过期的临时数据
     * 
     * @param daysOld 保留天数
     * @return 清理的记录数
     */
    int cleanupOldData(int daysOld);
}