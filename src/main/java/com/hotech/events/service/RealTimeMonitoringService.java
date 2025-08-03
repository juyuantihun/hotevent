package com.hotech.events.service;

import java.util.Map;

/**
 * 实时监控服务接口
 * 提供API调用状态和事件解析情况的实时监控
 */
public interface RealTimeMonitoringService {

    /**
     * 获取API调用状态监控数据
     * @return API调用状态信息
     */
    Map<String, Object> getApiCallStatus();

    /**
     * 获取事件解析状态监控数据
     * @return 事件解析状态信息
     */
    Map<String, Object> getEventParsingStatus();

    /**
     * 获取系统健康状态
     * @return 系统健康状态信息
     */
    Map<String, Object> getSystemHealthStatus();

    /**
     * 获取实时性能指标
     * @return 性能指标信息
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * 获取告警信息
     * @return 当前告警列表
     */
    Map<String, Object> getAlertStatus();

    /**
     * 获取完整的监控面板数据
     * @return 监控面板数据
     */
    Map<String, Object> getMonitoringDashboard();

    /**
     * 记录API调用事件
     * @param apiType API类型
     * @param success 是否成功
     * @param responseTime 响应时间
     * @param errorMessage 错误信息（如果有）
     */
    void recordApiCall(String apiType, boolean success, long responseTime, String errorMessage);

    /**
     * 记录事件解析事件
     * @param parseMethod 解析方法
     * @param success 是否成功
     * @param eventCount 解析出的事件数量
     * @param errorMessage 错误信息（如果有）
     */
    void recordEventParsing(String parseMethod, boolean success, int eventCount, String errorMessage);

    /**
     * 清理过期的监控数据
     */
    void cleanupExpiredData();
}