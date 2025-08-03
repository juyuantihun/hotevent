package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.service.DynamicDeepSeekService;
import com.hotech.events.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间线测试控制器
 * 用于测试基于时间的动态API选择功能
 */
@Slf4j
@RestController
@RequestMapping("/api/timeline-test")
@Tag(name = "时间线测试", description = "时间线动态API选择测试功能")
public class TimelineTestController {

    @Autowired
    private DynamicDeepSeekService dynamicDeepSeekService;

    @Autowired
    private TimelineService timelineService;

    /**
     * 测试API选择逻辑
     */
    @PostMapping("/test-api-selection")
    @Operation(summary = "测试API选择逻辑", description = "根据时间范围测试API选择逻辑")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testApiSelection(
            @RequestBody Map<String, String> request) {
        
        try {
            String startTimeStr = request.get("startTime");
            String endTimeStr = request.get("endTime");
            
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            boolean shouldUseWebSearch = dynamicDeepSeekService.shouldUseWebSearch(startTime, endTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("shouldUseWebSearch", shouldUseWebSearch);
            result.put("selectedApi", shouldUseWebSearch ? "火山引擎联网搜索API" : "DeepSeek官方API");
            result.put("reason", shouldUseWebSearch ? 
                "时间范围包含2024年及以后，需要获取最新信息" : 
                "时间范围在2024年以前，使用历史数据即可");
            
            // 添加时间分界点信息
            result.put("timeBoundary", "2024-01-01T00:00:00");
            result.put("isHistoricalData", endTime.isBefore(LocalDateTime.of(2024, 1, 1, 0, 0)));
            result.put("isRecentData", startTime.isAfter(LocalDateTime.of(2024, 1, 1, 0, 0)));
            result.put("isCrossTimeBoundary", 
                startTime.isBefore(LocalDateTime.of(2024, 1, 1, 0, 0)) && 
                endTime.isAfter(LocalDateTime.of(2024, 1, 1, 0, 0)));
            
            log.info("API选择测试: startTime={}, endTime={}, selectedApi={}", 
                    startTime, endTime, shouldUseWebSearch ? "火山引擎" : "官方API");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("API选择测试失败", e);
            return ResponseEntity.ok(ApiResponse.error("API选择测试失败: " + e.getMessage()));
        }
    }

    /**
     * 测试时间线生成（历史数据）
     */
    @PostMapping("/test-historical-timeline")
    @Operation(summary = "测试历史时间线生成", description = "测试2024年以前的时间线生成")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testHistoricalTimeline() {
        try {
            // 创建2023年的时间线生成请求
            LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2023, 12, 31, 23, 59);
            
            Long timelineId = timelineService.generateTimelineAsync(
                    "2023年历史事件时间线测试",
                    "测试使用官方API生成2023年历史事件时间线",
                    Arrays.asList(1L), // 假设地区ID为1
                    startTime,
                    endTime
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("timelineId", timelineId);
            result.put("name", "2023年历史事件时间线测试");
            result.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("expectedApi", "DeepSeek官方API");
            result.put("status", "GENERATING");
            result.put("message", "历史时间线生成任务已提交，将使用官方API");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("历史时间线生成测试失败", e);
            return ResponseEntity.ok(ApiResponse.error("历史时间线生成测试失败: " + e.getMessage()));
        }
    }

    /**
     * 测试时间线生成（最新数据）
     */
    @PostMapping("/test-recent-timeline")
    @Operation(summary = "测试最新时间线生成", description = "测试2024年及以后的时间线生成")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testRecentTimeline() {
        try {
            // 创建2024年至今的时间线生成请求
            LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.now();
            
            Long timelineId = timelineService.generateTimelineAsync(
                    "2024年最新事件时间线测试",
                    "测试使用火山引擎联网搜索API生成2024年最新事件时间线",
                    Arrays.asList(1L), // 假设地区ID为1
                    startTime,
                    endTime
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("timelineId", timelineId);
            result.put("name", "2024年最新事件时间线测试");
            result.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("expectedApi", "火山引擎联网搜索API");
            result.put("status", "GENERATING");
            result.put("message", "最新时间线生成任务已提交，将使用联网搜索API");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("最新时间线生成测试失败", e);
            return ResponseEntity.ok(ApiResponse.error("最新时间线生成测试失败: " + e.getMessage()));
        }
    }

    /**
     * 测试跨时间边界的时间线生成
     */
    @PostMapping("/test-cross-boundary-timeline")
    @Operation(summary = "测试跨时间边界时间线生成", description = "测试跨越2024年时间边界的时间线生成")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testCrossBoundaryTimeline() {
        try {
            // 创建跨越2024年的时间线生成请求
            LocalDateTime startTime = LocalDateTime.of(2023, 6, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2024, 6, 30, 23, 59);
            
            Long timelineId = timelineService.generateTimelineAsync(
                    "跨时间边界事件时间线测试",
                    "测试跨越2024年时间边界的时间线生成，应使用联网搜索API",
                    Arrays.asList(1L), // 假设地区ID为1
                    startTime,
                    endTime
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("timelineId", timelineId);
            result.put("name", "跨时间边界事件时间线测试");
            result.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("expectedApi", "火山引擎联网搜索API");
            result.put("status", "GENERATING");
            result.put("message", "跨边界时间线生成任务已提交，将使用联网搜索API");
            result.put("crossesBoundary", true);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("跨边界时间线生成测试失败", e);
            return ResponseEntity.ok(ApiResponse.error("跨边界时间线生成测试失败: " + e.getMessage()));
        }
    }
}