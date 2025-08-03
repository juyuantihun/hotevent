package com.hotech.events.service;

import java.util.Map;

/**
 * 系统性能指标收集器接口
 * 用于收集和监控系统各项性能指标
 * 
 * @author Kiro
 * @since 2024-01-01
 */
public interface SystemPerformanceCollector {
    
    /**
     * 开始性能指标收集
     */
    void startCollection();
    
    /**
     * 停止性能指标收集
     */
    void stopCollection();
    
    /**
     * 收集当前系统性能快照
     * 
     * @return 性能指标快照
     */
    Map<String, Object> collectPerformanceSnapshot();
    
    /**
     * 收集JVM性能指标
     * 
     * @return JVM性能指标
     */
    Map<String, Object> collectJvmMetrics();
    
    /**
     * 收集系统资源使用情况
     * 
     * @return 系统资源指标
     */
    Map<String, Object> collectSystemResourceMetrics();
    
    /**
     * 收集应用程序性能指标
     * 
     * @return 应用程序性能指标
     */
    Map<String, Object> collectApplicationMetrics();
    
    /**
     * 收集数据库性能指标
     * 
     * @return 数据库性能指标
     */
    Map<String, Object> collectDatabaseMetrics();
    
    /**
     * 收集API调用性能指标
     * 
     * @return API调用性能指标
     */
    Map<String, Object> collectApiMetrics();
    
    /**
     * 记录自定义性能指标
     * 
     * @param metricName 指标名称
     * @param value 指标值
     * @param tags 标签
     */
    void recordCustomMetric(String metricName, double value, Map<String, String> tags);
    
    /**
     * 记录计数器指标
     * 
     * @param counterName 计数器名称
     * @param increment 增量
     * @param tags 标签
     */
    void recordCounter(String counterName, long increment, Map<String, String> tags);
    
    /**
     * 记录计时器指标
     * 
     * @param timerName 计时器名称
     * @param duration 持续时间（毫秒）
     * @param tags 标签
     */
    void recordTimer(String timerName, long duration, Map<String, String> tags);
    
    /**
     * 记录直方图指标
     * 
     * @param histogramName 直方图名称
     * @param value 值
     * @param tags 标签
     */
    void recordHistogram(String histogramName, double value, Map<String, String> tags);
    
    /**
     * 获取历史性能数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param metricNames 指标名称列表
     * @return 历史性能数据
     */
    Map<String, Object> getHistoricalMetrics(long startTime, long endTime, String... metricNames);
    
    /**
     * 获取性能趋势分析
     * 
     * @param metricName 指标名称
     * @param timeRange 时间范围（小时）
     * @return 趋势分析结果
     */
    Map<String, Object> getPerformanceTrend(String metricName, int timeRange);
    
    /**
     * 获取性能告警信息
     * 
     * @return 告警信息列表
     */
    java.util.List<Map<String, Object>> getPerformanceAlerts();
    
    /**
     * 设置性能阈值
     * 
     * @param metricName 指标名称
     * @param threshold 阈值
     * @param operator 比较操作符（>, <, >=, <=, ==）
     */
    void setPerformanceThreshold(String metricName, double threshold, String operator);
    
    /**
     * 导出性能报告
     * 
     * @param format 导出格式（JSON, CSV, XML）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告内容
     */
    String exportPerformanceReport(String format, long startTime, long endTime);
    
    /**
     * 重置所有性能指标
     */
    void resetAllMetrics();
    
    /**
     * 获取收集器状态
     * 
     * @return 收集器状态信息
     */
    Map<String, Object> getCollectorStatus();
}