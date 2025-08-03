package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotech.events.config.DynamicSystemConfig;
import com.hotech.events.entity.Timeline;
import com.hotech.events.entity.TimelineCreationCache;
import com.hotech.events.mapper.TimelineCreationCacheMapper;
import com.hotech.events.mapper.TimelineMapper;
import com.hotech.events.service.TimelineDuplicationDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时间线重复检测服务实现类
 */
@Slf4j
@Service
public class TimelineDuplicationDetectionServiceImpl implements TimelineDuplicationDetectionService {
    
    @Autowired
    private TimelineMapper timelineMapper;
    
    @Autowired
    private TimelineCreationCacheMapper cacheMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DynamicSystemConfig dynamicSystemConfig;
    
    @Override
    public Timeline detectDuplicateByNameAndTimeRange(String name, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("基于名称和时间范围检测重复: name={}, startTime={}, endTime={}", name, startTime, endTime);
        
        if (!StringUtils.hasText(name)) {
            return null;
        }
        
        try {
            // 首先检查缓存
            TimelineCreationCache cacheRecord = cacheMapper.findByNameAndTimeRange(name, startTime, endTime);
            if (cacheRecord != null && cacheRecord.getTimelineId() != null) {
                Timeline timeline = timelineMapper.selectById(cacheRecord.getTimelineId());
                if (timeline != null) {
                    log.info("从缓存中发现重复时间线: id={}, name={}", timeline.getId(), timeline.getName());
                    return timeline;
                }
            }
            
            // 检查数据库中的时间线
            LambdaQueryWrapper<Timeline> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Timeline::getName, name);
            
            if (startTime != null) {
                wrapper.eq(Timeline::getStartTime, startTime);
            }
            
            if (endTime != null) {
                wrapper.eq(Timeline::getEndTime, endTime);
            }
            
            wrapper.orderByDesc(Timeline::getCreatedAt);
            
            Timeline existingTimeline = timelineMapper.selectOne(wrapper);
            if (existingTimeline != null) {
                log.info("发现重复时间线: id={}, name={}, startTime={}, endTime={}", 
                        existingTimeline.getId(), existingTimeline.getName(), 
                        existingTimeline.getStartTime(), existingTimeline.getEndTime());
            }
            
            return existingTimeline;
            
        } catch (Exception e) {
            log.error("基于名称和时间范围检测重复失败", e);
            return null;
        }
    }
    
    @Override
    public Timeline detectDuplicateByRequestFingerprint(String fingerprint) {
        log.info("基于请求指纹检测重复: fingerprint={}", fingerprint);
        
        if (!StringUtils.hasText(fingerprint)) {
            return null;
        }
        
        try {
            TimelineCreationCache cacheRecord = cacheMapper.findByRequestFingerprint(fingerprint);
            if (cacheRecord != null && cacheRecord.getTimelineId() != null) {
                Timeline timeline = timelineMapper.selectById(cacheRecord.getTimelineId());
                if (timeline != null) {
                    log.info("基于请求指纹发现重复时间线: id={}, name={}, fingerprint={}", 
                            timeline.getId(), timeline.getName(), fingerprint);
                    return timeline;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("基于请求指纹检测重复失败", e);
            return null;
        }
    }
    
    @Override
    public List<Timeline> detectRecentDuplicates(String userId, Duration timeWindow) {
        log.info("基于用户和时间窗口检测重复: userId={}, timeWindow={}", userId, timeWindow);
        
        if (!StringUtils.hasText(userId) || timeWindow == null) {
            return Collections.emptyList();
        }
        
        try {
            int windowMinutes = (int) timeWindow.toMinutes();
            List<TimelineCreationCache> cacheRecords = cacheMapper.findRecentByUserId(userId, windowMinutes);
            
            List<Timeline> timelines = new ArrayList<>();
            for (TimelineCreationCache cache : cacheRecords) {
                if (cache.getTimelineId() != null) {
                    Timeline timeline = timelineMapper.selectById(cache.getTimelineId());
                    if (timeline != null) {
                        timelines.add(timeline);
                    }
                }
            }
            
            log.info("发现用户最近的时间线: userId={}, count={}", userId, timelines.size());
            return timelines;
            
        } catch (Exception e) {
            log.error("基于用户和时间窗口检测重复失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public String generateRequestFingerprint(String name, String description, List<Long> regionIds,
                                           LocalDateTime startTime, LocalDateTime endTime, String userId) {
        try {
            // 构建指纹数据
            StringBuilder fingerprintData = new StringBuilder();
            fingerprintData.append("name:").append(name != null ? name.trim() : "");
            fingerprintData.append("|description:").append(description != null ? description.trim() : "");
            fingerprintData.append("|regionIds:");
            
            if (regionIds != null && !regionIds.isEmpty()) {
                // 对地区ID进行排序以确保一致性
                List<Long> sortedRegionIds = regionIds.stream().sorted().collect(Collectors.toList());
                fingerprintData.append(sortedRegionIds.toString());
            }
            
            fingerprintData.append("|startTime:").append(startTime != null ? startTime.toString() : "");
            fingerprintData.append("|endTime:").append(endTime != null ? endTime.toString() : "");
            fingerprintData.append("|userId:").append(userId != null ? userId : "");
            
            // 生成MD5哈希
            String fingerprint = DigestUtils.md5DigestAsHex(fingerprintData.toString().getBytes());
            
            log.debug("生成请求指纹: data={}, fingerprint={}", fingerprintData.toString(), fingerprint);
            return fingerprint;
            
        } catch (Exception e) {
            log.error("生成请求指纹失败", e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public TimelineCreationCache createCacheRecord(String fingerprint, String userId, String name, String description,
                                                  List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("创建缓存记录: fingerprint={}, userId={}, name={}", fingerprint, userId, name);
        
        try {
            TimelineCreationCache cache = new TimelineCreationCache();
            cache.setRequestFingerprint(fingerprint);
            cache.setUserId(userId);
            cache.setTimelineName(name);
            cache.setStartTime(startTime);
            cache.setEndTime(endTime);
            cache.setStatus(TimelineCreationCache.Status.CREATING);
            
            // 将地区ID列表转换为JSON字符串
            if (regionIds != null && !regionIds.isEmpty()) {
                cache.setRegionIds(objectMapper.writeValueAsString(regionIds));
            }
            
            LocalDateTime now = LocalDateTime.now();
            cache.setCreatedAt(now);
            cache.setUpdatedAt(now);
            cache.setExpiresAt(now.plusMinutes(dynamicSystemConfig.getDuplicationDetection().getFingerprintTtlMinutes()));
            
            cacheMapper.insert(cache);
            
            log.info("缓存记录创建成功: id={}, fingerprint={}", cache.getId(), fingerprint);
            return cache;
            
        } catch (Exception e) {
            log.error("创建缓存记录失败", e);
            throw new RuntimeException("创建缓存记录失败", e);
        }
    }
    
    @Override
    @Transactional
    public void updateCacheStatus(Long cacheId, String status, Long timelineId) {
        log.info("更新缓存记录状态: cacheId={}, status={}, timelineId={}", cacheId, status, timelineId);
        
        try {
            // 如果没有提供cacheId，尝试通过timelineId查找
            if (cacheId == null && timelineId != null) {
                TimelineCreationCache cache = cacheMapper.findByTimelineId(timelineId);
                if (cache != null) {
                    cacheId = cache.getId();
                    log.info("通过timelineId找到缓存记录: timelineId={}, cacheId={}", timelineId, cacheId);
                } else {
                    log.warn("未找到timelineId对应的缓存记录: timelineId={}", timelineId);
                    return;
                }
            }
            
            if (cacheId != null) {
                int updated = cacheMapper.updateStatus(cacheId, status, timelineId);
                if (updated > 0) {
                    log.info("缓存记录状态更新成功: cacheId={}, status={}", cacheId, status);
                } else {
                    log.warn("缓存记录状态更新失败，记录不存在: cacheId={}", cacheId);
                }
            }
        } catch (Exception e) {
            log.error("更新缓存记录状态失败", e);
            // 不抛出异常，避免影响主流程
            log.warn("缓存状态更新失败，但不影响主流程继续执行");
        }
    }
    
    @Override
    @Transactional
    public int cleanExpiredCache() {
        log.info("清理过期的缓存记录");
        
        try {
            int cleaned = cacheMapper.cleanExpiredRecords();
            log.info("清理过期缓存记录完成: count={}", cleaned);
            return cleaned;
        } catch (Exception e) {
            log.error("清理过期缓存记录失败", e);
            return 0;
        }
    }
    
    /**
     * 基于名称相似度检测重复
     */
    private Timeline detectDuplicateBySimilarity(String name, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("基于名称相似度检测重复: name={}, startTime={}, endTime={}", name, startTime, endTime);
        
        if (!StringUtils.hasText(name)) {
            return null;
        }
        
        try {
            // 获取相似时间范围内的时间线
            LambdaQueryWrapper<Timeline> wrapper = new LambdaQueryWrapper<>();
            
            if (startTime != null) {
                wrapper.ge(Timeline::getStartTime, startTime.minusDays(1));
                wrapper.le(Timeline::getStartTime, startTime.plusDays(1));
            }
            
            if (endTime != null) {
                wrapper.ge(Timeline::getEndTime, endTime.minusDays(1));
                wrapper.le(Timeline::getEndTime, endTime.plusDays(1));
            }
            
            wrapper.orderByDesc(Timeline::getCreatedAt);
            wrapper.last("LIMIT 100"); // 限制查询数量
            
            List<Timeline> candidates = timelineMapper.selectList(wrapper);
            
            double threshold = dynamicSystemConfig.getDuplicationDetection().getNameSimilarityThreshold();
            
            for (Timeline candidate : candidates) {
                if (candidate.getName() != null) {
                    double similarity = calculateStringSimilarity(name, candidate.getName());
                    if (similarity >= threshold) {
                        log.info("发现相似名称的时间线: id={}, name={}, similarity={}", 
                                candidate.getId(), candidate.getName(), similarity);
                        return candidate;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("基于名称相似度检测重复失败", e);
            return null;
        }
    }
    
    /**
     * 计算字符串相似度（使用编辑距离算法）
     */
    private double calculateStringSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        int editDistance = calculateEditDistance(str1, str2);
        return 1.0 - (double) editDistance / maxLength;
    }
    
    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        
        return dp[m][n];
    }

    @Override
    public DuplicationCheckResult checkDuplication(String name, String description, List<Long> regionIds,
                                                  LocalDateTime startTime, LocalDateTime endTime, String userId) {
        log.info("执行重复检测: name={}, userId={}, regionIds={}, startTime={}, endTime={}", 
                name, userId, regionIds, startTime, endTime);
        
        try {
            // 1. 生成请求指纹
            String fingerprint = generateRequestFingerprint(name, description, regionIds, startTime, endTime, userId);
            if (fingerprint == null) {
                log.warn("生成请求指纹失败，跳过重复检测");
                return new DuplicationCheckResult(false, null, null, "指纹生成失败");
            }
            
            // 2. 基于请求指纹检测重复
            Timeline duplicateByFingerprint = detectDuplicateByRequestFingerprint(fingerprint);
            if (duplicateByFingerprint != null) {
                TimelineCreationCache cache = cacheMapper.findByRequestFingerprint(fingerprint);
                return new DuplicationCheckResult(true, duplicateByFingerprint, cache, "相同请求指纹");
            }
            
            // 3. 基于名称和时间范围检测重复
            Timeline duplicateByNameAndTime = detectDuplicateByNameAndTimeRange(name, startTime, endTime);
            if (duplicateByNameAndTime != null) {
                return new DuplicationCheckResult(true, duplicateByNameAndTime, null, "相同名称和时间范围");
            }
            
            // 3.5. 基于名称相似度检测重复（如果启用严格模式）
            if (dynamicSystemConfig.getDuplicationDetection().isStrictMode()) {
                Timeline duplicateBySimilarity = detectDuplicateBySimilarity(name, startTime, endTime);
                if (duplicateBySimilarity != null) {
                    return new DuplicationCheckResult(true, duplicateBySimilarity, null, "相似名称检测");
                }
            }
            
            // 4. 基于用户和时间窗口检测最近的重复
            List<Timeline> recentDuplicates = detectRecentDuplicates(userId, Duration.ofMinutes(dynamicSystemConfig.getDuplicationDetection().getTimeWindowMinutes()));
            for (Timeline recent : recentDuplicates) {
                if (name.equals(recent.getName())) {
                    return new DuplicationCheckResult(true, recent, null, "用户最近创建了相同名称的时间线");
                }
            }
            
            // 5. 没有发现重复，创建缓存记录
            TimelineCreationCache newCache = createCacheRecord(fingerprint, userId, name, description, 
                                                             regionIds, startTime, endTime);
            
            log.info("重复检测完成，未发现重复: fingerprint={}", fingerprint);
            return new DuplicationCheckResult(false, null, newCache, "未发现重复");
            
        } catch (Exception e) {
            log.error("重复检测失败", e);
            return new DuplicationCheckResult(false, null, null, "检测失败: " + e.getMessage());
        }
    }
}