package com.hotech.events.util;

import com.hotech.events.dto.EventData;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件去重工具类
 * 实现智能事件去重算法
 * 
 * @author Kiro
 */
@Component
public class EventDeduplicator {
    
    // 时间相似度阈值（小时）- 调整为更宽松的6小时，避免误判
    private static final long TIME_SIMILARITY_THRESHOLD_HOURS = 6;
    
    // 文本相似度阈值 - 调整为更宽松的0.98，避免误判不同事件
    private static final double TEXT_SIMILARITY_THRESHOLD = 0.98;
    
    // 标题相似度阈值 - 调整为更严格的0.99，只有几乎完全相同的标题才认为重复
    private static final double TITLE_SIMILARITY_THRESHOLD = 0.99;
    
    /**
     * 对事件列表进行去重
     * 
     * @param events 原始事件列表
     * @return 去重后的事件列表
     */
    public List<EventData> deduplicateEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<EventData> uniqueEvents = new ArrayList<>();
        Set<String> processedHashes = new HashSet<>();
        
        for (EventData event : events) {
            if (event == null) {
                continue;
            }
            
            // 生成事件的唯一标识
            String eventHash = generateEventHash(event);
            
            // 检查是否已处理过完全相同的事件
            if (processedHashes.contains(eventHash)) {
                continue;
            }
            
            // 检查是否与已有事件相似
            EventData similarEvent = findSimilarEvent(event, uniqueEvents);
            
            if (similarEvent != null) {
                // 合并相似事件
                EventData mergedEvent = mergeEvents(similarEvent, event);
                // 替换原有事件
                int index = uniqueEvents.indexOf(similarEvent);
                uniqueEvents.set(index, mergedEvent);
            } else {
                // 添加新的唯一事件
                uniqueEvents.add(event);
            }
            
            processedHashes.add(eventHash);
        }
        
