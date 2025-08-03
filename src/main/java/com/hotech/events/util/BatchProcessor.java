package com.hotech.events.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 批处理工具类
 */
@Slf4j
public class BatchProcessor {
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    /**
     * 批量处理数据
     * 
     * @param items 待处理的数据列表
     * @param batchSize 批次大小
     * @param processor 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 处理结果列表
     */
    public static <T, R> List<R> processBatch(List<T> items, int batchSize, Function<List<T>, List<R>> processor) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<R> results = new ArrayList<>();
        
        // 分批处理
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, endIndex);
            
            try {
                List<R> batchResults = processor.apply(batch);
                if (batchResults != null) {
                    results.addAll(batchResults);
                }
            } catch (Exception e) {
                log.error("批处理失败: batch={}-{}", i, endIndex, e);
            }
        }
        
        return results;
    }
    
    /**
     * 异步批量处理数据
     * 
     * @param items 待处理的数据列表
     * @param batchSize 批次大小
     * @param processor 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 异步处理结果
     */
    public static <T, R> CompletableFuture<List<R>> processBatchAsync(List<T> items, int batchSize, Function<List<T>, List<R>> processor) {
        return CompletableFuture.supplyAsync(() -> processBatch(items, batchSize, processor), executor);
    }
    
    /**
     * 并行批量处理数据
     * 
     * @param items 待处理的数据列表
     * @param batchSize 批次大小
     * @param processor 处理函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 处理结果列表
     */
    public static <T, R> CompletableFuture<List<R>> processBatchParallel(List<T> items, int batchSize, Function<List<T>, List<R>> processor) {
        if (items == null || items.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        
        List<CompletableFuture<List<R>>> futures = new ArrayList<>();
        
        // 分批并行处理
        for (int i = 0; i < items.size(); i += batchSize) {
            final int startIndex = i;
            final int endIndex = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(startIndex, endIndex);
            
            CompletableFuture<List<R>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processor.apply(batch);
                } catch (Exception e) {
                    log.error("并行批处理失败: batch={}-{}", startIndex, endIndex, e);
                    return new ArrayList<>();
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有批次完成并合并结果
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<R> results = new ArrayList<>();
                    for (CompletableFuture<List<R>> future : futures) {
                        try {
                            List<R> batchResults = future.get();
                            if (batchResults != null) {
                                results.addAll(batchResults);
                            }
                        } catch (Exception e) {
                            log.error("获取并行批处理结果失败", e);
                        }
                    }
                    return results;
                });
    }
}