package com.hotech.events.service.impl;

import com.hotech.events.dto.DiagnosisIssue;
import com.hotech.events.dto.DiagnosisResult;
import com.hotech.events.entity.Timeline;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.mapper.TimelineEventMapper;
import com.hotech.events.mapper.TimelineMapper;
import com.hotech.events.service.TimelineEventDiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 时间线事件诊断服务实现类
 */
@Slf4j
@Service
public class TimelineEventDiagnosisServiceImpl implements TimelineEventDiagnosisService {

    @Autowired
    private TimelineMapper timelineMapper;

    @Autowired
    private TimelineEventMapper timelineEventMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private com.hotech.events.mapper.TimelineRegionMapper timelineRegionMapper;

    @Override
    public DiagnosisResult performFullDiagnosis() {
        log.info("开始执行完整系统诊断");

        try {
            List<DiagnosisIssue> allIssues = new ArrayList<>();
            Map<String, Object> statistics = new HashMap<>();

            // 检查数据一致性
            Map<String, Object> consistencyResult = checkDataConsistency();
            if (!(Boolean) consistencyResult.get("isConsistent")) {
                allIssues.addAll(createConsistencyIssues(consistencyResult));
            }

            // 检查事件关联
            Map<String, Object> associationResult = checkEventAssociations();
            if ((Integer) associationResult.get("invalidAssociations") > 0) {
                allIssues.addAll(createAssociationIssues(associationResult));
            }

            // 检查事件状态
            Map<String, Object> statusResult = checkEventStatus();
            if ((Integer) statusResult.get("disabledEvents") > 0) {
                allIssues.addAll(createStatusIssues(statusResult));
            }

            // 检查孤立数据
            Map<String, Object> orphanedResult = checkOrphanedData();
            if ((Integer) orphanedResult.get("orphanedCount") > 0) {
                allIssues.addAll(createOrphanedDataIssues(orphanedResult));
            }

            // 检查重复数据
            Map<String, Object> duplicateResult = checkDuplicateData();
            if ((Integer) duplicateResult.get("duplicateCount") > 0) {
                allIssues.addAll(createDuplicateDataIssues(duplicateResult));
            }

            // 汇总统计信息
            statistics.put("totalTimelines", timelineMapper.selectCount(null));
            statistics.put("totalEvents", eventMapper.selectCount(null));
            statistics.put("totalAssociations", timelineEventMapper.selectCount(null));
            statistics.put("issuesFound", allIssues.size());
            statistics.put("criticalIssues", allIssues.stream()
                    .mapToInt(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.CRITICAL ? 1 : 0)
                    .sum());

            // 确定整体诊断状态
            DiagnosisResult.DiagnosisStatus overallStatus = determineOverallStatus(allIssues);

            DiagnosisResult result = DiagnosisResult.builder()
                    .timelineId(null) // 全系统诊断
                    .timelineName("全系统诊断")
                    .diagnosisTime(LocalDateTime.now())
                    .status(overallStatus)
                    .issues(allIssues)
                    .statistics(statistics)
                    .build();

            log.info("完整系统诊断完成，发现 {} 个问题", allIssues.size());
            return result;

        } catch (Exception e) {
            log.error("执行完整系统诊断时发生错误", e);
            return DiagnosisResult.builder()
                    .diagnosisTime(LocalDateTime.now())
                    .status(DiagnosisResult.DiagnosisStatus.DIAGNOSIS_FAILED)
                    .issues(Collections.singletonList(
                            DiagnosisIssue.builder()
                                    .type(DiagnosisIssue.IssueType.PERFORMANCE_ISSUE)
                                    .severity(DiagnosisIssue.IssueSeverity.CRITICAL)
                                    .description("诊断过程中发生异常: " + e.getMessage())
                                    .recommendation("请检查系统日志并联系技术支持")
                                    .autoRepairable(false)
                                    .build()))
                    .build();
        }
    }

