package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.service.DeepSeekService;
import com.hotech.events.service.EnhancedDeepSeekService;
import com.hotech.events.service.EventService;
import com.hotech.events.task.EventFetchTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek管理控制器
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/deepseek")
@Tag(name = "DeepSeek管理", description = "DeepSeek AI事件抓取管理相关的API接口")
public class DeepSeekController {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private EventService eventService;

    @Autowired(required = false)
    private EventFetchTask eventFetchTask;

    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;

    /**
     * 检查DeepSeek连接状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查连接状态", description = "检查DeepSeek API连接状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus() {
        try {
            log.info("检查DeepSeek连接状态");
            
            Map<String, Object> status = new HashMap<>();
            Boolean connected = deepSeekService.checkConnection();
            status.put("connected", connected);
            status.put("message", connected ? "连接正常" : "连接失败");
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success("状态检查完成", status));
        } catch (Exception e) {
            log.error("检查DeepSeek连接状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("状态检查失败：" + e.getMessage()));
        }
    }

    /**
     * 手动抓取最新事件
     */
    @PostMapping("/fetch/latest")
    @Operation(summary = "抓取最新事件", description = "手动触发抓取最新国际热点事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchLatestEvents(
            @Parameter(description = "抓取数量限制") @RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("手动抓取最新事件，限制数量：{}", limit);
            
            List<EventDTO> events = deepSeekService.fetchLatestEvents(limit);
            
            // 批量保存事件
            int successCount = 0;
            for (EventDTO event : events) {
                try {
                    // 设置事件编码
                    if (event.getEventCode() == null || event.getEventCode().isEmpty()) {
                        event.setEventCode("MANUAL_" + System.currentTimeMillis() + "_" + successCount);
                    }
                    
                    // 设置创建人
                    event.setCreatedBy("manual_fetch");
                    event.setUpdatedBy("manual_fetch");
                    
                    // 保存事件
                    eventService.createEvent(event);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("保存事件失败：{}，错误：{}", event.getEventDescription(), e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalFetched", events.size());
            result.put("successSaved", successCount);
            result.put("events", events);
            
            return ResponseEntity.ok(ApiResponse.success("抓取完成", result));
        } catch (Exception e) {
            log.error("手动抓取最新事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("抓取失败：" + e.getMessage()));
        }
    }

    /**
     * 根据关键词抓取事件
     */
    @PostMapping("/fetch/keywords")
    @Operation(summary = "关键词抓取", description = "根据关键词抓取相关事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchEventsByKeywords(
            @Parameter(description = "关键词列表") @RequestBody List<String> keywords,
            @Parameter(description = "抓取数量限制") @RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("根据关键词抓取事件：{}，限制数量：{}", keywords, limit);
            
            List<EventDTO> events = deepSeekService.fetchEventsByKeywords(keywords, limit);
            
            // 批量保存事件
            int successCount = 0;
            for (EventDTO event : events) {
                try {
                    // 设置事件编码
                    if (event.getEventCode() == null || event.getEventCode().isEmpty()) {
                        event.setEventCode("KEYWORD_" + System.currentTimeMillis() + "_" + successCount);
                    }
                    
                    // 设置创建人
                    event.setCreatedBy("keyword_fetch");
                    event.setUpdatedBy("keyword_fetch");
                    
                    // 保存事件
                    eventService.createEvent(event);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("保存关键词事件失败：{}，错误：{}", event.getEventDescription(), e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("keywords", keywords);
            result.put("totalFetched", events.size());
            result.put("successSaved", successCount);
            result.put("events", events);
            
            return ResponseEntity.ok(ApiResponse.success("关键词抓取完成", result));
        } catch (Exception e) {
            log.error("根据关键词抓取事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("关键词抓取失败：" + e.getMessage()));
        }
    }

    /**
     * 根据日期范围抓取事件
     */
    @PostMapping("/fetch/daterange")
    @Operation(summary = "日期范围抓取", description = "根据日期范围抓取事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchEventsByDateRange(
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam String startDate,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam String endDate,
            @Parameter(description = "抓取数量限制") @RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("根据日期范围抓取事件：{} 到 {}，限制数量：{}", startDate, endDate, limit);
            
            List<EventDTO> events = deepSeekService.fetchEventsByDateRange(startDate, endDate, limit);
            
            // 批量保存事件
            int successCount = 0;
            for (EventDTO event : events) {
                try {
                    // 设置事件编码
                    if (event.getEventCode() == null || event.getEventCode().isEmpty()) {
                        event.setEventCode("DATE_" + System.currentTimeMillis() + "_" + successCount);
                    }
                    
                    // 设置创建人
                    event.setCreatedBy("date_fetch");
                    event.setUpdatedBy("date_fetch");
                    
                    // 保存事件
                    eventService.createEvent(event);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("保存日期范围事件失败：{}，错误：{}", event.getEventDescription(), e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("totalFetched", events.size());
            result.put("successSaved", successCount);
            result.put("events", events);
            
            return ResponseEntity.ok(ApiResponse.success("日期范围抓取完成", result));
        } catch (Exception e) {
            log.error("根据日期范围抓取事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("日期范围抓取失败：" + e.getMessage()));
        }
    }

    /**
     * 手动触发定时任务
     */
    @PostMapping("/task/trigger")
    @Operation(summary = "触发定时任务", description = "手动触发事件抓取定时任务")
    public ResponseEntity<ApiResponse<String>> triggerTask() {
        try {
            log.info("手动触发定时任务");
            
            if (eventFetchTask == null) {
                return ResponseEntity.ok(ApiResponse.error("定时任务未启用"));
            }
            
            // 异步执行定时任务
            new Thread(() -> {
                try {
                    eventFetchTask.manualFetch();
                } catch (Exception e) {
                    log.error("执行定时任务失败", e);
                }
            }).start();
            
            return ResponseEntity.ok(ApiResponse.success("定时任务已触发"));
        } catch (Exception e) {
            log.error("触发定时任务失败", e);
            return ResponseEntity.ok(ApiResponse.error("触发失败：" + e.getMessage()));
        }
    }

    /**
     * 解析GDELT数据
     */
    @PostMapping("/parse/gdelt")
    @Operation(summary = "解析GDELT数据", description = "解析GDELT格式的事件数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parseGdeltData(
            @Parameter(description = "GDELT格式数据") @RequestBody String gdeltData) {
        try {
            log.info("解析GDELT数据，数据长度：{}", gdeltData.length());
            
            List<EventDTO> events = deepSeekService.parseGdeltData(gdeltData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalParsed", events.size());
            result.put("events", events);
            
            return ResponseEntity.ok(ApiResponse.success("GDELT数据解析完成", result));
        } catch (Exception e) {
            log.error("解析GDELT数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("解析失败：" + e.getMessage()));
        }
    }

    // ==================== 调试相关API ====================

    /**
     * 检查API健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查API健康状态", description = "检查增强DeepSeek服务的健康状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkApiHealth() {
        try {
            log.info("检查API健康状态");
            
            com.hotech.events.dto.ApiHealthStatus healthStatus = enhancedDeepSeekService.checkApiHealth();
            
            Map<String, Object> result = new HashMap<>();
            result.put("isHealthy", healthStatus.getIsHealthy());
            result.put("statusCode", healthStatus.getStatusCode());
            result.put("errorMessage", healthStatus.getErrorMessage());
            result.put("responseTime", healthStatus.getResponseTime());
            result.put("checkTime", healthStatus.getCheckTime());
            
            return ResponseEntity.ok(ApiResponse.success("健康检查完成", result));
        } catch (Exception e) {
            log.error("检查API健康状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("健康检查失败：" + e.getMessage()));
        }
    }

    /**
     * 测试事件检索
     */
    @PostMapping("/test-fetch")
    @Operation(summary = "测试事件检索", description = "测试使用动态提示词检索事件")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testEventFetch(
            @RequestBody TimelineGenerateRequest request) {
        try {
            log.info("测试事件检索: {}", request.getName());
            
            long startTime = System.currentTimeMillis();
            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
            long endTime = System.currentTimeMillis();
            
            Map<String, Object> result = new HashMap<>();
            result.put("request", request);
            result.put("eventCount", events.size());
            result.put("responseTime", endTime - startTime);
            result.put("events", events);
            
            return ResponseEntity.ok(ApiResponse.success("事件检索测试完成", result));
        } catch (Exception e) {
            log.error("测试事件检索失败", e);
            return ResponseEntity.ok(ApiResponse.error("事件检索测试失败：" + e.getMessage()));
        }
    }

    /**
     * 获取API使用统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取API使用统计", description = "获取API调用统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiStats() {
        try {
            log.info("获取API使用统计");
            
            com.hotech.events.dto.ApiUsageStats usageStats = enhancedDeepSeekService.getUsageStats();
            com.hotech.events.dto.CacheStats cacheStats = enhancedDeepSeekService.getCacheStats();
            
            Map<String, Object> result = new HashMap<>();
            result.put("usage", usageStats);
            result.put("cache", cacheStats);
            
            return ResponseEntity.ok(ApiResponse.success("统计信息获取完成", result));
        } catch (Exception e) {
            log.error("获取API使用统计失败", e);
            return ResponseEntity.ok(ApiResponse.error("统计信息获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取API调用日志
     */
    @GetMapping("/logs")
    @Operation(summary = "获取API调用日志", description = "获取最近的API调用日志")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiLogs() {
        try {
            log.info("获取API调用日志");
            
            // 这里可以返回最近的日志信息
            Map<String, Object> result = new HashMap<>();
            result.put("message", "日志功能需要进一步实现");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success("日志获取完成", result));
        } catch (Exception e) {
            log.error("获取API调用日志失败", e);
            return ResponseEntity.ok(ApiResponse.error("日志获取失败：" + e.getMessage()));
        }
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "清理缓存", description = "清理API响应缓存")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        try {
            log.info("清理API缓存");
            
            enhancedDeepSeekService.clearCache();
            
            return ResponseEntity.ok(ApiResponse.success("缓存清理完成"));
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return ResponseEntity.ok(ApiResponse.error("缓存清理失败：" + e.getMessage()));
        }
    }

    /**
     * 重置限流器
     */
    @PostMapping("/rate-limit/reset")
    @Operation(summary = "重置限流器", description = "重置API调用限流器")
    public ResponseEntity<ApiResponse<String>> resetRateLimit() {
        try {
            log.info("重置限流器");
            
            enhancedDeepSeekService.resetRateLimit();
            
            return ResponseEntity.ok(ApiResponse.success("限流器重置完成"));
        } catch (Exception e) {
            log.error("重置限流器失败", e);
            return ResponseEntity.ok(ApiResponse.error("限流器重置失败：" + e.getMessage()));
        }
    }

    /**
     * 快速修复测试 - 直接创建测试时间线
     */
    @PostMapping("/quick-fix-timeline")
    @Operation(summary = "快速修复测试", description = "直接创建包含测试事件的时间线")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createQuickFixTimeline() {
        try {
            log.info("创建快速修复测试时间线");
            
            // 创建测试事件数据
            List<com.hotech.events.dto.EventData> testEvents = createQuickFixEvents();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "快速修复测试时间线创建成功");
            result.put("eventCount", testEvents.size());
            result.put("events", testEvents);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success("快速修复测试完成", result));
        } catch (Exception e) {
            log.error("创建快速修复测试时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("快速修复测试失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建快速修复测试事件
     */
    private List<com.hotech.events.dto.EventData> createQuickFixEvents() {
        List<com.hotech.events.dto.EventData> events = new ArrayList<>();
        
        // 事件1
        com.hotech.events.dto.EventData event1 = new com.hotech.events.dto.EventData();
        event1.setId("quick_fix_1");
        event1.setTitle("2025年6月13日以色列空袭伊朗核设施");
        event1.setDescription("以色列发动代号'狮子的力量'军事行动，空袭伊朗纳坦兹铀浓缩设施等目标");
        event1.setEventTime(java.time.LocalDateTime.of(2025, 6, 13, 10, 0));
        event1.setLocation("伊朗纳坦兹");
        event1.setSubject("以色列");
        event1.setObject("伊朗核设施");
        event1.setEventType("军事打击");
        event1.setCredibilityScore(0.95);
        event1.setKeywords(java.util.Arrays.asList("以色列", "伊朗", "核设施", "空袭"));
        events.add(event1);
        
        // 事件2
        com.hotech.events.dto.EventData event2 = new com.hotech.events.dto.EventData();
        event2.setId("quick_fix_2");
        event2.setTitle("2025年6月13日伊朗导弹反击以色列");
        event2.setDescription("伊朗展开代号'真实诺言-3'的报复，向以色列发射逾200枚弹道导弹和无人机");
        event2.setEventTime(java.time.LocalDateTime.of(2025, 6, 13, 18, 0));
        event2.setLocation("以色列特拉维夫");
        event2.setSubject("伊朗");
        event2.setObject("以色列城市");
        event2.setEventType("导弹攻击");
        event2.setCredibilityScore(0.92);
        event2.setKeywords(java.util.Arrays.asList("伊朗", "以色列", "导弹", "反击"));
        events.add(event2);
        
        // 事件3
        com.hotech.events.dto.EventData event3 = new com.hotech.events.dto.EventData();
        event3.setId("quick_fix_3");
        event3.setTitle("2025年6月22日美国军事介入");
        event3.setDescription("美国直接介入冲突，出动B-2轰炸机轰炸伊朗三处核设施");
        event3.setEventTime(java.time.LocalDateTime.of(2025, 6, 22, 14, 0));
        event3.setLocation("伊朗");
        event3.setSubject("美国");
        event3.setObject("伊朗核设施");
        event3.setEventType("军事介入");
        event3.setCredibilityScore(0.88);
        event3.setKeywords(java.util.Arrays.asList("美国", "伊朗", "军事介入", "B-2轰炸机"));
        events.add(event3);
        
        return events;
    }
} 