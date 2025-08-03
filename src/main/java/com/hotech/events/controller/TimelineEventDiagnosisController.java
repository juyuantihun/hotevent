package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.DiagnosisResult;
import com.hotech.events.dto.RepairResult;
import com.hotech.events.service.TimelineEventDiagnosisService;
import com.hotech.events.service.TimelineEventRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 时间线事件诊断控制器
 * 提供时间线事件诊断和修复的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/timeline-diagnosis")
@Tag(name = "时间线事件诊断", description = "时间线事件诊断和修复功能")
public class TimelineEventDiagnosisController {
    
    @Autowired
    private TimelineEventDiagnosisService diagnosisService;
    
    @Autowired
    private TimelineEventRepairService repairService;
    
    /**
     * 执行完整的系统诊断
     */
    @PostMapping("/full-diagnosis")
    @Operation(summary = "执行完整系统诊断", description = "检查所有时间线的事件关联情况")
    public ResponseEntity<ApiResponse<DiagnosisResult>> performFullDiagnosis() {
        log.info("接收到完整系统诊断请求");
        
        try {
            DiagnosisResult result = diagnosisService.performFullDiagnosis();
            
            log.info("完整系统诊断完成，发现 {} 个问题", result.getIssues().size());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("执行完整系统诊断失败", e);
            return ResponseEntity.ok(ApiResponse.error("诊断失败: " + e.getMessage()));
        }
    }
    