        return uniqueEvents;
    }
    
    /**
     * 生成事件的哈希值用于快速去重
     */
    private String generateEventHash(EventData event) {
        StringBuilder hashBuilder = new StringBuilder();
        
        // 使用关键字段生成哈希
        if (StringUtils.hasText(event.getTitle())) {
            hashBuilder.append(event.getTitle().toLowerCase().trim());
        }
        
        if (StringUtils.hasText(event.getSubject())) {
            hashBuilder.append("|").append(event.getSubject().toLowerCase().trim());
        }
        
        if (StringUtils.hasText(event.getObject())) {
            hashBuilder.append("|").append(event.getObject().toLowerCase().trim());
        }
        
        if (StringUtils.hasText(event.getEventType())) {
            hashBuilder.append("|").append(event.getEventType().toLowerCase().trim());
        }
        
        if (event.getEventTime() != null) {
            // 只使用日期部分，忽略具体时间
            hashBuilder.append("|").append(event.getEventTime().toLocalDate());
        }
        
        if (StringUtils.hasText(event.getLocation())) {
            hashBuilder.append("|").append(event.getLocation().toLowerCase().trim());
        }
        
        return Integer.toString(hashBuilder.toString().hashCode());
    }
    
    /**
     * 查找相似事件
     */
    private EventData findSimilarEvent(EventData targetEvent, List<EventData> existingEvents) {
        for (EventData existingEvent : existingEvents) {
            if (areEventsSimilar(targetEvent, existingEvent)) {
                return existingEvent;
            }
        }
        return null;
    }
    
    /**
     * 判断两个事件是否相似
     */
    private boolean areEventsSimilar(EventData event1, EventData event2) {
        // 检查标题相似度
        if (isTitleSimilar(event1.getTitle(), event2.getTitle())) {
            return true;
        }
        
        // 检查主体、客体、类型完全匹配且时间接近
        if (isKeyFieldsMatch(event1, event2) && isTimeClose(event1.getEventTime(), event2.getEventTime())) {
            return true;
        }
        
        // 检查描述相似度
        if (isDescriptionSimilar(event1.getDescription(), event2.getDescription()) && 
            isTimeClose(event1.getEventTime(), event2.getEventTime())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查标题相似度
     */
    private boolean isTitleSimilar(String title1, String title2) {
        if (!StringUtils.hasText(title1) || !StringUtils.hasText(title2)) {
            return false;
        }
        
        double similarity = calculateTextSimilarity(title1, title2);
        return similarity >= TITLE_SIMILARITY_THRESHOLD;
    }
    
    /**
     * 检查关键字段是否匹配
     */
    private boolean isKeyFieldsMatch(EventData event1, EventData event2) {
        return Objects.equals(normalizeString(event1.getSubject()), normalizeString(event2.getSubject())) &&
               Objects.equals(normalizeString(event1.getObject()), normalizeString(event2.getObject())) &&
               Objects.equals(normalizeString(event1.getEventType()), normalizeString(event2.getEventType()));
    }
    
    /**
     * 检查描述相似度
     */
    private boolean isDescriptionSimilar(String desc1, String desc2) {
        if (!StringUtils.hasText(desc1) || !StringUtils.hasText(desc2)) {
            return false;
        }
        
        double similarity = calculateTextSimilarity(desc1, desc2);
        return similarity >= TEXT_SIMILARITY_THRESHOLD;
    }
    
    /**
     * 检查时间是否接近
     */
    private boolean isTimeClose(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null || time2 == null) {
            return time1 == time2; // 都为null时认为相同
        }
        
        long hoursDiff = Math.abs(ChronoUnit.HOURS.between(time1, time2));
        return hoursDiff <= TIME_SIMILARITY_THRESHOLD_HOURS;
    }
    
    /**
     * 计算文本相似度
     */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // 简化的相似度计算：基于词汇重叠
        Set<String> words1 = Arrays.stream(text1.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
        Set<String> words2 = Arrays.stream(text2.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * 合并两个相似事件
     */
    private EventData mergeEvents(EventData existingEvent, EventData newEvent) {
        EventData mergedEvent = new EventData();
        
        // 使用更完整的信息
        mergedEvent.setId(existingEvent.getId()); // 保持原有ID
        mergedEvent.setTitle(chooseBetterText(existingEvent.getTitle(), newEvent.getTitle()));
        mergedEvent.setDescription(chooseBetterText(existingEvent.getDescription(), newEvent.getDescription()));
        mergedEvent.setSubject(chooseBetterText(existingEvent.getSubject(), newEvent.getSubject()));
        mergedEvent.setObject(chooseBetterText(existingEvent.getObject(), newEvent.getObject()));
        mergedEvent.setEventType(chooseBetterText(existingEvent.getEventType(), newEvent.getEventType()));
        mergedEvent.setLocation(chooseBetterText(existingEvent.getLocation(), newEvent.getLocation()));
        
        // 选择更精确的时间
        mergedEvent.setEventTime(chooseBetterTime(existingEvent.getEventTime(), newEvent.getEventTime()));
        
        // 合并关键词
        mergedEvent.setKeywords(mergeKeywords(existingEvent.getKeywords(), newEvent.getKeywords()));
        
        // 合并来源
        mergedEvent.setSources(mergeSources(existingEvent.getSources(), newEvent.getSources()));
        
        return mergedEvent;
    }
    
    /**
     * 选择更好的文本（更长且有意义的）
     */
    private String chooseBetterText(String text1, String text2) {
        if (!StringUtils.hasText(text1)) {
            return text2;
        }
        if (!StringUtils.hasText(text2)) {
            return text1;
        }
        
        // 选择更长的文本
        return text1.length() >= text2.length() ? text1 : text2;
    }
    
    /**
     * 选择更好的时间
     */
    private LocalDateTime chooseBetterTime(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null) {
            return time2;
        }
        if (time2 == null) {
            return time1;
        }
        
        // 选择更早的时间（通常更准确）
        return time1.isBefore(time2) ? time1 : time2;
    }
    
    /**
     * 合并关键词列表
     */
    private List<String> mergeKeywords(List<String> keywords1, List<String> keywords2) {
        Set<String> mergedKeywords = new HashSet<>();
        
        if (keywords1 != null) {
            mergedKeywords.addAll(keywords1);
        }
        
        if (keywords2 != null) {
            mergedKeywords.addAll(keywords2);
        }
        
        return new ArrayList<>(mergedKeywords);
    }
    
    /**
     * 合并来源列表
     */
    private List<String> mergeSources(List<String> sources1, List<String> sources2) {
        Set<String> mergedSources = new HashSet<>();
        
        if (sources1 != null) {
            mergedSources.addAll(sources1);
        }
        
        if (sources2 != null) {
            mergedSources.addAll(sources2);
        }
        
        return new ArrayList<>(mergedSources);
    }
    
    /**
     * 标准化字符串
     */
    private String normalizeString(String str) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        return str.toLowerCase().trim();
    }
}