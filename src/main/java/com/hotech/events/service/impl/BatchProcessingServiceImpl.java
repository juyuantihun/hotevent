package com.hotech.events.service.impl;

import com.hotech.events.dto.EventData;
import com.hotech.events.dto.EventValidationResult;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.service.BatchProcessingService;
import com.hotech.events.service.EnhancedDeepSeekService;
import com.hotech.events.service.EventStorageService;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 批处理服务实现类
 * 提供高效的批量数据处理能力
 */
@Slf4j
@Service
public class BatchProcessingServiceImpl implements BatchProcessingService {
    
    @Autowired
    private EnhancedDeepSeekService enhancedDeepSeekService;
    
    @Autowired
    private EventStorageService eventStorageService;
    
    @Autowired
    private SystemMonitoringService monitoringService;
    
    @Value("${app.deepseek.enhanced.batch-size:10}")
    private int defaultBatchSize;
    
    @Value("${app.deepseek.enhanced.async-thread-pool-size:10}")
    private int threadPoolSize;
    
    // 线程池
    private final ExecutorService executorService;
    
    // 统计信息
    private final AtomicLong totalBatches = new AtomicLong(0);
    private final AtomicLong successfulBatches = new AtomicLong(0);
    private final AtomicLong failedBatches = new AtomicLong(0);
    private final AtomicLong totalItemsProcessed = new AtomicLong(0);
    private volatile double totalProcessingTime = 0;
    
    public BatchProcessingServiceImpl() {
        this.executorService = Executors.newFixedThreadPool(10); // 默认线程池大小
    }
    
