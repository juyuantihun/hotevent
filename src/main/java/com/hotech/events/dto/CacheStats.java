package com.hotech.events.dto;

import lombok.Data;

/**
 * 缓存统计DTO
 */
@Data
public class CacheStats {
    
    /**
     * 缓存大小
     */
    private Integer cacheSize;
    
    /**
     * 缓存命中次数
     */
    private Long hitCount;
    
    /**
     * 缓存未命中次数
     */
    private Long missCount;
    
    /**
     * 缓存命中率
     */
    public Double getHitRate() {
        long total = hitCount + missCount;
        if (total == 0) {
            return 0.0;
        }
        return (double) hitCount / total * 100;
    }
}