package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.dto.DiagnosisIssue;
import com.hotech.events.dto.DiagnosisResult;
import com.hotech.events.dto.RepairAction;
import com.hotech.events.dto.RepairResult;
import com.hotech.events.service.DiagnosisReportService;
import com.hotech.events.service.TimelineEventDiagnosisService;
import com.hotech.events.service.TimelineEventMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 诊断报告服务实现类
 */
@Slf4j
@Service
public class DiagnosisReportServiceImpl implements DiagnosisReportService {
    
    @Autowired
    private TimelineEventDiagnosisService diagnosisService;
    
    @Autowired
    private TimelineEventMonitoringService monitoringService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 报告存储（实际应用中应该使用数据库）
    private final Map<Long, Map<String, Object>> reportStorage = new ConcurrentHashMap<>();
    private final AtomicLong reportIdGenerator = new AtomicLong(1);
    
    @Override
    public Map<String, Object> generateDiagnosisReport(DiagnosisResult result) {
        log.info("生成诊断报告: timelineId={}", result.getTimelineId());
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本信息
        report.put("reportType", "DIAGNOSIS");
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", LocalDateTime.now());
        report.put("timelineId", result.getTimelineId());
        report.put("timelineName", result.getTimelineName());
        report.put("diagnosisTime", result.getDiagnosisTime());
        report.put("diagnosisStatus", result.getStatus());
        
        // 问题统计
        Map<String, Object> issueSummary = new HashMap<>();
        issueSummary.put("totalIssues", result.getIssues().size());
        issueSummary.put("criticalIssues", result.getIssues().stream()
            .mapToInt(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.CRITICAL ? 1 : 0).sum());
        issueSummary.put("highIssues", result.getIssues().stream()
            .mapToInt(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.HIGH ? 1 : 0).sum());
        issueSummary.put("mediumIssues", result.getIssues().stream()
            .mapToInt(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.MEDIUM ? 1 : 0).sum());
        issueSummary.put("lowIssues", result.getIssues().stream()
            .mapToInt(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.LOW ? 1 : 0).sum());
        issueSummary.put("autoRepairableIssues", result.getIssues().stream()
            .mapToInt(issue -> issue.isAutoRepairable() ? 1 : 0).sum());
        
        report.put("issueSummary", issueSummary);
        
        // 问题详情
        List<Map<String, Object>> issueDetails = new ArrayList<>();
        for (DiagnosisIssue issue : result.getIssues()) {
            Map<String, Object> issueMap = new HashMap<>();
            issueMap.put("type", issue.getType());
            issueMap.put("severity", issue.getSeverity());
            issueMap.put("description", issue.getDescription());
            issueMap.put("recommendation", issue.getRecommendation());
            issueMap.put("autoRepairable", issue.isAutoRepairable());
            issueMap.put("details", issue.getDetails());
            issueDetails.add(issueMap);
        }
        report.put("issues", issueDetails);
        
        // 数据一致性信息
        if (result.getConsistencyInfo() != null) {
            report.put("consistencyInfo", result.getConsistencyInfo());
        }
        
        // 关联信息
        if (result.getAssociationInfo() != null) {
            report.put("associationInfo", result.getAssociationInfo());
        }
        
        // 状态信息
        if (result.getStatusInfo() != null) {
            report.put("statusInfo", result.getStatusInfo());
        }
        
        // 统计信息
        if (result.getStatistics() != null) {
            report.put("statistics", result.getStatistics());
        }
        
        return report;
    }    
    @Override
    public Map<String, Object> generateRepairReport(RepairResult result) {
        log.info("生成修复报告: timelineId={}", result.getTimelineId());
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本信息
        report.put("reportType", "REPAIR");
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", LocalDateTime.now());
        report.put("timelineId", result.getTimelineId());
        report.put("repairTime", result.getRepairTime());
        report.put("repairStatus", result.getStatus());
        report.put("message", result.getMessage());
        
        // 修复统计
        if (result.getStatistics() != null) {
            Map<String, Object> repairSummary = new HashMap<>();
            repairSummary.put("totalIssuesFound", result.getStatistics().getTotalIssuesFound());
            repairSummary.put("issuesRepaired", result.getStatistics().getIssuesRepaired());
            repairSummary.put("issuesFailed", result.getStatistics().getIssuesFailed());
            repairSummary.put("associationsCreated", result.getStatistics().getAssociationsCreated());
            repairSummary.put("associationsDeleted", result.getStatistics().getAssociationsDeleted());
            repairSummary.put("eventCountsUpdated", result.getStatistics().getEventCountsUpdated());
            repairSummary.put("statusesFixed", result.getStatistics().getStatusesFixed());
            
            double successRate = result.getStatistics().getTotalIssuesFound() > 0 ? 
                (double) result.getStatistics().getIssuesRepaired() / result.getStatistics().getTotalIssuesFound() * 100 : 100.0;
            repairSummary.put("successRate", successRate);
            
            report.put("repairSummary", repairSummary);
        }
        
        // 修复操作详情
        if (result.getActions() != null) {
            List<Map<String, Object>> actionDetails = new ArrayList<>();
            for (RepairAction action : result.getActions()) {
                Map<String, Object> actionMap = new HashMap<>();
                actionMap.put("type", action.getType());
                actionMap.put("description", action.getDescription());
                actionMap.put("successful", action.isSuccessful());
                actionMap.put("errorMessage", action.getErrorMessage());
                actionMap.put("affectedRecords", action.getAffectedRecords());
                actionMap.put("details", action.getDetails());
                actionDetails.add(actionMap);
            }
            report.put("actions", actionDetails);
            
            // 按操作类型统计
            Map<String, Long> actionTypeStats = result.getActions().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    action -> action.getType().name(),
                    java.util.stream.Collectors.counting()
                ));
            report.put("actionTypeStatistics", actionTypeStats);
        }
        
        return report;
    }
    
    @Override
    public Map<String, Object> generateComprehensiveReport(DiagnosisResult diagnosisResult, RepairResult repairResult) {
        log.info("生成综合报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本信息
        report.put("reportType", "COMPREHENSIVE");
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", LocalDateTime.now());
        
        // 诊断部分
        Map<String, Object> diagnosisReport = generateDiagnosisReport(diagnosisResult);
        report.put("diagnosis", diagnosisReport);
        
        // 修复部分
        Map<String, Object> repairReport = generateRepairReport(repairResult);
        report.put("repair", repairReport);
        
        // 综合统计
        Map<String, Object> comprehensiveSummary = new HashMap<>();
        comprehensiveSummary.put("totalIssuesFound", diagnosisResult.getIssues().size());
        
        if (repairResult.getStatistics() != null) {
            comprehensiveSummary.put("issuesRepaired", repairResult.getStatistics().getIssuesRepaired());
            comprehensiveSummary.put("issuesRemaining", 
                diagnosisResult.getIssues().size() - repairResult.getStatistics().getIssuesRepaired());
            
            double overallSuccessRate = diagnosisResult.getIssues().size() > 0 ? 
                (double) repairResult.getStatistics().getIssuesRepaired() / diagnosisResult.getIssues().size() * 100 : 100.0;
            comprehensiveSummary.put("overallSuccessRate", overallSuccessRate);
        }
        
        report.put("comprehensiveSummary", comprehensiveSummary);
        
        return report;
    }
    
    @Override
    public String exportToJson(Map<String, Object> report) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (Exception e) {
            log.error("导出JSON格式失败", e);
            throw new RuntimeException("导出JSON失败: " + e.getMessage());
        }
    }
    
    @Override
    public String exportToCsv(Map<String, Object> report) {
        StringBuilder csv = new StringBuilder();
        
        // CSV头部
        csv.append("报告类型,报告ID,生成时间,时间线ID,状态\n");
        
        // 基本信息
        csv.append(String.format("%s,%s,%s,%s,%s\n",
            report.get("reportType"),
            report.get("reportId"),
            report.get("generatedAt"),
            report.get("timelineId"),
            report.get("diagnosisStatus") != null ? report.get("diagnosisStatus") : report.get("repairStatus")
        ));
        
        // 如果是诊断报告，添加问题详情
        if ("DIAGNOSIS".equals(report.get("reportType")) && report.containsKey("issues")) {
            csv.append("\n问题类型,严重程度,描述,建议,可自动修复\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = (List<Map<String, Object>>) report.get("issues");
            for (Map<String, Object> issue : issues) {
                csv.append(String.format("%s,%s,%s,%s,%s\n",
                    issue.get("type"),
                    issue.get("severity"),
                    escapeForCsv(issue.get("description").toString()),
                    escapeForCsv(issue.get("recommendation").toString()),
                    issue.get("autoRepairable")
                ));
            }
        }
        
        // 如果是修复报告，添加操作详情
        if ("REPAIR".equals(report.get("reportType")) && report.containsKey("actions")) {
            csv.append("\n操作类型,描述,是否成功,影响记录数\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = (List<Map<String, Object>>) report.get("actions");
            for (Map<String, Object> action : actions) {
                csv.append(String.format("%s,%s,%s,%s\n",
                    action.get("type"),
                    escapeForCsv(action.get("description").toString()),
                    action.get("successful"),
                    action.get("affectedRecords")
                ));
            }
        }
        
        return csv.toString();
    }    
@Override
    public String exportToHtml(Map<String, Object> report) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>诊断报告</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append(".critical { color: #d32f2f; font-weight: bold; }\n");
        html.append(".high { color: #f57c00; font-weight: bold; }\n");
        html.append(".medium { color: #fbc02d; }\n");
        html.append(".low { color: #388e3c; }\n");
        html.append(".success { color: #4caf50; }\n");
        html.append(".failed { color: #f44336; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        // 报告标题
        html.append("<h1>").append(getReportTitle(report)).append("</h1>\n");
        
        // 基本信息
        html.append("<h2>基本信息</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>报告类型</th><td>").append(report.get("reportType")).append("</td></tr>\n");
        html.append("<tr><th>报告ID</th><td>").append(report.get("reportId")).append("</td></tr>\n");
        html.append("<tr><th>生成时间</th><td>").append(formatDateTime(report.get("generatedAt"))).append("</td></tr>\n");
        if (report.get("timelineId") != null) {
            html.append("<tr><th>时间线ID</th><td>").append(report.get("timelineId")).append("</td></tr>\n");
        }
        if (report.get("timelineName") != null) {
            html.append("<tr><th>时间线名称</th><td>").append(report.get("timelineName")).append("</td></tr>\n");
        }
        html.append("</table>\n");
        
        // 诊断报告特定内容
        if ("DIAGNOSIS".equals(report.get("reportType"))) {
            addDiagnosisHtmlContent(html, report);
        }
        
        // 修复报告特定内容
        if ("REPAIR".equals(report.get("reportType"))) {
            addRepairHtmlContent(html, report);
        }
        
        // 综合报告特定内容
        if ("COMPREHENSIVE".equals(report.get("reportType"))) {
            addComprehensiveHtmlContent(html, report);
        }
        
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    @Override
    public List<Map<String, Object>> getHistoricalReports(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> historicalReports = new ArrayList<>();
        
        for (Map<String, Object> report : reportStorage.values()) {
            LocalDateTime generatedAt = (LocalDateTime) report.get("generatedAt");
            if (generatedAt != null && 
                generatedAt.isAfter(startTime) && 
                generatedAt.isBefore(endTime)) {
                
                // 只返回基本信息，不包含详细内容
                Map<String, Object> summary = new HashMap<>();
                summary.put("reportId", report.get("reportId"));
                summary.put("reportType", report.get("reportType"));
                summary.put("generatedAt", report.get("generatedAt"));
                summary.put("timelineId", report.get("timelineId"));
                summary.put("timelineName", report.get("timelineName"));
                summary.put("status", report.get("diagnosisStatus") != null ? 
                    report.get("diagnosisStatus") : report.get("repairStatus"));
                
                historicalReports.add(summary);
            }
        }
        
        // 按生成时间降序排序
        historicalReports.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("generatedAt");
            LocalDateTime timeB = (LocalDateTime) b.get("generatedAt");
            return timeB.compareTo(timeA);
        });
        
        return historicalReports;
    }
    
    @Override
    public Long saveReport(Map<String, Object> report, String reportType) {
        Long reportId = reportIdGenerator.getAndIncrement();
        
        // 添加保存信息
        report.put("savedAt", LocalDateTime.now());
        report.put("reportType", reportType);
        
        reportStorage.put(reportId, report);
        
        log.info("报告已保存: reportId={}, reportType={}", reportId, reportType);
        return reportId;
    }
    
    @Override
    public Map<String, Object> getReport(Long reportId) {
        return reportStorage.get(reportId);
    }
    
    @Override
    public boolean deleteReport(Long reportId) {
        Map<String, Object> removed = reportStorage.remove(reportId);
        boolean deleted = removed != null;
        
        if (deleted) {
            log.info("报告已删除: reportId={}", reportId);
        } else {
            log.warn("报告不存在: reportId={}", reportId);
        }
        
        return deleted;
    }
    
    @Override
    public Map<String, Object> generateSystemHealthReport() {
        log.info("生成系统健康报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本信息
        report.put("reportType", "SYSTEM_HEALTH");
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", LocalDateTime.now());
        
        try {
            // 获取系统健康状态
            Map<String, Object> healthStatus = monitoringService.checkSystemHealth();
            report.put("systemHealth", healthStatus);
            
            // 获取诊断统计
            Map<String, Object> diagnosisStats = diagnosisService.getDiagnosisStatistics();
            report.put("diagnosisStatistics", diagnosisStats);
            
            // 获取实时监控数据
            Map<String, Object> realTimeData = monitoringService.getRealTimeMonitoringData();
            report.put("realTimeMonitoring", realTimeData);
            
            // 获取活跃警告
            List<com.hotech.events.dto.Alert> activeAlerts = monitoringService.getActiveAlerts();
            report.put("activeAlerts", activeAlerts);
            
            // 生成健康评分
            double healthScore = calculateHealthScore(healthStatus, activeAlerts);
            report.put("healthScore", healthScore);
            report.put("healthLevel", getHealthLevel(healthScore));
            
        } catch (Exception e) {
            log.error("生成系统健康报告时发生错误", e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }
    
    @Override
    public Map<String, Object> generatePerformanceReport(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("生成性能报告: startTime={}, endTime={}", startTime, endTime);
        
        Map<String, Object> report = new HashMap<>();
        
        // 基本信息
        report.put("reportType", "PERFORMANCE");
        report.put("reportId", UUID.randomUUID().toString());
        report.put("generatedAt", LocalDateTime.now());
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        
        try {
            // 获取性能指标
            Map<String, Object> performanceMetrics = monitoringService.getPerformanceMetrics(startTime, endTime);
            report.put("performanceMetrics", performanceMetrics);
            
            // 获取监控报告
            Map<String, Object> monitoringReport = monitoringService.getMonitoringReport(startTime, endTime);
            report.put("monitoringReport", monitoringReport);
            
            // 生成性能评分
            double performanceScore = calculatePerformanceScore(performanceMetrics);
            report.put("performanceScore", performanceScore);
            report.put("performanceLevel", getPerformanceLevel(performanceScore));
            
        } catch (Exception e) {
            log.error("生成性能报告时发生错误", e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }    // 辅助方法

    private String escapeForCsv(String value) {
        if (value == null) return "";
        
        // 如果包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String getReportTitle(Map<String, Object> report) {
        String reportType = (String) report.get("reportType");
        switch (reportType) {
            case "DIAGNOSIS":
                return "时间线事件诊断报告";
            case "REPAIR":
                return "时间线事件修复报告";
            case "COMPREHENSIVE":
                return "时间线事件综合报告";
            case "SYSTEM_HEALTH":
                return "系统健康报告";
            case "PERFORMANCE":
                return "系统性能报告";
            default:
                return "系统报告";
        }
    }
    
    private String formatDateTime(Object dateTime) {
        if (dateTime instanceof LocalDateTime) {
            return ((LocalDateTime) dateTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return dateTime != null ? dateTime.toString() : "";
    }
    
    private void addDiagnosisHtmlContent(StringBuilder html, Map<String, Object> report) {
        // 问题统计
        if (report.containsKey("issueSummary")) {
            html.append("<h2>问题统计</h2>\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) report.get("issueSummary");
            
            html.append("<table>\n");
            html.append("<tr><th>总问题数</th><td>").append(summary.get("totalIssues")).append("</td></tr>\n");
            html.append("<tr><th>严重问题</th><td class=\"critical\">").append(summary.get("criticalIssues")).append("</td></tr>\n");
            html.append("<tr><th>高级问题</th><td class=\"high\">").append(summary.get("highIssues")).append("</td></tr>\n");
            html.append("<tr><th>中级问题</th><td class=\"medium\">").append(summary.get("mediumIssues")).append("</td></tr>\n");
            html.append("<tr><th>低级问题</th><td class=\"low\">").append(summary.get("lowIssues")).append("</td></tr>\n");
            html.append("<tr><th>可自动修复</th><td>").append(summary.get("autoRepairableIssues")).append("</td></tr>\n");
            html.append("</table>\n");
        }
        
        // 问题详情
        if (report.containsKey("issues")) {
            html.append("<h2>问题详情</h2>\n");
            html.append("<table>\n");
            html.append("<tr><th>类型</th><th>严重程度</th><th>描述</th><th>建议</th><th>可自动修复</th></tr>\n");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = (List<Map<String, Object>>) report.get("issues");
            for (Map<String, Object> issue : issues) {
                String severityClass = getSeverityClass((String) issue.get("severity"));
                html.append("<tr>");
                html.append("<td>").append(issue.get("type")).append("</td>");
                html.append("<td class=\"").append(severityClass).append("\">").append(issue.get("severity")).append("</td>");
                html.append("<td>").append(issue.get("description")).append("</td>");
                html.append("<td>").append(issue.get("recommendation")).append("</td>");
                html.append("<td>").append(issue.get("autoRepairable")).append("</td>");
                html.append("</tr>\n");
            }
            html.append("</table>\n");
        }
    }
    
    private void addRepairHtmlContent(StringBuilder html, Map<String, Object> report) {
        // 修复统计
        if (report.containsKey("repairSummary")) {
            html.append("<h2>修复统计</h2>\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) report.get("repairSummary");
            
            html.append("<table>\n");
            html.append("<tr><th>发现问题数</th><td>").append(summary.get("totalIssuesFound")).append("</td></tr>\n");
            html.append("<tr><th>修复成功数</th><td class=\"success\">").append(summary.get("issuesRepaired")).append("</td></tr>\n");
            html.append("<tr><th>修复失败数</th><td class=\"failed\">").append(summary.get("issuesFailed")).append("</td></tr>\n");
            html.append("<tr><th>成功率</th><td>").append(String.format("%.2f%%", summary.get("successRate"))).append("</td></tr>\n");
            html.append("</table>\n");
        }
        
        // 修复操作详情
        if (report.containsKey("actions")) {
            html.append("<h2>修复操作详情</h2>\n");
            html.append("<table>\n");
            html.append("<tr><th>操作类型</th><th>描述</th><th>状态</th><th>影响记录数</th></tr>\n");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = (List<Map<String, Object>>) report.get("actions");
            for (Map<String, Object> action : actions) {
                String statusClass = (Boolean) action.get("successful") ? "success" : "failed";
                String statusText = (Boolean) action.get("successful") ? "成功" : "失败";
                
                html.append("<tr>");
                html.append("<td>").append(action.get("type")).append("</td>");
                html.append("<td>").append(action.get("description")).append("</td>");
                html.append("<td class=\"").append(statusClass).append("\">").append(statusText).append("</td>");
                html.append("<td>").append(action.get("affectedRecords")).append("</td>");
                html.append("</tr>\n");
            }
            html.append("</table>\n");
        }
    }
    
    private void addComprehensiveHtmlContent(StringBuilder html, Map<String, Object> report) {
        // 综合统计
        if (report.containsKey("comprehensiveSummary")) {
            html.append("<h2>综合统计</h2>\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) report.get("comprehensiveSummary");
            
            html.append("<table>\n");
            html.append("<tr><th>发现问题总数</th><td>").append(summary.get("totalIssuesFound")).append("</td></tr>\n");
            html.append("<tr><th>修复问题数</th><td class=\"success\">").append(summary.get("issuesRepaired")).append("</td></tr>\n");
            html.append("<tr><th>剩余问题数</th><td>").append(summary.get("issuesRemaining")).append("</td></tr>\n");
            html.append("<tr><th>整体成功率</th><td>").append(String.format("%.2f%%", summary.get("overallSuccessRate"))).append("</td></tr>\n");
            html.append("</table>\n");
        }
        
        // 诊断部分
        if (report.containsKey("diagnosis")) {
            html.append("<h2>诊断详情</h2>\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> diagnosis = (Map<String, Object>) report.get("diagnosis");
            addDiagnosisHtmlContent(html, diagnosis);
        }
        
        // 修复部分
        if (report.containsKey("repair")) {
            html.append("<h2>修复详情</h2>\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> repair = (Map<String, Object>) report.get("repair");
            addRepairHtmlContent(html, repair);
        }
    }
    
    private String getSeverityClass(String severity) {
        if (severity == null) return "";
        switch (severity.toUpperCase()) {
            case "CRITICAL": return "critical";
            case "HIGH": return "high";
            case "MEDIUM": return "medium";
            case "LOW": return "low";
            default: return "";
        }
    }
    
    private double calculateHealthScore(Map<String, Object> healthStatus, List<com.hotech.events.dto.Alert> activeAlerts) {
        double score = 100.0;
        
        // 根据错误率扣分
        if (healthStatus.containsKey("errorRate")) {
            double errorRate = (Double) healthStatus.get("errorRate");
            score -= errorRate * 2; // 错误率每1%扣2分
        }
        
        // 根据活跃警告扣分
        for (com.hotech.events.dto.Alert alert : activeAlerts) {
            switch (alert.getSeverity()) {
                case CRITICAL:
                    score -= 20;
                    break;
                case HIGH:
                    score -= 10;
                    break;
                case MEDIUM:
                    score -= 5;
                    break;
                case LOW:
                    score -= 2;
                    break;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private String getHealthLevel(double score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "一般";
        if (score >= 60) return "较差";
        return "危险";
    }
    
    private double calculatePerformanceScore(Map<String, Object> metrics) {
        double score = 100.0;
        
        // 根据成功率计算分数
        if (metrics.containsKey("successRate")) {
            double successRate = (Double) metrics.get("successRate");
            score = successRate; // 成功率直接作为基础分数
        }
        
        // 根据平均响应时间调整分数
        if (metrics.containsKey("avgDuration")) {
            double avgDuration = (Double) metrics.get("avgDuration");
            if (avgDuration > 5000) { // 超过5秒
                score -= 20;
            } else if (avgDuration > 3000) { // 超过3秒
                score -= 10;
            } else if (avgDuration > 1000) { // 超过1秒
                score -= 5;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private String getPerformanceLevel(double score) {
        if (score >= 95) return "卓越";
        if (score >= 85) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 65) return "一般";
        return "需要改进";
    }
}