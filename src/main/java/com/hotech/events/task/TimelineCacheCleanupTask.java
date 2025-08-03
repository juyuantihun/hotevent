package com.hotech.events.task;

import com.hotech.events.service.TimelineDuplicationDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 时间线缓存清理定时任务
 * 定期清理过期的时间线创建缓存记录
 */
@Slf4j
@Component
public class TimelineCacheCleanupTask {
    
    @Autowired
    private TimelineDuplicationDetectionService duplicationDetectionService;
    
    /**
     * 清理过期的缓存记录
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void cleanupExpiredCache() {
        log.info("开始清理过期的时间线创建缓存记录");
        
        try {
            int cleanedCount = duplicationDetectionService.cleanExpiredCache();
            
            if (cleanedCount > 0) {
                log.info("清理过期缓存记录完成: 清理了 {} 条记录", cleanedCount);
            } else {
                log.debug("没有过期的缓存记录需要清理");
            }
            
        } catch (Exception e) {
            log.error("清理过期缓存记录失败", e);
        }
    }
}