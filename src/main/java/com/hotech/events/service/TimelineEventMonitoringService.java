package com.hotech.events.service;

import com.hotech.events.dto.Alert;
import com.hotech.events.dto.MonitoringData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时间线事件监控服务接口
 * 提供实时监控时间线事件的创建和关联过程
 */
public interface TimelineEventMonitoringService {
    
    /**
     * 开始监控
     * 启动监控服务，开始收集监控数据
     */
    void startMonitoring();
    
    /**
     * 停止监控
     * 停止监控服务
     */
    void stopMonitoring();
    
    /**
     * 记录监控数据
     * 
     * @param data 监控数据
     */
    void recordMonitoringData(MonitoringData data);
    
    /**
     * 记录时间线操作
     * 
     * @param timelineId 时间线ID
     * @param operation 操作类型
     * @param successful 是否成功
     * @param duration 执行时长
     * @param errorMessage 错误消息（如果失败）
     */
    void recordTimelineOperation(Long timelineId, String operation, boolean successful, 
                               Long duration, String errorMessage);
    
    /**
     * 记录事件关联操作
     * 
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @param operation 操作类型
     * @param successful 是否成功
     * @param errorMessage 错误消息（如果失败）
     */
    void recordEventAssociation(Long timelineId, Long eventId, String operation, 
                              boolean successful, String errorMessage);
    
    /**
     * 记录诊断操作
     * 
     * @param timelineId 时间线ID（可选）
     * @param issuesFound 发现的问题数量
     * @param duration 执行时长
     */
    void recordDiagnosisOperation(Long timelineId, int issuesFound, Long duration);
    
    /**
     * 记录修复操作
     * 
     * @param timelineId 时间线ID（可选）
     * @param issuesRepaired 修复的问题数量
     * @param duration 执行时长
     * @param successful 是否成功
     */
    void recordRepairOperation(Long timelineId, int issuesRepaired, Long duration, boolean successful);
    
    /**
     * 获取监控报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 监控报告
     */
    Map<String, Object> getMonitoringReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取实时监控数据
     * 
     * @return 实时监控数据
     */
    Map<String, Object> getRealTimeMonitoringData();
    
    /**
     * 设置警告阈值
     * 
     * @param alertType 警告类型
     * @param threshold 阈值
     */
    void setAlertThreshold(String alertType, int threshold);
    
    /**
     * 获取活跃的警告
     * 
     * @return 活跃警告列表
     */
    List<Alert> getActiveAlerts();
    
    /**
     * 创建警告
     * 
     * @param type 警告类型
     * @param severity 严重程度
     * @param message 警告消息
     * @param timelineId 相关时间线ID
     * @param eventId 相关事件ID
     * @param details 详细信息
     * @return 创建的警告
     */
    Alert createAlert(Alert.AlertType type, Alert.AlertSeverity severity, String message,
                     Long timelineId, Long eventId, Map<String, Object> details);
    
    /**
     * 解决警告
     * 
     * @param alertId 警告ID
     * @return 是否成功解决
     */
    boolean resolveAlert(Long alertId);
    
    /**
     * 检查系统健康状态
     * 
     * @return 系统健康状态
     */
    Map<String, Object> checkSystemHealth();
    
    /**
     * 获取性能指标
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能指标
     */
    Map<String, Object> getPerformanceMetrics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 清理过期的监控数据
     * 
     * @param retentionDays 保留天数
     * @return 清理的记录数
     */
    int cleanupExpiredData(int retentionDays);
}