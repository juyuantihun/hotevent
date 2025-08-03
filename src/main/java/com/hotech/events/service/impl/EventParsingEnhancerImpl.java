package com.hotech.events.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.dto.EventData;
import com.hotech.events.entity.EventParsingRecord;
import com.hotech.events.mapper.EventParsingRecordMapper;
import com.hotech.events.service.EventParsingEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 事件解析增强器实现类
 * 提供多种解析策略来处理不同格式的API响应
 * 
 * @author Kiro
 */
@Service
public class EventParsingEnhancerImpl implements EventParsingEnhancer {
    
    private static final Logger logger = LoggerFactory.getLogger(EventParsingEnhancerImpl.class);
    
    @Autowired
    private EventParsingRecordMapper eventParsingRecordMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 常用的时间格式
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),  // 添加对缺少秒数的ISO格式的支持
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("MM/dd HH:mm"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // 添加标准ISO格式支持
        DateTimeFormatter.ISO_DATE_TIME         // 添加带时区的ISO格式支持
    );
    
    // JSON提取的正则表达式模式
    private static final List<Pattern> JSON_PATTERNS = Arrays.asList(
        Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL),
        Pattern.compile("\\{.*?\\}", Pattern.DOTALL),
        Pattern.compile("```json\\s*(.*?)\\s*```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
        Pattern.compile("```\\s*(\\[.*?\\])\\s*```", Pattern.DOTALL),
        Pattern.compile("```\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL)
    );
    
    // 事件文本解析的正则表达式
    private static final Pattern EVENT_TEXT_PATTERN = Pattern.compile(
        "(?:事件|Event)\\s*[:：]?\\s*([^\\n\\r]+).*?(?:时间|Time|日期|Date)\\s*[:：]?\\s*([^\\n\\r]+)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    @Override
    public List<EventData> parseWithMultipleStrategies(String response, String apiType, String requestSummary) {
        logger.info("开始使用多策略解析事件，API类型: {}", apiType);
        
        EventParsingRecord record = new EventParsingRecord();
        record.setOriginalResponse(response);
        record.setApiType(apiType);
        record.setRequestSummary(requestSummary);
        record.setParseTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        
        List<EventData> events = new ArrayList<>();
        String parsingMethod = "";
        String errorDetails = "";
        
        try {
            // 策略1: 直接JSON解析
            events = tryDirectJsonParsing(response);
            if (!events.isEmpty()) {
                parsingMethod = "DIRECT_JSON";
                logger.info("直接JSON解析成功，解析出 {} 个事件", events.size());
            } else {
                // 策略2: 增强JSON提取后解析
                String extractedJson = extractJsonWithAdvancedMethods(response);
                record.setExtractedJson(extractedJson);
                
                if (StringUtils.hasText(extractedJson)) {
                    events = tryDirectJsonParsing(extractedJson);
                    if (!events.isEmpty()) {
                        parsingMethod = "EXTRACTED_JSON";
                        logger.info("提取JSON解析成功，解析出 {} 个事件", events.size());
                    }
                }
                
                // 策略3: 文本解析
                if (events.isEmpty()) {
                    events = parseEventsFromText(response);
                    if (!events.isEmpty()) {
                        parsingMethod = "TEXT_PARSING";
                        logger.info("文本解析成功，解析出 {} 个事件", events.size());
                    }
                }
            }
            
            // 验证解析结果
            if (validateParsedEvents(events)) {
                record.setParsingStatus("SUCCESS");
                record.setParsedEventCount(events.size());
            } else {
                record.setParsingStatus("PARTIAL");
                record.setParsedEventCount(events.size());
                errorDetails = "解析结果验证部分失败";
            }
            
        } catch (Exception e) {
            logger.error("事件解析过程中发生错误", e);
            record.setParsingStatus("FAILED");
            record.setParsedEventCount(0);
            errorDetails = "解析异常: " + e.getMessage();
        }
        
        record.setParsingMethod(parsingMethod);
        record.setErrorDetails(errorDetails);
        record.setUpdatedAt(LocalDateTime.now());
        
        // 保存解析记录
        try {
            eventParsingRecordMapper.insert(record);
        } catch (Exception e) {
            logger.error("保存解析记录失败", e);
        }
        
        logger.info("多策略解析完成，最终解析出 {} 个事件，使用方法: {}", events.size(), parsingMethod);
        return events;
    }
    
    @Override
    public String extractJsonWithAdvancedMethods(String response) {
        if (!StringUtils.hasText(response)) {
            return "";
        }
        
        logger.debug("开始使用高级方法提取JSON");
        
        // 尝试各种JSON提取模式
        for (Pattern pattern : JSON_PATTERNS) {
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                String extracted = matcher.group(1) != null ? matcher.group(1) : matcher.group(0);
                logger.debug("使用模式 {} 提取到JSON: {}", pattern.pattern(), extracted.substring(0, Math.min(100, extracted.length())));
                
                // 验证提取的内容是否为有效JSON
                if (isValidJson(extracted)) {
                    return extracted;
                }
            }
        }
        
        // 如果没有找到明显的JSON结构，尝试查找可能的JSON片段
        return extractJsonFragments(response);
    }
    
    @Override
    public List<EventData> parseEventsFromText(String text) {
        logger.debug("开始从文本中解析事件");
        List<EventData> events = new ArrayList<>();
        
        if (!StringUtils.hasText(text)) {
            return events;
        }
        
        // 使用正则表达式匹配事件模式
        Matcher matcher = EVENT_TEXT_PATTERN.matcher(text);
        while (matcher.find()) {
            String eventTitle = matcher.group(1).trim();
            String eventTime = matcher.group(2).trim();
            
            EventData event = new EventData();
            event.setTitle(eventTitle);
            event.setDescription(eventTitle);
            
            // 尝试解析时间
            LocalDateTime parsedTime = parseDateTime(eventTime);
            if (parsedTime != null) {
                event.setEventTime(parsedTime);
            } else {
                // 如果时间解析失败，使用当前时间作为默认值
                logger.warn("事件时间解析失败，使用当前时间作为默认值: {}", eventTime);
                event.setEventTime(LocalDateTime.now());
            }
            
            events.add(event);
            logger.debug("从文本解析出事件: {} - {}", eventTitle, eventTime);
        }
        
        // 如果正则匹配失败，尝试按行分割并提取可能的事件
        if (events.isEmpty()) {
            events = parseEventsByLines(text);
        }
        
        logger.info("文本解析完成，共解析出 {} 个事件", events.size());
        return events;
    }
    
    @Override
    public boolean validateParsedEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            logger.warn("事件列表为空，验证失败");
            return false;
        }
        
        int validEvents = 0;
        for (EventData event : events) {
            if (StringUtils.hasText(event.getTitle()) && 
                StringUtils.hasText(event.getDescription())) {
                validEvents++;
            }
        }
        
        double validRatio = (double) validEvents / events.size();
        boolean isValid = validRatio >= 0.7; // 至少70%的事件有效
        
        logger.info("事件验证完成，总数: {}, 有效数: {}, 有效率: {:.2f}%, 验证结果: {}", 
                   events.size(), validEvents, validRatio * 100, isValid ? "通过" : "失败");
        
        return isValid;
    }
    
    @Override
    public Map<String, Object> getParsingStats(String apiType, int hours) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        
        return eventParsingRecordMapper.getParsingStats(apiType, startTime, endTime);
    }
    
    @Override
    public int cleanupOldParsingRecords(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        return eventParsingRecordMapper.deleteOldRecords(cutoffTime);
    }
    
    // 私有辅助方法
    
    /**
     * 尝试直接JSON解析
     */
    private List<EventData> tryDirectJsonParsing(String jsonStr) {
        List<EventData> events = new ArrayList<>();
        
        if (!StringUtils.hasText(jsonStr)) {
            return events;
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            
            if (rootNode.isArray()) {
                for (JsonNode eventNode : rootNode) {
                    EventData event = parseEventFromJsonNode(eventNode);
                    if (event != null) {
                        events.add(event);
                    }
                }
            } else if (rootNode.isObject()) {
                // 可能是单个事件或包含事件数组的对象
                if (rootNode.has("events") && rootNode.get("events").isArray()) {
                    for (JsonNode eventNode : rootNode.get("events")) {
                        EventData event = parseEventFromJsonNode(eventNode);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                } else {
                    EventData event = parseEventFromJsonNode(rootNode);
                    if (event != null) {
                        events.add(event);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            logger.debug("JSON解析失败: {}", e.getMessage());
        }
        
        return events;
    }
    
    /**
     * 从JSON节点解析事件
     */
    private EventData parseEventFromJsonNode(JsonNode eventNode) {
        if (eventNode == null || !eventNode.isObject()) {
            return null;
        }
        
        EventData event = new EventData();
        
        // 解析标题
        String title = getJsonStringValue(eventNode, "title", "name", "event", "事件");
        if (!StringUtils.hasText(title)) {
            return null; // 没有标题的事件无效
        }
        event.setTitle(title);
        
        // 解析描述
        String description = getJsonStringValue(eventNode, "description", "desc", "content", "详情", "内容");
        event.setDescription(StringUtils.hasText(description) ? description : title);
        
        // 解析时间
        String timeStr = getJsonStringValue(eventNode, "time", "date", "eventTime", "时间", "日期");
        if (StringUtils.hasText(timeStr)) {
            LocalDateTime eventTime = parseDateTime(timeStr);
            if (eventTime != null) {
                event.setEventTime(eventTime);
            } else {
                logger.warn("事件时间解析失败，使用当前时间作为默认值: {}", timeStr);
                event.setEventTime(LocalDateTime.now());
            }
        } else {
            // 如果没有提供时间信息，使用当前时间
            logger.warn("事件数据中缺少时间信息，使用当前时间作为默认值");
            event.setEventTime(LocalDateTime.now());
        }
        
        // 解析地点
        String location = getJsonStringValue(eventNode, "location", "place", "地点", "位置");
        event.setLocation(location);
        
        return event;
    }
    
    /**
     * 从JSON节点获取字符串值
     */
    private String getJsonStringValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName)) {
                JsonNode fieldNode = node.get(fieldName);
                if (fieldNode.isTextual()) {
                    return fieldNode.asText();
                } else if (!fieldNode.isNull()) {
                    return fieldNode.toString();
                }
            }
        }
        return null;
    }
    
    /**
     * 验证是否为有效JSON
     */
    private boolean isValidJson(String jsonStr) {
        try {
            objectMapper.readTree(jsonStr);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * 提取JSON片段
     */
    private String extractJsonFragments(String text) {
        StringBuilder jsonBuilder = new StringBuilder();
        
        // 查找可能的JSON对象或数组的开始和结束
        int braceCount = 0;
        int bracketCount = 0;
        boolean inJson = false;
        int jsonStart = -1;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c == '{') {
                if (!inJson) {
                    inJson = true;
                    jsonStart = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (inJson && braceCount == 0) {
                    String jsonFragment = text.substring(jsonStart, i + 1);
                    if (isValidJson(jsonFragment)) {
                        return jsonFragment;
                    }
                    inJson = false;
                }
            } else if (c == '[') {
                if (!inJson) {
                    inJson = true;
                    jsonStart = i;
                }
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
                if (inJson && bracketCount == 0 && braceCount == 0) {
                    String jsonFragment = text.substring(jsonStart, i + 1);
                    if (isValidJson(jsonFragment)) {
                        return jsonFragment;
                    }
                    inJson = false;
                }
            }
        }
        
        return "";
    }
    
    /**
     * 按行解析事件
     */
    private List<EventData> parseEventsByLines(String text) {
        List<EventData> events = new ArrayList<>();
        String[] lines = text.split("[\\r\\n]+");
        
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 10 && !line.startsWith("//") && !line.startsWith("#")) {
                // 简单的事件创建逻辑
                EventData event = new EventData();
                event.setTitle(line.length() > 50 ? line.substring(0, 50) + "..." : line);
                event.setDescription(line);
                // 设置默认时间
                event.setEventTime(LocalDateTime.now());
                events.add(event);
                
                if (events.size() >= 20) { // 限制最大数量
                    break;
                }
            }
        }
        
        return events;
    }
    
    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        
        // 清理时间字符串
        timeStr = timeStr.trim().replaceAll("[年月日时分秒]", "-").replaceAll("--+", "-");
        if (timeStr.endsWith("-")) {
            timeStr = timeStr.substring(0, timeStr.length() - 1);
        }
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(timeStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        logger.debug("无法解析时间字符串: {}", timeStr);
        return null;
    }
}