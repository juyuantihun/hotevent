package com.hotech.events.service;

import com.hotech.events.entity.Timeline;
import com.hotech.events.entity.TimelineCreationCache;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间线重复检测服务接口
 */
public interface TimelineDuplicationDetectionService {
    
    /**
     * 基于名称和时间范围的重复检测
     * @param name 时间线名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 已存在的时间线，如果没有重复则返回null
     */
    Timeline detectDuplicateByNameAndTimeRange(String name, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 基于请求指纹的重复检测
     * @param fingerprint 请求指纹
     * @return 已存在的时间线，如果没有重复则返回null
     */
    Timeline detectDuplicateByRequestFingerprint(String fingerprint);
    
    /**
     * 基于用户和时间窗口的重复检测
     * @param userId 用户ID
     * @param timeWindow 时间窗口
     * @return 最近的重复时间线列表
     */
    List<Timeline> detectRecentDuplicates(String userId, Duration timeWindow);
    
    /**
     * 生成请求指纹
     * @param name 时间线名称
     * @param description 时间线描述
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID
     * @return 请求指纹
     */
    String generateRequestFingerprint(String name, String description, List<Long> regionIds, 
                                    LocalDateTime startTime, LocalDateTime endTime, String userId);
    
    /**
     * 创建缓存记录
     * @param fingerprint 请求指纹
     * @param userId 用户ID
     * @param name 时间线名称
     * @param description 时间线描述
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 缓存记录
     */
    TimelineCreationCache createCacheRecord(String fingerprint, String userId, String name, String description,
                                          List<Long> regionIds, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新缓存记录状态
     * @param cacheId 缓存记录ID
     * @param status 新状态
     * @param timelineId 时间线ID
     */
    void updateCacheStatus(Long cacheId, String status, Long timelineId);
    
    /**
     * 清理过期的缓存记录
     * @return 清理的记录数
     */
    int cleanExpiredCache();
    
    /**
     * 检查是否为重复提交
     * @param name 时间线名称
     * @param description 时间线描述
     * @param regionIds 地区ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID
     * @return 重复检测结果，包含是否重复和相关的时间线信息
     */
    DuplicationCheckResult checkDuplication(String name, String description, List<Long> regionIds,
                                           LocalDateTime startTime, LocalDateTime endTime, String userId);
    
    /**
     * 重复检测结果
     */
    class DuplicationCheckResult {
        private boolean isDuplicate;
        private Timeline existingTimeline;
        private TimelineCreationCache cacheRecord;
        private String reason;
        
        public DuplicationCheckResult(boolean isDuplicate, Timeline existingTimeline, 
                                    TimelineCreationCache cacheRecord, String reason) {
            this.isDuplicate = isDuplicate;
            this.existingTimeline = existingTimeline;
            this.cacheRecord = cacheRecord;
            this.reason = reason;
        }
        
        // Getters
        public boolean isDuplicate() { return isDuplicate; }
        public Timeline getExistingTimeline() { return existingTimeline; }
        public TimelineCreationCache getCacheRecord() { return cacheRecord; }
        public String getReason() { return reason; }
    }
}