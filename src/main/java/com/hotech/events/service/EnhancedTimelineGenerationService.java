package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimelineGenerateRequest;

import java.util.List;
import java.util.Map;

/**
 * 增强的时间线生成服务接口
 * 提供时间线生成、异步处理、错误处理和性能监控功能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface EnhancedTimelineGenerationService {
    
    /**
     * 生成带错误处理的时间线
     * 
     * @param request 时间线生成请求
     * @return 事件数据列表
     */
    List<EventData> generateTimelineWithErrorHandling(TimelineGenerateRequest request);
    
    /**
     * 异步生成时间线
     * 
     * @param request 时间线生成请求
     * @return 任务ID
     */
    String generateTimelineAsync(TimelineGenerateRequest request);
    
    /**
     * 获取异步任务状态
     * 
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getAsyncTaskStatus(String taskId);
    
    /**
     * 获取异步任务结果
     * 
     * @param taskId 任务ID
     * @return 事件数据列表
     */
    List<EventData> getAsyncTaskResult(String taskId);
    
    /**
     * 取消异步任务
     * 
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelAsyncTask(String taskId);
    
    /**
     * 验证请求参数
     * 
     * @param request 时间线生成请求
     * @return 验证结果
     */
    Map<String, Object> validateRequest(TimelineGenerateRequest request);
    
    /**
     * 获取生成统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getGenerationStatistics();
    
    /**
     * 清理过期任务
     */
    void cleanupExpiredTasks();
}