    @Override
    public DiagnosisResult diagnoseTimeline(Long timelineId) {
        log.info("开始诊断时间线: {}", timelineId);

        try {
            Timeline timeline = timelineMapper.selectById(timelineId);
            if (timeline == null) {
                return DiagnosisResult.builder()
                        .timelineId(timelineId)
                        .diagnosisTime(LocalDateTime.now())
                        .status(DiagnosisResult.DiagnosisStatus.DIAGNOSIS_FAILED)
                        .issues(Collections.singletonList(
                                DiagnosisIssue.builder()
                                        .type(DiagnosisIssue.IssueType.INVALID_ASSOCIATION)
                                        .severity(DiagnosisIssue.IssueSeverity.HIGH)
                                        .description("时间线不存在: " + timelineId)
                                        .recommendation("请检查时间线ID是否正确")
                                        .autoRepairable(false)
                                        .build()))
                        .build();
            }

            List<DiagnosisIssue> issues = new ArrayList<>();

            // 检查该时间线的事件数量一致性
            int expectedCount = timeline.getEventCount() != null ? timeline.getEventCount() : 0;
            int actualCount = timelineEventMapper.countEventsByTimelineId(timelineId);

            if (expectedCount != actualCount) {
                issues.add(DiagnosisIssue.builder()
                        .type(DiagnosisIssue.IssueType.DATA_INCONSISTENCY)
                        .severity(DiagnosisIssue.IssueSeverity.MEDIUM)
                        .description(String.format("时间线 %d 的事件数量不一致：预期 %d，实际 %d",
                                timelineId, expectedCount, actualCount))
                        .recommendation("执行数据修复以同步事件数量")
                        .autoRepairable(true)
                        .details(Map.of(
                                "timelineId", timelineId,
                                "expectedCount", expectedCount,
                                "actualCount", actualCount))
                        .build());
            }

            // 检查该时间线的事件关联有效性
            List<Long> eventIds = timelineEventMapper.findEventIdsByTimelineId(timelineId);
            List<Long> invalidEventIds = new ArrayList<>();

            for (Long eventId : eventIds) {
                if (eventMapper.selectById(eventId) == null) {
                    invalidEventIds.add(eventId);
                }
            }

            if (!invalidEventIds.isEmpty()) {
                issues.add(DiagnosisIssue.builder()
                        .type(DiagnosisIssue.IssueType.INVALID_ASSOCIATION)
                        .severity(DiagnosisIssue.IssueSeverity.HIGH)
                        .description(String.format("时间线 %d 包含 %d 个无效的事件关联",
                                timelineId, invalidEventIds.size()))
                        .recommendation("清理无效的事件关联记录")
                        .autoRepairable(true)
                        .details(Map.of(
                                "timelineId", timelineId,
                                "invalidEventIds", invalidEventIds))
                        .build());
            }

            // 构建诊断结果
            DiagnosisResult.DiagnosisStatus status = issues.isEmpty() ? DiagnosisResult.DiagnosisStatus.HEALTHY
                    : DiagnosisResult.DiagnosisStatus.ISSUES_FOUND;

            DiagnosisResult result = DiagnosisResult.builder()
                    .timelineId(timelineId)
                    .timelineName(timeline.getName())
                    .diagnosisTime(LocalDateTime.now())
                    .status(status)
                    .issues(issues)
                    .consistencyInfo(DiagnosisResult.DataConsistencyInfo.builder()
                            .expectedEventCount(expectedCount)
                            .actualEventCount(actualCount)
                            .isConsistent(expectedCount == actualCount)
                            .build())
                    .associationInfo(DiagnosisResult.EventAssociationInfo.builder()
                            .totalAssociations(eventIds.size())
                            .validAssociations(eventIds.size() - invalidEventIds.size())
                            .invalidEventIds(invalidEventIds.size())
                            .build())
                    .build();

            log.info("时间线 {} 诊断完成，发现 {} 个问题", timelineId, issues.size());
            return result;

        } catch (Exception e) {
            log.error("诊断时间线 {} 时发生错误", timelineId, e);
            return DiagnosisResult.builder()
                    .timelineId(timelineId)
                    .diagnosisTime(LocalDateTime.now())
                    .status(DiagnosisResult.DiagnosisStatus.DIAGNOSIS_FAILED)
                    .issues(Collections.singletonList(
                            DiagnosisIssue.builder()
                                    .type(DiagnosisIssue.IssueType.PERFORMANCE_ISSUE)
                                    .severity(DiagnosisIssue.IssueSeverity.CRITICAL)
                                    .description("诊断过程中发生异常: " + e.getMessage())
                                    .recommendation("请检查系统日志并联系技术支持")
                                    .autoRepairable(false)
                                    .build()))
                    .build();
        }
    }

