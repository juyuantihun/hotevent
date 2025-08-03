package com.hotech.events.service.impl;

import com.hotech.events.dto.EnhancedEventData;
import com.hotech.events.dto.GeographicCoordinate;
import com.hotech.events.dto.GeographicCoordinate.LocationType;
import com.hotech.events.service.GeographicInfoService;
import com.hotech.events.service.GeographicValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 地理信息验证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeographicValidationServiceImpl implements GeographicValidationService {

    private final GeographicInfoService geographicInfoService;

    // 验证统计
    private final AtomicLong totalValidations = new AtomicLong(0);
    private final AtomicLong validCoordinates = new AtomicLong(0);
    private final AtomicLong invalidCoordinates = new AtomicLong(0);
    private final AtomicLong repairedCoordinates = new AtomicLong(0);

    // 地理范围定义（简化版本）
    private final Map<String, GeographicBounds> regionBounds = new HashMap<>();

    static {
        // 初始化一些主要地区的边界
    }

    @Override
    public GeographicValidationResult validateCoordinate(GeographicCoordinate coordinate) {
        totalValidations.incrementAndGet();

        GeographicValidationResult result = new GeographicValidationResult();
        result.setOriginalCoordinate(coordinate);
        result.setWarnings(new ArrayList<>());

        if (coordinate == null) {
            result.setValid(false);
            result.setErrorMessage("坐标对象为空");
            result.setValidationLevel(GeographicValidationResult.ValidationLevel.ERROR);
            invalidCoordinates.incrementAndGet();
            return result;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 基础坐标验证
        if (!validateBasicCoordinates(coordinate, errors, warnings)) {
            result.setValid(false);
            result.setErrorMessage(String.join("; ", errors));
            result.setWarnings(warnings);
            result.setValidationLevel(GeographicValidationResult.ValidationLevel.ERROR);
            invalidCoordinates.incrementAndGet();
            return result;
        }

        // 标准化坐标
        GeographicCoordinate standardized = standardizeCoordinate(coordinate);
        result.setStandardizedCoordinate(standardized);

        // 进一步验证
        validateCoordinateDetails(standardized, warnings);

        result.setValid(true);
        result.setWarnings(warnings);
        result.setValidationLevel(warnings.isEmpty() ? GeographicValidationResult.ValidationLevel.VALID
                : GeographicValidationResult.ValidationLevel.WARNING);

        validCoordinates.incrementAndGet();
        return result;
    }

    @Override
    public GeographicCoordinate standardizeCoordinate(GeographicCoordinate coordinate) {
        if (coordinate == null) {
            return null;
        }

        GeographicCoordinate standardized = GeographicCoordinate.builder()
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .locationName(coordinate.getLocationName())
                .locationType(coordinate.getLocationType())
                .countryCode(coordinate.getCountryCode())
                .regionCode(coordinate.getRegionCode())
                .isDefault(coordinate.getIsDefault())
                .accuracyLevel(coordinate.getAccuracyLevel())
                .build();

        // 标准化坐标精度（保留6位小数）
        if (standardized.getLatitude() != null) {
            standardized.setLatitude(Math.round(standardized.getLatitude() * 1000000.0) / 1000000.0);
        }

        if (standardized.getLongitude() != null) {
            standardized.setLongitude(Math.round(standardized.getLongitude() * 1000000.0) / 1000000.0);
        }

        // 标准化地点名称
        if (standardized.getLocationName() != null) {
            standardized.setLocationName(standardized.getLocationName().trim());
        }

        // 设置默认精度等级
        if (standardized.getAccuracyLevel() == null) {
            standardized.setAccuracyLevel(GeographicCoordinate.AccuracyLevel.MEDIUM);
        }

        // 设置默认地点类型
        if (standardized.getLocationType() == null) {
            standardized.setLocationType(LocationType.UNKNOWN);
        }

        return standardized;
    }

    @Override
    public List<GeographicValidationResult> validateEventsGeographicInfo(List<EnhancedEventData> events) {
        List<GeographicValidationResult> results = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return results;
        }

        for (EnhancedEventData event : events) {
            try {
                GeographicValidationResult result = validateEventGeographicInfo(event);
                results.add(result);
            } catch (Exception e) {
                log.warn("验证事件 {} 的地理信息时发生错误: {}", event.getId(), e.getMessage());

                GeographicValidationResult errorResult = new GeographicValidationResult();
                errorResult.setValid(false);
                errorResult.setErrorMessage("验证过程中发生异常: " + e.getMessage());
                errorResult.setValidationLevel(GeographicValidationResult.ValidationLevel.CRITICAL);
                results.add(errorResult);
            }
        }

        return results;
    }

    @Override
    public GeographicValidationResult validateEventGeographicInfo(EnhancedEventData event) {
        GeographicValidationResult result = new GeographicValidationResult();
        result.setWarnings(new ArrayList<>());

        if (event == null) {
            result.setValid(false);
            result.setErrorMessage("事件对象为空");
            result.setValidationLevel(GeographicValidationResult.ValidationLevel.ERROR);
            return result;
        }

        List<String> warnings = new ArrayList<>();
        boolean hasValidCoordinate = false;

        // 验证事件坐标
        if (event.getEventCoordinate() != null) {
            GeographicValidationResult coordResult = validateCoordinate(event.getEventCoordinate());
            if (coordResult.isValid()) {
                hasValidCoordinate = true;
                event.setEventCoordinate(coordResult.getStandardizedCoordinate());
            } else {
                warnings.add("事件坐标无效: " + coordResult.getErrorMessage());
            }
        }

        // 验证主体坐标
        if (event.getSubjectCoordinate() != null) {
            GeographicValidationResult subjectResult = validateCoordinate(event.getSubjectCoordinate());
            if (subjectResult.isValid()) {
                hasValidCoordinate = true;
                event.setSubjectCoordinate(subjectResult.getStandardizedCoordinate());
            } else {
                warnings.add("主体坐标无效: " + subjectResult.getErrorMessage());
            }
        }

        // 验证客体坐标
        if (event.getObjectCoordinate() != null) {
            GeographicValidationResult objectResult = validateCoordinate(event.getObjectCoordinate());
            if (objectResult.isValid()) {
                hasValidCoordinate = true;
                event.setObjectCoordinate(objectResult.getStandardizedCoordinate());
            } else {
                warnings.add("客体坐标无效: " + objectResult.getErrorMessage());
            }
        }

        // 验证基础坐标
        if (event.getLatitude() != null && event.getLongitude() != null) {
            GeographicCoordinate basicCoord = GeographicCoordinate.builder()
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .locationName(event.getLocation())
                    .build();

            GeographicValidationResult basicResult = validateCoordinate(basicCoord);
            if (basicResult.isValid()) {
                hasValidCoordinate = true;
                event.setLatitude(basicResult.getStandardizedCoordinate().getLatitude());
                event.setLongitude(basicResult.getStandardizedCoordinate().getLongitude());
            } else {
                warnings.add("基础坐标无效: " + basicResult.getErrorMessage());
            }
        }

        // 设置验证结果
        result.setValid(hasValidCoordinate);
        result.setWarnings(warnings);

        if (hasValidCoordinate) {
            result.setValidationLevel(warnings.isEmpty() ? GeographicValidationResult.ValidationLevel.VALID
                    : GeographicValidationResult.ValidationLevel.WARNING);
        } else {
            result.setErrorMessage("事件缺少有效的地理坐标信息");
            result.setValidationLevel(GeographicValidationResult.ValidationLevel.ERROR);
        }

        return result;
    }

    @Override
    public GeographicCoordinate repairInvalidCoordinate(GeographicCoordinate coordinate, String locationName) {
        if (coordinate == null && (locationName == null || locationName.trim().isEmpty())) {
            return null;
        }

        try {
            // 如果坐标无效但有地点名称，尝试通过地点名称获取坐标
            if ((coordinate == null || !geographicInfoService.validateCoordinate(coordinate))
                    && locationName != null && !locationName.trim().isEmpty()) {

                Optional<GeographicCoordinate> repaired = geographicInfoService.smartParseLocation(locationName);
                if (repaired.isPresent()) {
                    repairedCoordinates.incrementAndGet();
                    log.debug("成功修复坐标: {} -> {}", locationName, repaired.get().getCoordinateString());
                    return repaired.get();
                }
            }

            // 如果坐标部分有效，尝试修复
            if (coordinate != null) {
                GeographicCoordinate repaired = GeographicCoordinate.builder()
                        .latitude(coordinate.getLatitude())
                        .longitude(coordinate.getLongitude())
                        .locationName(locationName != null ? locationName : coordinate.getLocationName())
                        .locationType(coordinate.getLocationType() != null ? coordinate.getLocationType()
                                : LocationType.UNKNOWN)
                        .accuracyLevel(GeographicCoordinate.AccuracyLevel.LOW)
                        .build();

                // 修复超出范围的坐标
                if (repaired.getLatitude() != null) {
                    if (repaired.getLatitude() > 90)
                        repaired.setLatitude(90.0);
                    if (repaired.getLatitude() < -90)
                        repaired.setLatitude(-90.0);
                }

                if (repaired.getLongitude() != null) {
                    if (repaired.getLongitude() > 180)
                        repaired.setLongitude(180.0);
                    if (repaired.getLongitude() < -180)
                        repaired.setLongitude(-180.0);
                }

                if (geographicInfoService.validateCoordinate(repaired)) {
                    repairedCoordinates.incrementAndGet();
                    return repaired;
                }
            }

        } catch (Exception e) {
            log.warn("修复坐标时发生错误: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isCoordinateInReasonableRange(GeographicCoordinate coordinate, String expectedRegion) {
        if (coordinate == null || !geographicInfoService.validateCoordinate(coordinate)) {
            return false;
        }

        if (expectedRegion == null || expectedRegion.trim().isEmpty()) {
            return true; // 没有预期地区，认为合理
        }

        try {
            // 获取预期地区的坐标
            Optional<GeographicCoordinate> expectedCoord = geographicInfoService.smartParseLocation(expectedRegion);
            if (expectedCoord.isPresent()) {
                // 计算距离
                double distance = geographicInfoService.calculateDistance(coordinate, expectedCoord.get());

                // 根据地区类型设置合理距离阈值
                double threshold = getDistanceThreshold(expectedCoord.get().getLocationType());

                return distance <= threshold;
            }

        } catch (Exception e) {
            log.warn("检查坐标合理范围时发生错误: {}", e.getMessage());
        }

        return true; // 无法验证时认为合理
    }

    @Override
    public Map<String, Object> getValidationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalValidations", totalValidations.get());
        stats.put("validCoordinates", validCoordinates.get());
        stats.put("invalidCoordinates", invalidCoordinates.get());
        stats.put("repairedCoordinates", repairedCoordinates.get());

        long total = totalValidations.get();
        if (total > 0) {
            stats.put("validationSuccessRate", (double) validCoordinates.get() / total * 100);
            stats.put("repairSuccessRate", (double) repairedCoordinates.get() / invalidCoordinates.get() * 100);
        } else {
            stats.put("validationSuccessRate", 0.0);
            stats.put("repairSuccessRate", 0.0);
        }

        return stats;
    }

    // 私有辅助方法

    /**
     * 验证基础坐标
     */
    private boolean validateBasicCoordinates(GeographicCoordinate coordinate, List<String> errors,
            List<String> warnings) {
        boolean isValid = true;

        // 检查纬度
        if (coordinate.getLatitude() == null) {
            errors.add("纬度不能为空");
            isValid = false;
        } else {
            double lat = coordinate.getLatitude();
            if (lat < -90 || lat > 90) {
                errors.add("纬度必须在-90到90之间，当前值: " + lat);
                isValid = false;
            }
        }

        // 检查经度
        if (coordinate.getLongitude() == null) {
            errors.add("经度不能为空");
            isValid = false;
        } else {
            double lon = coordinate.getLongitude();
            if (lon < -180 || lon > 180) {
                errors.add("经度必须在-180到180之间，当前值: " + lon);
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * 验证坐标详细信息
     */
    private void validateCoordinateDetails(GeographicCoordinate coordinate, List<String> warnings) {
        // 检查地点名称
        if (coordinate.getLocationName() == null || coordinate.getLocationName().trim().isEmpty()) {
            warnings.add("地点名称为空");
        }

        // 检查坐标精度
        if (coordinate.getLatitude() != null && coordinate.getLongitude() != null) {
            // 检查是否为明显的默认值或无效值
            if (coordinate.getLatitude() == 0.0 && coordinate.getLongitude() == 0.0) {
                warnings.add("坐标为(0,0)，可能是默认值");
            }

            // 检查精度是否过低
            String latStr = String.valueOf(coordinate.getLatitude());
            String lonStr = String.valueOf(coordinate.getLongitude());

            if (latStr.length() < 5 || lonStr.length() < 5) {
                warnings.add("坐标精度可能过低");
            }
        }

        // 检查地点类型
        if (coordinate.getLocationType() == null || coordinate.getLocationType() == LocationType.UNKNOWN) {
            warnings.add("地点类型未知");
        }
    }

    /**
     * 获取距离阈值
     */
    private double getDistanceThreshold(LocationType locationType) {
        if (locationType == null) {
            return 1000; // 默认1000公里
        }

        switch (locationType) {
            case COUNTRY:
                return 2000; // 国家级别，2000公里
            case REGION:
                return 500; // 地区级别，500公里
            case CITY:
                return 100; // 城市级别，100公里
            case DISTRICT:
                return 50; // 区县级别，50公里
            default:
                return 1000; // 默认1000公里
        }
    }

    /**
     * 地理边界定义
     */
    private static class GeographicBounds {
        private final double minLatitude;
        private final double maxLatitude;
        private final double minLongitude;
        private final double maxLongitude;

        public GeographicBounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
            this.minLatitude = minLatitude;
            this.maxLatitude = maxLatitude;
            this.minLongitude = minLongitude;
            this.maxLongitude = maxLongitude;
        }

        public boolean contains(double latitude, double longitude) {
            return latitude >= minLatitude && latitude <= maxLatitude &&
                    longitude >= minLongitude && longitude <= maxLongitude;
        }
    }
}