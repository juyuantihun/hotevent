package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.dto.event.EventQueryDTO;
import com.hotech.events.entity.Event;
import com.hotech.events.entity.EventKeyword;
import com.hotech.events.entity.TimelineEvent;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.mapper.EventKeywordMapper;
import com.hotech.events.mapper.EventRelationMapper;
import com.hotech.events.mapper.TimelineEventMapper;
import com.hotech.events.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件服务实现类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventKeywordMapper eventKeywordMapper;

    @Autowired
    private EventRelationMapper eventRelationMapper;

    @Autowired
    private com.hotech.events.mapper.TimelineEventMapper timelineEventMapper;

    @Autowired(required = false)
    private com.hotech.events.service.EventGeographicIntegrationService eventGeographicIntegrationService;

    /**
     * 分页查询事件列表
     */
    @Override
    public Page<EventDTO> getEventList(EventQueryDTO queryDTO) {
        log.info("查询事件列表，查询条件：{}", queryDTO);

        Page<Event> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<Event> wrapper = buildQueryWrapper(queryDTO);

        Page<Event> eventPage = eventMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<EventDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(eventPage, resultPage, "records");

        List<EventDTO> eventDTOs = eventPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        resultPage.setRecords(eventDTOs);

        log.info("查询事件列表完成，共{}条记录", resultPage.getTotal());
        return resultPage;
    }

    /**
     * 根据ID获取事件详情
     */
    @Override
    public EventDTO getEventDetail(Long id) {
        log.info("获取事件详情，ID：{}", id);

        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("事件不存在，ID：" + id);
        }

        EventDTO eventDTO = convertToDTO(event);

        // 加载关键词
        QueryWrapper<EventKeyword> keywordWrapper = new QueryWrapper<>();
        keywordWrapper.eq("event_id", id);
        List<EventKeyword> keywords = eventKeywordMapper.selectList(keywordWrapper);

        if (!keywords.isEmpty()) {
            List<String> keywordList = keywords.stream()
                    .map(EventKeyword::getKeyword)
                    .collect(Collectors.toList());
            eventDTO.setKeywords(keywordList);
        }

        log.info("获取事件详情完成，ID：{}", id);
        return eventDTO;
    }

    /**
     * 创建事件
     */
    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        log.info("创建事件，事件信息：{}", eventDTO);
        log.info("EventDTO - eventTitle: {}, eventDescription: {}, eventLocation: {}", 
                eventDTO.getEventTitle(), eventDTO.getEventDescription(), eventDTO.getEventLocation());

        Event event = convertToEntity(eventDTO);
        log.info("转换后的Event - eventTitle: {}, eventDescription: {}, eventLocation: {}", 
                event.getEventTitle(), event.getEventDescription(), event.getEventLocation());

        // 生成事件编码
        if (!StringUtils.hasText(event.getEventCode())) {
            event.setEventCode(generateEventCode());
        }

        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        eventMapper.insert(event);

        // 保存关键词
        saveEventKeywords(event.getId(), eventDTO.getKeywords());

        log.info("创建事件完成，事件ID：{}", event.getId());
        return convertToDTO(event);
    }

    /**
     * 批量创建事件
     */
    @Override
    @Transactional
    public Integer createEventsBatch(List<EventDTO> eventDTOs) {
        log.info("批量创建事件，数量：{}", eventDTOs.size());

        int successCount = 0;
        for (EventDTO eventDTO : eventDTOs) {
            try {
                createEvent(eventDTO);
                successCount++;
            } catch (Exception e) {
                log.error("创建事件失败：{}", e.getMessage(), e);
            }
        }

        log.info("批量创建事件完成，成功：{}，失败：{}", successCount, eventDTOs.size() - successCount);
        return successCount;
    }

    /**
     * 批量创建事件并建立关系
     */
    @Override
    @Transactional
    public Map<String, Object> createEventsBatchWithRelations(
            com.hotech.events.dto.event.BatchEventRequestDTO batchRequest) {
        log.info("批量创建事件并建立关系，事件数量：{}", batchRequest.getEvents().size());

        Map<String, Object> result = new HashMap<>();
        List<Long> eventIds = new ArrayList<>();
        int successCount = 0;

        try {
            // 1. 批量创建事件
            for (EventDTO eventDTO : batchRequest.getEvents()) {
                try {
                    EventDTO created = createEvent(eventDTO);
                    eventIds.add(created.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("创建事件失败：{}", e.getMessage(), e);
                }
            }

            // 2. 创建事件关系（如果有的话）
            // 这里可以扩展关系创建逻辑

            result.put("success", true);
            result.put("eventCount", successCount);
            result.put("eventIds", eventIds);
            result.put("message", "批量创建事件成功");

        } catch (Exception e) {
            log.error("批量创建事件失败", e);
            result.put("success", false);
            result.put("message", "批量创建事件失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新事件
     */
    @Override
    @Transactional
    public EventDTO updateEvent(EventDTO eventDTO) {
        log.info("更新事件，事件信息：{}", eventDTO);

        Event existingEvent = eventMapper.selectById(eventDTO.getId());
        if (existingEvent == null) {
            throw new RuntimeException("事件不存在，ID：" + eventDTO.getId());
        }

        Event event = convertToEntity(eventDTO);
        event.setUpdatedAt(LocalDateTime.now());

        eventMapper.updateById(event);

        // 更新关键词
        deleteEventKeywords(event.getId());
        saveEventKeywords(event.getId(), eventDTO.getKeywords());

        log.info("更新事件完成，事件ID：{}", event.getId());
        return convertToDTO(event);
    }

    /**
     * 删除事件
     */
    @Override
    @Transactional
    public Boolean deleteEvent(Long id) {
        log.info("删除事件，ID：{}", id);

        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("事件不存在，ID：" + id);
        }

        // 删除关键词
        deleteEventKeywords(id);

        // 删除事件
        eventMapper.deleteById(id);

        log.info("删除事件完成，ID：{}", id);
        return true;
    }

    /**
     * 批量删除事件
     */
    @Override
    @Transactional
    public Integer deleteEventsBatch(List<Long> ids) {
        log.info("批量删除事件，IDs：{}", ids);

        int successCount = 0;
        for (Long id : ids) {
            try {
                deleteEvent(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除事件失败，ID：{}，错误：{}", id, e.getMessage(), e);
            }
        }

        log.info("批量删除事件完成，成功：{}，失败：{}", successCount, ids.size() - successCount);
        return successCount;
    }

    /**
     * 获取事件关联图谱数据
     */
    @Override
    public Object getEventGraph(Long eventId) {
        log.info("获取事件关联图谱，事件ID：{}", eventId);

        // TODO: 实现事件关联图谱逻辑
        Map<String, Object> graphData = new HashMap<>();
        graphData.put("nodes", new ArrayList<>());
        graphData.put("edges", new ArrayList<>());

        return graphData;
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<Event> buildQueryWrapper(EventQueryDTO queryDTO) {
        QueryWrapper<Event> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getEventType())) {
            wrapper.eq("event_type", queryDTO.getEventType());
        }

        if (StringUtils.hasText(queryDTO.getSubject())) {
            wrapper.like("subject", queryDTO.getSubject());
        }

        if (StringUtils.hasText(queryDTO.getObject())) {
            wrapper.like("object", queryDTO.getObject());
        }

        if (StringUtils.hasText(queryDTO.getEventLocation())) {
            wrapper.like("event_location", queryDTO.getEventLocation());
        }

        if (queryDTO.getSourceType() != null) {
            wrapper.eq("source_type", queryDTO.getSourceType());
        }

        if (queryDTO.getStartTime() != null) {
            wrapper.ge("event_time", queryDTO.getStartTime());
        }

        if (queryDTO.getEndTime() != null) {
            wrapper.le("event_time", queryDTO.getEndTime());
        }

        // 按创建时间倒序
        wrapper.orderByDesc("created_at");

        return wrapper;
    }

    /**
     * 实体转DTO
     */
    private EventDTO convertToDTO(Event event) {
        EventDTO eventDTO = new EventDTO();
        BeanUtils.copyProperties(event, eventDTO);

        // 加载地理信息
        if (eventGeographicIntegrationService != null) {
            try {
                com.hotech.events.dto.EventData eventData = eventGeographicIntegrationService
                        .loadGeographicInfoFromStoredEvent(event);

                if (eventData != null) {
                    // 将地理信息复制到EventDTO中
                    if (eventData.getEventCoordinate() != null) {
                        // 将Double类型的坐标转换为BigDecimal类型
                        Double lat = eventData.getEventCoordinate().getLatitude();
                        Double lon = eventData.getEventCoordinate().getLongitude();

                        if (lat != null) {
                            eventDTO.setLatitude(BigDecimal.valueOf(lat));
                        }
                        if (lon != null) {
                            eventDTO.setLongitude(BigDecimal.valueOf(lon));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("加载事件 {} 的地理信息时发生错误: {}", event.getId(), e.getMessage());
            }
        }

        return eventDTO;
    }

    /**
     * DTO转实体
     */
    private Event convertToEntity(EventDTO eventDTO) {
        Event event = new Event();
        BeanUtils.copyProperties(eventDTO, event);
        return event;
    }

    /**
     * 生成事件编码
     */
    private String generateEventCode() {
        return "EVT" + System.currentTimeMillis();
    }

    /**
     * 保存事件关键词
     */
    private void saveEventKeywords(Long eventId, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        List<EventKeyword> eventKeywords = keywords.stream()
                .map(keyword -> {
                    EventKeyword eventKeyword = new EventKeyword();
                    eventKeyword.setEventId(eventId);
                    eventKeyword.setKeyword(keyword);
                    eventKeyword.setCreatedAt(LocalDateTime.now());
                    return eventKeyword;
                })
                .collect(Collectors.toList());

        for (EventKeyword eventKeyword : eventKeywords) {
            eventKeywordMapper.insert(eventKeyword);
        }
    }

    /**
     * 删除事件关键词
     */
    private void deleteEventKeywords(Long eventId) {
        QueryWrapper<EventKeyword> wrapper = new QueryWrapper<>();
        wrapper.eq("event_id", eventId);
        eventKeywordMapper.delete(wrapper);
    }

    /**
     * 导出所有事件数据
     */
    @Override
    public List<EventDTO> exportAllEvents() {
        log.info("导出所有事件数据");

        // 查询所有事件
        List<Event> events = eventMapper.selectList(null);

        // 转换为DTO并加载关键词
        List<EventDTO> eventDTOs = events.stream()
                .map(event -> {
                    EventDTO eventDTO = convertToDTO(event);

                    // 加载关键词
                    QueryWrapper<EventKeyword> keywordWrapper = new QueryWrapper<>();
                    keywordWrapper.eq("event_id", event.getId());
                    List<EventKeyword> keywords = eventKeywordMapper.selectList(keywordWrapper);

                    if (!keywords.isEmpty()) {
                        List<String> keywordList = keywords.stream()
                                .map(EventKeyword::getKeyword)
                                .collect(Collectors.toList());
                        eventDTO.setKeywords(keywordList);
                    }

                    return eventDTO;
                })
                .collect(Collectors.toList());

        log.info("导出所有事件数据完成，共{}条记录", eventDTOs.size());
        return eventDTOs;
    }

    /**
     * 获取统计数据
     */
    @Override
    public Map<String, Object> getStats() {
        log.info("获取统计数据");

        Map<String, Object> stats = new HashMap<>();

        // 获取总事件数
        long totalEvents = eventMapper.selectCount(null);
        stats.put("totalEvents", totalEvents);

        // 获取今日新增事件数
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        QueryWrapper<Event> todayWrapper = new QueryWrapper<>();
        todayWrapper.between("created_at", startOfDay, endOfDay);
        long todayEvents = eventMapper.selectCount(todayWrapper);
        stats.put("todayEvents", todayEvents);

        // 获取人工录入事件数
        QueryWrapper<Event> manualWrapper = new QueryWrapper<>();
        manualWrapper.eq("source_type", 2);
        long manualEvents = eventMapper.selectCount(manualWrapper);
        stats.put("manualEvents", manualEvents);

        // 获取AI获取事件数
        QueryWrapper<Event> deepseekWrapper = new QueryWrapper<>();
        deepseekWrapper.eq("source_type", 1);
        long deepseekEvents = eventMapper.selectCount(deepseekWrapper);
        stats.put("deepseekEvents", deepseekEvents);

        log.info("统计数据：{}", stats);
        return stats;
    }

    /**
     * 获取地理分布统计数据
     */
    @Override
    public Map<String, Object> getGeographicStats() {
        log.info("获取地理分布统计数据");

        Map<String, Object> result = new HashMap<>();

        // 获取所有有地理位置的事件
        QueryWrapper<Event> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("event_location")
                .ne("event_location", "")
                .select("event_location", "latitude", "longitude");
        
        List<Event> events = eventMapper.selectList(wrapper);

        // 按国家/地区统计事件数量
        Map<String, Integer> countryStats = new HashMap<>();
        List<Map<String, Object>> mapData = new ArrayList<>();

        for (Event event : events) {
            String location = event.getEventLocation();
            if (location != null && !location.trim().isEmpty()) {
                // 提取国家名称（简化处理）
                String country = extractCountryFromLocation(location);
                countryStats.put(country, countryStats.getOrDefault(country, 0) + 1);

                // 如果有坐标信息，添加到地图数据中
                if (event.getLatitude() != null && event.getLongitude() != null) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("name", location);
                    point.put("value", new Object[]{
                        event.getLongitude().doubleValue(),
                        event.getLatitude().doubleValue(),
                        1 // 事件权重
                    });
                    mapData.add(point);
                }
            }
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> countryList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countryStats.entrySet()) {
            Map<String, Object> countryData = new HashMap<>();
            countryData.put("name", entry.getKey());
            countryData.put("value", entry.getValue());
            countryList.add(countryData);
        }

        // 按事件数量排序
        countryList.sort((a, b) -> Integer.compare((Integer) b.get("value"), (Integer) a.get("value")));

        result.put("countryStats", countryList);
        result.put("mapData", mapData);
        result.put("totalCountries", countryStats.size());
        result.put("totalEvents", events.size());

        log.info("地理分布统计数据：涉及{}个国家/地区，{}个事件", countryStats.size(), events.size());
        return result;
    }

    /**
     * 从地点字符串中提取国家名称
     */
    private String extractCountryFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return "未知";
        }

        // 地点到国家的映射表
        Map<String, String> locationToCountryMapping = new HashMap<>();
        
        // 美国相关地点
        locationToCountryMapping.put("华盛顿", "美国");
        locationToCountryMapping.put("华盛顿特区", "美国");
        locationToCountryMapping.put("纽约", "美国");
        locationToCountryMapping.put("洛杉矶", "美国");
        locationToCountryMapping.put("芝加哥", "美国");
        locationToCountryMapping.put("旧金山", "美国");
        
        // 中国相关地点
        locationToCountryMapping.put("北京", "中国");
        locationToCountryMapping.put("上海", "中国");
        locationToCountryMapping.put("广州", "中国");
        locationToCountryMapping.put("深圳", "中国");
        locationToCountryMapping.put("香港", "中国");
        locationToCountryMapping.put("台湾", "中国");
        locationToCountryMapping.put("台海", "中国");
        locationToCountryMapping.put("南海", "中国");
        
        // 俄罗斯相关地点
        locationToCountryMapping.put("莫斯科", "俄罗斯");
        locationToCountryMapping.put("圣彼得堡", "俄罗斯");
        locationToCountryMapping.put("顿涅茨克", "乌克兰");
        locationToCountryMapping.put("乌克兰东部", "乌克兰");
        locationToCountryMapping.put("基辅", "乌克兰");
        
        // 欧洲国家地点
        locationToCountryMapping.put("伦敦", "英国");
        locationToCountryMapping.put("巴黎", "法国");
        locationToCountryMapping.put("柏林", "德国");
        locationToCountryMapping.put("罗马", "意大利");
        locationToCountryMapping.put("马德里", "西班牙");
        locationToCountryMapping.put("布鲁塞尔", "比利时");
        
        // 亚洲国家地点
        locationToCountryMapping.put("东京", "日本");
        locationToCountryMapping.put("首尔", "韩国");
        locationToCountryMapping.put("平壤", "朝鲜");
        locationToCountryMapping.put("新德里", "印度");
        locationToCountryMapping.put("曼谷", "泰国");
        locationToCountryMapping.put("新加坡", "新加坡");
        
        // 中东地区
        locationToCountryMapping.put("加沙地带", "巴勒斯坦");
        locationToCountryMapping.put("耶路撒冷", "以色列");
        locationToCountryMapping.put("特拉维夫", "以色列");
        locationToCountryMapping.put("德黑兰", "伊朗");
        locationToCountryMapping.put("巴格达", "伊拉克");
        locationToCountryMapping.put("大马士革", "叙利亚");
        locationToCountryMapping.put("安卡拉", "土耳其");
        locationToCountryMapping.put("伊斯坦布尔", "土耳其");
        
        // 其他重要地点
        locationToCountryMapping.put("联合国总部", "国际组织");
        locationToCountryMapping.put("联合国", "国际组织");
        locationToCountryMapping.put("欧盟", "国际组织");
        
        // 直接匹配地点名称
        for (Map.Entry<String, String> entry : locationToCountryMapping.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 国家名称直接匹配
        Map<String, String> countryMapping = new HashMap<>();
        countryMapping.put("中国", "中国");
        countryMapping.put("美国", "美国");
        countryMapping.put("英国", "英国");
        countryMapping.put("日本", "日本");
        countryMapping.put("韩国", "韩国");
        countryMapping.put("朝鲜", "朝鲜");
        countryMapping.put("法国", "法国");
        countryMapping.put("德国", "德国");
        countryMapping.put("俄罗斯", "俄罗斯");
        countryMapping.put("乌克兰", "乌克兰");
        countryMapping.put("印度", "印度");
        countryMapping.put("巴西", "巴西");
        countryMapping.put("加拿大", "加拿大");
        countryMapping.put("澳大利亚", "澳大利亚");
        countryMapping.put("以色列", "以色列");
        countryMapping.put("伊朗", "伊朗");
        countryMapping.put("土耳其", "土耳其");
        countryMapping.put("意大利", "意大利");
        countryMapping.put("西班牙", "西班牙");
        countryMapping.put("比利时", "比利时");
        countryMapping.put("荷兰", "荷兰");
        countryMapping.put("瑞士", "瑞士");
        countryMapping.put("瑞典", "瑞典");
        countryMapping.put("挪威", "挪威");
        countryMapping.put("丹麦", "丹麦");
        countryMapping.put("芬兰", "芬兰");
        
        // 检查是否包含已知国家名称
        for (Map.Entry<String, String> entry : countryMapping.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 如果都没有匹配到，尝试从逗号分隔的最后部分提取
        String[] parts = location.split("[,，]");
        if (parts.length > 1) {
            String lastPart = parts[parts.length - 1].trim();
            // 再次检查最后部分是否是国家名
            for (Map.Entry<String, String> entry : countryMapping.entrySet()) {
                if (lastPart.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return lastPart.length() > 10 ? "其他" : lastPart;
        }

        // 如果没有匹配到任何已知地点或国家，返回原地点名（如果不太长）或"其他"
        return location.length() > 15 ? "其他" : location;
    }

    /**
     * 获取未关联到指定时间线的事件列表
     */
    @Override
    public Page<EventDTO> getUnlinkedEvents(Long timelineId, EventQueryDTO queryDTO) {
        log.info("获取未关联到时间线{}的事件列表，查询条件：{}", timelineId, queryDTO);

        // 创建分页对象
        Page<Event> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 首先获取已关联到该时间线的事件ID列表
        QueryWrapper<TimelineEvent> timelineEventWrapper = new QueryWrapper<>();
        timelineEventWrapper.eq("timeline_id", timelineId);
        List<TimelineEvent> timelineEvents = timelineEventMapper.selectList(timelineEventWrapper);
        
        List<Long> linkedEventIds = timelineEvents.stream()
                .map(TimelineEvent::getEventId)
                .collect(Collectors.toList());

        // 构建查询条件
        QueryWrapper<Event> wrapper = new QueryWrapper<>();
        
        // 排除已关联到指定时间线的事件
        if (!linkedEventIds.isEmpty()) {
            wrapper.notIn("id", linkedEventIds);
        }

        // 添加其他查询条件
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w.like("event_description", queryDTO.getKeyword())
                    .or().like("subject", queryDTO.getKeyword())
                    .or().like("object", queryDTO.getKeyword()));
        }

        if (queryDTO.getEventType() != null && !queryDTO.getEventType().trim().isEmpty()) {
            wrapper.eq("event_type", queryDTO.getEventType());
        }

        if (queryDTO.getStartTime() != null) {
            wrapper.ge("event_time", queryDTO.getStartTime());
        }

        if (queryDTO.getEndTime() != null) {
            wrapper.le("event_time", queryDTO.getEndTime());
        }

        // 按时间倒序排列
        wrapper.orderByDesc("event_time");

        // 执行分页查询
        IPage<Event> eventPage = eventMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<EventDTO> eventDTOs = eventPage.getRecords().stream()
                .map(event -> {
                    EventDTO eventDTO = convertToDTO(event);

                    // 加载关键词
                    QueryWrapper<EventKeyword> keywordWrapper = new QueryWrapper<>();
                    keywordWrapper.eq("event_id", event.getId());
                    List<EventKeyword> keywords = eventKeywordMapper.selectList(keywordWrapper);

                    if (!keywords.isEmpty()) {
                        List<String> keywordList = keywords.stream()
                                .map(EventKeyword::getKeyword)
                                .collect(Collectors.toList());
                        eventDTO.setKeywords(keywordList);
                    }

                    return eventDTO;
                })
                .collect(Collectors.toList());

        // 构建返回结果
        Page<EventDTO> result = new Page<>(eventPage.getCurrent(), eventPage.getSize(), eventPage.getTotal());
        result.setRecords(eventDTOs);

        log.info("获取未关联事件列表完成，共{}条记录，排除了{}个已关联事件", result.getTotal(), linkedEventIds.size());
        return result;
    }

    /**
     * 获取事件类型分布统计数据
     */
    @Override
    public Map<String, Object> getEventTypeStats() {
        log.info("获取事件类型分布统计数据");

        Map<String, Object> result = new HashMap<>();

        // 查询所有事件的类型分布
        QueryWrapper<Event> wrapper = new QueryWrapper<>();
        wrapper.select("event_type", "COUNT(*) as count")
                .isNotNull("event_type")
                .ne("event_type", "")
                .groupBy("event_type");

        List<Map<String, Object>> typeStats = eventMapper.selectMaps(wrapper);

        // 转换为前端需要的格式
        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        int totalCount = 0;

        for (Map<String, Object> stat : typeStats) {
            String eventType = (String) stat.get("event_type");
            Long count = (Long) stat.get("count");
            
            if (eventType != null && count != null) {
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("name", eventType);
                typeData.put("value", count.intValue());
                typeDistribution.add(typeData);
                totalCount += count.intValue();
            }
        }

        // 按数量排序
        typeDistribution.sort((a, b) -> Integer.compare((Integer) b.get("value"), (Integer) a.get("value")));

        // 只显示前6个主要类型，其余合并为"其他"
        List<Map<String, Object>> finalDistribution = new ArrayList<>();
        int otherCount = 0;
        int displayLimit = 6; // 显示前6个主要类型

        for (int i = 0; i < typeDistribution.size(); i++) {
            if (i < displayLimit) {
                finalDistribution.add(typeDistribution.get(i));
            } else {
                otherCount += (Integer) typeDistribution.get(i).get("value");
            }
        }

        // 如果有其他类型，添加"其他"分类
        if (otherCount > 0) {
            Map<String, Object> otherData = new HashMap<>();
            otherData.put("name", "其他");
            otherData.put("value", otherCount);
            finalDistribution.add(otherData);
        }

        result.put("typeDistribution", finalDistribution);
        result.put("totalCount", totalCount);
        result.put("typeCount", typeDistribution.size()); // 原始类型数量
        result.put("displayCount", finalDistribution.size()); // 显示的类型数量

        log.info("事件类型分布统计数据：共{}种类型，显示{}种，{}个事件", typeDistribution.size(), finalDistribution.size(), totalCount);
        return result;
    }
}