    @Override
    public Map<String, Object> checkDataConsistency() {
        log.info("检查数据一致性");

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> inconsistentTimelines = new ArrayList<>();

        try {
            // 获取所有时间线
            List<Timeline> timelines = timelineMapper.selectList(null);
            int totalInconsistent = 0;

            for (Timeline timeline : timelines) {
                int expectedCount = timeline.getEventCount() != null ? timeline.getEventCount() : 0;
                int actualCount = timelineEventMapper.countEventsByTimelineId(timeline.getId());

                if (expectedCount != actualCount) {
                    totalInconsistent++;
                    inconsistentTimelines.add(Map.of(
                            "timelineId", timeline.getId(),
                            "timelineName", timeline.getName(),
                            "expectedCount", expectedCount,
                            "actualCount", actualCount,
                            "difference", Math.abs(expectedCount - actualCount)));
                }
            }

            result.put("isConsistent", totalInconsistent == 0);
            result.put("totalTimelines", timelines.size());
            result.put("inconsistentTimelines", totalInconsistent);
            result.put("inconsistentDetails", inconsistentTimelines);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查数据一致性时发生错误", e);
            result.put("error", e.getMessage());
            result.put("isConsistent", false);
        }

        return result;
    }

    @Override
    public Map<String, Object> checkEventAssociations() {
        log.info("检查事件关联关系");

        Map<String, Object> result = new HashMap<>();

        try {
            // 统计总关联数
            long totalAssociations = timelineEventMapper.selectCount(null);

            // 使用新增的Mapper方法查找无效关联
            List<Map<String, Object>> invalidEventAssociations = timelineEventMapper.findInvalidEventAssociations();
            List<Map<String, Object>> invalidTimelineAssociations = timelineEventMapper
                    .findInvalidTimelineAssociations();
            List<Map<String, Object>> duplicateAssociations = timelineEventMapper.findDuplicateAssociations();

            result.put("totalAssociations", totalAssociations);
            result.put("invalidEventAssociations", invalidEventAssociations.size());
            result.put("invalidTimelineAssociations", invalidTimelineAssociations.size());
            result.put("duplicateAssociations", duplicateAssociations.size());
            result.put("invalidAssociations", invalidEventAssociations.size() + invalidTimelineAssociations.size());
            result.put("validAssociations",
                    totalAssociations - invalidEventAssociations.size() - invalidTimelineAssociations.size());
            result.put("invalidEventDetails", invalidEventAssociations);
            result.put("invalidTimelineDetails", invalidTimelineAssociations);
            result.put("duplicateDetails", duplicateAssociations);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查事件关联关系时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> checkEventStatus() {
        log.info("检查事件状态");

        Map<String, Object> result = new HashMap<>();

        try {
            // 统计各种状态的事件
            long totalEvents = eventMapper.selectCount(null);

            // 使用新增的Mapper方法查询事件状态
            List<Map<String, Object>> statusStats = eventMapper.countEventsByStatus();
            List<Map<String, Object>> disabledEvents = eventMapper.findDisabledEvents();
            List<Map<String, Object>> nullStatusEvents = eventMapper.findEventsWithNullStatus();
            List<Map<String, Object>> orphanedEvents = eventMapper.findOrphanedEvents();
            List<Map<String, Object>> abnormalCreationTimeEvents = eventMapper.findEventsWithAbnormalCreationTime();
            List<Map<String, Object>> abnormalEventTimeEvents = eventMapper.findEventsWithAbnormalEventTime();

            // 计算启用和禁用的事件数量
            int enabledEvents = eventMapper.countEventsByStatus(1);
            int disabledEventsCount = eventMapper.countEventsByStatus(0);

            result.put("totalEvents", totalEvents);
            result.put("enabledEvents", enabledEvents);
            result.put("disabledEvents", disabledEventsCount);
            result.put("nullStatusEvents", nullStatusEvents.size());
            result.put("orphanedEvents", orphanedEvents.size());
            result.put("abnormalCreationTimeEvents", abnormalCreationTimeEvents.size());
            result.put("abnormalEventTimeEvents", abnormalEventTimeEvents.size());
            result.put("statusStatistics", statusStats);
            result.put("disabledEventDetails", disabledEvents);
            result.put("nullStatusEventDetails", nullStatusEvents);
            result.put("orphanedEventDetails", orphanedEvents);
            result.put("abnormalCreationTimeDetails", abnormalCreationTimeEvents);
            result.put("abnormalEventTimeDetails", abnormalEventTimeEvents);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查事件状态时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> checkOrphanedData() {
        log.info("检查孤立数据");

        Map<String, Object> result = new HashMap<>();

        try {
            // 查找没有关联到任何时间线的事件
            List<Long> allEventIds = eventMapper.selectList(null).stream()
                    .map(event -> event.getId())
                    .collect(Collectors.toList());

            List<Long> associatedEventIds = timelineEventMapper.selectList(null).stream()
                    .map(te -> te.getEventId())
                    .distinct()
                    .collect(Collectors.toList());

            List<Long> orphanedEventIds = allEventIds.stream()
                    .filter(eventId -> !associatedEventIds.contains(eventId))
                    .collect(Collectors.toList());

            result.put("totalEvents", allEventIds.size());
            result.put("associatedEvents", associatedEventIds.size());
            result.put("orphanedCount", orphanedEventIds.size());
            result.put("orphanedEventIds", orphanedEventIds);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查孤立数据时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> checkDuplicateData() {
        log.info("检查重复数据");

        Map<String, Object> result = new HashMap<>();

        try {
            // 使用新增的Mapper方法查找重复的时间线事件关联
            List<Map<String, Object>> duplicateAssociations = timelineEventMapper.findDuplicateAssociations();

            result.put("duplicateCount", duplicateAssociations.size());
            result.put("duplicateDetails", duplicateAssociations);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查重复数据时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> getDiagnosisStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalTimelines", timelineMapper.selectCount(null));
            stats.put("totalEvents", eventMapper.selectCount(null));
            stats.put("totalAssociations", timelineEventMapper.selectCount(null));
            stats.put("lastDiagnosisTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("获取诊断统计信息时发生错误", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @Override
    public List<DiagnosisResult> batchDiagnose(List<Long> timelineIds) {
        log.info("批量诊断 {} 个时间线", timelineIds.size());

        return timelineIds.stream()
                .map(this::diagnoseTimeline)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> checkTimelineGenerationStatus() {
        log.info("检查时间线生成任务状态");

        Map<String, Object> result = new HashMap<>();

        try {
            // 查找状态为GENERATING但创建时间超过一定时间的时间线
            List<Timeline> stuckTimelines = timelineMapper.findByStatus("GENERATING");
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1); // 1小时前

            List<Timeline> possiblyStuck = stuckTimelines.stream()
                    .filter(timeline -> timeline.getCreatedAt().isBefore(cutoffTime))
                    .collect(Collectors.toList());

            result.put("generatingTimelines", stuckTimelines.size());
            result.put("possiblyStuckTimelines", possiblyStuck.size());
            result.put("stuckTimelineDetails", possiblyStuck.stream()
                    .map(timeline -> Map.of(
                            "id", timeline.getId(),
                            "name", timeline.getName(),
                            "createdAt", timeline.getCreatedAt(),
                            "status", timeline.getStatus()))
                    .collect(Collectors.toList()));
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查时间线生成任务状态时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> checkRegionAssociations() {
        log.info("检查时间线与地区关联的完整性");

        Map<String, Object> result = new HashMap<>();

        try {
            // 统计总地区关联数
            long totalRegionAssociations = timelineRegionMapper.selectCount(null);

            // 使用新增的Mapper方法查找无效关联
            List<Map<String, Object>> invalidRegionAssociations = timelineRegionMapper.findInvalidRegionAssociations();
            List<Map<String, Object>> invalidTimelineRegionAssociations = timelineRegionMapper
                    .findInvalidTimelineRegionAssociations();
            List<Map<String, Object>> duplicateRegionAssociations = timelineRegionMapper
                    .findDuplicateRegionAssociations();
            List<Map<String, Object>> timelinesWithoutRegions = timelineRegionMapper.findTimelinesWithoutRegions();

            result.put("totalRegionAssociations", totalRegionAssociations);
            result.put("invalidRegionAssociations", invalidRegionAssociations.size());
            result.put("invalidTimelineRegionAssociations", invalidTimelineRegionAssociations.size());
            result.put("duplicateRegionAssociations", duplicateRegionAssociations.size());
            result.put("timelinesWithoutRegions", timelinesWithoutRegions.size());
            result.put("invalidAssociations",
                    invalidRegionAssociations.size() + invalidTimelineRegionAssociations.size());
            result.put("validAssociations", totalRegionAssociations - invalidRegionAssociations.size()
                    - invalidTimelineRegionAssociations.size());
            result.put("invalidRegionDetails", invalidRegionAssociations);
            result.put("invalidTimelineRegionDetails", invalidTimelineRegionAssociations);
            result.put("duplicateRegionDetails", duplicateRegionAssociations);
            result.put("timelinesWithoutRegionsDetails", timelinesWithoutRegions);
            result.put("checkTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("检查时间线与地区关联完整性时发生错误", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // 辅助方法
    private List<DiagnosisIssue> createConsistencyIssues(Map<String, Object> consistencyResult) {
        List<DiagnosisIssue> issues = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inconsistentDetails = (List<Map<String, Object>>) consistencyResult
                .get("inconsistentDetails");

        for (Map<String, Object> detail : inconsistentDetails) {
            issues.add(DiagnosisIssue.builder()
                    .type(DiagnosisIssue.IssueType.DATA_INCONSISTENCY)
                    .severity(DiagnosisIssue.IssueSeverity.MEDIUM)
                    .description(String.format("时间线 %s 事件数量不一致", detail.get("timelineName")))
                    .recommendation("执行数据修复以同步事件数量")
                    .autoRepairable(true)
                    .details(detail)
                    .build());
        }

        return issues;
    }

    private List<DiagnosisIssue> createAssociationIssues(Map<String, Object> associationResult) {
        List<DiagnosisIssue> issues = new ArrayList<>();

        int invalidCount = (Integer) associationResult.get("invalidAssociations");
        if (invalidCount > 0) {
            issues.add(DiagnosisIssue.builder()
                    .type(DiagnosisIssue.IssueType.INVALID_ASSOCIATION)
                    .severity(DiagnosisIssue.IssueSeverity.HIGH)
                    .description(String.format("发现 %d 个无效的事件关联", invalidCount))
                    .recommendation("清理无效的关联记录")
                    .autoRepairable(true)
                    .details(associationResult)
                    .build());
        }

        return issues;
    }

    private List<DiagnosisIssue> createStatusIssues(Map<String, Object> statusResult) {
        List<DiagnosisIssue> issues = new ArrayList<>();

        int disabledCount = (Integer) statusResult.get("disabledEvents");
        if (disabledCount > 0) {
            issues.add(DiagnosisIssue.builder()
                    .type(DiagnosisIssue.IssueType.EVENT_STATUS_ISSUE)
                    .severity(DiagnosisIssue.IssueSeverity.MEDIUM)
                    .description(String.format("发现 %d 个禁用状态的事件", disabledCount))
                    .recommendation("检查事件状态是否正确，必要时启用事件")
                    .autoRepairable(true)
                    .details(statusResult)
                    .build());
        }

        return issues;
    }

    private List<DiagnosisIssue> createOrphanedDataIssues(Map<String, Object> orphanedResult) {
        List<DiagnosisIssue> issues = new ArrayList<>();

        int orphanedCount = (Integer) orphanedResult.get("orphanedCount");
        if (orphanedCount > 0) {
            issues.add(DiagnosisIssue.builder()
                    .type(DiagnosisIssue.IssueType.ORPHANED_DATA)
                    .severity(DiagnosisIssue.IssueSeverity.LOW)
                    .description(String.format("发现 %d 个孤立的事件", orphanedCount))
                    .recommendation("考虑将孤立事件关联到相关时间线或清理无用数据")
                    .autoRepairable(false)
                    .details(orphanedResult)
                    .build());
        }

        return issues;
    }

    private List<DiagnosisIssue> createDuplicateDataIssues(Map<String, Object> duplicateResult) {
        List<DiagnosisIssue> issues = new ArrayList<>();

        int duplicateCount = (Integer) duplicateResult.get("duplicateCount");
        if (duplicateCount > 0) {
            issues.add(DiagnosisIssue.builder()
                    .type(DiagnosisIssue.IssueType.DUPLICATE_DATA)
                    .severity(DiagnosisIssue.IssueSeverity.MEDIUM)
                    .description(String.format("发现 %d 个重复的关联记录", duplicateCount))
                    .recommendation("清理重复的关联记录")
                    .autoRepairable(true)
                    .details(duplicateResult)
                    .build());
        }

        return issues;
    }

    private DiagnosisResult.DiagnosisStatus determineOverallStatus(List<DiagnosisIssue> issues) {
        if (issues.isEmpty()) {
            return DiagnosisResult.DiagnosisStatus.HEALTHY;
        }

        boolean hasCritical = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == DiagnosisIssue.IssueSeverity.CRITICAL);

        if (hasCritical) {
            return DiagnosisResult.DiagnosisStatus.CRITICAL_ISSUES;
        }

        return DiagnosisResult.DiagnosisStatus.ISSUES_FOUND;
    }
}