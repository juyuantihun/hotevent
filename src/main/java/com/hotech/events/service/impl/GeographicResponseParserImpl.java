package com.hotech.events.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.dto.EnhancedEventData;

import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.dto.GeographicCoordinate.LocationType;
import com.hotech.events.service.GeographicInfoService;
import com.hotech.events.service.GeographicResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 地理信息响应解析器实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeographicResponseParserImpl implements GeographicResponseParser {

    private final ObjectMapper objectMapper;
    private final GeographicInfoService geographicInfoService;

    // 解析统计
    private final AtomicLong totalParsed = new AtomicLong(0);
    private final AtomicLong successfulParsed = new AtomicLong(0);
    private final AtomicLong failedParsed = new AtomicLong(0);
    private final AtomicLong geoInfoExtracted = new AtomicLong(0);

    // 地理信息提取的正则表达式
    private final Pattern coordinatePattern = Pattern.compile("(\\d+\\.\\d+)\\s*,\\s*(\\d+\\.\\d+)");
    private final Pattern locationPattern = Pattern.compile("在(.{2,20}?)(?:发生|举行|召开|进行)");

    @Override
    public List<EnhancedEventData> parseEventsWithGeographicInfo(String apiResponse) {
        List<EnhancedEventData> events = new ArrayList<>();

        if (apiResponse == null || apiResponse.trim().isEmpty()) {
            log.warn("API响应为空，无法解析事件");
            return events;
        }

        try {
            // 解析JSON响应
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(apiResponse, Map.class);

            if (responseMap.containsKey("events") && responseMap.get("events") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> eventsList = (List<Map<String, Object>>) responseMap.get("events");

                for (Map<String, Object> eventMap : eventsList) {
                    try {
                        totalParsed.incrementAndGet();
                        EnhancedEventData eventData = parseEventGeographicInfo(eventMap);
                        if (eventData != null) {
                            events.add(eventData);
                            successfulParsed.incrementAndGet();
                        } else {
                            failedParsed.incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.warn("解析单个事件时发生错误: {}", e.getMessage());
                        failedParsed.incrementAndGet();
                    }
                }
            }

            log.info("成功解析 {} 个事件，其中 {} 个包含地理信息",
                    events.size(), events.stream().mapToLong(e -> e.isGeoProcessingCompleted() ? 1 : 0).sum());

        } catch (JsonProcessingException e) {
            log.error("解析API响应JSON时发生错误", e);
        } catch (Exception e) {
            log.error("解析事件地理信息时发生未知错误", e);
        }

        return events;
    }

    @Override
    public EnhancedEventData parseEventGeographicInfo(Map<String, Object> eventMap) {
        if (eventMap == null || eventMap.isEmpty()) {
            return null;
        }

        try {
            // 创建增强事件数据对象
            EnhancedEventData eventData = new EnhancedEventData();

            // 解析基础事件信息
            parseBasicEventInfo(eventData, eventMap);

            // 解析地理坐标信息
            parseGeographicCoordinates(eventData, eventMap);

            // 如果没有从API响应中获取到地理信息，尝试从描述中提取
            if (!eventData.isGeoProcessingCompleted()) {
                extractGeographicInfoFromDescription(eventData);
            }

            return eventData;

        } catch (Exception e) {
            log.error("解析事件地理信息时发生错误", e);
            return null;
        }
    }

    @Override
    public GeographicCoordinate parseGeographicCoordinate(Map<String, Object> coordinateMap) {
        if (coordinateMap == null || coordinateMap.isEmpty()) {
            return null;
        }

        try {
            GeographicCoordinate.GeographicCoordinateBuilder builder = GeographicCoordinate.builder();

            // 解析纬度
            if (coordinateMap.containsKey("latitude")) {
                Object latObj = coordinateMap.get("latitude");
                if (latObj instanceof Number) {
                    builder.latitude(((Number) latObj).doubleValue());
                }
            }

            // 解析经度
            if (coordinateMap.containsKey("longitude")) {
                Object lonObj = coordinateMap.get("longitude");
                if (lonObj instanceof Number) {
                    builder.longitude(((Number) lonObj).doubleValue());
                }
            }

            // 解析地点名称
            if (coordinateMap.containsKey("locationName")) {
                builder.locationName((String) coordinateMap.get("locationName"));
            }

            // 解析地点类型
            if (coordinateMap.containsKey("locationType")) {
                String typeStr = (String) coordinateMap.get("locationType");
                try {
                    LocationType locationType = LocationType.valueOf(typeStr.toUpperCase());
                    builder.locationType(locationType);
                } catch (IllegalArgumentException e) {
                    builder.locationType(LocationType.UNKNOWN);
                }
            }

            // 解析国家代码
            if (coordinateMap.containsKey("countryCode")) {
                builder.countryCode((String) coordinateMap.get("countryCode"));
            }

            // 解析地区代码
            if (coordinateMap.containsKey("regionCode")) {
                builder.regionCode((String) coordinateMap.get("regionCode"));
            }

            // 解析城市代码
            if (coordinateMap.containsKey("cityCode")) {
                builder.cityCode((String) coordinateMap.get("cityCode"));
            }

            // 解析地址详情
            if (coordinateMap.containsKey("address")) {
                builder.address((String) coordinateMap.get("address"));
            }

            // 解析精度信息
            if (coordinateMap.containsKey("accuracy")) {
                Object accuracyObj = coordinateMap.get("accuracy");
                if (accuracyObj instanceof Number) {
                    builder.accuracy(((Number) accuracyObj).doubleValue());
                }
            }

            // 解析坐标系统
            if (coordinateMap.containsKey("coordinateSystem")) {
                builder.coordinateSystem((String) coordinateMap.get("coordinateSystem"));
            } else {
                builder.coordinateSystem("WGS84"); // 默认坐标系统
            }

            GeographicCoordinate coordinate = builder.build();
            return validateAndNormalizeCoordinate(coordinate);

        } catch (Exception e) {
            log.warn("解析地理坐标时发生错误: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public GeographicCoordinate validateAndNormalizeCoordinate(GeographicCoordinate coordinate) {
        if (coordinate == null) {
            return null;
        }

        // 验证坐标有效性
        if (!geographicInfoService.validateCoordinate(coordinate)) {
            log.debug("坐标验证失败: {}", coordinate);
            return null;
        }

        // 标准化坐标精度
        if (coordinate.getLatitude() != null && coordinate.getLongitude() != null) {
            double normalizedLat = Math.round(coordinate.getLatitude() * 1000000.0) / 1000000.0;
            double normalizedLon = Math.round(coordinate.getLongitude() * 1000000.0) / 1000000.0;

            coordinate.setLatitude(normalizedLat);
            coordinate.setLongitude(normalizedLon);
        }

        // 设置精度等级
        if (coordinate.getAccuracyLevel() == null) {
            coordinate.setAccuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM);
        }

        return coordinate;
    }

    @Override
    public boolean extractGeographicInfoFromDescription(EnhancedEventData eventData) {
        if (eventData == null) {
            return false;
        }

        boolean extracted = false;

        try {
            // 从描述中提取地理信息
            String description = eventData.getDescription();
            String location = eventData.getLocation();

            // 尝试从location字段解析坐标
            if (location != null && !location.trim().isEmpty()) {
                Optional<GeographicCoordinate> locationCoord = geographicInfoService.smartParseLocation(location);
                if (locationCoord.isPresent()) {
                    eventData.setEventCoordinate(locationCoord.get());
                    eventData.setLatitude(locationCoord.get().getLatitude());
                    eventData.setLongitude(locationCoord.get().getLongitude());
                    extracted = true;
                }
            }

            // 尝试从主体解析坐标
            String subject = eventData.getSubject();
            if (subject != null && !subject.trim().isEmpty()) {
                Optional<GeographicCoordinate> subjectCoord = geographicInfoService.smartParseLocation(subject);
                if (subjectCoord.isPresent()) {
                    eventData.setSubjectCoordinate(subjectCoord.get());
                    extracted = true;
                }
            }

            // 尝试从客体解析坐标
            String object = eventData.getObject();
            if (object != null && !object.trim().isEmpty()) {
                Optional<GeographicCoordinate> objectCoord = geographicInfoService.smartParseLocation(object);
                if (objectCoord.isPresent()) {
                    eventData.setObjectCoordinate(objectCoord.get());
                    extracted = true;
                }
            }

            // 从描述中提取地点信息
            if (description != null && !description.trim().isEmpty()) {
                extracted |= extractLocationFromDescription(eventData, description);
            }

            // 更新地理处理状态
            if (extracted) {
                geoInfoExtracted.incrementAndGet();
                if (eventData.hasValidEventCoordinate() ||
                        eventData.hasValidSubjectCoordinate() ||
                        eventData.hasValidObjectCoordinate()) {
                    eventData.markGeoProcessingCompleted();
                } else {
                    eventData.markGeoProcessingPartial("部分地理信息提取成功");
                }
            } else {
                eventData.markGeoProcessingFailed("无法提取地理信息");
            }

        } catch (Exception e) {
            log.warn("从描述中提取地理信息时发生错误: {}", e.getMessage());
            eventData.markGeoProcessingFailed("地理信息提取异常: " + e.getMessage());
        }

        return extracted;
    }

    @Override
    public Map<String, Object> getParsingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalParsed", totalParsed.get());
        stats.put("successfulParsed", successfulParsed.get());
        stats.put("failedParsed", failedParsed.get());
        stats.put("geoInfoExtracted", geoInfoExtracted.get());

        long total = totalParsed.get();
        if (total > 0) {
            stats.put("successRate", (double) successfulParsed.get() / total * 100);
            stats.put("geoExtractionRate", (double) geoInfoExtracted.get() / total * 100);
        } else {
            stats.put("successRate", 0.0);
            stats.put("geoExtractionRate", 0.0);
        }

        return stats;
    }

    // 私有辅助方法

    /**
     * 解析基础事件信息
     */
    private void parseBasicEventInfo(EnhancedEventData eventData, Map<String, Object> eventMap) {
        // 设置事件ID
        if (eventMap.containsKey("id")) {
            eventData.setId((String) eventMap.get("id"));
        } else {
            eventData.setId(UUID.randomUUID().toString());
        }

        // 设置标题
        if (eventMap.containsKey("title")) {
            eventData.setTitle((String) eventMap.get("title"));
        }

        // 设置描述
        if (eventMap.containsKey("description")) {
            eventData.setDescription((String) eventMap.get("description"));
        }

        // 设置事件时间
        if (eventMap.containsKey("eventTime")) {
            String timeStr = (String) eventMap.get("eventTime");
            try {
                LocalDateTime eventTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                eventData.setEventTime(eventTime);
            } catch (DateTimeParseException e) {
                log.warn("解析事件时间失败: {}", timeStr);
                eventData.setEventTime(LocalDateTime.now());
            }
        }

        // 设置地点
        if (eventMap.containsKey("location")) {
            eventData.setLocation((String) eventMap.get("location"));
        }

        // 设置主体
        if (eventMap.containsKey("subject")) {
            eventData.setSubject((String) eventMap.get("subject"));
        }

        // 设置客体
        if (eventMap.containsKey("object")) {
            eventData.setObject((String) eventMap.get("object"));
        }

        // 设置事件类型
        if (eventMap.containsKey("eventType")) {
            eventData.setEventType((String) eventMap.get("eventType"));
        }

        // 设置关键词
        if (eventMap.containsKey("keywords") && eventMap.get("keywords") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) eventMap.get("keywords");
            eventData.setKeywords(keywords);
        }

        // 设置来源
        if (eventMap.containsKey("sources") && eventMap.get("sources") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> sources = (List<String>) eventMap.get("sources");
            eventData.setSources(sources);
        }

        // 设置可信度评分
        if (eventMap.containsKey("credibilityScore")) {
            Object scoreObj = eventMap.get("credibilityScore");
            if (scoreObj instanceof Number) {
                eventData.setCredibilityScore(((Number) scoreObj).doubleValue());
            }
        }

        // 设置创建时间
        eventData.setCreatedAt(LocalDateTime.now());
        eventData.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 解析地理坐标信息
     */
    private void parseGeographicCoordinates(EnhancedEventData eventData, Map<String, Object> eventMap) {
        boolean hasAnyCoordinate = false;

        // 解析事件发生地坐标
        if (eventMap.containsKey("latitude") && eventMap.containsKey("longitude")) {
            try {
                Object latObj = eventMap.get("latitude");
                Object lonObj = eventMap.get("longitude");

                if (latObj instanceof Number && lonObj instanceof Number) {
                    double latitude = ((Number) latObj).doubleValue();
                    double longitude = ((Number) lonObj).doubleValue();

                    GeographicCoordinate eventCoord = GeographicCoordinate.builder()
                            .latitude(latitude)
                            .longitude(longitude)
                            .locationName(eventData.getLocation())
                            .locationType(LocationType.UNKNOWN)
                            .accuracyLevel(GeographicCoordinate.AccuracyLevel.HIGH)
                            .build();

                    eventCoord = validateAndNormalizeCoordinate(eventCoord);
                    if (eventCoord != null) {
                        eventData.setEventCoordinate(eventCoord);
                        eventData.setLatitude(eventCoord.getLatitude());
                        eventData.setLongitude(eventCoord.getLongitude());
                        hasAnyCoordinate = true;
                    }
                }
            } catch (Exception e) {
                log.warn("解析事件坐标时发生错误: {}", e.getMessage());
            }
        }

        // 解析主体坐标
        if (eventMap.containsKey("subjectCoordinate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subjectCoordMap = (Map<String, Object>) eventMap.get("subjectCoordinate");
            GeographicCoordinate subjectCoord = parseGeographicCoordinate(subjectCoordMap);
            if (subjectCoord != null) {
                eventData.setSubjectCoordinate(subjectCoord);
                hasAnyCoordinate = true;
            }
        }

        // 解析客体坐标
        if (eventMap.containsKey("objectCoordinate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> objectCoordMap = (Map<String, Object>) eventMap.get("objectCoordinate");
            GeographicCoordinate objectCoord = parseGeographicCoordinate(objectCoordMap);
            if (objectCoord != null) {
                eventData.setObjectCoordinate(objectCoord);
                hasAnyCoordinate = true;
            }
        }

        // 设置地理处理状态
        if (hasAnyCoordinate) {
            eventData.markGeoProcessingCompleted();
        } else {
            eventData.setGeoProcessingStatus(EnhancedEventData.GeoProcessingStatus.NOT_PROCESSED);
        }
    }

    /**
     * 从描述中提取地点信息
     */
    private boolean extractLocationFromDescription(EnhancedEventData eventData, String description) {
        boolean extracted = false;

        try {
            // 使用正则表达式提取地点信息
            Matcher locationMatcher = locationPattern.matcher(description);
            if (locationMatcher.find()) {
                String extractedLocation = locationMatcher.group(1);
                if (extractedLocation != null && !extractedLocation.trim().isEmpty()) {
                    Optional<GeographicCoordinate> coordinate = geographicInfoService
                            .smartParseLocation(extractedLocation);
                    if (coordinate.isPresent()) {
                        if (eventData.getEventCoordinate() == null) {
                            eventData.setEventCoordinate(coordinate.get());
                            eventData.setLatitude(coordinate.get().getLatitude());
                            eventData.setLongitude(coordinate.get().getLongitude());
                        }
                        extracted = true;
                    }
                }
            }

            // 尝试提取坐标信息
            Matcher coordinateMatcher = coordinatePattern.matcher(description);
            if (coordinateMatcher.find()) {
                try {
                    double lat = Double.parseDouble(coordinateMatcher.group(1));
                    double lon = Double.parseDouble(coordinateMatcher.group(2));

                    GeographicCoordinate coordinate = GeographicCoordinate.builder()
                            .latitude(lat)
                            .longitude(lon)
                            .locationName("从描述提取")
                            .locationType(LocationType.UNKNOWN)
                            .accuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM)
                            .build();

                    coordinate = validateAndNormalizeCoordinate(coordinate);
                    if (coordinate != null && eventData.getEventCoordinate() == null) {
                        eventData.setEventCoordinate(coordinate);
                        eventData.setLatitude(coordinate.getLatitude());
                        eventData.setLongitude(coordinate.getLongitude());
                        extracted = true;
                    }
                } catch (NumberFormatException e) {
                    log.debug("解析坐标数值时发生错误: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.warn("从描述中提取地点信息时发生错误: {}", e.getMessage());
        }

        return extracted;
    }
}