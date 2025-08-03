package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.service.DataValidationFailureHandler;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 数据验证失败处理服务实现类
 */
@Slf4j
@Service
public class DataValidationFailureHandlerImpl implements DataValidationFailureHandler {
    
    @Autowired(required = false)
    private SystemMonitoringService monitoringService;
    
    // 统计计数器
    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong failedValidations = new AtomicLong(0);
    private final AtomicLong fixedValidations = new AtomicLong(0);
    
    // 常用的日期时间格式
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
    );
    
    // 地点名称验证正则
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z\\s,.-]+$");
    
    // 标题长度限制
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_TITLE_LENGTH = 5;
    
    // 描述长度限制
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    
    @Override
    public EventData handleEventValidationFailure(EventData event, List<String> validationErrors) {
        totalValidations.incrementAndGet();
        
        if (event == null || validationErrors == null || validationErrors.isEmpty()) {
            return event;
        }
        
        log.info("处理事件验证失败: eventId={}, errors={}", event.getId(), validationErrors);
        
        boolean wasFixed = false;
        EventData fixedEvent = new EventData();
        copyEventData(event, fixedEvent);
        
        for (String error : validationErrors) {
            try {
                if (fixValidationError(fixedEvent, error)) {
                    wasFixed = true;
                }
            } catch (Exception e) {
                log.error("修复验证错误失败: error={}", error, e);
            }
        }
        
        if (wasFixed) {
            fixedValidations.incrementAndGet();
            fixedEvent.setValidationStatus("FIXED");
            log.info("事件验证错误已修复: eventId={}", event.getId());
            
            // 记录修复成功
            if (monitoringService != null) {
                monitoringService.recordSystemError("DATA_VALIDATION", "VALIDATION_FIXED", 
                        "事件验证错误已修复: " + String.join(", ", validationErrors), null);
            }
            
            return fixedEvent;
        } else {
            failedValidations.incrementAndGet();
            log.warn("无法修复事件验证错误: eventId={}, errors={}", event.getId(), validationErrors);
            
            // 记录修复失败
            if (monitoringService != null) {
                monitoringService.recordSystemError("DATA_VALIDATION", "VALIDATION_UNFIXABLE", 
                        "无法修复事件验证错误: " + String.join(", ", validationErrors), null);
            }
            
            return null; // 无法修复，返回null
        }
    }
    
    @Override
    public List<EventData> handleBatchValidationFailure(List<EventData> events, List<ValidationResult> validationResults) {
        if (events == null || validationResults == null || events.size() != validationResults.size()) {
            log.error("批量验证失败处理参数错误: eventsSize={}, resultsSize={}", 
                     events != null ? events.size() : 0, 
                     validationResults != null ? validationResults.size() : 0);
            return new ArrayList<>();
        }
        
        List<EventData> validEvents = new ArrayList<>();
        
        for (int i = 0; i < events.size(); i++) {
            EventData event = events.get(i);
            ValidationResult result = validationResults.get(i);
            
            if (result.isValid()) {
                validEvents.add(event);
            } else {
                // 尝试修复验证失败的事件
                EventData fixedEvent = handleEventValidationFailure(event, result.getErrors());
                if (fixedEvent != null) {
                    validEvents.add(fixedEvent);
                }
            }
        }
        
        log.info("批量验证失败处理完成: 原始数量={}, 有效数量={}, 修复数量={}", 
                events.size(), validEvents.size(), fixedValidations.get());
        
        return validEvents;
    }
    
    @Override
    public void recordValidationFailureStats(String operation, String failureType, int failureCount) {
        try {
            if (monitoringService != null) {
                monitoringService.recordSystemError(operation, failureType, 
                        "验证失败数量: " + failureCount, null);
            }
            
            log.debug("记录验证失败统计: operation={}, failureType={}, count={}", 
                     operation, failureType, failureCount);
        } catch (Exception e) {
            log.error("记录验证失败统计异常", e);
        }
    }
    
    @Override
    public ValidationFailureStats getValidationFailureStats() {
        ValidationFailureStats stats = new ValidationFailureStats();
        
        long total = totalValidations.get();
        long failed = failedValidations.get();
        long fixed = fixedValidations.get();
        
        stats.setTotalValidations(total);
        stats.setFailedValidations(failed);
        stats.setFixedValidations(fixed);
        
        if (total > 0) {
            stats.setFailureRate((double) failed / total * 100);
        }
        
        if (failed > 0) {
            stats.setFixRate((double) fixed / failed * 100);
        }
        
        return stats;
    }
    
    /**
     * 修复特定的验证错误
     */
    private boolean fixValidationError(EventData event, String error) {
        String lowerError = error.toLowerCase();
        
        // 修复标题相关错误
        if (lowerError.contains("title") || lowerError.contains("标题")) {
            return fixTitleError(event, error);
        }
        
        // 修复描述相关错误
        if (lowerError.contains("description") || lowerError.contains("描述")) {
            return fixDescriptionError(event, error);
        }
        
        // 修复时间相关错误
        if (lowerError.contains("time") || lowerError.contains("date") || lowerError.contains("时间")) {
            return fixTimeError(event, error);
        }
        
        // 修复地点相关错误
        if (lowerError.contains("location") || lowerError.contains("地点") || lowerError.contains("位置")) {
            return fixLocationError(event, error);
        }
        
        // 修复主体客体相关错误
        if (lowerError.contains("subject") || lowerError.contains("object") || 
            lowerError.contains("主体") || lowerError.contains("客体")) {
            return fixSubjectObjectError(event, error);
        }
        
        // 修复事件类型相关错误
        if (lowerError.contains("type") || lowerError.contains("类型")) {
            return fixEventTypeError(event, error);
        }
        
        return false;
    }
    
    /**
     * 修复标题错误
     */
    private boolean fixTitleError(EventData event, String error) {
        String title = event.getTitle();
        
        if (!StringUtils.hasText(title)) {
            // 标题为空，尝试从描述中提取
            if (StringUtils.hasText(event.getDescription())) {
                String description = event.getDescription();
                String extractedTitle = description.length() > MAX_TITLE_LENGTH ? 
                    description.substring(0, MAX_TITLE_LENGTH - 3) + "..." : description;
                event.setTitle(extractedTitle);
                return true;
            }
            // 设置默认标题
            event.setTitle("未知事件");
            return true;
        }
        
        if (title.length() > MAX_TITLE_LENGTH) {
            // 标题过长，截断
            event.setTitle(title.substring(0, MAX_TITLE_LENGTH - 3) + "...");
            return true;
        }
        
        if (title.length() < MIN_TITLE_LENGTH) {
            // 标题过短，尝试补充
            if (StringUtils.hasText(event.getDescription())) {
                String enhanced = title + " - " + event.getDescription().substring(0, 
                    Math.min(50, event.getDescription().length()));
                event.setTitle(enhanced.length() > MAX_TITLE_LENGTH ? 
                    enhanced.substring(0, MAX_TITLE_LENGTH - 3) + "..." : enhanced);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 修复描述错误
     */
    private boolean fixDescriptionError(EventData event, String error) {
        String description = event.getDescription();
        
        if (!StringUtils.hasText(description)) {
            // 描述为空，使用标题作为描述
            if (StringUtils.hasText(event.getTitle())) {
                event.setDescription(event.getTitle());
                return true;
            }
            // 设置默认描述
            event.setDescription("暂无详细描述");
            return true;
        }
        
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            // 描述过长，截断
            event.setDescription(description.substring(0, MAX_DESCRIPTION_LENGTH - 3) + "...");
            return true;
        }
        
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            // 描述过短，尝试补充
            if (StringUtils.hasText(event.getTitle())) {
                event.setDescription(event.getTitle() + " - " + description);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 修复时间错误
     */
    private boolean fixTimeError(EventData event, String error) {
        if (event.getEventTime() == null) {
            // 时间为空，设置当前时间
            event.setEventTime(LocalDateTime.now());
            return true;
        }
        
        // 尝试解析时间字符串（如果时间是字符串格式）
        String timeStr = event.getEventTime().toString();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime parsedTime = LocalDateTime.parse(timeStr, formatter);
                event.setEventTime(parsedTime);
                return true;
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        return false;
    }
    
    /**
     * 修复地点错误
     */
    private boolean fixLocationError(EventData event, String error) {
        String location = event.getLocation();
        
        if (!StringUtils.hasText(location)) {
            // 地点为空，设置默认值
            event.setLocation("未知地点");
            return true;
        }
        
        // 验证地点格式
        if (!LOCATION_PATTERN.matcher(location).matches()) {
            // 地点格式不正确，清理特殊字符
            String cleanedLocation = location.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z\\s,.-]", "");
            if (StringUtils.hasText(cleanedLocation)) {
                event.setLocation(cleanedLocation);
                return true;
            } else {
                event.setLocation("未知地点");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 修复主体客体错误
     */
    private boolean fixSubjectObjectError(EventData event, String error) {
        if (!StringUtils.hasText(event.getSubject())) {
            event.setSubject("未知主体");
        }
        
        if (!StringUtils.hasText(event.getObject())) {
            event.setObject("未知客体");
        }
        
        return true;
    }
    
    /**
     * 修复事件类型错误
     */
    private boolean fixEventTypeError(EventData event, String error) {
        if (!StringUtils.hasText(event.getEventType())) {
            event.setEventType("其他");
            return true;
        }
        
        return false;
    }
    
    /**
     * 复制事件数据
     */
    private void copyEventData(EventData source, EventData target) {
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        
        // 确保事件时间不为空
        LocalDateTime eventTime = source.getEventTime();
        if (eventTime == null) {
            log.warn("源事件时间为空，使用当前时间作为默认值: eventId={}", source.getId());
            eventTime = LocalDateTime.now();
        }
        target.setEventTime(eventTime);
        
        target.setLocation(source.getLocation());
        target.setSubject(source.getSubject());
        target.setObject(source.getObject());
        target.setEventType(source.getEventType());
        target.setKeywords(source.getKeywords());
        target.setSources(source.getSources());
        target.setCredibilityScore(source.getCredibilityScore());
        target.setFetchMethod(source.getFetchMethod());
        target.setValidationStatus(source.getValidationStatus());
    }
}