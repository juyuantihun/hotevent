package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EnhancedEventData;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.dto.GeographicCoordinate.LocationType;
import com.hotech.events.mapper.GeographicCoordinateMapper;
import com.hotech.events.service.GeographicInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 地理信息处理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeographicInfoServiceImpl implements GeographicInfoService {

    private final GeographicCoordinateMapper geographicCoordinateMapper;

    // 坐标缓存
    private final Map<String, GeographicCoordinate> coordinateCache = new ConcurrentHashMap<>();

    // 默认国家首都坐标数据
    private final Map<String, GeographicCoordinate> capitalCoordinates = new ConcurrentHashMap<>();

    // 默认地区首府坐标数据
    private final Map<String, GeographicCoordinate> regionCapitals = new ConcurrentHashMap<>();

    // 地点名称模式匹配
    private final Pattern countryPattern = Pattern.compile(".*国$|.*共和国$|.*王国$|.*联邦$");
    private final Pattern regionPattern = Pattern.compile(".*省$|.*市$|.*区$|.*州$|.*县$");

    // 缓存统计
    private long cacheHits = 0;
    private long cacheMisses = 0;

    @PostConstruct
    public void init() {
        initializeDefaultGeographicData();
        log.info("地理信息服务初始化完成，加载了 {} 个国家首都坐标，{} 个地区首府坐标",
                capitalCoordinates.size(), regionCapitals.size());
    }

    @Override
    public Optional<GeographicCoordinate> parseLocationCoordinate(String locationName, LocationType locationType) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedName = normalizeLocationName(locationName);

        // 先从缓存查找
        Optional<GeographicCoordinate> cached = getCachedCoordinate(normalizedName);
        if (cached.isPresent()) {
            cacheHits++;
            return cached;
        }

        cacheMisses++;

        // 根据类型查找坐标
        Optional<GeographicCoordinate> coordinate = Optional.empty();

        switch (locationType) {
            case COUNTRY:
                coordinate = getCapitalCoordinate(normalizedName);
                break;
            case REGION:
                coordinate = findRegionCoordinate(normalizedName);
                break;
            case CITY:
                coordinate = findCityCoordinate(normalizedName);
                break;
            default:
                coordinate = smartParseLocation(normalizedName);
                break;
        }

        // 缓存结果
        coordinate.ifPresent(coord -> cacheCoordinate(normalizedName, coord));

        return coordinate;
    }

    @Override
    public List<EventData> enhanceEventsWithGeographicInfo(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }

        log.info("开始为 {} 个事件增强地理信息", events.size());

        for (EventData event : events) {
            try {
                enhanceSingleEventGeographicInfo(event);
            } catch (Exception e) {
                log.warn("为事件 {} 增强地理信息时发生错误: {}", event.getId(), e.getMessage());
            }
        }

        log.info("地理信息增强完成");
        return events;
    }

    @Override
    public Optional<GeographicCoordinate> getDefaultCoordinate(String locationName, LocationType locationType) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedName = normalizeLocationName(locationName);

        switch (locationType) {
            case COUNTRY:
                return getCapitalCoordinate(normalizedName);
            case REGION:
                return findRegionCoordinate(normalizedName);
            default:
                return Optional.empty();
        }
    }

    @Override
    public Optional<GeographicCoordinate> smartParseLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedName = normalizeLocationName(locationName);

        // 先检查缓存
        Optional<GeographicCoordinate> cached = getCachedCoordinate(normalizedName);
        if (cached.isPresent()) {
            return cached;
        }

        // 智能识别地点类型
        LocationType detectedType = detectLocationType(normalizedName);

        return parseLocationCoordinate(normalizedName, detectedType);
    }

    @Override
    public void cacheCoordinate(String locationName, GeographicCoordinate coordinate) {
        if (locationName != null && coordinate != null && coordinate.isValid()) {
            coordinateCache.put(normalizeLocationName(locationName), coordinate);

            // 同时保存到数据库（如果不存在的话）
            try {
                com.hotech.events.entity.GeographicCoordinate existing = geographicCoordinateMapper
                        .findByLocationNameAndType(locationName,
                                coordinate.getLocationType() != null ? coordinate.getLocationType().name() : "UNKNOWN");

                if (existing == null) {
                    com.hotech.events.entity.GeographicCoordinate entity = com.hotech.events.entity.GeographicCoordinate
                            .fromDTO(coordinate);
                    entity.setDataSource("CACHE");
                    geographicCoordinateMapper.insert(entity);
                    log.debug("保存新的地理坐标到数据库: {}", locationName);
                }
            } catch (Exception e) {
                log.warn("保存地理坐标到数据库时发生错误: {}", e.getMessage());
            }
        }
    }

    @Override
    public Optional<GeographicCoordinate> getCachedCoordinate(String locationName) {
        if (locationName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(coordinateCache.get(normalizeLocationName(locationName)));
    }

    @Override
    public void clearCoordinateCache() {
        coordinateCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
        log.info("地理坐标缓存已清除");
    }

    @Override
    public Optional<GeographicCoordinate> getCapitalCoordinate(String countryName) {
        if (countryName == null) {
            return Optional.empty();
        }

        String normalizedName = normalizeLocationName(countryName);

        // 先从内存缓存查找
        GeographicCoordinate coordinate = capitalCoordinates.get(normalizedName);

        if (coordinate == null) {
            // 从数据库查找
            try {
                com.hotech.events.entity.GeographicCoordinate dbCoordinate = geographicCoordinateMapper
                        .findByLocationNameAndType(countryName, "COUNTRY");
                if (dbCoordinate != null) {
                    coordinate = dbCoordinate.toDTO();
                    // 缓存到内存
                    capitalCoordinates.put(normalizedName, coordinate);
                }
            } catch (Exception e) {
                log.warn("从数据库查找国家坐标时发生错误: {}", e.getMessage());
            }
        }

        if (coordinate == null) {
            // 尝试模糊匹配
            coordinate = findBestMatch(normalizedName, capitalCoordinates);
        }

        return Optional.ofNullable(coordinate);
    }

    @Override
    public Optional<GeographicCoordinate> getRegionCapitalCoordinate(String regionName, String countryName) {
        if (regionName == null) {
            return Optional.empty();
        }

        String normalizedRegionName = normalizeLocationName(regionName);
        String key = normalizedRegionName;
        if (countryName != null) {
            key = normalizeLocationName(countryName) + "_" + key;
        }

        // 先从内存缓存查找
        GeographicCoordinate coordinate = regionCapitals.get(key);

        if (coordinate == null) {
            // 尝试只用地区名称查找
            coordinate = regionCapitals.get(normalizedRegionName);
        }

        if (coordinate == null) {
            // 从数据库查找
            try {
                com.hotech.events.entity.GeographicCoordinate dbCoordinate = geographicCoordinateMapper
                        .findByLocationNameAndType(regionName, "REGION");
                if (dbCoordinate != null) {
                    coordinate = dbCoordinate.toDTO();
                    // 缓存到内存
                    regionCapitals.put(normalizedRegionName, coordinate);
                }
            } catch (Exception e) {
                log.warn("从数据库查找地区坐标时发生错误: {}", e.getMessage());
            }
        }

        if (coordinate == null) {
            // 尝试模糊匹配
            coordinate = findBestMatch(normalizedRegionName, regionCapitals);
        }

        return Optional.ofNullable(coordinate);
    }

    @Override
    public boolean validateCoordinate(GeographicCoordinate coordinate) {
        return coordinate != null && coordinate.isValid();
    }

    @Override
    public double calculateDistance(GeographicCoordinate coord1, GeographicCoordinate coord2) {
        if (!validateCoordinate(coord1) || !validateCoordinate(coord2)) {
            return -1;
        }

        // 使用Haversine公式计算距离
        double lat1Rad = Math.toRadians(coord1.getLatitude());
        double lat2Rad = Math.toRadians(coord2.getLatitude());
        double deltaLatRad = Math.toRadians(coord2.getLatitude() - coord1.getLatitude());
        double deltaLonRad = Math.toRadians(coord2.getLongitude() - coord1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 地球半径（公里）
        double earthRadius = 6371;

        return earthRadius * c;
    }

    @Override
    public void initializeDefaultGeographicData() {
        // 初始化主要国家首都坐标
        initializeCapitalCoordinates();

        // 初始化中国主要地区首府坐标
        initializeChineseRegionCapitals();

        log.info("默认地理数据初始化完成");
    }

    @Override
    public String getCacheStatistics() {
        long total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (double) cacheHits / total * 100 : 0;

        return String.format("缓存统计 - 总请求: %d, 命中: %d, 未命中: %d, 命中率: %.2f%%, 缓存大小: %d",
                total, cacheHits, cacheMisses, hitRate, coordinateCache.size());
    }

    // 私有辅助方法

    private void enhanceSingleEventGeographicInfo(EventData event) {
        try {
            // 如果是增强事件数据，进行完整的地理信息处理
            if (event instanceof EnhancedEventData) {
                enhanceEnhancedEventData((EnhancedEventData) event);
            } else {
                // 对于普通事件数据，只处理基础地理信息
                enhanceBasicEventData(event);
            }
        } catch (Exception e) {
            log.warn("处理事件地理信息时发生错误: {}", e.getMessage());
        }
    }

    private void enhanceEnhancedEventData(EnhancedEventData enhancedEvent) {
        enhancedEvent.setGeoProcessingStatus(EnhancedEventData.GeoProcessingStatus.PROCESSING);

        boolean hasAnyCoordinate = false;
        StringBuilder errorMessages = new StringBuilder();

        // 处理事件发生地
        if (enhancedEvent.getLocation() != null && !enhancedEvent.getLocation().trim().isEmpty()) {
            Optional<GeographicCoordinate> eventCoord = smartParseLocation(enhancedEvent.getLocation());
            if (eventCoord.isPresent()) {
                enhancedEvent.setEventCoordinate(eventCoord.get());
                enhancedEvent.setLatitude(eventCoord.get().getLatitude());
                enhancedEvent.setLongitude(eventCoord.get().getLongitude());
                hasAnyCoordinate = true;
            } else {
                errorMessages.append("无法解析事件发生地坐标: ").append(enhancedEvent.getLocation()).append("; ");
            }
        }

        // 处理主体地理信息
        if (enhancedEvent.getSubject() != null && !enhancedEvent.getSubject().trim().isEmpty()) {
            Optional<GeographicCoordinate> subjectCoord = smartParseLocation(enhancedEvent.getSubject());
            if (subjectCoord.isPresent()) {
                enhancedEvent.setSubjectCoordinate(subjectCoord.get());
                hasAnyCoordinate = true;
            } else {
                errorMessages.append("无法解析主体坐标: ").append(enhancedEvent.getSubject()).append("; ");
            }
        }

        // 处理客体地理信息
        if (enhancedEvent.getObject() != null && !enhancedEvent.getObject().trim().isEmpty()) {
            Optional<GeographicCoordinate> objectCoord = smartParseLocation(enhancedEvent.getObject());
            if (objectCoord.isPresent()) {
                enhancedEvent.setObjectCoordinate(objectCoord.get());
                hasAnyCoordinate = true;
            } else {
                errorMessages.append("无法解析客体坐标: ").append(enhancedEvent.getObject()).append("; ");
            }
        }

        // 设置处理状态
        if (hasAnyCoordinate) {
            if (errorMessages.length() > 0) {
                enhancedEvent.markGeoProcessingPartial(errorMessages.toString());
            } else {
                enhancedEvent.markGeoProcessingCompleted();
            }
        } else {
            enhancedEvent.markGeoProcessingFailed(errorMessages.toString());
        }
    }

    private void enhanceBasicEventData(EventData event) {
        // 处理事件发生地
        if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
            Optional<GeographicCoordinate> coordinate = smartParseLocation(event.getLocation());
            coordinate.ifPresent(coord -> {
                event.setLatitude(coord.getLatitude());
                event.setLongitude(coord.getLongitude());
            });
        }
    }

    private String normalizeLocationName(String locationName) {
        if (locationName == null) {
            return "";
        }
        return locationName.trim().toLowerCase();
    }

    private LocationType detectLocationType(String locationName) {
        if (countryPattern.matcher(locationName).matches()) {
            return LocationType.COUNTRY;
        } else if (regionPattern.matcher(locationName).matches()) {
            return LocationType.REGION;
        } else {
            return LocationType.CITY;
        }
    }

    private Optional<GeographicCoordinate> findRegionCoordinate(String regionName) {
        return getRegionCapitalCoordinate(regionName, null);
    }

    private Optional<GeographicCoordinate> findCityCoordinate(String cityName) {
        // 对于城市，先尝试作为地区查找，如果找不到则返回空
        return findRegionCoordinate(cityName);
    }

    private GeographicCoordinate findBestMatch(String target, Map<String, GeographicCoordinate> dataMap) {
        // 简单的模糊匹配实现
        for (Map.Entry<String, GeographicCoordinate> entry : dataMap.entrySet()) {
            if (entry.getKey().contains(target) || target.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void initializeCapitalCoordinates() {
        // 主要国家首都坐标数据
        capitalCoordinates.put("中国", GeographicCoordinate.createDefault("中国", LocationType.COUNTRY, 39.9042, 116.4074));
        capitalCoordinates.put("美国", GeographicCoordinate.createDefault("美国", LocationType.COUNTRY, 38.9072, -77.0369));
        capitalCoordinates.put("日本", GeographicCoordinate.createDefault("日本", LocationType.COUNTRY, 35.6762, 139.6503));
        capitalCoordinates.put("韩国", GeographicCoordinate.createDefault("韩国", LocationType.COUNTRY, 37.5665, 126.9780));
        capitalCoordinates.put("英国", GeographicCoordinate.createDefault("英国", LocationType.COUNTRY, 51.5074, -0.1278));
        capitalCoordinates.put("法国", GeographicCoordinate.createDefault("法国", LocationType.COUNTRY, 48.8566, 2.3522));
        capitalCoordinates.put("德国", GeographicCoordinate.createDefault("德国", LocationType.COUNTRY, 52.5200, 13.4050));
        capitalCoordinates.put("俄罗斯",
                GeographicCoordinate.createDefault("俄罗斯", LocationType.COUNTRY, 55.7558, 37.6176));
        capitalCoordinates.put("印度", GeographicCoordinate.createDefault("印度", LocationType.COUNTRY, 28.6139, 77.2090));
        capitalCoordinates.put("巴西",
                GeographicCoordinate.createDefault("巴西", LocationType.COUNTRY, -15.8267, -47.9218));
        capitalCoordinates.put("澳大利亚",
                GeographicCoordinate.createDefault("澳大利亚", LocationType.COUNTRY, -35.2809, 149.1300));
        capitalCoordinates.put("加拿大",
                GeographicCoordinate.createDefault("加拿大", LocationType.COUNTRY, 45.4215, -75.6972));
        capitalCoordinates.put("意大利",
                GeographicCoordinate.createDefault("意大利", LocationType.COUNTRY, 41.9028, 12.4964));
        capitalCoordinates.put("西班牙",
                GeographicCoordinate.createDefault("西班牙", LocationType.COUNTRY, 40.4168, -3.7038));
        capitalCoordinates.put("荷兰", GeographicCoordinate.createDefault("荷兰", LocationType.COUNTRY, 52.3676, 4.9041));
        capitalCoordinates.put("瑞士", GeographicCoordinate.createDefault("瑞士", LocationType.COUNTRY, 46.9481, 7.4474));
        capitalCoordinates.put("瑞典", GeographicCoordinate.createDefault("瑞典", LocationType.COUNTRY, 59.3293, 18.0686));
        capitalCoordinates.put("挪威", GeographicCoordinate.createDefault("挪威", LocationType.COUNTRY, 59.9139, 10.7522));
        capitalCoordinates.put("丹麦", GeographicCoordinate.createDefault("丹麦", LocationType.COUNTRY, 55.6761, 12.5683));
        capitalCoordinates.put("芬兰", GeographicCoordinate.createDefault("芬兰", LocationType.COUNTRY, 60.1699, 24.9384));
    }

    private void initializeChineseRegionCapitals() {
        // 中国主要省份首府坐标
        regionCapitals.put("北京", GeographicCoordinate.createDefault("北京", LocationType.REGION, 39.9042, 116.4074));
        regionCapitals.put("上海", GeographicCoordinate.createDefault("上海", LocationType.REGION, 31.2304, 121.4737));
        regionCapitals.put("天津", GeographicCoordinate.createDefault("天津", LocationType.REGION, 39.3434, 117.3616));
        regionCapitals.put("重庆", GeographicCoordinate.createDefault("重庆", LocationType.REGION, 29.5647, 106.5507));
        regionCapitals.put("河北", GeographicCoordinate.createDefault("河北", LocationType.REGION, 38.0428, 114.5149));
        regionCapitals.put("山西", GeographicCoordinate.createDefault("山西", LocationType.REGION, 37.8570, 112.5490));
        regionCapitals.put("辽宁", GeographicCoordinate.createDefault("辽宁", LocationType.REGION, 41.8057, 123.4315));
        regionCapitals.put("吉林", GeographicCoordinate.createDefault("吉林", LocationType.REGION, 43.8868, 125.3245));
        regionCapitals.put("黑龙江", GeographicCoordinate.createDefault("黑龙江", LocationType.REGION, 45.8038, 126.5349));
        regionCapitals.put("江苏", GeographicCoordinate.createDefault("江苏", LocationType.REGION, 32.0603, 118.7969));
        regionCapitals.put("浙江", GeographicCoordinate.createDefault("浙江", LocationType.REGION, 30.2741, 120.1551));
        regionCapitals.put("安徽", GeographicCoordinate.createDefault("安徽", LocationType.REGION, 31.8612, 117.2830));
        regionCapitals.put("福建", GeographicCoordinate.createDefault("福建", LocationType.REGION, 26.0745, 119.2965));
        regionCapitals.put("江西", GeographicCoordinate.createDefault("江西", LocationType.REGION, 28.6820, 115.8579));
        regionCapitals.put("山东", GeographicCoordinate.createDefault("山东", LocationType.REGION, 36.6512, 117.1201));
        regionCapitals.put("河南", GeographicCoordinate.createDefault("河南", LocationType.REGION, 34.7466, 113.6254));
        regionCapitals.put("湖北", GeographicCoordinate.createDefault("湖北", LocationType.REGION, 30.5928, 114.3055));
        regionCapitals.put("湖南", GeographicCoordinate.createDefault("湖南", LocationType.REGION, 28.2282, 112.9388));
        regionCapitals.put("广东", GeographicCoordinate.createDefault("广东", LocationType.REGION, 23.1291, 113.2644));
        regionCapitals.put("广西", GeographicCoordinate.createDefault("广西", LocationType.REGION, 22.8160, 108.3669));
        regionCapitals.put("海南", GeographicCoordinate.createDefault("海南", LocationType.REGION, 20.0444, 110.1999));
        regionCapitals.put("四川", GeographicCoordinate.createDefault("四川", LocationType.REGION, 30.6171, 104.0648));
        regionCapitals.put("贵州", GeographicCoordinate.createDefault("贵州", LocationType.REGION, 26.6470, 106.6302));
        regionCapitals.put("云南", GeographicCoordinate.createDefault("云南", LocationType.REGION, 25.0389, 102.7183));
        regionCapitals.put("西藏", GeographicCoordinate.createDefault("西藏", LocationType.REGION, 29.6625, 91.1146));
        regionCapitals.put("陕西", GeographicCoordinate.createDefault("陕西", LocationType.REGION, 34.3416, 108.9398));
        regionCapitals.put("甘肃", GeographicCoordinate.createDefault("甘肃", LocationType.REGION, 36.0611, 103.8343));
        regionCapitals.put("青海", GeographicCoordinate.createDefault("青海", LocationType.REGION, 36.6171, 101.7782));
        regionCapitals.put("宁夏", GeographicCoordinate.createDefault("宁夏", LocationType.REGION, 38.4872, 106.2309));
        regionCapitals.put("新疆", GeographicCoordinate.createDefault("新疆", LocationType.REGION, 43.7793, 87.6177));
        regionCapitals.put("内蒙古", GeographicCoordinate.createDefault("内蒙古", LocationType.REGION, 40.8414, 111.7519));
        regionCapitals.put("香港", GeographicCoordinate.createDefault("香港", LocationType.REGION, 22.3193, 114.1694));
        regionCapitals.put("澳门", GeographicCoordinate.createDefault("澳门", LocationType.REGION, 22.1987, 113.5439));
        regionCapitals.put("台湾", GeographicCoordinate.createDefault("台湾", LocationType.REGION, 25.0330, 121.5654));
    }
}