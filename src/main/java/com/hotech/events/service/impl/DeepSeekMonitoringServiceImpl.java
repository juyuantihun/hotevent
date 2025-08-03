package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiUsageStats;
import com.hotech.events.entity.DeepSeekApiUsage;
import com.hotech.events.mapper.DeepSeekApiUsageMapper;
import com.hotech.events.service.DeepSeekMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek监控服务实现类
 */
@Slf4j
@Service
public class DeepSeekMonitoringServiceImpl implements DeepSeekMonitoringService {
    
    @Autowired
    private DeepSeekApiUsageMapper apiUsageMapper;
    
    @Override
    @Async
    public void recordApiCall(String requestType, String requestParams, String responseStatus, 
                             Integer tokenUsage, Integer responseTimeMs, String errorMessage) {
        try {
            DeepSeekApiUsage usage = new DeepSeekApiUsage();
            usage.setRequestType(requestType);
            usage.setRequestParams(requestParams);
            usage.setResponseStatus(responseStatus);
            usage.setTokenUsage(tokenUsage);
            usage.setResponseTimeMs(responseTimeMs);
            usage.setErrorMessage(errorMessage);
            usage.setCreatedAt(LocalDateTime.now());
            
            apiUsageMapper.insert(usage);
            
            log.debug("记录API调用: type={}, status={}, responseTime={}ms", 
                     requestType, responseStatus, responseTimeMs);
        } catch (Exception e) {
            log.error("记录API调用失败", e);
        }
    }
    
    @Override
    public ApiUsageStats getUsageStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = apiUsageMapper.getUsageStatistics(startTime, endTime);
            Map<String, Object> todayStats = apiUsageMapper.getTodayUsageStatistics(LocalDateTime.now());
            
            ApiUsageStats result = new ApiUsageStats();
            result.setTotalRequests(getLongValue(stats, "totalRequests"));
            result.setSuccessfulRequests(getLongValue(stats, "successfulRequests"));
            result.setFailedRequests(getLongValue(stats, "failedRequests"));
            result.setAverageResponseTime(getDoubleValue(stats, "averageResponseTime"));
            result.setTotalTokenUsage(getLongValue(stats, "totalTokenUsage"));
            result.setTodayRequests(getLongValue(todayStats, "todayRequests"));
            result.setTodayTokenUsage(getLongValue(todayStats, "todayTokenUsage"));
            result.setStatisticsTime(LocalDateTime.now());
            
            return result;
        } catch (Exception e) {
            log.error("获取使用统计失败", e);
            return new ApiUsageStats();
        }
    }
    
    @Override
    public List<Map<String, Object>> getRequestTypeStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return apiUsageMapper.getRequestTypeStatistics(startTime, endTime);
        } catch (Exception e) {
            log.error("获取请求类型统计失败", e);
            return List.of();
        }
    }
    
    @Override
    public int cleanupOldRecords(LocalDateTime beforeTime) {
        try {
            int deletedCount = apiUsageMapper.cleanupOldRecords(beforeTime);
            log.info("清理过期API调用记录: count={}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            return 0;
        }
    }
    
    @Override
    public List<DeepSeekApiUsage> getRecentApiCalls(int limit) {
        try {
            QueryWrapper<DeepSeekApiUsage> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("created_at");
            
            Page<DeepSeekApiUsage> page = new Page<>(1, limit);
            Page<DeepSeekApiUsage> result = apiUsageMapper.selectPage(page, queryWrapper);
            
            return result.getRecords();
        } catch (Exception e) {
            log.error("获取最近API调用记录失败", e);
            return List.of();
        }
    }
    
    /**
     * 安全获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}