package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.service.EventReasoningClient;
import com.hotech.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件推理控制器
 * 提供事件关系分析、链条分析、相似度分析等推理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/event/reasoning")
@Tag(name = "事件推理", description = "事件推理相关的API接口")
public class EventReasoningController {

    @Autowired
    private EventReasoningClient eventReasoningClient;

    @Autowired
    private EventService eventService;

    /**
     * 分析事件关系
     */
    @PostMapping("/relations")
    @Operation(summary = "分析事件关系", description = "分析一组事件之间的关联关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeEventRelations(
            @RequestBody List<Long> eventIds) {
        try {
            log.info("分析事件关系请求，事件数量：{}", eventIds.size());
            
            // 获取事件详情
            List<EventDTO> events = eventIds.stream()
                .map(eventService::getEventDetail)
                .toList();
            
            // 调用推理服务
            Map<String, Object> result = eventReasoningClient.analyzeEventRelations(events);
            
            return ResponseEntity.ok(ApiResponse.success("事件关系分析完成", result));
        } catch (Exception e) {
            log.error("分析事件关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("分析事件关系失败：" + e.getMessage()));
        }
    }

    /**
     * 分析事件链条
     */
    @PostMapping("/chains")
    @Operation(summary = "分析事件链条", description = "分析事件链条和因果关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeEventChains(
            @RequestBody List<Long> eventIds) {
        try {
            log.info("分析事件链条请求，事件数量：{}", eventIds.size());
            
            // 获取事件详情
            List<EventDTO> events = eventIds.stream()
                .map(eventService::getEventDetail)
                .toList();
            
            // 调用推理服务
            Map<String, Object> result = eventReasoningClient.analyzeEventChains(events);
            
            return ResponseEntity.ok(ApiResponse.success("事件链条分析完成", result));
        } catch (Exception e) {
            log.error("分析事件链条失败", e);
            return ResponseEntity.ok(ApiResponse.error("分析事件链条失败：" + e.getMessage()));
        }
    }

    /**
     * 分析事件相似度
     */
    @PostMapping("/similarity")
    @Operation(summary = "分析事件相似度", description = "分析目标事件与候选事件的相似度")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeEventSimilarity(
            @Parameter(description = "目标事件ID") @RequestParam Long targetEventId,
            @Parameter(description = "候选事件ID列表") @RequestBody List<Long> candidateEventIds) {
        try {
            log.info("分析事件相似度请求，目标事件：{}，候选事件数量：{}", targetEventId, candidateEventIds.size());
            
            // 获取事件详情
            EventDTO targetEvent = eventService.getEventDetail(targetEventId);
            List<EventDTO> candidateEvents = candidateEventIds.stream()
                .map(eventService::getEventDetail)
                .toList();
            
            // 调用推理服务
            Map<String, Object> result = eventReasoningClient.analyzeEventSimilarity(targetEvent, candidateEvents);
            
            return ResponseEntity.ok(ApiResponse.success("事件相似度分析完成", result));
        } catch (Exception e) {
            log.error("分析事件相似度失败", e);
            return ResponseEntity.ok(ApiResponse.error("分析事件相似度失败：" + e.getMessage()));
        }
    }

    /**
     * 智能搜索热点事件
     */
    @PostMapping("/search-hot-events")
    @Operation(summary = "智能搜索热点事件", description = "调用event项目的智能搜索服务发现热点事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchHotEvents(
            @RequestBody Map<String, Object> searchRequest) {
        try {
            log.info("智能搜索热点事件请求：{}", searchRequest);
            
            // 构建搜索配置
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> searchConfigs = (List<Map<String, Object>>) searchRequest.get("configs");
            
            // 调用推理服务
            Map<String, Object> result = eventReasoningClient.searchHotEvents(searchConfigs);
            
            return ResponseEntity.ok(ApiResponse.success("智能搜索完成", result));
        } catch (Exception e) {
            log.error("智能搜索热点事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("智能搜索热点事件失败：" + e.getMessage()));
        }
    }

    /**
     * 批量分析最近事件关系
     */
    @PostMapping("/recent-relations")
    @Operation(summary = "分析最近事件关系", description = "分析最近一段时间内事件的关联关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeRecentEventRelations(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "最大事件数量") @RequestParam(defaultValue = "50") int maxEvents) {
        try {
            log.info("分析最近事件关系请求，天数：{}，最大事件数量：{}", days, maxEvents);
            
            // 获取最近的事件
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            // 这里需要实现一个按时间范围查询事件的方法
            // List<EventDTO> recentEvents = eventService.getEventsByTimeRange(startTime, LocalDateTime.now(), maxEvents);
            
            // 暂时使用统计API获取事件数量，实际实现需要添加时间范围查询
            Map<String, Object> stats = eventService.getStats();
            
            Map<String, Object> result = Map.of(
                "message", "最近事件关系分析功能正在开发中",
                "days", days,
                "maxEvents", maxEvents,
                "stats", stats
            );
            
            return ResponseEntity.ok(ApiResponse.success("最近事件关系分析（开发中）", result));
        } catch (Exception e) {
            log.error("分析最近事件关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("分析最近事件关系失败：" + e.getMessage()));
        }
    }

    /**
     * 检查推理服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查推理服务状态", description = "检查event项目推理服务的健康状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkReasoningServiceHealth() {
        try {
            log.info("检查推理服务状态");
            
            boolean isHealthy = eventReasoningClient.isEventServiceHealthy();
            
            Map<String, Object> result = Map.of(
                "healthy", isHealthy,
                "status", isHealthy ? "服务正常" : "服务异常",
                "checkTime", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(ApiResponse.success("推理服务状态检查完成", result));
        } catch (Exception e) {
            log.error("检查推理服务状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("检查推理服务状态失败：" + e.getMessage()));
        }
    }
} 