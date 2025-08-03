package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.entity.Event;
import com.hotech.events.mapper.EventMapper;
import com.hotech.events.mapper.GeographicCoordinateMapper;
import com.hotech.events.service.EventGeographicIntegrationService;
import com.hotech.events.service.GeographicInfoService;
import com.hotech.events.service.GeographicResponseParser;
import com.hotech.events.service.GeographicValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 事件地理信息集成服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventGeographicIntegrationServiceImpl implements EventGeographicIntegrationService {

    private final GeographicInfoService geographicInfoService;
    private final GeographicValidationService geographicValidationService;
    private final GeographicResponseParser geographicResponseParser;
    private final GeographicCoordinateMapper geographicCoordinateMapper;
    private final EventMapper eventMapper;

    // 统计信息
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong successfullyProcessed = new AtomicLong(0);
    private final AtomicLong partiallyProcessed = new AtomicLong(0);
    private final AtomicLong failed = new AtomicLong(0);
    private final AtomicLong coordinatesCached = new AtomicLong(0);

    // 地理信息解析模式
    private final Pattern coordinatePattern = Pattern.compile(
            "(?:纬度|latitude)[：:]*\\s*([+-]?\\d+\\.?\\d*).*?(?:经度|longitude)[：:]*\\s*([+-]?\\d+\\.?\\d*)",
            Pattern.CASE_INSENSITIVE);
    private final Pattern locationPattern = Pattern.compile(
            "(?:地点|location|地区|region)[：:]*\\s*([^，,。.\\n]+)",
            Pattern.CASE_INSENSITIVE);

    @Override
    public List<EventData> integrateGeographicInfoInParsing(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }

        log.info("开始为 {} 个事件集成地理信息处理", events.size());

        List<EventData> processedEvents = new ArrayList<>();

        for (EventData event : events) {
            try {
                EventData processedEvent = integrateGeographicInfoForSingleEvent(event);
                processedEvents.add(processedEvent);
                totalProcessed.incrementAndGet();
            } catch (Exception e) {
                log.warn("为事件 {} 集成地理信息时发生错误: {}", event.getId(), e.getMessage());
                processedEvents.add(event); // 添加原始事件
                failed.incrementAndGet();
            }
        }

        log.info("地理信息集成完成: 总数={}, 成功={}, 部分成功={}, 失败={}",
                totalProcessed.get(), successfullyProcessed.get(),
                partiallyProcessed.get(), failed.get());

        return processedEvents;
    }

    @Override
    public EventData integrateGeographicInfoForSingleEvent(EventData event) {
        if (event == null) {
            return null;
        }

        log.debug("开始为事件 {} 集成地理信息", event.getId());

        try {
            // 阶段1：从现有字段中提取地理信息
            extractGeographicInfoFromExistingFields(event);

            // 阶段2：智能解析地理位置
            enhanceGeographicInfoWithSmartParsing(event);

            // 阶段3：验证和标准化地理信息
            GeographicValidationResult validationResult = validateAndStandardizeEventGeographicInfo(event);

            // 阶段4：应用验证结果
            applyValidationResults(event, validationResult);

            // 统计处理结果
            if (event.hasValidGeographicInfo()) {
                if (validationResult.getWarnings() != null && !validationResult.getWarnings().isEmpty()) {
                    partiallyProcessed.incrementAndGet();
                } else {
                    successfullyProcessed.incrementAndGet();
                }
            } else {
                failed.incrementAndGet();
            }

            log.debug("事件 {} 地理信息集成完成", event.getId());

        } catch (Exception e) {
            log.warn("为事件 {} 集成地理信息时发生错误: {}", event.getId(), e.getMessage());
            failed.incrementAndGet();
        }

        return event;
    }

    @Override
    public boolean parseGeographicInfoFromApiResponse(String apiResponse, EventData event) {
        if (apiResponse == null || apiResponse.trim().isEmpty() || event == null) {
            return false;
        }

        try {
            log.debug("开始从API响应中解析地理信息");

            // 使用地理响应解析器解析事件列表
            List<com.hotech.events.dto.EnhancedEventData> parsedEvents = geographicResponseParser
                    .parseEventsWithGeographicInfo(apiResponse);

            boolean hasAnyCoordinate = false;

            // 如果解析到事件，尝试从第一个事件中提取地理信息
            if (!parsedEvents.isEmpty()) {
                com.hotech.events.dto.EnhancedEventData firstEvent = parsedEvents.get(0);

                // 提取事件坐标
                if (firstEvent.getEventCoordinate() != null) {
                    event.setEventCoordinate(firstEvent.getEventCoordinate());
                    hasAnyCoordinate = true;
                }

                // 提取主体坐标
                if (firstEvent.getSubjectCoordinate() != null) {
                    event.setSubjectCoordinate(firstEvent.getSubjectCoordinate());
                    hasAnyCoordinate = true;
                }

                // 提取客体坐标
                if (firstEvent.getObjectCoordinate() != null) {
                    event.setObjectCoordinate(firstEvent.getObjectCoordinate());
                    hasAnyCoordinate = true;
                }

                // 如果没有专门的坐标，尝试从基础坐标字段获取
                if (!hasAnyCoordinate && firstEvent.getLatitude() != null && firstEvent.getLongitude() != null) {
                    GeographicCoordinate basicCoord = new GeographicCoordinate();
                    basicCoord.setLatitude(firstEvent.getLatitude());
                    basicCoord.setLongitude(firstEvent.getLongitude());
                    basicCoord.setLocationName(firstEvent.getLocation());
                    basicCoord.setLocationType(GeographicCoordinate.LocationType.UNKNOWN);
                    basicCoord.setAccuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM);

                    event.setEventCoordinate(basicCoord);
                    hasAnyCoordinate = true;
                }
            }

            // 如果没有专门的坐标，尝试使用正则表达式解析
            if (!hasAnyCoordinate) {
                hasAnyCoordinate = parseGeographicInfoWithRegex(apiResponse, event);
            }

            // 更新基础坐标字段以保持兼容性
            if (hasAnyCoordinate && event.getEventCoordinate() != null) {
                event.setPrimaryCoordinate(event.getEventCoordinate());
            }

            log.debug("API响应地理信息解析完成: hasCoordinate={}", hasAnyCoordinate);
            return hasAnyCoordinate;

        } catch (Exception e) {
            log.warn("从API响应解析地理信息时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public GeographicValidationResult validateAndStandardizeEventGeographicInfo(EventData event) {
        GeographicValidationResult result = new GeographicValidationResult();
        result.setWarnings(new ArrayList<>());

        if (event == null) {
            result.setValid(false);
            result.setErrorMessage("事件对象为空");
            return result;
        }

        boolean hasValidCoordinate = false;
        List<String> warnings = new ArrayList<>();

        try {
            // 验证事件坐标
            if (event.getEventCoordinate() != null) {
                com.hotech.events.service.GeographicValidationService.GeographicValidationResult coordResult = geographicValidationService
                        .validateCoordinate(event.getEventCoordinate());

                if (coordResult.isValid()) {
                    result.setStandardizedEventCoordinate(coordResult.getStandardizedCoordinate());
                    hasValidCoordinate = true;
                } else {
                    warnings.add("事件坐标无效: " + coordResult.getErrorMessage());
                }
            }

            // 验证主体坐标
            if (event.getSubjectCoordinate() != null) {
                com.hotech.events.service.GeographicValidationService.GeographicValidationResult subjectResult = geographicValidationService
                        .validateCoordinate(event.getSubjectCoordinate());

                if (subjectResult.isValid()) {
                    result.setStandardizedSubjectCoordinate(subjectResult.getStandardizedCoordinate());
                    hasValidCoordinate = true;
                } else {
                    warnings.add("主体坐标无效: " + subjectResult.getErrorMessage());
                }
            }

            // 验证客体坐标
            if (event.getObjectCoordinate() != null) {
                com.hotech.events.service.GeographicValidationService.GeographicValidationResult objectResult = geographicValidationService
                        .validateCoordinate(event.getObjectCoordinate());

                if (objectResult.isValid()) {
                    result.setStandardizedObjectCoordinate(objectResult.getStandardizedCoordinate());
                    hasValidCoordinate = true;
                } else {
                    warnings.add("客体坐标无效: " + objectResult.getErrorMessage());
                }
            }

            // 验证基础坐标
            if (!hasValidCoordinate && event.getLatitude() != null && event.getLongitude() != null) {
                GeographicCoordinate basicCoord = GeographicCoordinate.builder()
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .locationName(event.getLocation())
                        .locationType(GeographicCoordinate.LocationType.UNKNOWN)
                        .accuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM)
                        .build();

                com.hotech.events.service.GeographicValidationService.GeographicValidationResult basicResult = geographicValidationService
                        .validateCoordinate(basicCoord);

                if (basicResult.isValid()) {
                    result.setStandardizedEventCoordinate(basicResult.getStandardizedCoordinate());
                    hasValidCoordinate = true;
                } else {
                    warnings.add("基础坐标无效: " + basicResult.getErrorMessage());
                }
            }

            result.setValid(hasValidCoordinate);
            result.setWarnings(warnings);

            if (!hasValidCoordinate) {
                result.setErrorMessage("事件缺少有效的地理坐标信息");
            }

        } catch (Exception e) {
            log.warn("验证事件地理信息时发生错误: {}", e.getMessage());
            result.setValid(false);
            result.setErrorMessage("验证过程中发生异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public GeographicStorageResult prepareGeographicDataForStorage(EventData event) {
        GeographicStorageResult result = new GeographicStorageResult();

        if (event == null) {
            result.setSuccess(false);
            result.setErrorMessage("事件对象为空");
            return result;
        }

        try {
            // 存储事件坐标
            if (event.getEventCoordinate() != null && event.getEventCoordinate().isValid()) {
                Long eventCoordId = storeGeographicCoordinate(event.getEventCoordinate(), "EVENT");
                result.setEventCoordinateId(eventCoordId);
            }

            // 存储主体坐标
            if (event.getSubjectCoordinate() != null && event.getSubjectCoordinate().isValid()) {
                Long subjectCoordId = storeGeographicCoordinate(event.getSubjectCoordinate(), "SUBJECT");
                result.setSubjectCoordinateId(subjectCoordId);
            }

            // 存储客体坐标
            if (event.getObjectCoordinate() != null && event.getObjectCoordinate().isValid()) {
                Long objectCoordId = storeGeographicCoordinate(event.getObjectCoordinate(), "OBJECT");
                result.setObjectCoordinateId(objectCoordId);
            }

            result.setSuccess(true);
            coordinatesCached.incrementAndGet();

        } catch (Exception e) {
            log.error("准备地理数据存储时发生错误: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("存储准备失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public EventData loadGeographicInfoFromStoredEvent(Event event) {
        if (event == null) {
            return null;
        }

        EventData eventData = convertEventEntityToDTO(event);

        try {
            // 加载事件坐标
            if (event.getEventCoordinateId() != null) {
                com.hotech.events.entity.GeographicCoordinate eventCoord = geographicCoordinateMapper
                        .selectById(event.getEventCoordinateId());
                if (eventCoord != null) {
                    eventData.setEventCoordinate(eventCoord.toDTO());
                }
            }

            // 加载主体坐标
            if (event.getSubjectCoordinateId() != null) {
                com.hotech.events.entity.GeographicCoordinate subjectCoord = geographicCoordinateMapper
                        .selectById(event.getSubjectCoordinateId());
                if (subjectCoord != null) {
                    eventData.setSubjectCoordinate(subjectCoord.toDTO());
                }
            }

            // 加载客体坐标
            if (event.getObjectCoordinateId() != null) {
                com.hotech.events.entity.GeographicCoordinate objectCoord = geographicCoordinateMapper
                        .selectById(event.getObjectCoordinateId());
                if (objectCoord != null) {
                    eventData.setObjectCoordinate(objectCoord.toDTO());
                }
            }

            // 如果没有专门的坐标但有基础坐标，设置基础坐标
            if (!eventData.hasValidGeographicInfo() && event.getBasicCoordinate() != null) {
                eventData.setPrimaryCoordinate(event.getBasicCoordinate());
            }

        } catch (Exception e) {
            log.warn("从存储事件加载地理信息时发生错误: {}", e.getMessage());
        }

        return eventData;
    }

    @Override
    public List<EventData> loadGeographicInfoFromStoredEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return new ArrayList<>();
        }

        List<EventData> eventDataList = new ArrayList<>();

        for (Event event : events) {
            try {
                EventData eventData = loadGeographicInfoFromStoredEvent(event);
                if (eventData != null) {
                    eventDataList.add(eventData);
                }
            } catch (Exception e) {
                log.warn("加载事件 {} 的地理信息时发生错误: {}", event.getId(), e.getMessage());
            }
        }

        return eventDataList;
    }

    @Override
    @Transactional
    public boolean updateEventGeographicInfo(Long eventId, GeographicCoordinate coordinate) {
        if (eventId == null || coordinate == null || !coordinate.isValid()) {
            return false;
        }

        try {
            // 存储新的地理坐标
            Long coordinateId = storeGeographicCoordinate(coordinate, "EVENT");

            // 更新事件的地理坐标ID
            Event event = eventMapper.selectById(eventId);
            if (event != null) {
                event.setEventCoordinateId(coordinateId);
                event.setGeographicStatus(com.hotech.events.constant.GeographicStatus.PROCESSED); // 1-已处理
                event.setGeographicUpdatedAt(LocalDateTime.now());

                // 同时更新基础坐标字段以保持兼容性
                event.setBasicCoordinate(coordinate);

                eventMapper.updateById(event);
                return true;
            }

        } catch (Exception e) {
            log.error("更新事件地理信息时发生错误: {}", e.getMessage(), e);
        }

        return false;
    }

    @Override
    public GeographicProcessingStats getGeographicProcessingStats() {
        GeographicProcessingStats stats = new GeographicProcessingStats();

        long total = totalProcessed.get();
        stats.setTotalProcessed(total);
        stats.setSuccessfullyProcessed(successfullyProcessed.get());
        stats.setPartiallyProcessed(partiallyProcessed.get());
        stats.setFailed(failed.get());
        stats.setCoordinatesCached(coordinatesCached.get());

        if (total > 0) {
            stats.setSuccessRate((double) successfullyProcessed.get() / total * 100);
        } else {
            stats.setSuccessRate(0.0);
        }

        return stats;
    }

    // 私有辅助方法

    /**
     * 从现有字段中提取地理信息
     */
    private void extractGeographicInfoFromExistingFields(EventData event) {
        // 如果已有基础坐标但没有专门的事件坐标，创建事件坐标
        if (event.getEventCoordinate() == null &&
                event.getLatitude() != null && event.getLongitude() != null) {

            GeographicCoordinate eventCoord = GeographicCoordinate.builder()
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .locationName(event.getLocation())
                    .locationType(GeographicCoordinate.LocationType.UNKNOWN)
                    .accuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM)
                    .build();

            event.setEventCoordinate(eventCoord);
        }
    }

    /**
     * 使用智能解析增强地理信息
     */
    private void enhanceGeographicInfoWithSmartParsing(EventData event) {
        // 如果事件地点不为空但没有坐标，尝试解析
        if (event.getLocation() != null && !event.getLocation().trim().isEmpty() &&
                event.getEventCoordinate() == null) {

            Optional<GeographicCoordinate> parsedCoord = geographicInfoService.smartParseLocation(event.getLocation());

            parsedCoord.ifPresent(event::setEventCoordinate);
        }

        // 如果主体不为空但没有坐标，尝试解析
        if (event.getSubject() != null && !event.getSubject().trim().isEmpty() &&
                event.getSubjectCoordinate() == null) {

            Optional<GeographicCoordinate> parsedCoord = geographicInfoService.smartParseLocation(event.getSubject());

            parsedCoord.ifPresent(event::setSubjectCoordinate);
        }

        // 如果客体不为空但没有坐标，尝试解析
        if (event.getObject() != null && !event.getObject().trim().isEmpty() &&
                event.getObjectCoordinate() == null) {

            Optional<GeographicCoordinate> parsedCoord = geographicInfoService.smartParseLocation(event.getObject());

            parsedCoord.ifPresent(event::setObjectCoordinate);
        }
    }

    /**
     * 应用验证结果
     */
    private void applyValidationResults(EventData event, GeographicValidationResult validationResult) {
        if (validationResult.getStandardizedEventCoordinate() != null) {
            event.setEventCoordinate(validationResult.getStandardizedEventCoordinate());
            event.setPrimaryCoordinate(validationResult.getStandardizedEventCoordinate());
        }

        if (validationResult.getStandardizedSubjectCoordinate() != null) {
            event.setSubjectCoordinate(validationResult.getStandardizedSubjectCoordinate());
        }

        if (validationResult.getStandardizedObjectCoordinate() != null) {
            event.setObjectCoordinate(validationResult.getStandardizedObjectCoordinate());
        }
    }

    /**
     * 使用正则表达式解析地理信息
     */
    private boolean parseGeographicInfoWithRegex(String apiResponse, EventData event) {
        boolean hasCoordinate = false;

        try {
            // 解析坐标
            Matcher coordMatcher = coordinatePattern.matcher(apiResponse);
            if (coordMatcher.find()) {
                double latitude = Double.parseDouble(coordMatcher.group(1));
                double longitude = Double.parseDouble(coordMatcher.group(2));

                GeographicCoordinate coordinate = GeographicCoordinate.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .locationType(GeographicCoordinate.LocationType.UNKNOWN)
                        .accuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM)
                        .build();

                if (coordinate.isValid()) {
                    event.setEventCoordinate(coordinate);
                    hasCoordinate = true;
                }
            }

            // 解析地点名称
            Matcher locationMatcher = locationPattern.matcher(apiResponse);
            if (locationMatcher.find()) {
                String locationName = locationMatcher.group(1).trim();
                if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
                    event.setLocation(locationName);
                }

                // 如果有地点名称但没有坐标，尝试解析坐标
                if (!hasCoordinate) {
                    Optional<GeographicCoordinate> parsedCoord = geographicInfoService.smartParseLocation(locationName);

                    if (parsedCoord.isPresent()) {
                        event.setEventCoordinate(parsedCoord.get());
                        hasCoordinate = true;
                    }
                }
            }

        } catch (Exception e) {
            log.warn("使用正则表达式解析地理信息时发生错误: {}", e.getMessage());
        }

        return hasCoordinate;
    }

    /**
     * 存储地理坐标
     */
    private Long storeGeographicCoordinate(GeographicCoordinate coordinate, String dataSource) {
        if (coordinate == null || !coordinate.isValid()) {
            return null;
        }

        try {
            // 检查是否已存在相同的坐标
            com.hotech.events.entity.GeographicCoordinate existing = geographicCoordinateMapper
                    .findByLocationNameAndType(
                            coordinate.getLocationName(),
                            coordinate.getLocationType() != null ? coordinate.getLocationType().name() : "UNKNOWN");

            if (existing != null) {
                return existing.getId();
            }

            // 创建新的坐标记录
            com.hotech.events.entity.GeographicCoordinate entity = com.hotech.events.entity.GeographicCoordinate
                    .fromDTO(coordinate);
            entity.setDataSource(dataSource);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            geographicCoordinateMapper.insert(entity);
            return entity.getId();

        } catch (Exception e) {
            log.error("存储地理坐标时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将事件实体转换为DTO
     */
    private EventData convertEventEntityToDTO(Event event) {
        EventData eventData = new EventData();

        eventData.setId(event.getId() != null ? event.getId().toString() : null);
        eventData.setTitle(event.getEventTitle());
        eventData.setDescription(event.getEventDescription());
        eventData.setEventTime(event.getEventTime());
        eventData.setLocation(event.getEventLocation());
        eventData.setSubject(event.getSubject());
        eventData.setObject(event.getObject());
        eventData.setEventType(event.getEventType());
        eventData.setCredibilityScore(event.getCredibilityScore());
        eventData.setValidationStatus(event.getValidationStatus());
        eventData.setFetchMethod(event.getFetchMethod());
        eventData.setCreatedAt(event.getCreatedAt());
        eventData.setUpdatedAt(event.getUpdatedAt());

        // 设置基础坐标
        if (event.getLatitude() != null && event.getLongitude() != null) {
            eventData.setLatitude(event.getLatitude().doubleValue());
            eventData.setLongitude(event.getLongitude().doubleValue());
        }

        return eventData;
    }
}