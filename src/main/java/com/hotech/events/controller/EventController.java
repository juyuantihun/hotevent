package com.hotech.events.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.dto.event.EventQueryDTO;
import com.hotech.events.dto.event.BatchEventRequestDTO;
import com.hotech.events.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 事件控制器
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/event")
@Tag(name = "事件管理", description = "事件相关的API接口")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * 获取事件列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取事件列表", description = "分页查询事件列表")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getEventList(EventQueryDTO queryDTO) {
        try {
            log.info("获取事件列表请求，查询条件：{}", queryDTO);
            
            Page<EventDTO> result = eventService.getEventList(queryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取事件列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取事件详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取事件详情", description = "根据ID获取事件详细信息")
    public ResponseEntity<ApiResponse<EventDTO>> getEventDetail(
            @Parameter(description = "事件ID") @PathVariable Long id) {
        try {
            log.info("获取事件详情请求，ID：{}", id);
            
            EventDTO result = eventService.getEventDetail(id);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取事件详情失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 创建事件
     */
    @PostMapping("/create")
    @Operation(summary = "创建事件", description = "创建新的事件")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(
            @RequestBody EventDTO eventDTO) {
        try {
            log.info("创建事件请求，事件信息：{}", eventDTO);
            log.info("eventTime: {}, sourceType: {}", eventDTO.getEventTime(), eventDTO.getSourceType());
            log.info("eventTitle: {}, eventDescription: {}, eventLocation: {}", 
                    eventDTO.getEventTitle(), eventDTO.getEventDescription(), eventDTO.getEventLocation());
            
            // 手动验证必要字段
            if (eventDTO.getEventTime() == null) {
                return ResponseEntity.ok(ApiResponse.error("事件时间不能为空"));
            }
            if (eventDTO.getSourceType() == null) {
                return ResponseEntity.ok(ApiResponse.error("来源类型不能为空"));
            }
            
            EventDTO result = eventService.createEvent(eventDTO);
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", result));
        } catch (Exception e) {
            log.error("创建事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 批量创建事件
     */
    @PostMapping("/batch")
    @Operation(summary = "批量创建事件", description = "批量创建多个事件及其关联关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createEventsBatch(
            @Valid @RequestBody BatchEventRequestDTO batchRequest) {
        try {
            log.info("批量创建事件请求，事件数量：{}，关联数量：{}", 
                    batchRequest.getEvents().size(), 
                    batchRequest.getRelations() != null ? batchRequest.getRelations().size() : 0);
            
            Map<String, Object> result = eventService.createEventsBatchWithRelations(batchRequest);
            
            return ResponseEntity.ok(ApiResponse.success("批量创建完成", result));
        } catch (Exception e) {
            log.error("批量创建事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新事件
     */
    @PutMapping("/update")
    @Operation(summary = "更新事件", description = "更新事件信息")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @Valid @RequestBody EventDTO eventDTO) {
        try {
            log.info("更新事件请求，事件信息：{}", eventDTO);
            
            EventDTO result = eventService.updateEvent(eventDTO);
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", result));
        } catch (Exception e) {
            log.error("更新事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除事件
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除事件", description = "根据ID删除事件")
    public ResponseEntity<ApiResponse<String>> deleteEvent(
            @Parameter(description = "事件ID") @PathVariable Long id) {
        try {
            log.info("删除事件请求，ID：{}", id);
            
            Boolean result = eventService.deleteEvent(id);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("删除失败"));
            }
        } catch (Exception e) {
            log.error("删除事件失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 批量删除事件
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除事件", description = "批量删除多个事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteEventsBatch(
            @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            log.info("批量删除事件请求，IDs：{}", ids);
            
            Integer successCount = eventService.deleteEventsBatch(ids);
            
            Map<String, Object> result = Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", ids.size() - successCount
            );
            
            return ResponseEntity.ok(ApiResponse.success("批量删除完成", result));
        } catch (Exception e) {
            log.error("批量删除事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取事件关联图谱
     */
    @GetMapping("/relation/graph/{eventId}")
    @Operation(summary = "获取事件关联图谱", description = "获取事件的关联关系图谱数据")
    public ResponseEntity<ApiResponse<Object>> getEventGraph(
            @Parameter(description = "事件ID") @PathVariable Long eventId) {
        try {
            log.info("获取事件关联图谱请求，事件ID：{}", eventId);
            
            Object result = eventService.getEventGraph(eventId);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取事件关联图谱失败，事件ID：{}", eventId, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 导出所有事件数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出事件数据", description = "导出所有事件数据用于Excel导出")
    public ResponseEntity<ApiResponse<List<EventDTO>>> exportEvents() {
        try {
            log.info("导出事件数据请求");
            
            List<EventDTO> result = eventService.exportAllEvents();
            
            return ResponseEntity.ok(ApiResponse.success("导出成功", result));
        } catch (Exception e) {
            log.error("导出事件数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("导出失败：" + e.getMessage()));
        }
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计数据", description = "获取事件统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            log.info("获取统计数据请求");
            
            Map<String, Object> result = eventService.getStats();
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取地理分布统计数据
     */
    @GetMapping("/geographic-stats")
    @Operation(summary = "获取地理分布统计", description = "获取按国家/地区统计的事件分布数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGeographicStats() {
        try {
            log.info("获取地理分布统计数据请求");
            
            Map<String, Object> result = eventService.getGeographicStats();
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取地理分布统计数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取未关联到指定时间线的事件列表
     */
    @GetMapping("/unlinked/{timelineId}")
    @Operation(summary = "获取未关联事件列表", description = "获取未关联到指定时间线的事件列表")
    public ResponseEntity<ApiResponse<Page<EventDTO>>> getUnlinkedEvents(
            @Parameter(description = "时间线ID") @PathVariable Long timelineId,
            EventQueryDTO queryDTO) {
        try {
            log.info("获取未关联事件列表请求，时间线ID：{}，查询条件：{}", timelineId, queryDTO);
            
            Page<EventDTO> result = eventService.getUnlinkedEvents(timelineId, queryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取未关联事件列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取事件类型分布统计数据
     */
    @GetMapping("/type-stats")
    @Operation(summary = "获取事件类型分布统计", description = "获取按事件类型统计的分布数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventTypeStats() {
        try {
            log.info("获取事件类型分布统计数据请求");
            
            Map<String, Object> result = eventService.getEventTypeStats();
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取事件类型分布统计数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
} 