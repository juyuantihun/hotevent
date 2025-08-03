package com.hotech.events.service;

import com.hotech.events.dto.RepairResult;

import java.util.List;

/**
 * 时间线事件修复服务接口
 * 提供自动修复时间线事件问题的功能
 */
public interface TimelineEventRepairService {
    
    /**
     * 修复指定时间线的缺失关联
     * 为孤立的事件创建时间线关联
     * 
     * @param timelineId 时间线ID
     * @return 修复结果
     */
    RepairResult repairMissingAssociations(Long timelineId);
    
    /**
     * 修复事件状态问题
     * 启用被错误禁用的事件
     * 
     * @return 修复结果
     */
    RepairResult repairEventStatus();
    
    /**
     * 重建事件数量统计
     * 同步timeline表中的event_count与实际关联数量
     * 
     * @return 修复结果
     */
    RepairResult rebuildEventCounts();
    
    /**
     * 清理重复的关联记录
     * 删除timeline_event表中的重复关联
     * 
     * @return 修复结果
     */
    RepairResult cleanupDuplicateAssociations();
    
    /**
     * 清理无效的关联记录
     * 删除指向不存在事件或时间线的关联
     * 
     * @return 修复结果
     */
    RepairResult cleanupInvalidAssociations();
    
    /**
     * 执行完整的系统修复
     * 修复所有检测到的问题
     * 
     * @return 修复结果
     */
    RepairResult performFullRepair();
    
    /**
     * 修复指定时间线的所有问题
     * 
     * @param timelineId 时间线ID
     * @return 修复结果
     */
    RepairResult repairTimeline(Long timelineId);
    
    /**
     * 批量修复多个时间线
     * 
     * @param timelineIds 时间线ID列表
     * @return 修复结果列表
     */
    List<RepairResult> batchRepair(List<Long> timelineIds);
    
    /**
     * 修复地区关联问题
     * 清理无效的时间线地区关联
     * 
     * @return 修复结果
     */
    RepairResult repairRegionAssociations();
    
    /**
     * 为孤立事件自动创建时间线关联
     * 根据事件的时间和地点信息，自动关联到合适的时间线
     * 
     * @return 修复结果
     */
    RepairResult autoAssociateOrphanedEvents();
}