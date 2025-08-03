package com.hotech.events.service;

import com.hotech.events.dto.DiagnosisResult;

import java.util.List;
import java.util.Map;

/**
 * 时间线事件诊断服务接口
 * 提供系统性的时间线事件诊断功能
 */
public interface TimelineEventDiagnosisService {
    
    /**
     * 执行完整的系统诊断
     * 检查所有时间线的事件关联情况
     * 
     * @return 完整的诊断结果
     */
    DiagnosisResult performFullDiagnosis();
    
    /**
     * 诊断指定时间线的事件关联情况
     * 
     * @param timelineId 时间线ID
     * @return 指定时间线的诊断结果
     */
    DiagnosisResult diagnoseTimeline(Long timelineId);
    
    /**
     * 检查数据一致性
     * 验证timeline表中的event_count与实际关联数量是否一致
     * 
     * @return 数据一致性检查结果
     */
    Map<String, Object> checkDataConsistency();
    
    /**
     * 检查事件关联关系
     * 验证timeline_event表中的关联是否有效
     * 
     * @return 事件关联检查结果
     */
    Map<String, Object> checkEventAssociations();
    
    /**
     * 检查事件状态
     * 验证event表中事件的状态是否正常
     * 
     * @return 事件状态检查结果
     */
    Map<String, Object> checkEventStatus();
    
    /**
     * 检查孤立数据
     * 查找没有关联到任何时间线的事件
     * 
     * @return 孤立数据检查结果
     */
    Map<String, Object> checkOrphanedData();
    
    /**
     * 检查重复数据
     * 查找重复的时间线事件关联
     * 
     * @return 重复数据检查结果
     */
    Map<String, Object> checkDuplicateData();
    
    /**
     * 获取诊断统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getDiagnosisStatistics();
    
    /**
     * 批量诊断多个时间线
     * 
     * @param timelineIds 时间线ID列表
     * @return 批量诊断结果
     */
    List<DiagnosisResult> batchDiagnose(List<Long> timelineIds);
    
    /**
     * 检查时间线生成任务状态
     * 查找可能失败的时间线生成任务
     * 
     * @return 任务状态检查结果
     */
    Map<String, Object> checkTimelineGenerationStatus();
    
    /**
     * 检查时间线与地区关联的完整性
     * 验证timeline_region表中的关联是否有效
     * 
     * @return 地区关联检查结果
     */
    Map<String, Object> checkRegionAssociations();
}