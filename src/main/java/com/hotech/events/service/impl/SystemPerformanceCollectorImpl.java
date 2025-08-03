package com.hotech.events.service.impl;

import com.hotech.events.config.TimelineEnhancementConfig;
import com.hotech.events.service.SystemPerformanceCollector;
import com.hotech.events.service.TimelinePerformanceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统性能指标收集器实现
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SystemPerformanceCollectorImpl implements SystemPerformanceCollector {

    @Autowired
    private TimelineEnhancementConfig config;

    @Autowired
    private TimelinePerformanceMonitoringService performanceMonitoringService;

    // JVM管理Bean
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final GarbageCollectorMXBean[] gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
            .toArray(new GarbageCollectorMXBean[0]);

    // 收集器状态
    private final AtomicBoolean isCollecting = new AtomicBoolean(false);
    private ScheduledExecutorService collectionExecutor;
    private long collectionStartTime;

    // 性能指标存储
    private final Map<String, Queue<MetricPoint>> metricsHistory = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Queue<Long>> timers = new ConcurrentHashMap<>();
    private final Map<String, Queue<Double>> histograms = new ConcurrentHashMap<>();

    // 性能阈值配置
    private final Map<String, PerformanceThreshold> thresholds = new ConcurrentHashMap<>();
    private final Queue<PerformanceAlert> alerts = new ConcurrentLinkedQueue<>();

    // 配置参数
    private int collectionInterval = 5; // 秒
    private int historyRetentionHours = 24;
    private int maxHistoryPoints = 1000;

    @PostConstruct
    public void init() {
        // 从配置中获取参数
        if (config.getMonitoring() != null) {
            this.collectionInterval = config.getMonitoring().getMetricsInterval();
            this.historyRetentionHours = config.getMonitoring().getHistoryRetentionHours();
        }

        // 初始化默认阈值
        initializeDefaultThresholds();

        log.info("系统性能收集器初始化完成，收集间隔: {}秒, 历史保留: {}小时",
                collectionInterval, historyRetentionHours);
    }

    @PreDestroy
    public void destroy() {
        stopCollection();
    }

    @Override
    public void startCollection() {
        if (isCollecting.compareAndSet(false, true)) {
            collectionStartTime = System.currentTimeMillis();

            collectionExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "performance-collector");
                t.setDaemon(true);
                return t;
            });

            // 定期收集性能指标
            collectionExecutor.scheduleWithFixedDelay(
                    this::collectAndStoreMetrics,
                    0,
                    collectionInterval,
                    TimeUnit.SECONDS);

            // 定期清理过期数据
            collectionExecutor.scheduleWithFixedDelay(
                    this::cleanupExpiredData,
                    1,
                    1,
                    TimeUnit.HOURS);

            log.info("系统性能指标收集已启动");
        }
    }

    @Override
    public void stopCollection() {
        if (isCollecting.compareAndSet(true, false)) {
            if (collectionExecutor != null) {
                collectionExecutor.shutdown();
                try {
                    if (!collectionExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        collectionExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    collectionExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            log.info("系统性能指标收集已停止");
        }
    }

    @Override
    public Map<String, Object> collectPerformanceSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();

        snapshot.put("timestamp", System.currentTimeMillis());
        snapshot.put("jvm", collectJvmMetrics());
        snapshot.put("system", collectSystemResourceMetrics());
        snapshot.put("application", collectApplicationMetrics());
        snapshot.put("database", collectDatabaseMetrics());
        snapshot.put("api", collectApiMetrics());

        return snapshot;
    }

    @Override
    public Map<String, Object> collectJvmMetrics() {
        Map<String, Object> jvmMetrics = new HashMap<>();

        // 内存使用情况
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", heapMemory.getUsed());
        memory.put("heapMax", heapMemory.getMax());
        memory.put("heapCommitted", heapMemory.getCommitted());
        memory.put("heapUsagePercent",
                heapMemory.getMax() > 0 ? (double) heapMemory.getUsed() / heapMemory.getMax() * 100 : 0);
        memory.put("nonHeapUsed", nonHeapMemory.getUsed());
        memory.put("nonHeapMax", nonHeapMemory.getMax());
        memory.put("nonHeapCommitted", nonHeapMemory.getCommitted());

        jvmMetrics.put("memory", memory);

        // 线程信息
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", threadBean.getThreadCount());
        threads.put("peakThreadCount", threadBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());

        jvmMetrics.put("threads", threads);

        // 垃圾回收信息
        List<Map<String, Object>> gcInfo = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Map<String, Object> gc = new HashMap<>();
            gc.put("name", gcBean.getName());
            gc.put("collectionCount", gcBean.getCollectionCount());
            gc.put("collectionTime", gcBean.getCollectionTime());
            gcInfo.add(gc);
        }
        jvmMetrics.put("garbageCollection", gcInfo);

        // 运行时信息
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("uptime", runtimeBean.getUptime());
        runtime.put("startTime", runtimeBean.getStartTime());
        runtime.put("vmName", runtimeBean.getVmName());
        runtime.put("vmVersion", runtimeBean.getVmVersion());

        jvmMetrics.put("runtime", runtime);

        return jvmMetrics;
    }

    @Override
    public Map<String, Object> collectSystemResourceMetrics() {
        Map<String, Object> systemMetrics = new HashMap<>();

        // CPU信息
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("availableProcessors", osBean.getAvailableProcessors());
        cpu.put("systemLoadAverage", osBean.getSystemLoadAverage());

        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            cpu.put("processCpuLoad", sunOsBean.getProcessCpuLoad() * 100);
            cpu.put("systemCpuLoad", sunOsBean.getSystemCpuLoad() * 100);
            cpu.put("processCpuTime", sunOsBean.getProcessCpuTime());
        }

        systemMetrics.put("cpu", cpu);

        // 操作系统信息
        Map<String, Object> os = new HashMap<>();
        os.put("name", osBean.getName());
        os.put("version", osBean.getVersion());
        os.put("arch", osBean.getArch());

        systemMetrics.put("operatingSystem", os);

        return systemMetrics;
    }

    @Override
    public Map<String, Object> collectApplicationMetrics() {
        Map<String, Object> appMetrics = new HashMap<>();

        // 从性能监控服务获取应用指标
        appMetrics.putAll(performanceMonitoringService.getPerformanceStatistics());

        // 添加自定义计数器
        Map<String, Object> customCounters = new HashMap<>();
        counters.forEach((name, counter) -> customCounters.put(name, counter.get()));
        appMetrics.put("customCounters", customCounters);

        // 添加计时器统计
        Map<String, Object> timerStats = new HashMap<>();
        timers.forEach((name, timerQueue) -> {
            if (!timerQueue.isEmpty()) {
                List<Long> values = new ArrayList<>(timerQueue);
                Map<String, Object> stats = new HashMap<>();
                stats.put("count", values.size());
                stats.put("average", values.stream().mapToLong(Long::longValue).average().orElse(0.0));
                stats.put("min", values.stream().mapToLong(Long::longValue).min().orElse(0));
                stats.put("max", values.stream().mapToLong(Long::longValue).max().orElse(0));
                timerStats.put(name, stats);
            }
        });
        appMetrics.put("timers", timerStats);

        return appMetrics;
    }

    @Override
    public Map<String, Object> collectDatabaseMetrics() {
        Map<String, Object> dbMetrics = new HashMap<>();

        // 这里可以集成数据库连接池监控
        // 例如HikariCP、Druid等的监控指标

        // 模拟数据库指标
        Map<String, Object> connectionPool = new HashMap<>();
        connectionPool.put("activeConnections", 5);
        connectionPool.put("idleConnections", 10);
        connectionPool.put("maxConnections", 20);
        connectionPool.put("connectionUtilization", 25.0);

        dbMetrics.put("connectionPool", connectionPool);

        return dbMetrics;
    }

    @Override
    public Map<String, Object> collectApiMetrics() {
        Map<String, Object> apiMetrics = new HashMap<>();

        // 从性能监控服务获取API指标
        Map<String, Object> performanceStats = performanceMonitoringService.getPerformanceStatistics();
        if (performanceStats.containsKey("apiCall")) {
            apiMetrics.putAll((Map<String, Object>) performanceStats.get("apiCall"));
        }

        return apiMetrics;
    }

    @Override
    public void recordCustomMetric(String metricName, double value, Map<String, String> tags) {
        MetricPoint point = new MetricPoint(System.currentTimeMillis(), value, tags);

        metricsHistory.computeIfAbsent(metricName, k -> new ConcurrentLinkedQueue<>()).offer(point);

        // 限制历史数据大小
        Queue<MetricPoint> history = metricsHistory.get(metricName);
        while (history.size() > maxHistoryPoints) {
            history.poll();
        }

        // 检查阈值
        checkThreshold(metricName, value);
    }

    @Override
    public void recordCounter(String counterName, long increment, Map<String, String> tags) {
        counters.computeIfAbsent(counterName, k -> new AtomicLong(0)).addAndGet(increment);

        // 记录到历史数据
        recordCustomMetric(counterName, increment, tags);
    }

    @Override
    public void recordTimer(String timerName, long duration, Map<String, String> tags) {
        Queue<Long> timerQueue = timers.computeIfAbsent(timerName, k -> new ConcurrentLinkedQueue<>());
        timerQueue.offer(duration);

        // 限制队列大小
        while (timerQueue.size() > 1000) {
            timerQueue.poll();
        }

        // 记录到历史数据
        recordCustomMetric(timerName, duration, tags);
    }

    @Override
    public void recordHistogram(String histogramName, double value, Map<String, String> tags) {
        Queue<Double> histogramQueue = histograms.computeIfAbsent(histogramName, k -> new ConcurrentLinkedQueue<>());
        histogramQueue.offer(value);

        // 限制队列大小
        while (histogramQueue.size() > 1000) {
            histogramQueue.poll();
        }

        // 记录到历史数据
        recordCustomMetric(histogramName, value, tags);
    }

    @Override
    public Map<String, Object> getHistoricalMetrics(long startTime, long endTime, String... metricNames) {
        Map<String, Object> result = new HashMap<>();

        for (String metricName : metricNames) {
            Queue<MetricPoint> history = metricsHistory.get(metricName);
            if (history != null) {
                List<MetricPoint> filteredPoints = history.stream()
                        .filter(point -> point.getTimestamp() >= startTime && point.getTimestamp() <= endTime)
                        .sorted(Comparator.comparing(MetricPoint::getTimestamp))
                        .toList();

                result.put(metricName, filteredPoints);
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getPerformanceTrend(String metricName, int timeRange) {
        Map<String, Object> trend = new HashMap<>();

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (timeRange * 3600 * 1000L); // 转换为毫秒

        Queue<MetricPoint> history = metricsHistory.get(metricName);
        if (history != null) {
            List<MetricPoint> points = history.stream()
                    .filter(point -> point.getTimestamp() >= startTime)
                    .sorted(Comparator.comparing(MetricPoint::getTimestamp))
                    .toList();

            if (!points.isEmpty()) {
                double[] values = points.stream().mapToDouble(MetricPoint::getValue).toArray();

                trend.put("metricName", metricName);
                trend.put("timeRange", timeRange);
                trend.put("dataPoints", points.size());
                trend.put("average", Arrays.stream(values).average().orElse(0.0));
                trend.put("min", Arrays.stream(values).min().orElse(0.0));
                trend.put("max", Arrays.stream(values).max().orElse(0.0));
                trend.put("trend", calculateTrend(values));
                trend.put("volatility", calculateVolatility(values));
            }
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getPerformanceAlerts() {
        List<Map<String, Object>> alertList = new ArrayList<>();

        // 转换告警队列为列表
        for (PerformanceAlert alert : alerts) {
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("metricName", alert.getMetricName());
            alertMap.put("threshold", alert.getThreshold());
            alertMap.put("actualValue", alert.getActualValue());
            alertMap.put("operator", alert.getOperator());
            alertMap.put("timestamp", alert.getTimestamp());
            alertMap.put("severity", alert.getSeverity());
            alertMap.put("message", alert.getMessage());

            alertList.add(alertMap);
        }

        return alertList;
    }

    @Override
    public void setPerformanceThreshold(String metricName, double threshold, String operator) {
        PerformanceThreshold thresholdObj = new PerformanceThreshold(metricName, threshold, operator);
        thresholds.put(metricName, thresholdObj);

        log.info("设置性能阈值: {} {} {}", metricName, operator, threshold);
    }

    @Override
    public String exportPerformanceReport(String format, long startTime, long endTime) {
        Map<String, Object> report = new HashMap<>();
        report.put("exportTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        report.put("collectorStatus", getCollectorStatus());
        report.put("currentSnapshot", collectPerformanceSnapshot());

        // 获取所有指标的历史数据
        String[] allMetrics = metricsHistory.keySet().toArray(new String[0]);
        report.put("historicalData", getHistoricalMetrics(startTime, endTime, allMetrics));

        // 获取告警信息
        report.put("alerts", getPerformanceAlerts());

        try {
            switch (format.toUpperCase()) {
                case "JSON":
                    return new com.fasterxml.jackson.databind.ObjectMapper()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(report);
                case "CSV":
                    return convertToCSV(report);
                case "XML":
                    return convertToXML(report);
                default:
                    return new com.fasterxml.jackson.databind.ObjectMapper()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(report);
            }
        } catch (Exception e) {
            log.error("导出性能报告失败", e);
            return "导出失败: " + e.getMessage();
        }
    }

    @Override
    public void resetAllMetrics() {
        metricsHistory.clear();
        counters.clear();
        timers.clear();
        histograms.clear();
        alerts.clear();

        log.info("所有性能指标已重置");
    }

    @Override
    public Map<String, Object> getCollectorStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("isCollecting", isCollecting.get());
        status.put("collectionStartTime", collectionStartTime);
        status.put("collectionInterval", collectionInterval);
        status.put("historyRetentionHours", historyRetentionHours);
        status.put("maxHistoryPoints", maxHistoryPoints);

        status.put("metricsCount", metricsHistory.size());
        status.put("countersCount", counters.size());
        status.put("timersCount", timers.size());
        status.put("histogramsCount", histograms.size());
        status.put("thresholdsCount", thresholds.size());
        status.put("alertsCount", alerts.size());

        if (isCollecting.get()) {
            status.put("uptime", System.currentTimeMillis() - collectionStartTime);
        }

        return status;
    }

    /**
     * 定期收集并存储指标
     */
    private void collectAndStoreMetrics() {
        try {
            Map<String, Object> snapshot = collectPerformanceSnapshot();

            // 将快照数据转换为指标点
            flattenAndStoreMetrics("", snapshot);

        } catch (Exception e) {
            log.error("收集性能指标失败", e);
        }
    }

    /**
     * 扁平化并存储指标数据
     */
    private void flattenAndStoreMetrics(String prefix, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Number) {
                recordCustomMetric(key, ((Number) value).doubleValue(), new HashMap<>());
            } else if (value instanceof Map) {
                flattenAndStoreMetrics(key, (Map<String, Object>) value);
            }
        }
    }

    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        long cutoffTime = System.currentTimeMillis() - (historyRetentionHours * 3600 * 1000L);

        metricsHistory.values().forEach(queue -> queue.removeIf(point -> point.getTimestamp() < cutoffTime));

        // 清理过期告警
        alerts.removeIf(alert -> alert.getTimestamp() < cutoffTime);

        log.debug("清理过期性能数据完成");
    }

    /**
     * 检查阈值
     */
    private void checkThreshold(String metricName, double value) {
        PerformanceThreshold threshold = thresholds.get(metricName);
        if (threshold != null && threshold.isExceeded(value)) {
            PerformanceAlert alert = new PerformanceAlert(
                    metricName,
                    threshold.getThreshold(),
                    value,
                    threshold.getOperator(),
                    System.currentTimeMillis());

            alerts.offer(alert);

            // 限制告警数量
            while (alerts.size() > 1000) {
                alerts.poll();
            }

            log.warn("性能阈值告警: {} {} {} (实际值: {})",
                    metricName, threshold.getOperator(), threshold.getThreshold(), value);
        }
    }

    /**
     * 初始化默认阈值
     */
    private void initializeDefaultThresholds() {
        setPerformanceThreshold("jvm.memory.heapUsagePercent", 80.0, ">");
        setPerformanceThreshold("system.cpu.processCpuLoad", 80.0, ">");
        setPerformanceThreshold("jvm.threads.threadCount", 200, ">");
        setPerformanceThreshold("application.segmentation.averageTime", 5000, ">");
        setPerformanceThreshold("application.apiCall.averageTime", 10000, ">");
    }

    /**
     * 计算趋势
     */
    private String calculateTrend(double[] values) {
        if (values.length < 2)
            return "STABLE";

        double firstHalf = Arrays.stream(values, 0, values.length / 2).average().orElse(0.0);
        double secondHalf = Arrays.stream(values, values.length / 2, values.length).average().orElse(0.0);

        double change = (secondHalf - firstHalf) / firstHalf * 100;

        if (change > 10)
            return "INCREASING";
        if (change < -10)
            return "DECREASING";
        return "STABLE";
    }

    /**
     * 计算波动性
     */
    private double calculateVolatility(double[] values) {
        if (values.length < 2)
            return 0.0;

        double mean = Arrays.stream(values).average().orElse(0.0);
        double variance = Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    /**
     * 转换为CSV格式
     */
    private String convertToCSV(Map<String, Object> data) {
        StringBuilder csv = new StringBuilder();
        csv.append("指标,值,时间戳\n");

        // 简化的CSV转换实现
        flattenForCSV("", data, csv);

        return csv.toString();
    }

    /**
     * 转换为XML格式
     */
    private String convertToXML(Map<String, Object> data) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<performanceReport>\n");

        buildXMLContent(data, xml, 1);

        xml.append("</performanceReport>");
        return xml.toString();
    }

    /**
     * 扁平化数据用于CSV导出
     */
    private void flattenForCSV(String prefix, Map<String, Object> map, StringBuilder csv) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenForCSV(key, (Map<String, Object>) value, csv);
            } else {
                csv.append(key).append(",").append(value).append(",")
                        .append(System.currentTimeMillis()).append("\n");
            }
        }
    }

    /**
     * 构建XML内容
     */
    private void buildXMLContent(Map<String, Object> map, StringBuilder xml, int indent) {
        String indentStr = "  ".repeat(indent);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                xml.append(indentStr).append("<").append(key).append(">\n");
                buildXMLContent((Map<String, Object>) value, xml, indent + 1);
                xml.append(indentStr).append("</").append(key).append(">\n");
            } else {
                xml.append(indentStr).append("<").append(key).append(">")
                        .append(value).append("</").append(key).append(">\n");
            }
        }
    }

    /**
     * 指标点类
     */
    private static class MetricPoint {
        private final long timestamp;
        private final double value;
        private final Map<String, String> tags;

        public MetricPoint(long timestamp, double value, Map<String, String> tags) {
            this.timestamp = timestamp;
            this.value = value;
            this.tags = tags != null ? new HashMap<>(tags) : new HashMap<>();
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }

        public Map<String, String> getTags() {
            return tags;
        }
    }

    /**
     * 性能阈值类
     */
    private static class PerformanceThreshold {
        private final String metricName;
        private final double threshold;
        private final String operator;

        public PerformanceThreshold(String metricName, double threshold, String operator) {
            this.metricName = metricName;
            this.threshold = threshold;
            this.operator = operator;
        }

        public boolean isExceeded(double value) {
            switch (operator) {
                case ">":
                    return value > threshold;
                case "<":
                    return value < threshold;
                case ">=":
                    return value >= threshold;
                case "<=":
                    return value <= threshold;
                case "==":
                    return Math.abs(value - threshold) < 0.001;
                default:
                    return false;
            }
        }

        public String getMetricName() {
            return metricName;
        }

        public double getThreshold() {
            return threshold;
        }

        public String getOperator() {
            return operator;
        }
    }

    /**
     * 性能告警类
     */
    private static class PerformanceAlert {
        private final String metricName;
        private final double threshold;
        private final double actualValue;
        private final String operator;
        private final long timestamp;
        private final String severity;
        private final String message;

        public PerformanceAlert(String metricName, double threshold, double actualValue,
                String operator, long timestamp) {
            this.metricName = metricName;
            this.threshold = threshold;
            this.actualValue = actualValue;
            this.operator = operator;
            this.timestamp = timestamp;
            this.severity = determineSeverity(actualValue, threshold, operator);
            this.message = String.format("指标 %s 超过阈值: %s %s %.2f (实际值: %.2f)",
                    metricName, operator, threshold, threshold, actualValue);
        }

        private String determineSeverity(double actualValue, double threshold, String operator) {
            double deviation = Math.abs(actualValue - threshold) / threshold;

            if (deviation > 0.5)
                return "CRITICAL";
            if (deviation > 0.2)
                return "HIGH";
            if (deviation > 0.1)
                return "MEDIUM";
            return "LOW";
        }

        public String getMetricName() {
            return metricName;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getActualValue() {
            return actualValue;
        }

        public String getOperator() {
            return operator;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }
    }
}