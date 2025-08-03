package com.hotech.events.service;

import com.hotech.events.dto.DiagnosisResult;
import com.hotech.events.dto.RepairResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 诊断报告服务接口
 * 提供诊断和修复报告的生成和导出功能
 */
public interface DiagnosisReportService {
    
    /**
     * 生成诊断报告
     * 
     * @param result 诊断结果
     * @return 报告内容
     */
    Map<String, Object> generateDiagnosisReport(DiagnosisResult result);
    
    /**
     * 生成修复报告
     * 
     * @param result 修复结果
     * @return 报告内容
     */
    Map<String, Object> generateRepairReport(RepairResult result);
    
    /**
     * 生成综合报告
     * 包含诊断和修复的完整信息
     * 
     * @param diagnosisResult 诊断结果
     * @param repairResult 修复结果
     * @return 综合报告内容
     */
    Map<String, Object> generateComprehensiveReport(DiagnosisResult diagnosisResult, RepairResult repairResult);
    
    /**
     * 导出报告为JSON格式
     * 
     * @param report 报告内容
     * @return JSON字符串
     */
    String exportToJson(Map<String, Object> report);
    
    /**
     * 导出报告为CSV格式
     * 
     * @param report 报告内容
     * @return CSV字符串
     */
    String exportToCsv(Map<String, Object> report);
    
    /**
     * 导出报告为HTML格式
     * 
     * @param report 报告内容
     * @return HTML字符串
     */
    String exportToHtml(Map<String, Object> report);
    
    /**
     * 获取历史报告列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史报告列表
     */
    List<Map<String, Object>> getHistoricalReports(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 保存报告
     * 
     * @param report 报告内容
     * @param reportType 报告类型
     * @return 报告ID
     */
    Long saveReport(Map<String, Object> report, String reportType);
    
    /**
     * 获取报告
     * 
     * @param reportId 报告ID
     * @return 报告内容
     */
    Map<String, Object> getReport(Long reportId);
    
    /**
     * 删除报告
     * 
     * @param reportId 报告ID
     * @return 是否删除成功
     */
    boolean deleteReport(Long reportId);
    
    /**
     * 生成系统健康报告
     * 
     * @return 系统健康报告
     */
    Map<String, Object> generateSystemHealthReport();
    
    /**
     * 生成性能报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告
     */
    Map<String, Object> generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime);
}