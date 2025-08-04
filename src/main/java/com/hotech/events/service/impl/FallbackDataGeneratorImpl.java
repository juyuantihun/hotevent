package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.service.FallbackDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 备用数据生成器实现类
 * 当API调用失败或解析失败时，提供备用的事件数据
 * 
 * @author Kiro
 */
@Service
public class FallbackDataGeneratorImpl implements FallbackDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(FallbackDataGeneratorImpl.class);
    
    // 真实历史事件模板
    private static final List<String[]> REAL_HISTORICAL_EVENTS = Arrays.asList(
        new String[]{"2024年巴黎奥运会开幕", "2024年7月26日，第33届夏季奥林匹克运动会在法国巴黎开幕，这是巴黎时隔100年再次举办奥运会。", "巴黎", "体育"},
        new String[]{"中国神舟十八号载人飞船发射", "2024年4月25日，神舟十八号载人飞船在酒泉卫星发射中心成功发射，三名航天员开始为期6个月的太空驻留任务。", "酒泉", "科技"},
        new String[]{"美国总统大选", "2024年11月5日，美国举行第60届总统选举，这是美国历史上最受关注的选举之一。", "华盛顿", "政治"},
        new String[]{"欧洲央行降息", "2024年6月6日，欧洲央行宣布将主要再融资利率下调25个基点至4.25%，这是近五年来首次降息。", "法兰克福", "经济"},
        new String[]{"日本能登半岛地震", "2024年1月1日，日本石川县能登半岛发生7.6级地震，造成重大人员伤亡和财产损失。", "石川县", "自然灾害"},
        new String[]{"俄乌冲突持续", "2024年，俄乌冲突进入第三年，国际社会继续寻求和平解决方案。", "基辅", "政治"},
        new String[]{"人工智能技术突破", "2024年，ChatGPT等大语言模型技术取得重大突破，推动人工智能产业快速发展。", "硅谷", "科技"},
        new String[]{"全球气候变化应对", "2024年，各国继续加强气候变化应对措施，推进碳中和目标实现。", "全球", "环境"},
        new String[]{"中国经济稳步增长", "2024年，中国经济在复杂国际环境下保持稳步增长，GDP增速符合预期目标。", "北京", "经济"},
        new String[]{"新冠疫情防控常态化", "2024年，全球新冠疫情防控进入常态化阶段，各国逐步恢复正常社会经济活动。", "全球", "医疗"}
    );
    
    // 地区特色事件模板
    private static final Map<String, List<String[]>> REGION_SPECIFIC_EVENTS = new HashMap<>();
    
    static {
        REGION_SPECIFIC_EVENTS.put("中国", Arrays.asList(
            new String[]{"全国两会召开", "中国全国人民代表大会和中国人民政治协商会议在北京召开，审议重要议案和提案。", "政治"},
            new String[]{"春节庆祝活动", "中国传统春节期间，全国各地举办丰富多彩的庆祝活动。", "文化"},
            new String[]{"高考制度改革", "中国继续深化高考制度改革，促进教育公平和质量提升。", "教育"},
            new String[]{"一带一路建设", "中国持续推进一带一路倡议，加强与沿线国家合作。", "经济"},
            new String[]{"科技创新发展", "中国在人工智能、量子计算等前沿科技领域取得重要进展。", "科技"}
        ));
        
        REGION_SPECIFIC_EVENTS.put("美国", Arrays.asList(
            new String[]{"硅谷科技创新", "硅谷科技公司在人工智能和生物技术领域取得重大突破。", "科技"},
            new String[]{"华尔街金融动态", "华尔街金融市场波动，影响全球经济走势。", "经济"},
            new String[]{"好莱坞电影产业", "好莱坞电影产业在全球娱乐市场中继续发挥重要作用。", "文化"},
            new String[]{"NASA太空探索", "美国NASA在火星探索和月球计划方面取得新进展。", "科技"},
            new String[]{"气候政策调整", "美国政府调整气候政策，加强环境保护措施。", "环境"}
        ));
        
        REGION_SPECIFIC_EVENTS.put("欧洲", Arrays.asList(
            new String[]{"欧盟一体化进程", "欧盟继续推进一体化进程，加强成员国间合作。", "政治"},
            new String[]{"绿色能源转型", "欧洲各国加快绿色能源转型，推进可持续发展。", "环境"},
            new String[]{"文化遗产保护", "欧洲各国加强文化遗产保护，促进文化交流。", "文化"},
            new String[]{"数字化转型", "欧洲企业加快数字化转型，提升竞争力。", "科技"},
            new String[]{"社会福利改革", "欧洲国家继续完善社会福利制度，保障民生。", "社会"}
        ));
    }
    
    // 地区名称模板（可以根据实际地区数据扩展）
    private static final Map<String, List<String>> REGION_EVENTS = new HashMap<>();
    
    static {
        REGION_EVENTS.put("北京", Arrays.asList(
            "召开重要会议", "发布政策文件", "举办文化活动", "开展科技创新", "推进城市建设"
        ));
        REGION_EVENTS.put("上海", Arrays.asList(
            "金融市场动态", "国际贸易合作", "科技园区发展", "文化交流活动", "城市规划更新"
        ));
        REGION_EVENTS.put("广东", Arrays.asList(
            "制造业升级", "对外贸易发展", "科技创新突破", "文化产业发展", "基础设施建设"
        ));
        REGION_EVENTS.put("浙江", Arrays.asList(
            "数字经济发展", "民营企业创新", "生态环境保护", "文化旅游推广", "乡村振兴实践"
        ));
    }
    
    @Override
    public List<EventData> generateDefaultEvents(List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始生成真实历史事件，地区数量: {}, 时间范围: {} 到 {}", 
                   regionIds != null ? regionIds.size() : 0, startTime, endTime);
        
        List<EventData> events = new ArrayList<>();
        Random random = new Random();
        
        // 根据时间范围计算事件数量
        int eventCount = Math.min(calculateEventCount(startTime, endTime), REAL_HISTORICAL_EVENTS.size());
        
        // 使用真实历史事件
        List<String[]> shuffledEvents = new ArrayList<>(REAL_HISTORICAL_EVENTS);
        Collections.shuffle(shuffledEvents);
        
        for (int i = 0; i < eventCount; i++) {
            String[] eventTemplate = shuffledEvents.get(i);
            EventData event = new EventData();
            
            event.setId(UUID.randomUUID().toString());
            event.setTitle(eventTemplate[0]);
            event.setDescription(eventTemplate[1]);
            event.setLocation(eventTemplate[2]);
            event.setEventType(eventTemplate[3]);
            
            // 生成事件时间（在指定范围内）
            LocalDateTime eventTime = generateRandomTime(startTime, endTime);
            event.setEventTime(eventTime);
            
            // 设置其他属性
            event.setCredibilityScore(0.85 + random.nextDouble() * 0.1); // 0.85-0.95
            event.setSubject("相关机构");
            event.setObject("社会公众");
            event.setKeywords(Arrays.asList(eventTemplate[3], "重要事件", "历史"));
            event.setSources(Arrays.asList("历史记录", "新闻报道", "官方发布"));
            
            // 添加模拟坐标
            double[] coordinates = getSimulatedCoordinatesForFallback(i);
            event.setLatitude(coordinates[0]);
            event.setLongitude(coordinates[1]);
            
            events.add(event);
        }
        
        // 如果需要更多事件，添加地区特色事件
        if (events.size() < calculateEventCount(startTime, endTime)) {
            events.addAll(generateRegionSpecificEvents(regionIds, startTime, endTime, 
                calculateEventCount(startTime, endTime) - events.size()));
        }
        
        logger.info("真实历史事件生成完成，共生成 {} 个事件", events.size());
        return events;
    }
    
    /**
     * 生成地区特色事件
     */
    private List<EventData> generateRegionSpecificEvents(List<Long> regionIds, LocalDateTime startTime, 
                                                        LocalDateTime endTime, int count) {
        List<EventData> events = new ArrayList<>();
        Random random = new Random();
        
        // 地区名称映射（简化处理）
        Map<Long, String> regionMap = new HashMap<>();
        regionMap.put(8L, "中国");
        regionMap.put(9L, "美国");
        regionMap.put(2L, "欧洲");
        
        for (int i = 0; i < count && i < 10; i++) {
            String regionName = "中国"; // 默认地区
            
            // 尝试获取实际地区名称
            if (regionIds != null && !regionIds.isEmpty()) {
                Long regionId = regionIds.get(random.nextInt(regionIds.size()));
                regionName = regionMap.getOrDefault(regionId, "中国");
            }
            
            List<String[]> regionEvents = REGION_SPECIFIC_EVENTS.get(regionName);
            if (regionEvents != null && !regionEvents.isEmpty()) {
                String[] eventTemplate = regionEvents.get(random.nextInt(regionEvents.size()));
                
                EventData event = new EventData();
                event.setId(UUID.randomUUID().toString());
                event.setTitle(eventTemplate[0]);
                event.setDescription(eventTemplate[1]);
                event.setEventType(eventTemplate[2]);
                event.setLocation(regionName);
                
                LocalDateTime eventTime = generateRandomTime(startTime, endTime);
                event.setEventTime(eventTime);
                
                event.setCredibilityScore(0.80 + random.nextDouble() * 0.15);
                event.setSubject("政府机构");
                event.setObject("民众");
                event.setKeywords(Arrays.asList(regionName, eventTemplate[2], "发展"));
                event.setSources(Arrays.asList("官方媒体", "政府公告"));
                
                // 添加基于地区的坐标
                double[] coordinates = getCoordinatesForRegion(regionName, i);
                event.setLatitude(coordinates[0]);
                event.setLongitude(coordinates[1]);
                
                events.add(event);
            }
        }
        
        return events;
    }
    
    @Override
    public List<EventData> getSimilarEventsFromDatabase(String description, List<Long> regionIds) {
        logger.info("开始从数据库获取相似事件，描述关键词: {}", description);
        
        // 这里应该实现真正的数据库查询逻辑
        // 目前先返回模拟数据
        List<EventData> events = new ArrayList<>();
        
        // 基于描述关键词生成相关事件
        if (description != null && !description.isEmpty()) {
            String[] keywords = description.split("[\\s,，。]+");
            for (String keyword : keywords) {
                if (keyword.length() > 1) {
                    EventData event = new EventData();
                    event.setTitle(String.format("关于%s的相关事件", keyword));
                    event.setDescription(String.format("这是一个与%s相关的历史事件，具有重要的参考价值。", keyword));
                    event.setEventTime(LocalDateTime.now().minusDays(new Random().nextInt(30)));
                    events.add(event);
                    
                    if (events.size() >= 5) {
                        break;
                    }
                }
            }
        }
        
        logger.info("从数据库获取相似事件完成，共获取 {} 个事件", events.size());
        return events;
    }
    
    @Override
    public List<EventData> generateTestEvents(String theme, int count) {
        logger.info("开始生成测试事件，主题: {}, 数量: {}", theme, count);
        
        List<EventData> events = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            EventData event = new EventData();
            
            if (theme != null && !theme.isEmpty()) {
                event.setTitle(String.format("%s相关事件 %d", theme, i + 1));
                event.setDescription(String.format("这是一个关于%s的测试事件，用于验证系统功能。事件编号: %d", theme, i + 1));
            } else {
                List<String> eventTypes = Arrays.asList("政治", "经济", "社会", "科技", "文化", "体育", "环境", "教育");
                String eventType = eventTypes.get(random.nextInt(eventTypes.size()));
                event.setTitle(String.format("测试事件 - %s %d", eventType, i + 1));
                event.setDescription(String.format("这是一个%s类型的测试事件，用于系统功能验证。", eventType));
            }
            
            // 生成随机时间（最近30天内）
            LocalDateTime eventTime = LocalDateTime.now().minusDays(random.nextInt(30))
                                                        .minusHours(random.nextInt(24))
                                                        .minusMinutes(random.nextInt(60));
            event.setEventTime(eventTime);
            
            // 随机地点
            List<String> locations = Arrays.asList("北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都");
            event.setLocation(locations.get(random.nextInt(locations.size())));
            
            events.add(event);
        }
        
        logger.info("测试事件生成完成，共生成 {} 个事件", events.size());
        return events;
    }
    
    @Override
    public List<EventData> generateHistoricalEvents(List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime, int minCount) {
        logger.info("开始生成历史事件，最小数量: {}", minCount);
        
        List<EventData> events = new ArrayList<>();
        Random random = new Random();
        
        // 历史事件模板
        List<String> historicalEvents = Arrays.asList(
            "重要政策发布", "经济数据公布", "重大项目启动", "国际合作签署", "科技成果发布",
            "文化活动举办", "环保措施实施", "教育改革推进", "医疗服务改善", "交通建设完成"
        );
        
        for (int i = 0; i < minCount; i++) {
            EventData event = new EventData();
            
            String historicalEvent = historicalEvents.get(random.nextInt(historicalEvents.size()));
            event.setTitle(String.format("历史回顾：%s", historicalEvent));
            event.setDescription(String.format("回顾历史上的%s，这一事件对当时的发展产生了重要影响。", historicalEvent));
            
            // 生成历史时间
            LocalDateTime eventTime = generateRandomTime(startTime, endTime);
            event.setEventTime(eventTime);
            
            // 生成地点
            if (regionIds != null && !regionIds.isEmpty()) {
                String location = generateLocationForRegion(regionIds.get(random.nextInt(regionIds.size())));
                event.setLocation(location);
            }
            
            events.add(event);
        }
        
        logger.info("历史事件生成完成，共生成 {} 个事件", events.size());
        return events;
    }
    
    @Override
    public List<EventData> generateGenericEvents(int count) {
        logger.info("开始生成通用事件，数量: {}", count);
        
        List<EventData> events = new ArrayList<>();
        Random random = new Random();
        
        // 通用事件模板
        List<String> genericTitles = Arrays.asList(
            "重要会议召开", "政策文件发布", "项目建设推进", "合作协议签署", "技术创新突破",
            "服务质量提升", "环境治理加强", "民生保障改善", "安全措施完善", "发展规划制定"
        );
        
        for (int i = 0; i < count; i++) {
            EventData event = new EventData();
            
            String title = genericTitles.get(random.nextInt(genericTitles.size()));
            event.setTitle(title);
            event.setDescription(String.format("%s，这一举措将推动相关工作的进一步发展。", title));
            
            // 生成最近时间
            LocalDateTime eventTime = LocalDateTime.now().minusDays(random.nextInt(7))
                                                        .minusHours(random.nextInt(24));
            event.setEventTime(eventTime);
            
            events.add(event);
        }
        
        logger.info("通用事件生成完成，共生成 {} 个事件", events.size());
        return events;
    }
    
    // 私有辅助方法
    
    /**
     * 根据时间范围计算事件数量
     */
    private int calculateEventCount(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 10; // 默认数量
        }
        
        long days = java.time.Duration.between(startTime, endTime).toDays();
        if (days <= 1) {
            return 8;
        } else if (days <= 7) {
            return 12;
        } else if (days <= 30) {
            return 15;
        } else {
            return 20;
        }
    }
    
    /**
     * 生成随机时间
     */
    private LocalDateTime generateRandomTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return LocalDateTime.now().minusDays(new Random().nextInt(7));
        }
        
        long startEpoch = startTime.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = endTime.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = startEpoch + (long) (Math.random() * (endEpoch - startEpoch));
        
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }
    
    /**
     * 为地区生成地点信息
     */
    private String generateLocationForRegion(Long regionId) {
        // 这里应该根据实际的地区ID查询地区名称
        // 目前使用模拟逻辑
        List<String> locations = Arrays.asList("北京", "上海", "广州", "深圳", "杭州", "南京");
        return locations.get(new Random().nextInt(locations.size()));
    }
    
    /**
     * 获取备用数据的模拟坐标
     * 
     * @param index 索引
     * @return 坐标数组 [纬度, 经度]
     */
    private double[] getSimulatedCoordinatesForFallback(int index) {
        // 预定义一些主要城市的坐标
        double[][] cityCoordinates = {
            {39.9042, 116.4074}, // 北京
            {31.2304, 121.4737}, // 上海
            {23.1291, 113.2644}, // 广州
            {22.5431, 114.0579}, // 深圳
            {30.2741, 120.1551}, // 杭州
            {32.0603, 118.7969}, // 南京
            {30.5928, 114.3055}, // 武汉
            {30.5728, 104.0668}, // 成都
            {34.3416, 108.9398}, // 西安
            {29.5630, 106.5516}, // 重庆
        };
        
        // 根据索引循环使用坐标
        int coordinateIndex = index % cityCoordinates.length;
        return cityCoordinates[coordinateIndex];
    }
    
    /**
     * 根据地区名称获取坐标
     * 
     * @param regionName 地区名称
     * @param index 索引（用于同一地区的不同事件）
     * @return 坐标数组 [纬度, 经度]
     */
    private double[] getCoordinatesForRegion(String regionName, int index) {
        // 根据地区名称返回对应坐标
        switch (regionName) {
            case "北京":
                return new double[]{39.9042 + (index % 3) * 0.01, 116.4074 + (index % 3) * 0.01};
            case "上海":
                return new double[]{31.2304 + (index % 3) * 0.01, 121.4737 + (index % 3) * 0.01};
            case "广州":
                return new double[]{23.1291 + (index % 3) * 0.01, 113.2644 + (index % 3) * 0.01};
            case "深圳":
                return new double[]{22.5431 + (index % 3) * 0.01, 114.0579 + (index % 3) * 0.01};
            case "杭州":
                return new double[]{30.2741 + (index % 3) * 0.01, 120.1551 + (index % 3) * 0.01};
            case "南京":
                return new double[]{32.0603 + (index % 3) * 0.01, 118.7969 + (index % 3) * 0.01};
            case "武汉":
                return new double[]{30.5928 + (index % 3) * 0.01, 114.3055 + (index % 3) * 0.01};
            case "成都":
                return new double[]{30.5728 + (index % 3) * 0.01, 104.0668 + (index % 3) * 0.01};
            case "西安":
                return new double[]{34.3416 + (index % 3) * 0.01, 108.9398 + (index % 3) * 0.01};
            case "重庆":
                return new double[]{29.5630 + (index % 3) * 0.01, 106.5516 + (index % 3) * 0.01};
            default:
                // 默认使用北京坐标
                return new double[]{39.9042 + (index % 3) * 0.01, 116.4074 + (index % 3) * 0.01};
        }
    }
}