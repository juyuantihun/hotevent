package com.hotech.events.debug;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeepSeek响应调试工具
 * 用于诊断为什么事件解析失败
 * 增强版本，提供更详细的调试信息和统计功能
 */
@Slf4j
@Component
public class DeepSeekResponseDebugger {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, AtomicLong> debugCounters = new ConcurrentHashMap<>();
    private final Map<String, String> lastResponses = new ConcurrentHashMap<>();

    /**
     * 调试DeepSeek API响应
     * @param response 原始响应
     * @param context 上下文信息
     */
    public void debugResponse(String response, String context) {
        String debugId = generateDebugId();
        
        try {
            log.info("=== DeepSeek响应调试开始 [{}] ===", debugId);
            log.info("上下文: {}", context);
            log.info("时间戳: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info("响应长度: {}", response != null ? response.length() : 0);
            
            // 更新调试计数器
            debugCounters.computeIfAbsent("total_debug_calls", k -> new AtomicLong(0)).incrementAndGet();
            
            if (response == null || response.trim().isEmpty()) {
                log.error("响应为空或null");
                debugCounters.computeIfAbsent("empty_responses", k -> new AtomicLong(0)).incrementAndGet();
                generateEmptyResponseAnalysis(context);
                return;
            }
            
            // 保存最近的响应用于分析
            lastResponses.put(context, response);
            
            // 打印原始响应（截取前1000字符）
            String truncatedResponse = response.length() > 1000 ? 
                response.substring(0, 1000) + "..." : response;
            log.info("原始响应: {}", truncatedResponse);
            
            // 响应质量分析
            analyzeResponseQuality(response);
            
            // 尝试提取JSON
            String jsonContent = extractJsonFromResponse(response);
            if (jsonContent == null) {
                log.error("无法从响应中提取JSON内容");
                debugCounters.computeIfAbsent("json_extraction_failures", k -> new AtomicLong(0)).incrementAndGet();
                analyzeResponseStructure(response);
                generateAdvancedFixSuggestions(response, context);
                return;
            }
            
            log.info("提取的JSON: {}", jsonContent.length() > 500 ? 
                jsonContent.substring(0, 500) + "..." : jsonContent);
            
            // 尝试解析JSON
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);
                analyzeJsonStructure(responseMap);
                debugCounters.computeIfAbsent("successful_json_parsing", k -> new AtomicLong(0)).incrementAndGet();
                
                // 验证事件数据质量
                validateEventData(responseMap);
                
            } catch (Exception e) {
                log.error("JSON解析失败: {}", e.getMessage());
                log.error("问题JSON: {}", jsonContent);
                debugCounters.computeIfAbsent("json_parsing_failures", k -> new AtomicLong(0)).incrementAndGet();
                
                // 尝试修复JSON
                String fixedJson = attemptJsonFix(jsonContent);
                if (fixedJson != null) {
                    log.info("尝试修复后的JSON: {}", fixedJson);
                }
            }
            
            log.info("=== DeepSeek响应调试结束 [{}] ===", debugId);
            
        } catch (Exception e) {
            log.error("调试过程中发生异常: {}", e.getMessage(), e);
            debugCounters.computeIfAbsent("debug_exceptions", k -> new AtomicLong(0)).incrementAndGet();
        }
    }
    
    /**
     * 从响应中提取JSON内容
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = response.trim();
        
        // 检查是否是完整的JSON
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            log.info("响应是完整的JSON格式");
            return trimmed;
        }
        
        // 查找JSON块
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String extracted = response.substring(jsonStart, jsonEnd + 1);
            log.info("从响应中提取JSON: 开始位置={}, 结束位置={}", jsonStart, jsonEnd);
            return extracted;
        }
        
        // 尝试查找JSON数组
        int arrayStart = response.indexOf("[");
        int arrayEnd = response.lastIndexOf("]");
        
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            String extracted = response.substring(arrayStart, arrayEnd + 1);
            log.info("从响应中提取JSON数组: 开始位置={}, 结束位置={}", arrayStart, arrayEnd);
            return "{\"events\":" + extracted + "}"; // 包装成对象
        }
        
        log.warn("无法提取有效的JSON内容");
        return null;
    }
    
    /**
     * 分析响应结构
     */
    private void analyzeResponseStructure(String response) {
        log.info("=== 响应结构分析 ===");
        
        // 检查常见的标记
        if (response.contains("```json")) {
            log.info("响应包含markdown代码块标记");
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                String jsonPart = response.substring(start, end).trim();
                log.info("提取的JSON部分: {}", jsonPart);
            }
        }
        
        if (response.contains("events")) {
            log.info("响应包含'events'关键字");
        }
        
        if (response.contains("伊以战争") || response.contains("以色列") || response.contains("巴勒斯坦")) {
            log.info("响应包含相关事件内容");
        }
        
        // 统计特殊字符
        long braceCount = response.chars().filter(ch -> ch == '{').count();
        long bracketCount = response.chars().filter(ch -> ch == '[').count();
        log.info("大括号数量: {}, 方括号数量: {}", braceCount, bracketCount);
    }
    
    /**
     * 分析JSON结构
     */
    private void analyzeJsonStructure(Map<String, Object> responseMap) {
        log.info("=== JSON结构分析 ===");
        log.info("顶级键: {}", responseMap.keySet());
        
        if (responseMap.containsKey("events")) {
            Object eventsObj = responseMap.get("events");
            log.info("events字段类型: {}", eventsObj.getClass().getSimpleName());
            
            if (eventsObj instanceof java.util.List) {
                java.util.List<?> eventsList = (java.util.List<?>) eventsObj;
                log.info("events数组长度: {}", eventsList.size());
                
                if (!eventsList.isEmpty()) {
                    Object firstEvent = eventsList.get(0);
                    log.info("第一个事件类型: {}", firstEvent.getClass().getSimpleName());
                    
                    if (firstEvent instanceof Map) {
                        Map<?, ?> eventMap = (Map<?, ?>) firstEvent;
                        log.info("第一个事件的键: {}", eventMap.keySet());
                    }
                }
            }
        } else {
            log.warn("响应中没有'events'字段");
        }
        
        // 检查其他可能的字段
        for (String key : responseMap.keySet()) {
            Object value = responseMap.get(key);
            log.info("字段 '{}': 类型={}, 值={}", key, 
                value != null ? value.getClass().getSimpleName() : "null",
                value != null ? value.toString().substring(0, Math.min(100, value.toString().length())) : "null");
        }
    }
    
    /**
     * 生成修复建议
     */
    public void generateFixSuggestions(String response) {
        log.info("=== 修复建议 ===");
        
        if (response == null || response.trim().isEmpty()) {
            log.info("建议1: 检查DeepSeek API调用是否成功");
            log.info("建议2: 检查API密钥是否有效");
            log.info("建议3: 检查网络连接");
            return;
        }
        
        if (!response.contains("{") && !response.contains("[")) {
            log.info("建议1: 响应不包含JSON，可能是纯文本回复");
            log.info("建议2: 检查提示词是否明确要求JSON格式");
            log.info("建议3: 考虑使用更严格的JSON格式要求");
        }
        
        if (response.contains("```")) {
            log.info("建议1: 响应包含markdown格式，需要提取代码块内容");
        }
        
        if (response.contains("events") && !response.contains("\"events\"")) {
            log.info("建议1: 响应提到了events但可能不是JSON格式");
            log.info("建议2: 可能需要改进JSON提取逻辑");
        }
    }

    /**
     * 获取调试统计信息
     * @return 调试统计信息
     */
    public Map<String, Object> getDebugStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 调试计数器统计
            Map<String, Long> counters = new HashMap<>();
            debugCounters.forEach((key, value) -> counters.put(key, value.get()));
            stats.put("debugCounters", counters);
            
            // 最近响应统计
            stats.put("recentResponsesCount", lastResponses.size());
            stats.put("recentContexts", lastResponses.keySet());
            
            // 成功率计算
            long totalCalls = debugCounters.getOrDefault("total_debug_calls", new AtomicLong(0)).get();
            long successfulParsing = debugCounters.getOrDefault("successful_json_parsing", new AtomicLong(0)).get();
            
            if (totalCalls > 0) {
                double successRate = (double) successfulParsing / totalCalls * 100;
                stats.put("jsonParsingSuccessRate", String.format("%.2f%%", successRate));
            }
            
            stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            log.error("获取调试统计信息失败: {}", e.getMessage(), e);
            stats.put("error", "获取统计信息失败: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * 清理调试数据
     */
    public void cleanupDebugData() {
        try {
            log.info("开始清理调试数据");
            
            // 保留最近的10个响应
            if (lastResponses.size() > 10) {
                lastResponses.clear();
                log.info("清理了过多的响应缓存");
            }
            
            log.info("调试数据清理完成");
            
        } catch (Exception e) {
            log.error("清理调试数据失败: {}", e.getMessage(), e);
        }
    }

    // 私有辅助方法

    private String generateDebugId() {
        return "DEBUG_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private void generateEmptyResponseAnalysis(String context) {
        log.info("=== 空响应分析 ===");
        log.info("上下文: {}", context);
        log.info("可能原因1: API调用失败");
        log.info("可能原因2: 网络连接问题");
        log.info("可能原因3: API密钥无效");
        log.info("可能原因4: 请求参数错误");
        log.info("建议: 检查API调用日志和网络状态");
    }

    private void analyzeResponseQuality(String response) {
        try {
            log.info("=== 响应质量分析 ===");
            
            // 长度分析
            if (response.length() < 50) {
                log.warn("响应过短，可能不完整");
            } else if (response.length() > 10000) {
                log.info("响应较长，内容丰富");
            }
            
            // 编码分析
            boolean hasChineseChars = response.chars().anyMatch(ch -> ch >= 0x4e00 && ch <= 0x9fff);
            if (hasChineseChars) {
                log.info("响应包含中文字符");
            }
            
            // 结构完整性分析
            long openBraces = response.chars().filter(ch -> ch == '{').count();
            long closeBraces = response.chars().filter(ch -> ch == '}').count();
            
            if (openBraces != closeBraces) {
                log.warn("大括号不匹配: 开={}, 闭={}", openBraces, closeBraces);
            }
            
        } catch (Exception e) {
            log.debug("响应质量分析失败: {}", e.getMessage());
        }
    }

    private void validateEventData(Map<String, Object> responseMap) {
        try {
            log.info("=== 事件数据验证 ===");
            
            if (!responseMap.containsKey("events")) {
                log.warn("缺少events字段");
                return;
            }
            
            Object eventsObj = responseMap.get("events");
            if (!(eventsObj instanceof java.util.List)) {
                log.warn("events字段不是数组类型");
                return;
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Object> eventsList = (java.util.List<Object>) eventsObj;
            
            if (eventsList.isEmpty()) {
                log.warn("events数组为空");
                return;
            }
            
            log.info("事件数量: {}", eventsList.size());
            
            // 验证第一个事件的结构
            Object firstEvent = eventsList.get(0);
            if (firstEvent instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> eventMap = (Map<String, Object>) firstEvent;
                
                String[] requiredFields = {"eventTime", "eventDescription", "eventLocation"};
                for (String field : requiredFields) {
                    if (!eventMap.containsKey(field)) {
                        log.warn("事件缺少必需字段: {}", field);
                    }
                }
                
                log.info("第一个事件验证完成");
            }
            
        } catch (Exception e) {
            log.error("事件数据验证失败: {}", e.getMessage());
        }
    }

    private void generateAdvancedFixSuggestions(String response, String context) {
        log.info("=== 高级修复建议 ===");
        log.info("上下文: {}", context);
        
        // 基于响应内容的具体建议
        if (response.contains("error") || response.contains("Error")) {
            log.info("建议1: 响应包含错误信息，检查API调用参数");
        }
        
        if (response.contains("```")) {
            log.info("建议2: 使用正则表达式提取代码块内容");
            
            Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
            Matcher matcher = codeBlockPattern.matcher(response);
            if (matcher.find()) {
                String extractedContent = matcher.group(1).trim();
                log.info("提取的代码块内容: {}", extractedContent.substring(0, Math.min(200, extractedContent.length())));
            }
        }
        
        if (response.contains("events") && !response.contains("{")) {
            log.info("建议3: 响应提到events但不是JSON格式，可能需要文本解析");
        }
        
        log.info("建议4: 考虑使用备用数据生成器");
        log.info("建议5: 检查提示词模板是否需要优化");
    }

    private String attemptJsonFix(String jsonContent) {
        try {
            log.info("=== 尝试修复JSON ===");
            
            // 移除可能的markdown标记
            String cleaned = jsonContent.replaceAll("```(?:json)?", "").trim();
            
            // 尝试修复常见的JSON问题
            cleaned = cleaned.replaceAll(",\\s*}", "}"); // 移除尾随逗号
            cleaned = cleaned.replaceAll(",\\s*]", "]");
            
            // 尝试解析修复后的JSON
            objectMapper.readValue(cleaned, Map.class);
            log.info("JSON修复成功");
            return cleaned;
            
        } catch (Exception e) {
            log.warn("JSON修复失败: {}", e.getMessage());
            return null;
        }
    }
}