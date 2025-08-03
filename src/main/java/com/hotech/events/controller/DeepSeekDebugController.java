package com.hotech.events.controller;

import com.hotech.events.debug.DeepSeekResponseDebugger;
import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.service.EnhancedDeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek调试控制器
 * 用于测试和调试DeepSeek API响应解析
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/deepseek")
@Tag(name = "DeepSeek调试", description = "DeepSeek API调试和测试工具")
public class DeepSeekDebugController {

    @Autowired(required = false)
    private EnhancedDeepSeekService enhancedDeepSeekService;

    @Autowired
    private DeepSeekResponseDebugger responseDebugger;

    @Autowired(required = false)
    private com.hotech.events.service.impl.DeepSeekRawTestService rawTestService;

    @Autowired(required = false)
    private com.hotech.events.service.impl.WebSearchParameterTestService parameterTestService;

    @PostMapping("/test-event-fetch")
    @Operation(summary = "测试事件检索", description = "测试DeepSeek事件检索功能并返回调试信息")
    public ResponseEntity<Map<String, Object>> testEventFetch(
            @RequestParam(defaultValue = "伊以战争时间线") 
            @Parameter(description = "时间线名称") String name,
            @RequestParam(defaultValue = "伊以战争相关事件") 
            @Parameter(description = "时间线描述") String description) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始测试事件检索: name={}, description={}", name, description);
            
            // 构建测试请求
            TimelineGenerateRequest request = new TimelineGenerateRequest();
            request.setName(name);
            request.setDescription(description);
            request.setRegionIds(Arrays.asList(1L, 2L)); // 假设的地区ID
            request.setStartTime(LocalDateTime.of(2023, 10, 1, 0, 0));
            request.setEndTime(LocalDateTime.of(2024, 12, 31, 23, 59));
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 调用事件检索
            if (enhancedDeepSeekService == null) {
                result.put("success", false);
                result.put("error", "EnhancedDeepSeekService服务不可用");
                result.put("eventCount", 0);
                return ResponseEntity.ok(result);
            }
            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
            
            // 记录结束时间
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            // 构建结果
            result.put("success", true);
            result.put("eventCount", events.size());
            result.put("responseTime", responseTime);
            result.put("events", events);
            result.put("request", request);
            
            // 添加事件详情
            if (!events.isEmpty()) {
                result.put("firstEvent", events.get(0));
                result.put("eventTitles", events.stream()
                    .map(EventData::getTitle)
                    .limit(10)
                    .toArray());
            }
            