    @Override
    public List<EventData> batchFetchEvents(List<TimelineGenerateRequest> requests, int batchSize) {
        log.info("开始批量获取事件数据: requestCount={}, batchSize={}", requests.size(), batchSize);
        
        long startTime = System.currentTimeMillis();
        List<EventData> allEvents = new ArrayList<>();
        
        try {
            // 分批处理请求
            for (int i = 0; i < requests.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, requests.size());
                List<TimelineGenerateRequest> batch = requests.subList(i, endIndex);
                
                log.debug("处理批次 {}/{}: 大小={}", (i / batchSize) + 1, 
                         (requests.size() + batchSize - 1) / batchSize, batch.size());
                
                // 处理当前批次
                List<EventData> batchEvents = processBatchRequests(batch);
                allEvents.addAll(batchEvents);
                
                // 记录批次统计
                totalBatches.incrementAndGet();
                successfulBatches.incrementAndGet();
                totalItemsProcessed.addAndGet(batchEvents.size());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            updateProcessingTime(processingTime);
            
            // 记录性能指标
            monitoringService.recordPerformanceMetrics("BATCH_FETCH_EVENTS", 
                    processingTime, getMemoryUsage(), getCpuUsage());
            
            log.info("批量获取事件数据完成: totalEvents={}, processingTime={}ms", 
                    allEvents.size(), processingTime);
            
            return allEvents;
            
        } catch (Exception e) {
            failedBatches.incrementAndGet();
            log.error("批量获取事件数据失败", e);
            
            // 记录系统错误
            monitoringService.recordSystemError("BATCH_FETCH_EVENTS", "BATCH_PROCESSING_ERROR", 
                    e.getMessage(), getStackTrace(e));
            
            throw new RuntimeException("批量获取事件数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<List<EventData>> batchFetchEventsAsync(List<TimelineGenerateRequest> requests, int batchSize) {
        return CompletableFuture.supplyAsync(() -> batchFetchEvents(requests, batchSize), executorService);
    }
    
    @Override
    public List<EventValidationResult> batchValidateEvents(List<EventData> events, int batchSize) {
        log.info("开始批量验证事件: eventCount={}, batchSize={}", events.size(), batchSize);
        
        long startTime = System.currentTimeMillis();
        List<EventValidationResult> allResults = new ArrayList<>();
        
        try {
            // 分批处理事件
            for (int i = 0; i < events.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, events.size());
                List<EventData> batch = events.subList(i, endIndex);
                
                log.debug("验证批次 {}/{}: 大小={}", (i / batchSize) + 1, 
                         (events.size() + batchSize - 1) / batchSize, batch.size());
                
                // 验证当前批次
                List<EventValidationResult> batchResults = enhancedDeepSeekService.validateEvents(batch);
                allResults.addAll(batchResults);
                
                // 记录批次统计
                totalBatches.incrementAndGet();
                successfulBatches.incrementAndGet();
                totalItemsProcessed.addAndGet(batchResults.size());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            updateProcessingTime(processingTime);
            
            // 记录性能指标
            monitoringService.recordPerformanceMetrics("BATCH_VALIDATE_EVENTS", 
                    processingTime, getMemoryUsage(), getCpuUsage());
            
            log.info("批量验证事件完成: totalResults={}, processingTime={}ms", 
                    allResults.size(), processingTime);
            
            return allResults;
            
        } catch (Exception e) {
            failedBatches.incrementAndGet();
            log.error("批量验证事件失败", e);
            
            // 记录系统错误
            monitoringService.recordSystemError("BATCH_VALIDATE_EVENTS", "BATCH_VALIDATION_ERROR", 
                    e.getMessage(), getStackTrace(e));
            
            throw new RuntimeException("批量验证事件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<List<EventValidationResult>> batchValidateEventsAsync(List<EventData> events, int batchSize) {
        return CompletableFuture.supplyAsync(() -> batchValidateEvents(events, batchSize), executorService);
    }
    
    @Override
    public List<Long> batchStoreEvents(List<EventData> events, int batchSize) {
        log.info("开始批量存储事件: eventCount={}, batchSize={}", events.size(), batchSize);
        
        long startTime = System.currentTimeMillis();
        List<Long> allEventIds = new ArrayList<>();
        
        try {
            // 分批处理事件
            for (int i = 0; i < events.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, events.size());
                List<EventData> batch = events.subList(i, endIndex);
                
                log.debug("存储批次 {}/{}: 大小={}", (i / batchSize) + 1, 
                         (events.size() + batchSize - 1) / batchSize, batch.size());
                
                // 存储当前批次
                List<Long> batchEventIds = eventStorageService.storeEventsBatch(batch);
                allEventIds.addAll(batchEventIds);
                
                // 记录批次统计
                totalBatches.incrementAndGet();
                successfulBatches.incrementAndGet();
                totalItemsProcessed.addAndGet(batchEventIds.size());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            updateProcessingTime(processingTime);
            
            // 记录性能指标
            monitoringService.recordPerformanceMetrics("BATCH_STORE_EVENTS", 
                    processingTime, getMemoryUsage(), getCpuUsage());
            
            log.info("批量存储事件完成: totalEventIds={}, processingTime={}ms", 
                    allEventIds.size(), processingTime);
            
            return allEventIds;
            
        } catch (Exception e) {
            failedBatches.incrementAndGet();
            log.error("批量存储事件失败", e);
            
            // 记录系统错误
            monitoringService.recordSystemError("BATCH_STORE_EVENTS", "BATCH_STORAGE_ERROR", 
                    e.getMessage(), getStackTrace(e));
            
            throw new RuntimeException("批量存储事件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<List<Long>> batchStoreEventsAsync(List<EventData> events, int batchSize) {
        return CompletableFuture.supplyAsync(() -> batchStoreEvents(events, batchSize), executorService);
    }
    
    @Override
    public BatchProcessingStats getBatchProcessingStats() {
        BatchProcessingStats stats = new BatchProcessingStats();
        
        long totalBatchCount = totalBatches.get();
        long totalItems = totalItemsProcessed.get();
        
        stats.setTotalBatches(totalBatchCount);
        stats.setSuccessfulBatches(successfulBatches.get());
        stats.setFailedBatches(failedBatches.get());
        
        if (totalBatchCount > 0) {
            stats.setAverageProcessingTime(totalProcessingTime / totalBatchCount);
        }
        
        if (totalProcessingTime > 0) {
            stats.setThroughput(totalItems / (totalProcessingTime / 1000.0)); // 每秒处理项目数
        }
        
        stats.setTotalItemsProcessed(totalItems);
        
        return stats;
    }
    
    /**
     * 处理批次请求
     */
    private List<EventData> processBatchRequests(List<TimelineGenerateRequest> batch) {
        List<EventData> batchEvents = new ArrayList<>();
        
        for (TimelineGenerateRequest request : batch) {
            try {
                List<EventData> events = enhancedDeepSeekService.fetchEventsWithDynamicPrompt(request);
                batchEvents.addAll(events);
            } catch (Exception e) {
                log.error("处理单个请求失败: name={}", request.getName(), e);
                // 继续处理其他请求，不中断整个批次
            }
        }
        
        return batchEvents;
    }
    
    /**
     * 更新处理时间统计
     */
    private synchronized void updateProcessingTime(long processingTime) {
        totalProcessingTime += processingTime;
    }
    
    /**
     * 获取内存使用量
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) 
                    java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                    .getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Throwable throwable) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "无法获取堆栈跟踪: " + e.getMessage();
        }
    }
}