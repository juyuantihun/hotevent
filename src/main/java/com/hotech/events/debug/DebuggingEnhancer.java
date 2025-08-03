package com.hotech.events.debug;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 调试增强器
 * 提供详细的调试信息和系统监控功能
 */
@Slf4j
@Component
public class DebuggingEnhancer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, AtomicLong> operationCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> operationTimings = new ConcurrentHashMap<>();
    
    @Autowired
    private DeepSeekResponseDebugger responseDebugger;

    /**
     * 记录详细的请求日志
     * @param operation 操作名称
     * @param request 请求对象
     * @param apiType API类型
     */
    public void logDetailedRequest(String operation, Object request, String apiType) {
        String requestId = generateRequestId();
        
        try {
            log.info("=== 详细请求日志开始 [{}] ===", requestId);
            log.info("操作: {}", operation);
            log.info("API类型: {}", apiType);
            log.info("时间戳: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 记录请求参数
            if (request != null) {
                String requestJson = objectMapper.writeValueAsString(request);
                log.info("请求参数: {}", requestJson);
                log.info("请求参数长度: {} 字符", requestJson.length());
            } else {
                log.warn("请求参数为空");
            }
            
            // 更新操作计数器
            operationCounters.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
            
            // 记录操作开始时间
            operationTimings.put(requestId, System.currentTimeMillis());
            
            log.info("=== 详细请求日志结束 [{}] ===", requestId);
            
        } catch (Exception e) {
            log.error("记录详细请求日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 分析响应内容
     * @param response 响应内容
     * @param operation 操作名称
     */
    public void analyzeResponseContent(String response, String operation) {
        String requestId = generateRequestId();
        
        try {
            log.info("=== 响应内容分析开始 [{}] ===", requestId);
            log.info("操作: {}", operation);
            
            if (response == null || response.trim().isEmpty()) {
                log.error("响应内容为空或null");
                generateEmptyResponseSuggestions(operation);
                return;
            }
            
            // 基本响应统计
            log.info("响应长度: {} 字符", response.length());
            log.info("响应行数: {} 行", response.split("\n").length);
            
            // 内容类型分析
            analyzeContentType(response);
            
            // JSON结构分析
            analyzeJsonStructure(response);
            
            // 关键词分析
            analyzeKeywords(response, operation);
            
            // 使用现有的响应调试器进行深度分析
            responseDebugger.debugResponse(response, operation);
            
            log.info("=== 响应内容分析结束 [{}] ===", requestId);
            
        } catch (Exception e) {
            log.error("分析响应内容失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 错误诊断
     * @param error 异常对象
     * @param context 上下文信息
     * @return 诊断结果
     */
    public String diagnoseError(Exception error, String context) {
        StringBuilder diagnosis = new StringBuilder();
        
        try {
            diagnosis.append("=== 错误诊断报告 ===\n");
            diagnosis.append("上下文: ").append(context).append("\n");
            diagnosis.append("时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
            diagnosis.append("异常类型: ").append(error.getClass().getSimpleName()).append("\n");
            diagnosis.append("异常消息: ").append(error.getMessage()).append("\n");
            
            // 根据异常类型提供具体诊断
            if (error instanceof java.net.ConnectException) {
                diagnosis.append("诊断: 网络连接异常\n");
                diagnosis.append("建议: 1. 检查网络连接\n");
                diagnosis.append("建议: 2. 检查API服务是否可用\n");
                diagnosis.append("建议: 3. 检查防火墙设置\n");
                
            } else if (error instanceof java.net.SocketTimeoutException) {
                diagnosis.append("诊断: 请求超时\n");
                diagnosis.append("建议: 1. 增加超时时间\n");
                diagnosis.append("建议: 2. 检查网络稳定性\n");
                diagnosis.append("建议: 3. 考虑使用重试机制\n");
                
            } else if (error instanceof com.fasterxml.jackson.core.JsonParseException) {
                diagnosis.append("诊断: JSON解析失败\n");
                diagnosis.append("建议: 1. 检查响应格式\n");
                diagnosis.append("建议: 2. 使用更宽松的解析策略\n");
                diagnosis.append("建议: 3. 检查字符编码\n");
                
            } else if (error instanceof IllegalArgumentException) {
                diagnosis.append("诊断: 参数异常\n");
                diagnosis.append("建议: 1. 检查输入参数\n");
                diagnosis.append("建议: 2. 验证参数格式\n");
                diagnosis.append("建议: 3. 检查参数范围\n");
                
            } else {
                diagnosis.append("诊断: 未知异常类型\n");
                diagnosis.append("建议: 1. 查看完整堆栈跟踪\n");
                diagnosis.append("建议: 2. 检查相关配置\n");
                diagnosis.append("建议: 3. 联系技术支持\n");
            }
            
            // 添加堆栈跟踪（前10行）
            diagnosis.append("堆栈跟踪（前10行）:\n");
            StackTraceElement[] stackTrace = error.getStackTrace();
            for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                diagnosis.append("  ").append(stackTrace[i].toString()).append("\n");
            }
            
            diagnosis.append("=== 错误诊断报告结束 ===");
            
            String result = diagnosis.toString();
            log.error(result);
            return result;
            
        } catch (Exception e) {
            log.error("错误诊断失败: {}", e.getMessage(), e);
            return "错误诊断失败: " + e.getMessage();
        }
    }

    /**
     * 记录性能指标
     * @param operation 操作名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param success 是否成功
     */
    public void recordPerformanceMetrics(String operation, long startTime, long endTime, boolean success) {
        try {
            long duration = endTime - startTime;
            
            log.info("=== 性能指标记录 ===");
            log.info("操作: {}", operation);
            log.info("耗时: {} ms", duration);
            log.info("状态: {}", success ? "成功" : "失败");
            log.info("开始时间: {}", LocalDateTime.now().minusNanos((endTime - startTime) * 1_000_000));
            log.info("结束时间: {}", LocalDateTime.now());
            
            // 性能分析
            if (duration > 10000) { // 超过10秒
                log.warn("操作耗时过长: {} ms，建议优化", duration);
            } else if (duration > 5000) { // 超过5秒
                log.info("操作耗时较长: {} ms，可考虑优化", duration);
            }
            
            // 更新统计信息
            String key = operation + (success ? "_success" : "_failure");
            operationCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
            
        } catch (Exception e) {
            log.error("记录性能指标失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取调试统计信息
     * @return 统计信息
     */
    public Map<String, Object> getDebugStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 操作计数统计
            Map<String, Long> counters = new HashMap<>();
            operationCounters.forEach((key, value) -> counters.put(key, value.get()));
            stats.put("operationCounters", counters);
            
            // 系统信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("totalMemory", runtime.totalMemory());
            systemInfo.put("freeMemory", runtime.freeMemory());
            systemInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            systemInfo.put("maxMemory", runtime.maxMemory());
            systemInfo.put("availableProcessors", runtime.availableProcessors());
            stats.put("systemInfo", systemInfo);
            
            // 时间信息
            stats.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.debug("调试统计信息: {}", objectMapper.writeValueAsString(stats));
            
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
            
            // 清理过期的操作计时数据
            long currentTime = System.currentTimeMillis();
            operationTimings.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > 3600000); // 1小时过期
            
            log.info("调试数据清理完成");
            
        } catch (Exception e) {
            log.error("清理调试数据失败: {}", e.getMessage(), e);
        }
    }

    // 私有辅助方法

    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private void analyzeContentType(String response) {
        try {
            if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                log.info("响应类型: JSON格式");
            } else if (response.contains("```json")) {
                log.info("响应类型: Markdown包装的JSON");
            } else if (response.contains("<html>") || response.contains("<xml>")) {
                log.info("响应类型: HTML/XML格式");
            } else {
                log.info("响应类型: 纯文本格式");
            }
        } catch (Exception e) {
            log.debug("内容类型分析失败: {}", e.getMessage());
        }
    }

    private void analyzeJsonStructure(String response) {
        try {
            // 统计JSON相关字符
            long braceCount = response.chars().filter(ch -> ch == '{').count();
            long bracketCount = response.chars().filter(ch -> ch == '[').count();
            long quoteCount = response.chars().filter(ch -> ch == '"').count();
            
            log.info("JSON结构分析: 大括号={}, 方括号={}, 引号={}", braceCount, bracketCount, quoteCount);
            
            if (braceCount > 0 || bracketCount > 0) {
                log.info("响应包含JSON结构");
            }
            
        } catch (Exception e) {
            log.debug("JSON结构分析失败: {}", e.getMessage());
        }
    }

    private void analyzeKeywords(String response, String operation) {
        try {
            // 根据操作类型分析关键词
            if ("timeline_generation".equals(operation)) {
                if (response.contains("events")) {
                    log.info("关键词分析: 包含'events'字段");
                }
                if (response.contains("timeline")) {
                    log.info("关键词分析: 包含'timeline'字段");
                }
            }
            
            // 通用关键词分析
            if (response.contains("error") || response.contains("Error")) {
                log.warn("关键词分析: 响应包含错误信息");
            }
            
            if (response.contains("success") || response.contains("Success")) {
                log.info("关键词分析: 响应包含成功信息");
            }
            
        } catch (Exception e) {
            log.debug("关键词分析失败: {}", e.getMessage());
        }
    }

    private void generateEmptyResponseSuggestions(String operation) {
        log.info("=== 空响应处理建议 ===");
        log.info("操作: {}", operation);
        log.info("建议1: 检查API调用是否成功");
        log.info("建议2: 检查网络连接状态");
        log.info("建议3: 检查API密钥配置");
        log.info("建议4: 检查请求参数格式");
        log.info("建议5: 考虑使用备用数据源");
    }
}