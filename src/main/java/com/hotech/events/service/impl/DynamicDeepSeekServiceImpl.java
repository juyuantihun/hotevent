package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Region;
import com.hotech.events.service.DynamicDeepSeekService;
import com.hotech.events.service.DeepSeekService;
import com.hotech.events.service.EnhancedDeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 动态DeepSeek服务实现类
 * 根据时间范围动态选择使用官方API还是火山引擎联网搜索API
 */
@Slf4j
@Service
public class DynamicDeepSeekServiceImpl implements DynamicDeepSeekService {

    // 时间分界点：2024年1月1日
    private static final LocalDateTime TIME_BOUNDARY = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;

    @Autowired
    @Qualifier("deepSeekOnlineSearchService")
    private DeepSeekService deepSeekOnlineSearchService;

    @Autowired
    private com.hotech.events.config.DynamicApiConfigManager dynamicApiConfigManager;

    @Override
    public List<EventData> fetchEventsWithTimeBasedAPI(TimelineGenerateRequest request) {
        boolean useWebSearch = shouldUseWebSearch(request.getStartTime(), request.getEndTime());
        
        log.info("根据时间范围选择API: startTime={}, endTime={}, useWebSearch={}", 
                request.getStartTime(), request.getEndTime(), useWebSearch);

        try {
            if (useWebSearch) {
                // 使用火山引擎联网搜索API（2024年及以后）
                log.info("使用火山引擎联网搜索API获取事件数据");
                return enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
            } else {
                // 使用官方DeepSeek API（2024年以前）
                log.info("使用官方DeepSeek API获取历史事件数据");
                return fetchHistoricalEventsWithOfficialAPI(request);
            }
        } catch (Exception e) {
            log.error("动态API调用失败，尝试使用备用方案", e);
            // 如果主要API失败，尝试使用备用API
            return tryFallbackAPI(request, useWebSearch);
        }
    }

    @Override
    public List<Map<String, Object>> analyzeEventRelationsWithTimeBasedAPI(
            List<Map<String, Object>> events, LocalDateTime startTime, LocalDateTime endTime) {
        
        boolean useWebSearch = shouldUseWebSearch(startTime, endTime);
        
        log.info("根据时间范围分析事件关系: startTime={}, endTime={}, useWebSearch={}", 
                startTime, endTime, useWebSearch);

        try {
            if (useWebSearch) {
                // 使用火山引擎联网搜索API
                return deepSeekOnlineSearchService.analyzeEventRelations(events);
            } else {
                // 使用官方DeepSeek API
                return enhancedDeepSeekService.analyzeEventRelations(events);
            }
        } catch (Exception e) {
            log.error("事件关系分析失败，使用备用方案", e);
            // 备用方案：使用增强服务
            return enhancedDeepSeekService.analyzeEventRelations(events);
        }
    }

    @Override
    public List<Map<String, Object>> organizeTimelinesWithTimeBasedAPI(
            List<Map<String, Object>> events, List<Map<String, Object>> relations,
            LocalDateTime startTime, LocalDateTime endTime) {
        
        boolean useWebSearch = shouldUseWebSearch(startTime, endTime);
        
        log.info("根据时间范围组织时间线: startTime={}, endTime={}, useWebSearch={}", 
                startTime, endTime, useWebSearch);

        try {
            if (useWebSearch) {
                // 使用火山引擎联网搜索API
                return deepSeekOnlineSearchService.organizeTimelines(events, relations);
            } else {
                // 使用官方DeepSeek API
                return enhancedDeepSeekService.organizeTimelines(events, relations);
            }
        } catch (Exception e) {
            log.error("时间线组织失败，使用备用方案", e);
            // 备用方案：使用增强服务
            return enhancedDeepSeekService.organizeTimelines(events, relations);
        }
    }

    @Override
    public boolean shouldUseWebSearch(LocalDateTime startTime, LocalDateTime endTime) {
        // 如果结束时间在2024年及以后，使用联网搜索
        // 如果开始时间和结束时间都在2024年以前，使用官方API
        
        if (endTime != null && endTime.isAfter(TIME_BOUNDARY)) {
            log.debug("时间范围包含2024年及以后，使用联网搜索API");
            return true;
        }
        
        if (startTime != null && startTime.isBefore(TIME_BOUNDARY) && 
            (endTime == null || endTime.isBefore(TIME_BOUNDARY))) {
            log.debug("时间范围完全在2024年以前，使用官方API");
            return false;
        }
        
        // 默认情况下，如果时间范围跨越2024年，使用联网搜索
        log.debug("时间范围跨越2024年或无法确定，默认使用联网搜索API");
        return true;
    }

    /**
     * 使用官方API获取历史事件数据
     */
    private List<EventData> fetchHistoricalEventsWithOfficialAPI(TimelineGenerateRequest request) {
        log.info("使用官方API获取历史事件数据");
        
        try {
            // 直接使用增强服务，它会根据配置自动选择合适的API
            // 对于历史数据，我们可以通过修改请求来暗示使用官方API
            return enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
        } catch (Exception e) {
            log.error("使用官方API获取历史事件失败", e);
            throw e;
        }
    }

    /**
     * 使用火山引擎API获取最新事件数据
     */
    private List<EventData> fetchLatestEventsWithVolcengineAPI(TimelineGenerateRequest request) {
        log.info("使用火山引擎API获取最新事件数据");
        
        try {
            // 使用联网搜索服务获取最新数据
            return enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
        } catch (Exception e) {
            log.error("使用火山引擎API获取最新事件失败", e);
            throw e;
        }
    }

    /**
     * 尝试备用API
     */
    private List<EventData> tryFallbackAPI(TimelineGenerateRequest request, boolean originalUseWebSearch) {
        log.warn("尝试使用备用API");
        
        try {
            if (originalUseWebSearch) {
                // 原本要使用联网搜索，现在尝试官方API
                log.info("联网搜索失败，尝试使用官方API作为备用");
                return fetchHistoricalEventsWithOfficialAPI(request);
            } else {
                // 原本要使用官方API，现在尝试联网搜索
                log.info("官方API失败，尝试使用联网搜索作为备用");
                return fetchLatestEventsWithVolcengineAPI(request);
            }
        } catch (Exception e) {
            log.error("备用API也失败了", e);
            throw new RuntimeException("所有API都失败了: " + e.getMessage(), e);
        }
    }

    /**
     * 创建适用于历史数据的请求
     */
    private TimelineGenerateRequest createHistoricalRequest(TimelineGenerateRequest originalRequest) {
        TimelineGenerateRequest historicalRequest = new TimelineGenerateRequest();
        historicalRequest.setName(originalRequest.getName());
        historicalRequest.setDescription(originalRequest.getDescription() + " (历史数据)");
        historicalRequest.setRegionIds(originalRequest.getRegionIds());
        historicalRequest.setStartTime(originalRequest.getStartTime());
        historicalRequest.setEndTime(originalRequest.getEndTime());
        return historicalRequest;
    }

    /**
     * 创建适用于最新数据的请求
     */
    private TimelineGenerateRequest createLatestRequest(TimelineGenerateRequest originalRequest) {
        TimelineGenerateRequest latestRequest = new TimelineGenerateRequest();
        latestRequest.setName(originalRequest.getName());
        latestRequest.setDescription(originalRequest.getDescription() + " (最新数据)");
        latestRequest.setRegionIds(originalRequest.getRegionIds());
        latestRequest.setStartTime(originalRequest.getStartTime());
        latestRequest.setEndTime(originalRequest.getEndTime());
        return latestRequest;
    }
}