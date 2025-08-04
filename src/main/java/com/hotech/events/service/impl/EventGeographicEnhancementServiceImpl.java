package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.entity.Event;
import com.hotech.events.service.EventGeographicEnhancementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 事件地理信息增强服务实现类
 * 为缺少经纬度信息的事件补充地理坐标
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EventGeographicEnhancementServiceImpl implements EventGeographicEnhancementService {
    
    // 统计信息
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong successfullyEnhanced = new AtomicLong(0);
    private final AtomicLong alreadyHasCoordinates = new AtomicLong(0);
    private final AtomicLong failedToEnhance = new AtomicLong(0);
    
    // 地点名称提取正则表达式
    private final Pattern locationPattern = Pattern.compile(
        "(?:在|于|位于|发生在)\\s*([^，,。.\\s]{2,20}?)(?:[，,。.\\s]|$)", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 国家/地区坐标映射（主要城市/首都坐标）
    private final Map<String, double[]> locationCoordinates = new HashMap<>();
    
    public EventGeographicEnhancementServiceImpl() {
        initializeLocationCoordinates();
    }
    
    /**
     * 初始化地点坐标数据
     */
    private void initializeLocationCoordinates() {
        // 主要国家首都坐标 [纬度, 经度]
        locationCoordinates.put("北京", new double[]{39.9042, 116.4074});
        locationCoordinates.put("上海", new double[]{31.2304, 121.4737});
        locationCoordinates.put("广州", new double[]{23.1291, 113.2644});
        locationCoordinates.put("深圳", new double[]{22.5431, 114.0579});
        locationCoordinates.put("杭州", new double[]{30.2741, 120.1551});
        locationCoordinates.put("南京", new double[]{32.0603, 118.7969});
        locationCoordinates.put("武汉", new double[]{30.5928, 114.3055});
        locationCoordinates.put("成都", new double[]{30.5728, 104.0668});
        locationCoordinates.put("西安", new double[]{34.3416, 108.9398});
        locationCoordinates.put("重庆", new double[]{29.5630, 106.5516});
        
        // 国际主要城市
        locationCoordinates.put("华盛顿", new double[]{38.9072, -77.0369});
        locationCoordinates.put("纽约", new double[]{40.7128, -74.0060});
        locationCoordinates.put("洛杉矶", new double[]{34.0522, -118.2437});
        locationCoordinates.put("伦敦", new double[]{51.5074, -0.1278});
        locationCoordinates.put("巴黎", new double[]{48.8566, 2.3522});
        locationCoordinates.put("柏林", new double[]{52.5200, 13.4050});
        locationCoordinates.put("东京", new double[]{35.6762, 139.6503});
        locationCoordinates.put("首尔", new double[]{37.5665, 126.9780});
        locationCoordinates.put("莫斯科", new double[]{55.7558, 37.6176});
        locationCoordinates.put("悉尼", new double[]{-33.8688, 151.2093});
        locationCoordinates.put("新德里", new double[]{28.6139, 77.2090});
        locationCoordinates.put("新加坡", new double[]{1.3521, 103.8198});
        locationCoordinates.put("香港", new double[]{22.3193, 114.1694});
        locationCoordinates.put("台北", new double[]{25.0330, 121.5654});
        
        // 中东地区
        locationCoordinates.put("德黑兰", new double[]{35.6892, 51.3890});
        locationCoordinates.put("巴格达", new double[]{33.3152, 44.3661});
        locationCoordinates.put("大马士革", new double[]{33.5138, 36.2765});
        locationCoordinates.put("开罗", new double[]{30.0444, 31.2357});
        locationCoordinates.put("利雅得", new double[]{24.7136, 46.6753});
        locationCoordinates.put("迪拜", new double[]{25.2048, 55.2708});
        locationCoordinates.put("耶路撒冷", new double[]{31.7683, 35.2137});
        locationCoordinates.put("特拉维夫", new double[]{32.0853, 34.7818});
        
        // 欧洲主要城市
        locationCoordinates.put("罗马", new double[]{41.9028, 12.4964});
        locationCoordinates.put("马德里", new double[]{40.4168, -3.7038});
        locationCoordinates.put("阿姆斯特丹", new double[]{52.3676, 4.9041});
        locationCoordinates.put("布鲁塞尔", new double[]{50.8503, 4.3517});
        locationCoordinates.put("维也纳", new double[]{48.2082, 16.3738});
        locationCoordinates.put("苏黎世", new double[]{47.3769, 8.5417});
        locationCoordinates.put("斯德哥尔摩", new double[]{59.3293, 18.0686});
        locationCoordinates.put("哥本哈根", new double[]{55.6761, 12.5683});
        
        // 非洲主要城市
        locationCoordinates.put("开普敦", new double[]{-33.9249, 18.4241});
        locationCoordinates.put("约翰内斯堡", new double[]{-26.2041, 28.0473});
        locationCoordinates.put("拉各斯", new double[]{6.5244, 3.3792});
        locationCoordinates.put("内罗毕", new double[]{-1.2921, 36.8219});
        
        // 南美主要城市
        locationCoordinates.put("圣保罗", new double[]{-23.5505, -46.6333});
        locationCoordinates.put("里约热内卢", new double[]{-22.9068, -43.1729});
        locationCoordinates.put("布宜诺斯艾利斯", new double[]{-34.6118, -58.3960});
        locationCoordinates.put("利马", new double[]{-12.0464, -77.0428});
        
        // 添加国家名称映射到首都
        locationCoordinates.put("中国", locationCoordinates.get("北京"));
        locationCoordinates.put("美国", locationCoordinates.get("华盛顿"));
        locationCoordinates.put("英国", locationCoordinates.get("伦敦"));
        locationCoordinates.put("法国", locationCoordinates.get("巴黎"));
        locationCoordinates.put("德国", locationCoordinates.get("柏林"));
        locationCoordinates.put("日本", locationCoordinates.get("东京"));
        locationCoordinates.put("韩国", locationCoordinates.get("首尔"));
        locationCoordinates.put("俄罗斯", locationCoordinates.get("莫斯科"));
        locationCoordinates.put("澳大利亚", locationCoordinates.get("悉尼"));
        locationCoordinates.put("印度", locationCoordinates.get("新德里"));
        locationCoordinates.put("伊朗", locationCoordinates.get("德黑兰"));
        locationCoordinates.put("伊拉克", locationCoordinates.get("巴格达"));
        locationCoordinates.put("叙利亚", locationCoordinates.get("大马士革"));
        locationCoordinates.put("埃及", locationCoordinates.get("开罗"));
        locationCoordinates.put("沙特阿拉伯", locationCoordinates.get("利雅得"));
        locationCoordinates.put("以色列", locationCoordinates.get("耶路撒冷"));
        
        log.info("初始化地点坐标数据完成，共加载 {} 个地点", locationCoordinates.size());
    }
    
    @Override
    public Event enhanceEventGeographicInfo(Event event) {
        if (event == null) {
            return null;
        }
        
        totalProcessed.incrementAndGet();
        
        // 检查是否已有坐标信息
        if (!needsGeographicEnhancement(event)) {
            alreadyHasCoordinates.incrementAndGet();
            log.debug("事件 {} 已有坐标信息，跳过增强", event.getId());
            return event;
        }
        
        try {
            // 尝试从事件地点获取坐标
            double[] coordinates = null;
            
            if (StringUtils.hasText(event.getEventLocation())) {
                coordinates = getCoordinatesByLocation(event.getEventLocation());
            }
            
            // 如果从地点无法获取，尝试从描述中提取
            if (coordinates == null && StringUtils.hasText(event.getEventDescription())) {
                String extractedLocation = extractLocationFromDescription(event.getEventDescription());
                if (extractedLocation != null) {
                    coordinates = getCoordinatesByLocation(extractedLocation);
                    // 如果从描述中提取到了地点，更新事件地点字段
                    if (coordinates != null && !StringUtils.hasText(event.getEventLocation())) {
                        event.setEventLocation(extractedLocation);
                    }
                }
            }
            
            // 设置坐标
            if (coordinates != null) {
                event.setLatitude(BigDecimal.valueOf(coordinates[0]));
                event.setLongitude(BigDecimal.valueOf(coordinates[1]));
                event.setGeographicStatus(1); // 已处理
                event.setGeographicUpdatedAt(java.time.LocalDateTime.now());
                
                successfullyEnhanced.incrementAndGet();
                log.debug("成功为事件 {} 增强地理信息: 纬度={}, 经度={}", 
                    event.getId(), coordinates[0], coordinates[1]);
            } else {
                event.setGeographicStatus(2); // 处理失败
                event.setGeographicUpdatedAt(java.time.LocalDateTime.now());
                failedToEnhance.incrementAndGet();
                log.debug("无法为事件 {} 获取地理信息", event.getId());
            }
            
        } catch (Exception e) {
            event.setGeographicStatus(2); // 处理失败
            event.setGeographicUpdatedAt(java.time.LocalDateTime.now());
            failedToEnhance.incrementAndGet();
            log.warn("为事件 {} 增强地理信息时发生异常: {}", event.getId(), e.getMessage());
        }
        
        return event;
    }
    
    @Override
    public EventData enhanceEventDataGeographicInfo(EventData eventData) {
        if (eventData == null) {
            return null;
        }
        
        totalProcessed.incrementAndGet();
        
        // 检查是否已有坐标信息
        if (!needsGeographicEnhancement(eventData)) {
            alreadyHasCoordinates.incrementAndGet();
            log.debug("事件数据 {} 已有坐标信息，跳过增强", eventData.getId());
            return eventData;
        }
        
        try {
            // 尝试从事件地点获取坐标
            double[] coordinates = null;
            
            if (StringUtils.hasText(eventData.getLocation())) {
                coordinates = getCoordinatesByLocation(eventData.getLocation());
            }
            
            // 如果从地点无法获取，尝试从描述中提取
            if (coordinates == null && StringUtils.hasText(eventData.getDescription())) {
                String extractedLocation = extractLocationFromDescription(eventData.getDescription());
                if (extractedLocation != null) {
                    coordinates = getCoordinatesByLocation(extractedLocation);
                    // 如果从描述中提取到了地点，更新事件地点字段
                    if (coordinates != null && !StringUtils.hasText(eventData.getLocation())) {
                        eventData.setLocation(extractedLocation);
                    }
                }
            }
            
            // 设置坐标
            if (coordinates != null) {
                eventData.setLatitude(coordinates[0]);
                eventData.setLongitude(coordinates[1]);
                
                successfullyEnhanced.incrementAndGet();
                log.debug("成功为事件数据 {} 增强地理信息: 纬度={}, 经度={}", 
                    eventData.getId(), coordinates[0], coordinates[1]);
            } else {
                failedToEnhance.incrementAndGet();
                log.debug("无法为事件数据 {} 获取地理信息", eventData.getId());
            }
            
        } catch (Exception e) {
            failedToEnhance.incrementAndGet();
            log.warn("为事件数据 {} 增强地理信息时发生异常: {}", eventData.getId(), e.getMessage());
        }
        
        return eventData;
    }
    
    @Override
    public List<Event> enhanceEventsGeographicInfo(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }
        
        log.info("开始批量增强 {} 个事件的地理信息", events.size());
        
        for (Event event : events) {
            enhanceEventGeographicInfo(event);
        }
        
        log.info("批量地理信息增强完成");
        return events;
    }
    
    @Override
    public List<EventData> enhanceEventDataListGeographicInfo(List<EventData> eventDataList) {
        if (eventDataList == null || eventDataList.isEmpty()) {
            return eventDataList;
        }
        
        log.info("开始批量增强 {} 个事件数据的地理信息", eventDataList.size());
        
        for (EventData eventData : eventDataList) {
            enhanceEventDataGeographicInfo(eventData);
        }
        
        log.info("批量地理信息增强完成");
        return eventDataList;
    }
    
    @Override
    public boolean needsGeographicEnhancement(Event event) {
        if (event == null) {
            return false;
        }
        
        // 如果已有经纬度信息，则不需要增强
        return event.getLatitude() == null || event.getLongitude() == null;
    }
    
    @Override
    public boolean needsGeographicEnhancement(EventData eventData) {
        if (eventData == null) {
            return false;
        }
        
        // 如果已有经纬度信息，则不需要增强
        return eventData.getLatitude() == null || eventData.getLongitude() == null ||
               eventData.getLatitude() == 0.0 || eventData.getLongitude() == 0.0;
    }
    
    @Override
    public double[] getCoordinatesByLocation(String locationName) {
        if (!StringUtils.hasText(locationName)) {
            return null;
        }
        
        // 标准化地点名称
        String normalizedName = locationName.trim();
        
        // 直接查找
        double[] coordinates = locationCoordinates.get(normalizedName);
        if (coordinates != null) {
            return coordinates.clone(); // 返回副本避免修改原数据
        }
        
        // 模糊匹配
        for (Map.Entry<String, double[]> entry : locationCoordinates.entrySet()) {
            if (entry.getKey().contains(normalizedName) || normalizedName.contains(entry.getKey())) {
                return entry.getValue().clone();
            }
        }
        
        log.debug("未找到地点 '{}' 的坐标信息", locationName);
        return null;
    }
    
    @Override
    public String extractLocationFromDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        
        Matcher matcher = locationPattern.matcher(description);
        if (matcher.find()) {
            String location = matcher.group(1).trim();
            log.debug("从描述中提取到地点: {}", location);
            return location;
        }
        
        return null;
    }
    
    @Override
    public String getEnhancementStatistics() {
        return String.format(
            "地理信息增强统计 - 总处理: %d, 成功增强: %d, 已有坐标: %d, 增强失败: %d, 成功率: %.2f%%",
            totalProcessed.get(),
            successfullyEnhanced.get(),
            alreadyHasCoordinates.get(),
            failedToEnhance.get(),
            totalProcessed.get() > 0 ? (successfullyEnhanced.get() * 100.0 / totalProcessed.get()) : 0.0
        );
    }
}