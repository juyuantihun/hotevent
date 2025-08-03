package com.hotech.events.service;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.TimelineGenerateRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 批处理服务接口
 * 提供高效的批量数据处理能力
 */
public interface BatchProcessingService {
    
    /**
     * 批量获取事件数据
     * 
     * @param requests 时间线生成请求列表
     * @param batchSize 批处理大小
     * @return 事件数据列表
     */
    List<EventData> batchFetchEvents(List<TimelineGenerateRequest> requests, int batchSize);
    
    /**
     * 异步批量获取事件数据
     * 
     * @param requests 时间线生成请求列表
     * @param batchSize 批处理大小
     * @return 异步事件数据列表
     */
    CompletableFuture<List<EventData>> batchFetchEventsAsync(List<TimelineGenerateRequest> requests, int batchSize);
    
    /**
     * 批量验证事件
     * 
     * @param events 事件数据列表
     * @param batchSize 批处理大小
     * @return 验证结果列表
     */
    List<EventValidationResult> batchValidateEvents(List<EventData> events, int batchSize);
    
    /**
     * 异步批量验证事件
     * 
     * @param events 事件数据列表
     * @param batchSize 批处理大小
     * @return 异步验证结果列表
     */
    CompletableFuture<List<EventValidationResult>> batchValidateEventsAsync(List<EventData> events, int batchSize);
    
    /**
     * 批量存储事件
     * 
     * @param events 事件数据列表
     * @param batchSize 批处理大小
     * @return 存储的事件ID列表
     */
    List<Long> batchStoreEvents(List<EventData> events, int batchSize);
    
    /**
     * 异步批量存储事件
     * 
     * @param events 事件数据列表
     * @param batchSize 批处理大小
     * @return 异步存储的事件ID列表
     */
    CompletableFuture<List<Long>> batchStoreEventsAsync(List<EventData> events, int batchSize);
    
    /**
     * 获取批处理统计信息
     * 
     * @return 批处理统计信息
     */
    BatchProcessingStats getBatchProcessingStats();
    
    /**
     * 批处理统计信息
     */
    class BatchProcessingStats {
        private long totalBatches;
        private long successfulBatches;
        private long failedBatches;
        private double averageProcessingTime;
        private double throughput; // 每秒处理的项目数
        private long totalItemsProcessed;
        
        // Getters and Setters
        public long getTotalBatches() { return totalBatches; }
        public void setTotalBatches(long totalBatches) { this.totalBatches = totalBatches; }
        
        public long getSuccessfulBatches() { return successfulBatches; }
        public void setSuccessfulBatches(long successfulBatches) { this.successfulBatches = successfulBatches; }
        
        public long getFailedBatches() { return failedBatches; }
        public void setFailedBatches(long failedBatches) { this.failedBatches = failedBatches; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
        
        public double getThroughput() { return throughput; }
        public void setThroughput(double throughput) { this.throughput = throughput; }
        
        public long getTotalItemsProcessed() { return totalItemsProcessed; }
        public void setTotalItemsProcessed(long totalItemsProcessed) { this.totalItemsProcessed = totalItemsProcessed; }
    }
}