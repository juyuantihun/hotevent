package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.StorageStats;
import com.hotech.events.entity.Dictionary;
import com.hotech.events.entity.Event;
import com.hotech.events.mapper.DictionaryMapper;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.service.EventStorageService;
import com.hotech.events.service.FallbackDataGenerator;
import com.hotech.events.util.EventDeduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 事件存储服务实现类
 * 
 * @author Kiro
 */
@Service
public class EventStorageServiceImpl implements EventStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventStorageServiceImpl.class);
    
    @Autowired
    private EventMapper eventMapper;
    
    @Autowired
    private DictionaryMapper dictionaryMapper;
    
    @Autowired
    private EventDeduplicator eventDeduplicator;
    
    @Autowired
    private FallbackDataGenerator fallbackDataGenerator;
    
    @Autowired(required = false)
    private com.hotech.events.service.EventGeographicIntegrationService eventGeographicIntegrationService;
    
    @Autowired(required = false)
    private com.hotech.events.service.EventGeographicEnhancementService eventGeographicEnhancementService;
    
    // 统计信息
    private final AtomicLong totalStoredEvents = new AtomicLong(0);
    private final AtomicLong newEventsCreated = new AtomicLong(0);
    private final AtomicLong eventsUpdated = new AtomicLong(0);
    private final AtomicLong duplicateEventsFound = new AtomicLong(0);
    private final AtomicLong dictionaryUpdates = new AtomicLong(0);
    private final AtomicLong batchOperations = new AtomicLong(0);
    private volatile LocalDateTime statsStartTime = LocalDateTime.now();
    
    @Override
    @Transactional
    public Long storeValidatedEvent(EventData eventData) {
        if (eventData == null) {
            throw new IllegalArgumentException("事件数据不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("开始存储事件: {}", eventData.getTitle());
            
            // 集成地理信息处理
            if (eventGeographicIntegrationService != null) {
                eventData = eventGeographicIntegrationService.integrateGeographicInfoForSingleEvent(eventData);
            }
            
            // 增强地理信息（为缺少经纬度的事件补充坐标）
            if (eventGeographicEnhancementService != null) {
                eventData = eventGeographicEnhancementService.enhanceEventDataGeographicInfo(eventData);
            }
            
            // 检查是否已存在相似事件
            Event existingEvent = findExistingEvent(eventData);
            
            Event savedEvent;
            if (existingEvent != null) {
                logger.debug("发现相似事件，更新现有事件: {}", existingEvent.getId());
                savedEvent = updateExistingEvent(existingEvent, eventData);
                eventsUpdated.incrementAndGet();
            } else {
                logger.debug("创建新事件");
                savedEvent = createNewEvent(eventData);
                newEventsCreated.incrementAndGet();
            }
            
            // 更新字典表
            updateDictionaries(eventData);
            
            totalStoredEvents.incrementAndGet();
            
            long endTime = System.currentTimeMillis();
            logger.debug("事件存储完成，耗时: {}ms", endTime - startTime);
            
            return savedEvent.getId();
            
        } catch (Exception e) {
            logger.error("存储事件失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储事件失败", e);
        }
    }
    
    @Override
    @Transactional
    public List<Long> storeEventsBatch(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            logger.warn("=== ❌ 批量存储警告 ===");
            logger.warn("输入事件列表为空，无法存储任何事件");
            logger.warn("这可能是因为API响应解析失败或事件验证失败");
            logger.warn("调用堆栈信息:");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 1; i <= Math.min(5, stackTrace.length - 1); i++) {
                logger.warn("  at {}", stackTrace[i]);
            }
            return new ArrayList<>();
        }
        
        logger.info("=== 🚀 开始批量存储事件 ===");
        logger.info("输入事件数量: {}", events.size());
        
        // 打印前几个事件的详细信息用于调试
        for (int i = 0; i < Math.min(3, events.size()); i++) {
            EventData event = events.get(i);
            logger.info("事件 {}: id={}, title={}, time={}, location={}", 
                    i + 1, event.getId(), event.getTitle(), event.getEventTime(), event.getLocation());
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 先进行去重
            List<EventData> deduplicatedEvents = deduplicateEvents(events);
            logger.info("去重后剩余事件数量: {}", deduplicatedEvents.size());
            
            List<Long> storedEventIds = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            // 批量处理
            for (int i = 0; i < deduplicatedEvents.size(); i++) {
                EventData eventData = deduplicatedEvents.get(i);
                try {
                    logger.debug("正在存储事件 {}/{}: {}", i + 1, deduplicatedEvents.size(), eventData.getTitle());
                    Long eventId = storeValidatedEvent(eventData);
                    storedEventIds.add(eventId);
                    successCount++;
                    logger.debug("事件存储成功: eventId={}, title={}", eventId, eventData.getTitle());
                } catch (Exception e) {
                    failCount++;
                    logger.error("批量存储中单个事件失败: title={}, error={}", eventData.getTitle(), e.getMessage());
                    // 继续处理其他事件
                }
            }
            
            batchOperations.incrementAndGet();
            
            long endTime = System.currentTimeMillis();
            
            logger.info("=== 批量存储完成 ===");
            logger.info("成功存储事件数: {}", successCount);
            logger.info("失败事件数: {}", failCount);
            logger.info("返回的事件ID数量: {}", storedEventIds.size());
            logger.info("存储耗时: {}ms", endTime - startTime);
            
            // 打印前几个存储成功的事件ID
            for (int i = 0; i < Math.min(5, storedEventIds.size()); i++) {
                logger.info("存储成功的事件ID {}: {}", i + 1, storedEventIds.get(i));
            }
            
            return storedEventIds;
            
        } catch (Exception e) {
            logger.error("=== 批量存储失败 ===");
            logger.error("批量存储事件失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量存储事件失败", e);
        }
    }
    
    @Override
    public void updateDictionaries(EventData eventData) {
        try {
            // 更新主体字典
            if (StringUtils.hasText(eventData.getSubject())) {
                updateDictionaryEntry("subject", eventData.getSubject());
            }
            
            // 更新客体字典
            if (StringUtils.hasText(eventData.getObject())) {
                updateDictionaryEntry("object", eventData.getObject());
            }
            
            // 更新事件类型字典
            if (StringUtils.hasText(eventData.getEventType())) {
                updateDictionaryEntry("event_type", eventData.getEventType());
            }
            
            // 更新地点字典
            if (StringUtils.hasText(eventData.getLocation())) {
                updateDictionaryEntry("location", eventData.getLocation());
            }
            
            // 更新关键词字典
            if (eventData.getKeywords() != null && !eventData.getKeywords().isEmpty()) {
                for (String keyword : eventData.getKeywords()) {
                    if (StringUtils.hasText(keyword)) {
                        updateDictionaryEntry("keyword", keyword);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("更新字典表失败: {}", e.getMessage(), e);
            // 字典更新失败不应该影响事件存储
        }
    }
    
    @Override
    public List<EventData> deduplicateEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            // 如果没有事件，直接返回空列表，不再自动创建测试事件
            logger.warn("输入事件列表为空，返回空列表");
            return new ArrayList<>();
        }
        
        logger.debug("开始事件去重，原始事件数: {}", events.size());
        
        List<EventData> deduplicatedEvents = eventDeduplicator.deduplicateEvents(events);
        
        int duplicatesFound = events.size() - deduplicatedEvents.size();
        duplicateEventsFound.addAndGet(duplicatesFound);
        
        logger.debug("事件去重完成，去重后事件数: {}，发现重复: {}", 
                   deduplicatedEvents.size(), duplicatesFound);
        
        // 如果去重后没有事件，直接返回空列表，不再自动创建测试事件
        if (deduplicatedEvents.isEmpty()) {
            logger.warn("去重后事件列表为空，返回空列表");
            return new ArrayList<>();
        }
        
        return deduplicatedEvents;
    }
    
    /**
     * 创建测试事件（使用增强的备用数据生成器）
     */
    private List<EventData> createTestEvents() {
        logger.info("开始使用备用数据生成器创建测试事件");
        
        List<EventData> testEvents = new ArrayList<>();
        
        try {
            // 使用备用数据生成器创建多种类型的测试事件
            
            // 1. 生成通用事件
            List<EventData> genericEvents = fallbackDataGenerator.generateGenericEvents(5);
            testEvents.addAll(genericEvents);
            
            // 2. 生成基于主题的测试事件
            List<EventData> themeEvents = fallbackDataGenerator.generateTestEvents("国际关系", 3);
            testEvents.addAll(themeEvents);
            
            // 3. 生成历史事件
            LocalDateTime startTime = LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = LocalDateTime.now();
            List<Long> regionIds = Arrays.asList(1L, 2L, 3L); // 模拟地区ID
            List<EventData> historicalEvents = fallbackDataGenerator.generateHistoricalEvents(
                regionIds, startTime, endTime, 4);
            testEvents.addAll(historicalEvents);
            
            // 4. 生成默认事件
            List<EventData> defaultEvents = fallbackDataGenerator.generateDefaultEvents(
                regionIds, startTime, endTime);
            testEvents.addAll(defaultEvents);
            
            // 为所有测试事件设置统一的属性
            for (int i = 0; i < testEvents.size(); i++) {
                EventData event = testEvents.get(i);
                event.setId("enhanced_test_event_" + (i + 1));
                event.setFetchMethod("FALLBACK_GENERATOR");
                event.setValidationStatus("VERIFIED");
                event.setCredibilityScore(1.0);
                
                // 确保事件有来源信息
                if (event.getSources() == null || event.getSources().isEmpty()) {
                    event.setSources(Arrays.asList("备用数据生成器"));
                }
                
                // 确保事件有关键词
                if (event.getKeywords() == null || event.getKeywords().isEmpty()) {
                    List<String> keywords = new ArrayList<>();
                    if (StringUtils.hasText(event.getSubject())) {
                        keywords.add(event.getSubject());
                    }
                    if (StringUtils.hasText(event.getObject())) {
                        keywords.add(event.getObject());
                    }
                    if (StringUtils.hasText(event.getEventType())) {
                        keywords.add(event.getEventType());
                    }
                    event.setKeywords(keywords);
                }
            }
            
            // 限制最大数量，避免过多事件
            if (testEvents.size() > 15) {
                testEvents = testEvents.subList(0, 15);
            }
            
            logger.info("使用备用数据生成器创建了 {} 个增强测试事件", testEvents.size());
            
        } catch (Exception e) {
            logger.error("使用备用数据生成器创建测试事件失败，回退到简单模式: {}", e.getMessage());
            
            // 如果备用数据生成器失败，回退到简单的测试事件创建
            testEvents = createSimpleTestEvents();
        }
        
        return testEvents;
    }
    
    /**
     * 创建简单的测试事件（备用方案）
     */
    private List<EventData> createSimpleTestEvents() {
        List<EventData> testEvents = new ArrayList<>();
        
        // 创建基本的测试事件
        for (int i = 1; i <= 8; i++) {
            EventData event = new EventData();
            event.setId("simple_test_event_" + i);
            event.setTitle("测试事件 " + i);
            event.setDescription("这是第 " + i + " 个简单测试事件，用于确保系统正常运行");
            event.setEventTime(LocalDateTime.now().minusDays(i).minusHours(i * 2));
            event.setLocation("测试地点 " + i);
            event.setSubject("测试主体 " + i);
            event.setObject("测试客体 " + i);
            event.setEventType("测试类型");
            event.setFetchMethod("SIMPLE_FALLBACK");
            event.setValidationStatus("VERIFIED");
            event.setCredibilityScore(1.0);
            event.setKeywords(Arrays.asList("测试", "事件", "简单"));
            event.setSources(Arrays.asList("简单测试生成器"));
            
            // 添加测试坐标
            double[] coordinates = getTestCoordinates(i);
            event.setLatitude(coordinates[0]);
            event.setLongitude(coordinates[1]);
            
            testEvents.add(event);
        }
        
        logger.info("创建了 {} 个简单测试事件", testEvents.size());
        return testEvents;
    }
    
    @Override
    public Event findExistingEvent(EventData eventData) {
        try {
            // 基于多个条件查找相似事件
            List<Event> candidateEvents = new ArrayList<>();
            
            // 1. 基于标题查找
            if (StringUtils.hasText(eventData.getTitle())) {
                List<Event> titleMatches = eventMapper.findByTitleSimilar(eventData.getTitle());
                candidateEvents.addAll(titleMatches);
            }
            
            // 2. 基于主体、客体、类型查找
            if (StringUtils.hasText(eventData.getSubject()) && 
                StringUtils.hasText(eventData.getObject()) && 
                StringUtils.hasText(eventData.getEventType())) {
                
                List<Event> keyFieldMatches = eventMapper.findByKeyFields(
                    eventData.getSubject(), 
                    eventData.getObject(), 
                    eventData.getEventType()
                );
                candidateEvents.addAll(keyFieldMatches);
            }
            
            // 3. 基于时间和地点查找 - 缩小时间范围，避免误判
            if (eventData.getEventTime() != null && StringUtils.hasText(eventData.getLocation())) {
                LocalDateTime startTime = eventData.getEventTime().minusHours(1);  // 缩小到1小时
                LocalDateTime endTime = eventData.getEventTime().plusHours(1);     // 缩小到1小时
                
                List<Event> timeLocationMatches = eventMapper.findByTimeAndLocation(
                    startTime, endTime, eventData.getLocation()
                );
                candidateEvents.addAll(timeLocationMatches);
            }
            
            // 去重候选事件
            Set<Long> seenIds = new HashSet<>();
            candidateEvents = candidateEvents.stream()
                    .filter(event -> seenIds.add(event.getId()))
                    .collect(Collectors.toList());
            
            // 找到最相似的事件
            return findMostSimilarEvent(eventData, candidateEvents);
            
        } catch (Exception e) {
            logger.error("查找现有事件失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public Event updateExistingEvent(Event existingEvent, EventData newEventData) {
        try {
            // 更新事件信息，保留更完整的数据
            if (StringUtils.hasText(newEventData.getTitle()) && 
                (existingEvent.getEventTitle() == null || 
                 newEventData.getTitle().length() > existingEvent.getEventTitle().length())) {
                existingEvent.setEventTitle(newEventData.getTitle());
            }
            
            if (StringUtils.hasText(newEventData.getDescription()) && 
                (existingEvent.getEventDescription() == null || 
                 newEventData.getDescription().length() > existingEvent.getEventDescription().length())) {
                existingEvent.setEventDescription(newEventData.getDescription());
            }
            
            // 更新验证相关字段（已取消可信度判断）
            existingEvent.setCredibilityScore(1.0); // 固定设置为1.0，不再进行可信度判断
            
            existingEvent.setValidationStatus("UPDATED");
            existingEvent.setLastValidatedAt(LocalDateTime.now());
            existingEvent.setUpdatedAt(LocalDateTime.now());
            
            // 更新来源信息
            if (newEventData.getSources() != null && !newEventData.getSources().isEmpty()) {
                // 这里简化处理，实际应该合并来源
                existingEvent.setSourceUrls(String.join(",", newEventData.getSources()));
            }
            
            eventMapper.updateById(existingEvent);
            
            logger.debug("更新现有事件完成: {}", existingEvent.getId());
            
            return existingEvent;
            
        } catch (Exception e) {
            logger.error("更新现有事件失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新现有事件失败", e);
        }
    }
    
    @Override
    @Transactional
    public Event createNewEvent(EventData eventData) {
        try {
            Event newEvent = new Event();
            
            // 生成唯一的事件编码
            String eventCode = generateEventCode();
            newEvent.setEventCode(eventCode);
            
            // 设置基本信息
            newEvent.setEventTitle(eventData.getTitle());
            newEvent.setEventDescription(eventData.getDescription());
            
            // 确保事件时间不为空，如果为空则使用当前时间
            LocalDateTime eventTime = eventData.getEventTime();
            if (eventTime == null) {
                logger.warn("事件时间为空，使用当前时间作为默认值: {}", eventData.getTitle());
                eventTime = LocalDateTime.now();
            }
            newEvent.setEventTime(eventTime);
            
            newEvent.setEventLocation(eventData.getLocation());
            newEvent.setSubject(eventData.getSubject());
            newEvent.setObject(eventData.getObject());
            newEvent.setEventType(eventData.getEventType());
            
            // 设置基础坐标（向后兼容）
            if (eventData.getLatitude() != null && eventData.getLongitude() != null) {
                newEvent.setLatitude(java.math.BigDecimal.valueOf(eventData.getLatitude()));
                newEvent.setLongitude(java.math.BigDecimal.valueOf(eventData.getLongitude()));
            }
            
            // 处理地理信息存储
            if (eventGeographicIntegrationService != null) {
                com.hotech.events.service.EventGeographicIntegrationService.GeographicStorageResult geoResult = 
                    eventGeographicIntegrationService.prepareGeographicDataForStorage(eventData);
                
                if (geoResult.isSuccess()) {
                    newEvent.setEventCoordinateId(geoResult.getEventCoordinateId());
                    newEvent.setSubjectCoordinateId(geoResult.getSubjectCoordinateId());
                    newEvent.setObjectCoordinateId(geoResult.getObjectCoordinateId());
                    newEvent.setGeographicStatus(com.hotech.events.constant.GeographicStatus.PROCESSED);
                    newEvent.setGeographicUpdatedAt(LocalDateTime.now());
                    
                    logger.debug("事件地理信息存储成功: eventCoordId={}, subjectCoordId={}, objectCoordId={}", 
                            geoResult.getEventCoordinateId(), geoResult.getSubjectCoordinateId(), geoResult.getObjectCoordinateId());
                } else {
                    newEvent.setGeographicStatus(com.hotech.events.constant.GeographicStatus.FAILED);
                    logger.warn("事件地理信息存储失败: {}", geoResult.getErrorMessage());
                }
            }
            
            // 设置验证相关信息（已取消可信度判断）
            newEvent.setCredibilityScore(1.0); // 固定设置为1.0，不再进行可信度判断
            newEvent.setValidationStatus("PASSED");
            newEvent.setFetchMethod("DEEPSEEK");
            newEvent.setLastValidatedAt(LocalDateTime.now());
            
            // 设置来源信息
            if (eventData.getSources() != null && !eventData.getSources().isEmpty()) {
                newEvent.setSourceUrls(String.join(",", eventData.getSources()));
            }
            
            // 设置时间戳
            LocalDateTime now = LocalDateTime.now();
            newEvent.setCreatedAt(now);
            newEvent.setUpdatedAt(now);
            
            eventMapper.insert(newEvent);
            
            logger.debug("创建新事件完成: {}", newEvent.getId());
            
            return newEvent;
            
        } catch (Exception e) {
            logger.error("创建新事件失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建新事件失败", e);
        }
    }
    
    @Override
    public StorageStats getStorageStats() {
        StorageStats stats = new StorageStats();
        stats.setTotalStoredEvents(totalStoredEvents.get());
        stats.setNewEventsCreated(newEventsCreated.get());
        stats.setEventsUpdated(eventsUpdated.get());
        stats.setDuplicateEventsFound(duplicateEventsFound.get());
        stats.setDictionaryUpdates(dictionaryUpdates.get());
        stats.setBatchOperations(batchOperations.get());
        stats.setStartTime(statsStartTime);
        stats.setEndTime(LocalDateTime.now());
        
        return stats;
    }
    
    @Override
    @Transactional
    public int cleanupOldData(int daysOld) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
            
            // 清理旧的验证记录
            int cleanedRecords = eventMapper.deleteOldValidationRecords(cutoffTime);
            
            logger.info("清理了 {} 条 {} 天前的旧数据", cleanedRecords, daysOld);
            
            return cleanedRecords;
            
        } catch (Exception e) {
            logger.error("清理旧数据失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 更新字典条目
     */
    private void updateDictionaryEntry(String dictType, String dictValue) {
        try {
            // 检查是否已存在
            Dictionary existing = dictionaryMapper.findByTypeAndValue(dictType, dictValue);
            
            if (existing == null) {
                // 创建新的字典条目
                Dictionary newDict = new Dictionary(dictType, dictValue, dictValue);
                // 设置其他必要字段...
                
                dictionaryMapper.insert(newDict);
                dictionaryUpdates.incrementAndGet();
                
                logger.debug("添加新字典条目: {} = {}", dictType, dictValue);
            }
            
        } catch (Exception e) {
            logger.error("更新字典条目失败: {} = {}, 错误: {}", dictType, dictValue, e.getMessage());
        }
    }
    
    /**
     * 找到最相似的事件
     */
    private Event findMostSimilarEvent(EventData eventData, List<Event> candidateEvents) {
        if (candidateEvents == null || candidateEvents.isEmpty()) {
            return null;
        }
        
        // 简化的相似度计算，实际应该使用更复杂的算法
        for (Event candidate : candidateEvents) {
            if (isEventSimilar(eventData, candidate)) {
                return candidate;
            }
        }
        
        return null;
    }
    
    /**
     * 判断事件是否相似
     */
    private boolean isEventSimilar(EventData eventData, Event existingEvent) {
        // 标题相似度检查
        if (StringUtils.hasText(eventData.getTitle()) && 
            StringUtils.hasText(existingEvent.getEventTitle())) {
            
            String title1 = eventData.getTitle().toLowerCase();
            String title2 = existingEvent.getEventTitle().toLowerCase();
            
            if (title1.equals(title2) || title1.contains(title2) || title2.contains(title1)) {
                return true;
            }
        }
        
        // 关键字段匹配检查
        if (Objects.equals(eventData.getSubject(), existingEvent.getSubject()) &&
            Objects.equals(eventData.getObject(), existingEvent.getObject()) &&
            Objects.equals(eventData.getEventType(), existingEvent.getEventType())) {
            
            // 时间接近检查
            if (eventData.getEventTime() != null && existingEvent.getEventTime() != null) {
                long hoursDiff = Math.abs(java.time.Duration.between(
                    eventData.getEventTime(), existingEvent.getEventTime()).toHours());
                
                if (hoursDiff <= 24) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 生成唯一的事件编码
     */
    private String generateEventCode() {
        // 使用时间戳 + 随机数生成唯一编码
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("EVT_%d_%03d", timestamp, random);
    }
    
    /**
     * 获取测试事件的坐标
     * 
     * @param index 索引
     * @return 坐标数组 [纬度, 经度]
     */
    private double[] getTestCoordinates(int index) {
        // 预定义一些测试城市的坐标
        double[][] testCoordinates = {
            {39.9042, 116.4074}, // 北京
            {31.2304, 121.4737}, // 上海
            {23.1291, 113.2644}, // 广州
            {22.5431, 114.0579}, // 深圳
            {30.2741, 120.1551}, // 杭州
            {32.0603, 118.7969}, // 南京
            {30.5928, 114.3055}, // 武汉
            {30.5728, 104.0668}, // 成都
        };
        
        // 根据索引循环使用坐标
        int coordinateIndex = (index - 1) % testCoordinates.length;
        return testCoordinates[coordinateIndex];
    }
}