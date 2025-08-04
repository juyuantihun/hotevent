package com.hotech.events.controller;

import com.hotech.events.common.Result;
import com.hotech.events.entity.Event;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.service.EventGeographicEnhancementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 地理信息增强控制器
 * 提供地理信息增强相关的API接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/geographic-enhancement")
@Tag(name = "地理信息增强", description = "地理信息增强相关的API接口")
@RequiredArgsConstructor
public class GeographicEnhancementController {

    private final EventGeographicEnhancementService eventGeographicEnhancementService;
    private final EventMapper eventMapper;

    /**
     * 获取地理信息增强统计
     */
    @Operation(summary = "获取地理信息增强统计", description = "获取地理信息增强的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<Result<String>> getStatistics() {
        try {
            String statistics = eventGeographicEnhancementService.getEnhancementStatistics();
            return ResponseEntity.ok(Result.success("获取统计信息成功", statistics));
        } catch (Exception e) {
            log.error("获取地理信息增强统计失败", e);
            return ResponseEntity.ok(Result.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 为单个事件增强地理信息
     */
    @Operation(summary = "为单个事件增强地理信息", description = "为指定ID的事件增强地理信息")
    @PostMapping("/enhance-event/{eventId}")
    public ResponseEntity<Result<Map<String, Object>>> enhanceEventGeographicInfo(@PathVariable Long eventId) {
        try {
            // 获取事件
            Event event = eventMapper.selectById(eventId);
            if (event == null) {
                return ResponseEntity.ok(Result.error("事件不存在"));
            }

            // 记录增强前的状态
            boolean hadCoordinatesBefore = !eventGeographicEnhancementService.needsGeographicEnhancement(event);
            Double latitudeBefore = event.getLatitude() != null ? event.getLatitude().doubleValue() : null;
            Double longitudeBefore = event.getLongitude() != null ? event.getLongitude().doubleValue() : null;

            // 进行地理信息增强
            Event enhancedEvent = eventGeographicEnhancementService.enhanceEventGeographicInfo(event);

            // 更新数据库
            eventMapper.updateById(enhancedEvent);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("eventId", eventId);
            response.put("hadCoordinatesBefore", hadCoordinatesBefore);
            response.put("hasCoordinatesAfter",
                    !eventGeographicEnhancementService.needsGeographicEnhancement(enhancedEvent));
            response.put("coordinatesBefore", Map.of(
                    "latitude", latitudeBefore,
                    "longitude", longitudeBefore));
            response.put("coordinatesAfter", Map.of(
                    "latitude", enhancedEvent.getLatitude() != null ? enhancedEvent.getLatitude().doubleValue() : null,
                    "longitude",
                    enhancedEvent.getLongitude() != null ? enhancedEvent.getLongitude().doubleValue() : null));
            response.put("location", enhancedEvent.getEventLocation());
            response.put("geographicStatus", enhancedEvent.getGeographicStatus());

            return ResponseEntity.ok(Result.success("地理信息增强完成", response));

        } catch (Exception e) {
            log.error("为事件 {} 增强地理信息失败", eventId, e);
            return ResponseEntity.ok(Result.error("地理信息增强失败: " + e.getMessage()));
        }
    }

    /**
     * 批量增强缺少地理信息的事件
     */
    @Operation(summary = "批量增强地理信息", description = "为所有缺少地理信息的事件批量增强坐标")
    @PostMapping("/enhance-missing")
    public ResponseEntity<Result<Map<String, Object>>> enhanceMissingGeographicInfo(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            // 查询缺少地理信息的事件
            List<Event> eventsNeedingEnhancement = eventMapper.selectList(
                    new LambdaQueryWrapper<Event>()
                            .and(wrapper -> wrapper
                                    .isNull(Event::getLatitude)
                                    .or()
                                    .isNull(Event::getLongitude)
                                    .or()
                                    .eq(Event::getGeographicStatus, 0))
                            .last("LIMIT " + limit));

            if (eventsNeedingEnhancement.isEmpty()) {
                return ResponseEntity.ok(Result.success("没有需要增强地理信息的事件", Map.of(
                        "processedCount", 0,
                        "enhancedCount", 0)));
            }

            log.info("开始批量增强 {} 个事件的地理信息", eventsNeedingEnhancement.size());

            int enhancedCount = 0;
            for (Event event : eventsNeedingEnhancement) {
                boolean needsEnhancement = eventGeographicEnhancementService.needsGeographicEnhancement(event);

                Event enhancedEvent = eventGeographicEnhancementService.enhanceEventGeographicInfo(event);
                eventMapper.updateById(enhancedEvent);

                if (needsEnhancement && !eventGeographicEnhancementService.needsGeographicEnhancement(enhancedEvent)) {
                    enhancedCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("processedCount", eventsNeedingEnhancement.size());
            response.put("enhancedCount", enhancedCount);
            response.put("statistics", eventGeographicEnhancementService.getEnhancementStatistics());

            log.info("批量地理信息增强完成: 处理 {} 个事件，成功增强 {} 个",
                    eventsNeedingEnhancement.size(), enhancedCount);

            return ResponseEntity.ok(Result.success("批量地理信息增强完成", response));

        } catch (Exception e) {
            log.error("批量增强地理信息失败", e);
            return ResponseEntity.ok(Result.error("批量增强失败: " + e.getMessage()));
        }
    }

    /**
     * 根据地点名称获取坐标
     */
    @Operation(summary = "根据地点名称获取坐标", description = "根据地点名称获取经纬度坐标")
    @GetMapping("/coordinates")
    public ResponseEntity<Result<Map<String, Object>>> getCoordinatesByLocation(
            @RequestParam String locationName) {
        try {
            double[] coordinates = eventGeographicEnhancementService.getCoordinatesByLocation(locationName);

            Map<String, Object> response = new HashMap<>();
            response.put("locationName", locationName);

            if (coordinates != null) {
                response.put("found", true);
                response.put("latitude", coordinates[0]);
                response.put("longitude", coordinates[1]);
            } else {
                response.put("found", false);
                response.put("message", "未找到该地点的坐标信息");
            }

            return ResponseEntity.ok(Result.success("查询完成", response));

        } catch (Exception e) {
            log.error("获取地点坐标失败: {}", locationName, e);
            return ResponseEntity.ok(Result.error("获取坐标失败: " + e.getMessage()));
        }
    }

    /**
     * 从文本中提取地点信息
     */
    @Operation(summary = "从文本中提取地点信息", description = "从事件描述文本中提取地点信息")
    @PostMapping("/extract-location")
    public ResponseEntity<Result<Map<String, Object>>> extractLocationFromText(
            @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error("文本内容不能为空"));
            }

            String extractedLocation = eventGeographicEnhancementService.extractLocationFromDescription(text);

            Map<String, Object> response = new HashMap<>();
            response.put("originalText", text);
            response.put("extractedLocation", extractedLocation);
            response.put("found", extractedLocation != null);

            if (extractedLocation != null) {
                // 尝试获取提取地点的坐标
                double[] coordinates = eventGeographicEnhancementService.getCoordinatesByLocation(extractedLocation);
                if (coordinates != null) {
                    response.put("coordinates", Map.of(
                            "latitude", coordinates[0],
                            "longitude", coordinates[1]));
                }
            }

            return ResponseEntity.ok(Result.success("提取完成", response));

        } catch (Exception e) {
            log.error("提取地点信息失败", e);
            return ResponseEntity.ok(Result.error("提取失败: " + e.getMessage()));
        }
    }
}