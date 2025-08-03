package com.hotech.events.service.impl;

import com.hotech.events.config.TimelineEnhancementConfig;
import com.hotech.events.dto.EventData;
import com.hotech.events.model.TimeSegment;
import com.hotech.events.model.TimelineGenerateRequest;
import com.hotech.events.service.ConcurrentApiCallManager;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 并发API调用管理器实现
 * 优化批量API调用的并发性能
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ConcurrentApiCallManagerImpl implements ConcurrentApiCallManager {

    @Autowired
    private TimelineEnhancementConfig config;

    @Autowired(required = false)
    private TimelinePerformanceMonitoringService performanceMonitoringService;

    // 线程池配置
    private ThreadPoolExecutor executorService;
    private int concurrency = 5; // 默认并发度
    private long timeoutMs = 30000; // 默认超时30秒

    // 统计信息
    private final AtomicLong totalApiCalls = new AtomicLong(0);
    private final AtomicLong successfulApiCalls = new AtomicLong(0);
    private final AtomicLong failedApiCalls = new AtomicLong(0);
    private final AtomicLong timeoutApiCalls = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicInteger activeApiCalls = new AtomicInteger(0);

    // 性能监控
    private final Map<String, Long> segmentExecutionTimes = new ConcurrentHashMap<>();
    private final Queue<Long> recentExecutionTimes = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECENT_TIMES = 100;

    @PostConstruct
    public void init() {
        // 从配置中获取并发度设置
        if (config.getApi() != null) {
            // 优先使用火山引擎配置，如果不可用则使用DeepSeek配置
            if (config.getApi().getVolcengine() != null) {
                this.concurrency = config.getApi().getVolcengine().getConcurrentThreads();
                this.timeoutMs = config.getApi().getVolcengine().getTimeout();
            } else if (config.getApi().getDeepseek() != null) {
                this.concurrency = config.getApi().getDeepseek().getConcurrentThreads();
                this.timeoutMs = config.getApi().getDeepseek().getTimeout();
            }
        }

        initializeThreadPool();
        log.info("并发API调用管理器初始化完成，并发度: {}, 超时时间: {}ms", concurrency, timeoutMs);
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("并发API调用管理器已关闭");
    }

    @Override
    public CompletableFuture<List<EventData>> executeConcurrentApiCalls(List<TimeSegment> segments,
            TimelineGenerateRequest request) {
        long startTime = System.currentTimeMillis();
        final String monitoringSessionId = performanceMonitoringService != null
                ? performanceMonitoringService.startApiCallMonitoring("CONCURRENT_BATCH", segments.size())
                : null;

        log.info("开始并发执行{}个时间段的API调用", segments.size());

        // 创建并发任务列表
        List<CompletableFuture<List<EventData>>> futures = segments.stream()
                .map(segment -> executeAsyncApiCall(segment, request))
                .collect(Collectors.toList());

        // 处理批量结果
        return processBatchResults(futures)
                .whenComplete((result, throwable) -> {
                    long executionTime = System.currentTimeMillis() - startTime;
                    totalExecutionTime.addAndGet(executionTime);

                    // 记录执行时间
                    recordExecutionTime(executionTime);

                    if (throwable == null) {
                        successfulApiCalls.incrementAndGet();
                        if (performanceMonitoringService != null && monitoringSessionId != null) {
                            performanceMonitoringService.endApiCallMonitoring(monitoringSessionId, true,
                                    result.size(), calculateTotalTokens(result));
                        }
                        log.info("并发API调用完成，耗时: {}ms, 获取事件数: {}", executionTime, result.size());
                    } else {
                        failedApiCalls.incrementAndGet();
                        if (performanceMonitoringService != null && monitoringSessionId != null) {
                            performanceMonitoringService.endApiCallMonitoring(monitoringSessionId, false, 0, 0);
                        }
                        log.error("并发API调用失败，耗时: {}ms", executionTime, throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<List<EventData>> executeAsyncApiCall(TimeSegment segment,
            TimelineGenerateRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String segmentId = segment.getSegmentId();
            activeApiCalls.incrementAndGet();
            totalApiCalls.incrementAndGet();

            try {
                log.debug("开始执行时间段API调用，段ID: {}, 时间范围: {} - {}",
                        segmentId, segment.getStartTime(), segment.getEndTime());

                // 模拟API调用（在实际实现中应该调用真实的API服务）
                List<EventData> events = simulateApiCall(segment, request);

                long executionTime = System.currentTimeMillis() - startTime;
                segmentExecutionTimes.put(segmentId, executionTime);

                log.debug("时间段API调用完成，段ID: {}, 耗时: {}ms, 事件数: {}",
                        segmentId, executionTime, events.size());

                return events;

            } catch (Exception e) {
                log.error("时间段API调用失败，段ID: {}", segmentId, e);
                throw new RuntimeException("API调用失败: " + e.getMessage(), e);
            } finally {
                activeApiCalls.decrementAndGet();
            }
        }, executorService)
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        timeoutApiCalls.incrementAndGet();
                        log.warn("API调用超时，段ID: {}, 超时时间: {}ms", segment.getSegmentId(), timeoutMs);
                    }
                    return new ArrayList<>(); // 返回空列表而不是抛出异常
                });
    }

    @Override
    public CompletableFuture<List<EventData>> processBatchResults(List<CompletableFuture<List<EventData>>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<EventData> allEvents = new ArrayList<>();

                    for (CompletableFuture<List<EventData>> future : futures) {
                        try {
                            List<EventData> events = future.get();
                            if (events != null && !events.isEmpty()) {
                                allEvents.addAll(events);
                            }
                        } catch (Exception e) {
                            log.warn("获取API调用结果失败", e);
                            // 继续处理其他结果，不中断整个流程
                        }
                    }

                    // 按时间排序并去重
                    return deduplicateAndSort(allEvents);
                });
    }

    @Override
    public void setConcurrency(int concurrency) {
        if (concurrency <= 0) {
            throw new IllegalArgumentException("并发度必须大于0");
        }

        this.concurrency = concurrency;

        // 重新初始化线程池
        if (executorService != null) {
            executorService.shutdown();
        }
        initializeThreadPool();

        log.info("并发度已更新为: {}", concurrency);
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    @Override
    public void setTimeout(long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }

        this.timeoutMs = timeoutMs;
        log.info("超时时间已更新为: {}ms", timeoutMs);
    }

    @Override
    public Map<String, Object> getApiCallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 基本统计
        stats.put("totalApiCalls", totalApiCalls.get());
        stats.put("successfulApiCalls", successfulApiCalls.get());
        stats.put("failedApiCalls", failedApiCalls.get());
        stats.put("timeoutApiCalls", timeoutApiCalls.get());
        stats.put("activeApiCalls", activeApiCalls.get());

        // 成功率统计
        long total = totalApiCalls.get();
        if (total > 0) {
            stats.put("successRate", (double) successfulApiCalls.get() / total);
            stats.put("failureRate", (double) failedApiCalls.get() / total);
            stats.put("timeoutRate", (double) timeoutApiCalls.get() / total);
        } else {
            stats.put("successRate", 0.0);
            stats.put("failureRate", 0.0);
            stats.put("timeoutRate", 0.0);
        }

        // 性能统计
        stats.put("totalExecutionTime", totalExecutionTime.get());
        stats.put("averageExecutionTime", total > 0 ? (double) totalExecutionTime.get() / total : 0.0);

        // 最近执行时间统计
        if (!recentExecutionTimes.isEmpty()) {
            List<Long> recentTimes = new ArrayList<>(recentExecutionTimes);
            stats.put("recentAverageTime", recentTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
            stats.put("recentMinTime", recentTimes.stream().mapToLong(Long::longValue).min().orElse(0));
            stats.put("recentMaxTime", recentTimes.stream().mapToLong(Long::longValue).max().orElse(0));
        }

        // 线程池统计
        if (executorService != null) {
            stats.put("threadPoolSize", executorService.getPoolSize());
            stats.put("activeThreads", executorService.getActiveCount());
            stats.put("queueSize", executorService.getQueue().size());
            stats.put("completedTasks", executorService.getCompletedTaskCount());
        }

        // 配置信息
        stats.put("concurrency", concurrency);
        stats.put("timeoutMs", timeoutMs);

        return stats;
    }

    @Override
    public void resetStatistics() {
        totalApiCalls.set(0);
        successfulApiCalls.set(0);
        failedApiCalls.set(0);
        timeoutApiCalls.set(0);
        totalExecutionTime.set(0);
        segmentExecutionTimes.clear();
        recentExecutionTimes.clear();

        log.info("API调用统计信息已重置");
    }

    /**
     * 初始化线程池
     */
    private void initializeThreadPool() {
        executorService = new ThreadPoolExecutor(
                concurrency, // 核心线程数
                concurrency * 2, // 最大线程数
                60L, TimeUnit.SECONDS, // 空闲线程存活时间
                new LinkedBlockingQueue<>(concurrency * 10), // 工作队列
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "concurrent-api-call-" + threadNumber.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
        );

        log.debug("线程池初始化完成，核心线程数: {}, 最大线程数: {}", concurrency, concurrency * 2);
    }

    /**
     * 模拟API调用（在实际实现中应该调用真实的API服务）
     */
    private List<EventData> simulateApiCall(TimeSegment segment, TimelineGenerateRequest request) {
        // 模拟API调用延迟
        try {
            Thread.sleep(100 + new Random().nextInt(200)); // 100-300ms的随机延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API调用被中断", e);
        }

        List<EventData> events = new ArrayList<>();

        // 创建模拟事件
        for (int i = 0; i < 2; i++) {
            EventData event = new EventData();
            event.setId("concurrent-event-" + segment.getSegmentId() + "-" + i);
            event.setTitle("并发API调用事件 " + (i + 1));
            event.setDescription("这是通过并发API调用获取的事件，时间段: " + segment.getSegmentId());
            event.setEventTime(segment.getStartTime().plusHours(i * 12));
            event.setSource("并发API");
            event.setLatitude(39.9042 + i * 0.01);
            event.setLongitude(116.4074 + i * 0.01);
            event.setLocation("北京市区域" + (i + 1));

            events.add(event);
        }

        return events;
    }

    /**
     * 去重并排序事件列表
     */
    private List<EventData> deduplicateAndSort(List<EventData> events) {
        // 使用LinkedHashMap保持插入顺序的同时去重
        Map<String, EventData> uniqueEvents = new LinkedHashMap<>();

        for (EventData event : events) {
            String key = generateEventKey(event);
            if (!uniqueEvents.containsKey(key)) {
                uniqueEvents.put(key, event);
            }
        }

        // 按时间排序
        return uniqueEvents.values().stream()
                .sorted(Comparator.comparing(EventData::getEventTime))
                .collect(Collectors.toList());
    }

    /**
     * 生成事件唯一键用于去重
     */
    private String generateEventKey(EventData event) {
        return String.format("%s_%s_%s",
                event.getTitle() != null ? event.getTitle() : "",
                event.getEventTime() != null ? event.getEventTime().toString() : "",
                event.getLocation() != null ? event.getLocation() : "");
    }

    /**
     * 记录执行时间
     */
    private void recordExecutionTime(long executionTime) {
        recentExecutionTimes.offer(executionTime);

        // 保持队列大小不超过限制
        while (recentExecutionTimes.size() > MAX_RECENT_TIMES) {
            recentExecutionTimes.poll();
        }
    }

    /**
     * 计算总Token数（模拟）
     */
    private int calculateTotalTokens(List<EventData> events) {
        // 简单估算：每个事件大约消耗50个token
        return events.size() * 50;
    }
}