    /**
     * 诊断指定时间线
     */
    @PostMapping("/timeline/{timelineId}")
    @Operation(summary = "诊断指定时间线", description = "检查指定时间线的事件关联情况")
    public ResponseEntity<ApiResponse<DiagnosisResult>> diagnoseTimeline(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long timelineId) {
        log.info("接收到时间线诊断请求: timelineId={}", timelineId);
        
        try {
            DiagnosisResult result = diagnosisService.diagnoseTimeline(timelineId);
            
            log.info("时间线 {} 诊断完成，发现 {} 个问题", timelineId, result.getIssues().size());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("诊断时间线 {} 失败", timelineId, e);
            return ResponseEntity.ok(ApiResponse.error("诊断失败: " + e.getMessage()));
        }
    }    /**

     * 批量诊断多个时间线
     */
    @PostMapping("/batch-diagnosis")
    @Operation(summary = "批量诊断时间线", description = "批量检查多个时间线的事件关联情况")
    public ResponseEntity<ApiResponse<List<DiagnosisResult>>> batchDiagnose(
            @Parameter(description = "时间线ID列表", required = true) @RequestBody List<Long> timelineIds) {
        log.info("接收到批量诊断请求: timelineIds={}", timelineIds);
        
        try {
            List<DiagnosisResult> results = diagnosisService.batchDiagnose(timelineIds);
            
            log.info("批量诊断完成，处理 {} 个时间线", timelineIds.size());
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            log.error("批量诊断失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量诊断失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查数据一致性
     */
    @GetMapping("/check/data-consistency")
    @Operation(summary = "检查数据一致性", description = "验证timeline表中的event_count与实际关联数量是否一致")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDataConsistency() {
        log.info("接收到数据一致性检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkDataConsistency();
            
            log.info("数据一致性检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("数据一致性检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查事件关联关系
     */
    @GetMapping("/check/event-associations")
    @Operation(summary = "检查事件关联关系", description = "验证timeline_event表中的关联是否有效")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEventAssociations() {
        log.info("接收到事件关联检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkEventAssociations();
            
            log.info("事件关联检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("事件关联检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查事件状态
     */
    @GetMapping("/check/event-status")
    @Operation(summary = "检查事件状态", description = "验证event表中事件的状态是否正常")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEventStatus() {
        log.info("接收到事件状态检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkEventStatus();
            
            log.info("事件状态检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("事件状态检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查孤立数据
     */
    @GetMapping("/check/orphaned-data")
    @Operation(summary = "检查孤立数据", description = "查找没有关联到任何时间线的事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkOrphanedData() {
        log.info("接收到孤立数据检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkOrphanedData();
            
            log.info("孤立数据检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("孤立数据检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查重复数据
     */
    @GetMapping("/check/duplicate-data")
    @Operation(summary = "检查重复数据", description = "查找重复的时间线事件关联")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDuplicateData() {
        log.info("接收到重复数据检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkDuplicateData();
            
            log.info("重复数据检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("重复数据检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查地区关联
     */
    @GetMapping("/check/region-associations")
    @Operation(summary = "检查地区关联", description = "验证timeline_region表中的关联是否有效")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRegionAssociations() {
        log.info("接收到地区关联检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkRegionAssociations();
            
            log.info("地区关联检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("地区关联检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }  
  /**
     * 检查时间线生成任务状态
     */
    @GetMapping("/check/generation-status")
    @Operation(summary = "检查时间线生成任务状态", description = "查找可能失败的时间线生成任务")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTimelineGenerationStatus() {
        log.info("接收到时间线生成任务状态检查请求");
        
        try {
            Map<String, Object> result = diagnosisService.checkTimelineGenerationStatus();
            
            log.info("时间线生成任务状态检查完成");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("时间线生成任务状态检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取诊断统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取诊断统计信息", description = "获取系统诊断的统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDiagnosisStatistics() {
        log.info("接收到诊断统计信息请求");
        
        try {
            Map<String, Object> statistics = diagnosisService.getDiagnosisStatistics();
            
            log.info("诊断统计信息获取完成");
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取诊断统计信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    // 修复相关API
    
    /**
     * 执行完整的系统修复
     */
    @PostMapping("/repair/full-repair")
    @Operation(summary = "执行完整系统修复", description = "修复所有检测到的问题")
    public ResponseEntity<ApiResponse<RepairResult>> performFullRepair() {
        log.info("接收到完整系统修复请求");
        
        try {
            RepairResult result = repairService.performFullRepair();
            
            log.info("完整系统修复完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("执行完整系统修复失败", e);
            return ResponseEntity.ok(ApiResponse.error("修复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修复指定时间线
     */
    @PostMapping("/repair/timeline/{timelineId}")
    @Operation(summary = "修复指定时间线", description = "修复指定时间线的所有问题")
    public ResponseEntity<ApiResponse<RepairResult>> repairTimeline(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long timelineId) {
        log.info("接收到时间线修复请求: timelineId={}", timelineId);
        
        try {
            RepairResult result = repairService.repairTimeline(timelineId);
            
            log.info("时间线 {} 修复完成，状态: {}", timelineId, result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("修复时间线 {} 失败", timelineId, e);
            return ResponseEntity.ok(ApiResponse.error("修复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量修复时间线
     */
    @PostMapping("/repair/batch-repair")
    @Operation(summary = "批量修复时间线", description = "批量修复多个时间线的问题")
    public ResponseEntity<ApiResponse<List<RepairResult>>> batchRepair(
            @Parameter(description = "时间线ID列表", required = true) @RequestBody List<Long> timelineIds) {
        log.info("接收到批量修复请求: timelineIds={}", timelineIds);
        
        try {
            List<RepairResult> results = repairService.batchRepair(timelineIds);
            
            log.info("批量修复完成，处理 {} 个时间线", timelineIds.size());
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            log.error("批量修复失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量修复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重建事件数量统计
     */
    @PostMapping("/repair/rebuild-event-counts")
    @Operation(summary = "重建事件数量统计", description = "同步timeline表中的event_count与实际关联数量")
    public ResponseEntity<ApiResponse<RepairResult>> rebuildEventCounts() {
        log.info("接收到重建事件数量统计请求");
        
        try {
            RepairResult result = repairService.rebuildEventCounts();
            
            log.info("重建事件数量统计完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("重建事件数量统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("重建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清理无效关联
     */
    @PostMapping("/repair/cleanup-invalid-associations")
    @Operation(summary = "清理无效关联", description = "删除指向不存在事件或时间线的关联")
    public ResponseEntity<ApiResponse<RepairResult>> cleanupInvalidAssociations() {
        log.info("接收到清理无效关联请求");
        
        try {
            RepairResult result = repairService.cleanupInvalidAssociations();
            
            log.info("清理无效关联完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("清理无效关联失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清理重复关联
     */
    @PostMapping("/repair/cleanup-duplicate-associations")
    @Operation(summary = "清理重复关联", description = "删除timeline_event表中的重复关联")
    public ResponseEntity<ApiResponse<RepairResult>> cleanupDuplicateAssociations() {
        log.info("接收到清理重复关联请求");
        
        try {
            RepairResult result = repairService.cleanupDuplicateAssociations();
            
            log.info("清理重复关联完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("清理重复关联失败", e);
            return ResponseEntity.ok(ApiResponse.error("清理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修复地区关联
     */
    @PostMapping("/repair/region-associations")
    @Operation(summary = "修复地区关联", description = "清理无效的时间线地区关联")
    public ResponseEntity<ApiResponse<RepairResult>> repairRegionAssociations() {
        log.info("接收到修复地区关联请求");
        
        try {
            RepairResult result = repairService.repairRegionAssociations();
            
            log.info("修复地区关联完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("修复地区关联失败", e);
            return ResponseEntity.ok(ApiResponse.error("修复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动关联孤立事件
     */
    @PostMapping("/repair/auto-associate-orphaned-events")
    @Operation(summary = "自动关联孤立事件", description = "为孤立事件自动创建时间线关联")
    public ResponseEntity<ApiResponse<RepairResult>> autoAssociateOrphanedEvents() {
        log.info("接收到自动关联孤立事件请求");
        
        try {
            RepairResult result = repairService.autoAssociateOrphanedEvents();
            
            log.info("自动关联孤立事件完成，状态: {}", result.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("自动关联孤立事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("自动关联失败: " + e.getMessage()));
        }
    }
}