            log.info("事件检索测试完成: eventCount={}, responseTime={}ms", 
                    events.size(), responseTime);
            
        } catch (Exception e) {
            log.error("事件检索测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("eventCount", 0);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-raw-response")
    @Operation(summary = "获取原始响应", description = "获取DeepSeek API的原始响应内容")
    public ResponseEntity<Map<String, Object>> testRawResponse(
            @RequestParam(defaultValue = "伊以战争时间线") 
            @Parameter(description = "时间线名称") String name) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始获取DeepSeek原始响应: name={}", name);
            
            // 构建测试请求
            TimelineGenerateRequest request = new TimelineGenerateRequest();
            request.setName(name);
            request.setDescription("伊以战争相关事件");
            request.setRegionIds(Arrays.asList(1L, 2L));
            request.setStartTime(LocalDateTime.of(2023, 10, 1, 0, 0));
            request.setEndTime(LocalDateTime.of(2024, 12, 31, 23, 59));
            
            // 这里我们需要直接调用API获取原始响应
            // 由于无法直接访问私有方法，我们通过反射或创建新的测试方法
            
            result.put("success", true);
            result.put("message", "请查看应用日志获取详细的原始响应信息");
            result.put("request", request);
            
            // 调用事件检索来触发调试日志
            if (enhancedDeepSeekService == null) {
                result.put("success", false);
                result.put("error", "EnhancedDeepSeekService服务不可用");
                return ResponseEntity.ok(result);
            }
            List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
            result.put("eventCount", events.size());
            result.put("events", events);
            
        } catch (Exception e) {
            log.error("获取原始响应失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-response-parsing")
    @Operation(summary = "测试响应解析", description = "测试DeepSeek响应解析逻辑")
    public ResponseEntity<Map<String, Object>> testResponseParsing(
            @RequestBody @Parameter(description = "模拟的DeepSeek响应") String mockResponse) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始测试响应解析，响应长度: {}", mockResponse.length());
            
            // 使用调试器分析响应
            responseDebugger.debugResponse(mockResponse, "手动测试");
            
            // 生成修复建议
            responseDebugger.generateFixSuggestions(mockResponse);
            
            result.put("success", true);
            result.put("responseLength", mockResponse.length());
            result.put("message", "响应解析测试完成，请查看日志获取详细信息");
            
        } catch (Exception e) {
            log.error("响应解析测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api-status")
    @Operation(summary = "检查API状态", description = "检查DeepSeek API连接状态")
    public ResponseEntity<Map<String, Object>> checkApiStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (enhancedDeepSeekService == null) {
                result.put("connected", false);
                result.put("timestamp", LocalDateTime.now());
                result.put("message", "EnhancedDeepSeekService服务不可用");
                return ResponseEntity.ok(result);
            }
            
            // 检查API连接
            Boolean connected = enhancedDeepSeekService.checkConnection();
            
            result.put("connected", connected);
            result.put("timestamp", LocalDateTime.now());
            
            if (connected) {
                result.put("message", "DeepSeek API连接正常");
            } else {
                result.put("message", "DeepSeek API连接失败");
            }
            
        } catch (Exception e) {
            log.error("检查API状态失败", e);
            result.put("connected", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/simulate-issue")
    @Operation(summary = "模拟常见问题", description = "模拟常见的响应解析问题")
    public ResponseEntity<Map<String, Object>> simulateIssue(
            @RequestParam @Parameter(description = "问题类型") String issueType) {
        
        Map<String, Object> result = new HashMap<>();
        String mockResponse = "";
        
        switch (issueType.toLowerCase()) {
            case "markdown":
                mockResponse = "以下是伊以战争的相关事件：\n\n```json\n" +
                    "{\"events\":[{\"title\":\"2023年10月7日哈马斯袭击\",\"description\":\"哈马斯对以色列发动大规模袭击\"}]}\n" +
                    "```\n\n希望这些信息对您有帮助。";
                break;
                
            case "text_only":
                mockResponse = "关于伊以战争的主要事件包括：\n" +
                    "1. 2023年10月7日：哈马斯对以色列发动袭击\n" +
                    "2. 2023年10月8日：以色列宣布进入战争状态\n" +
                    "3. 2023年10月9日：以色列开始对加沙地带进行空袭";
                break;
                
            case "malformed_json":
                mockResponse = "{\"events\":[{\"title\":\"测试事件\",\"description\":\"这是一个测试\",}]}";
                break;
                
            case "empty_response":
                mockResponse = "";
                break;
                
            default:
                mockResponse = "{\"events\":[{\"title\":\"正常事件\",\"description\":\"这是一个正常的JSON响应\"}]}";
        }
        
        try {
            // 使用调试器分析模拟响应
            responseDebugger.debugResponse(mockResponse, "模拟问题: " + issueType);
            responseDebugger.generateFixSuggestions(mockResponse);
            
            result.put("success", true);
            result.put("issueType", issueType);
            result.put("mockResponse", mockResponse);
            result.put("message", "问题模拟完成，请查看日志");
            
        } catch (Exception e) {
            log.error("模拟问题失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-raw-api")
    @Operation(summary = "测试原始API", description = "直接测试DeepSeek API并返回原始响应")
    public ResponseEntity<Map<String, Object>> testRawApi(
            @RequestParam(defaultValue = "伊以战争时间线") 
            @Parameter(description = "查询内容") String query) {
        
        try {
            if (rawTestService == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "DeepSeekRawTestService服务不可用");
                return ResponseEntity.ok(error);
            }
            
            Map<String, Object> result = rawTestService.testRawApiCall(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("原始API测试失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/test-websearch-formats")
    @Operation(summary = "测试联网搜索格式", description = "测试不同的DeepSeek联网搜索参数格式")
    public ResponseEntity<Map<String, Object>> testWebSearchFormats() {
        try {
            if (parameterTestService == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "WebSearchParameterTestService服务不可用");
                return ResponseEntity.ok(error);
            }
            
            log.info("开始测试不同的联网搜索参数格式");
            Map<String, Object> result = parameterTestService.testDifferentWebSearchFormats();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试联网搜索格式失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/usage-stats")
    @Operation(summary = "获取使用统计", description = "获取DeepSeek API使用统计信息")
    public ResponseEntity<Map<String, Object>> getUsageStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            if (enhancedDeepSeekService == null) {
                stats.put("success", false);
                stats.put("error", "EnhancedDeepSeekService服务不可用");
                return ResponseEntity.ok(stats);
            }
            
            // 获取API使用统计
            Object usageStatsObj = enhancedDeepSeekService.getUsageStats();
            stats.put("apiUsage", usageStatsObj);
            
            // 获取缓存统计
            Object cacheStatsObj = enhancedDeepSeekService.getCacheStats();
            stats.put("cache", cacheStatsObj);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("获取使用统计失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}