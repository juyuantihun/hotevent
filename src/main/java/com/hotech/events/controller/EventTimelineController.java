package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.entity.EventNode;
import com.hotech.events.service.EventTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件时间线控制器
 * 提供事件关联分析和图形化展示功能
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/timeline")
@Tag(name = "事件时间线", description = "事件关联分析和图形化展示")
@ConditionalOnProperty(name = "spring.neo4j.uri")
public class EventTimelineController {
    
    @Autowired(required = false)
    private EventTimelineService timelineService;
    
    /**
     * 检查Neo4j服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查服务状态", description = "检查Neo4j服务是否可用")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("neo4jEnabled", timelineService != null);
        status.put("message", timelineService != null ? "Neo4j服务可用" : "Neo4j服务不可用");
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    /**
     * 同步MySQL事件到Neo4j
     */
    @PostMapping("/sync")
    @Operation(summary = "同步事件到Neo4j", description = "将MySQL中的事件同步到Neo4j图数据库")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncEvents(
            @Parameter(description = "事件编码列表，为空则同步所有事件")
            @RequestBody(required = false) List<String> eventCodes) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.syncEventsToNeo4j(eventCodes);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("同步事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("同步事件失败：" + e.getMessage()));
        }
    }
    
    /**
     * 分析并建立事件关联关系
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析事件关联", description = "使用DeepSeek R1模型分析事件之间的关联关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeRelations(
            @Parameter(description = "事件编码列表，为空则分析所有事件")
            @RequestBody(required = false) List<String> eventCodes) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.analyzeAndCreateRelations(eventCodes);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分析事件关联失败", e);
            return ResponseEntity.ok(ApiResponse.error("分析事件关联失败：" + e.getMessage()));
        }
    }
    
    /**
     * 批量事件关联分析测试
     */
    @PostMapping("/batch-relation-analysis")
    @Operation(summary = "批量事件关联分析", description = "使用DeepSeek R1对多个事件进行批量关联分析（非两两分析）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchRelationAnalysis(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> eventCodes = (List<String>) request.get("eventCodes");
            
            if (eventCodes == null || eventCodes.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.validateFailed("事件编码列表不能为空"));
            }
            
            if (eventCodes.size() > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.validateFailed("批量分析最多支持100个事件"));
            }
            
            log.info("开始批量事件关联分析，事件数量：{}", eventCodes.size());
            
            // 调用事件时间线服务进行批量关联分析
            Map<String, Object> result = timelineService.analyzeAndCreateRelations(eventCodes);
            
            // 添加批量分析标识
            result.put("analysisType", "BATCH_DEEPSEEK_R1");
            result.put("analysisTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("message", "使用DeepSeek R1进行批量关联分析，而非两两分析");
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("批量事件关联分析失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量关联分析失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取事件时间线
     */
    @GetMapping("/timeline/{eventCode}")
    @Operation(summary = "获取事件时间线", description = "获取以指定事件为起点的时间线")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventTimeline(
            @Parameter(description = "事件编码", required = true)
            @PathVariable String eventCode) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.getEventTimeline(eventCode);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取事件时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取事件时间线失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取事件关联图
     */
    @GetMapping("/graph/{eventCode}")
    @Operation(summary = "获取事件关联图", description = "获取以指定事件为中心的关联图")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventGraph(
            @Parameter(description = "事件编码", required = true)
            @PathVariable String eventCode,
            @Parameter(description = "关联深度，默认为2")
            @RequestParam(defaultValue = "2") int depth) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.getEventGraph(eventCode, depth);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取事件关联图失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取事件关联图失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取事件集群
     */
    @GetMapping("/cluster/{eventCode}")
    @Operation(summary = "获取事件集群", description = "获取与指定事件相似的事件集群")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventCluster(
            @Parameter(description = "事件编码", required = true)
            @PathVariable String eventCode,
            @Parameter(description = "最大集群大小，默认为10")
            @RequestParam(defaultValue = "10") int maxSize) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.getEventCluster(eventCode, maxSize);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取事件集群失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取事件集群失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取因果关系链
     */
    @GetMapping("/causal/{eventCode}")
    @Operation(summary = "获取因果关系链", description = "获取以指定事件为起点的因果关系链")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCausalChain(
            @Parameter(description = "事件编码", required = true)
            @PathVariable String eventCode) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.getCausalChain(eventCode);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取因果关系链失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取因果关系链失败：" + e.getMessage()));
        }
    }
    
    /**
     * 搜索相关事件
     */
    @GetMapping("/search")
    @Operation(summary = "搜索相关事件", description = "根据关键词搜索相关事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchRelatedEvents(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "结果数量限制，默认为20")
            @RequestParam(defaultValue = "20") int limit) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Map<String, Object> result = timelineService.searchRelatedEvents(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("搜索相关事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("搜索相关事件失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取热点事件
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热点事件", description = "获取关联度最高的热点事件")
    public ResponseEntity<ApiResponse<List<EventNode>>> getHotEvents(
            @Parameter(description = "结果数量限制，默认为10")
            @RequestParam(defaultValue = "10") int limit) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            List<EventNode> result = timelineService.getHotEvents(limit);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取热点事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取热点事件失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取关联统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取关联统计", description = "获取事件关联的统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRelationStatistics() {
        
        if (timelineService == null) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("message", "Neo4j服务不可用，无法获取关联统计");
            emptyStats.put("totalEvents", 0);
            emptyStats.put("totalRelations", 0);
            return ResponseEntity.ok(ApiResponse.success(emptyStats));
        }
        
        try {
            Map<String, Object> result = timelineService.getRelationStatistics();
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取关联统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取关联统计失败：" + e.getMessage()));
        }
    }
    
    /**
     * 删除事件及其关联关系
     */
    @DeleteMapping("/{eventCode}")
    @Operation(summary = "删除事件关联", description = "删除指定事件及其所有关联关系")
    public ResponseEntity<ApiResponse<Boolean>> deleteEventAndRelations(
            @Parameter(description = "事件编码", required = true)
            @PathVariable String eventCode) {
        
        if (timelineService == null) {
            return ResponseEntity.ok(ApiResponse.error("Neo4j服务不可用"));
        }
        
        try {
            Boolean result = timelineService.deleteEventAndRelations(eventCode);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除事件关联失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除事件关联失败：" + e.getMessage()));
        }
    }
} 