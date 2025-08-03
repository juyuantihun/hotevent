package com.hotech.events.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hotech.events.config.TimelineEnhancementConfig;
import com.hotech.events.exception.TimelineEnhancementException;
import com.hotech.events.service.ErrorHandlingService;
import com.hotech.events.service.TimelineConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间线配置管理服务实现类
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class TimelineConfigurationServiceImpl implements TimelineConfigurationService {
    
    @Autowired
    private TimelineEnhancementConfig timelineConfig;
    
    @Autowired
    private ErrorHandlingService errorHandlingService;
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final List<TimelineConfigChangeRecord> changeHistory = new ArrayList<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    private final AtomicBoolean hotReloadEnabled = new AtomicBoolean(true);
    private final AtomicLong lastModified = new AtomicLong(0);
    private final AtomicLong configLoadCount = new AtomicLong(0);
    private final AtomicLong configUpdateCount = new AtomicLong(0);
    
    private ScheduledExecutorService scheduledExecutor;
    private WatchService watchService;
    private final String CONFIG_FILE_PATH = "config/timeline-enhancement-config.yml";
    
    @PostConstruct
    public void init() {
        try {
            // 初始化性能指标
            initializePerformanceMetrics();
            
            // 启动配置文件监听
            enableHotReload();
            
            // 记录初始化日志
            log.info("时间线配置管理服务初始化完成");
            
        } catch (Exception e) {
            log.error("时间线配置管理服务初始化失败", e);
        }
    }
    
    @Override
    public TimelineEnhancementConfig getCurrentConfig() {
        configLoadCount.incrementAndGet();
        return timelineConfig;
    }
    
    @Override
    public void updateSegmentationConfig(TimelineEnhancementConfig.SegmentationConfig config) {
        TimelineEnhancementConfig.SegmentationConfig oldConfig = new TimelineEnhancementConfig.SegmentationConfig();
        BeanUtils.copyProperties(timelineConfig.getSegmentation(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getSegmentation());
        
        recordConfigChange("segmentation", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新时间段分割配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("时间段分割配置已更新");
    }
    
    @Override
    public void updateGeographicConfig(TimelineEnhancementConfig.GeographicConfig config) {
        TimelineEnhancementConfig.GeographicConfig oldConfig = new TimelineEnhancementConfig.GeographicConfig();
        BeanUtils.copyProperties(timelineConfig.getGeographic(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getGeographic());
        
        recordConfigChange("geographic", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新地理信息处理配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("地理信息处理配置已更新");
    }
    
    @Override
    public void updateApiConfig(TimelineEnhancementConfig.ApiConfig config) {
        TimelineEnhancementConfig.ApiConfig oldConfig = new TimelineEnhancementConfig.ApiConfig();
        BeanUtils.copyProperties(timelineConfig.getApi(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getApi());
        
        recordConfigChange("api", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新API调用配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("API调用配置已更新");
    }
    
    @Override
    public void updateMonitoringConfig(TimelineEnhancementConfig.MonitoringConfig config) {
        TimelineEnhancementConfig.MonitoringConfig oldConfig = new TimelineEnhancementConfig.MonitoringConfig();
        BeanUtils.copyProperties(timelineConfig.getMonitoring(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getMonitoring());
        
        recordConfigChange("monitoring", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新性能监控配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("性能监控配置已更新");
    }
    
    @Override
    public void updateCacheConfig(TimelineEnhancementConfig.CacheConfig config) {
        TimelineEnhancementConfig.CacheConfig oldConfig = new TimelineEnhancementConfig.CacheConfig();
        BeanUtils.copyProperties(timelineConfig.getCache(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getCache());
        
        recordConfigChange("cache", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新缓存配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("缓存配置已更新");
    }
    
    @Override
    public void updateFrontendConfig(TimelineEnhancementConfig.FrontendConfig config) {
        TimelineEnhancementConfig.FrontendConfig oldConfig = new TimelineEnhancementConfig.FrontendConfig();
        BeanUtils.copyProperties(timelineConfig.getFrontend(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getFrontend());
        
        recordConfigChange("frontend", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新前端UI配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("前端UI配置已更新");
    }
    
    @Override
    public void updateValidationConfig(TimelineEnhancementConfig.ValidationConfig config) {
        TimelineEnhancementConfig.ValidationConfig oldConfig = new TimelineEnhancementConfig.ValidationConfig();
        BeanUtils.copyProperties(timelineConfig.getValidation(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getValidation());
        
        recordConfigChange("validation", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新数据验证配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("数据验证配置已更新");
    }
    
    @Override
    public void updateErrorHandlingConfig(TimelineEnhancementConfig.ErrorHandlingConfig config) {
        TimelineEnhancementConfig.ErrorHandlingConfig oldConfig = new TimelineEnhancementConfig.ErrorHandlingConfig();
        BeanUtils.copyProperties(timelineConfig.getErrorHandling(), oldConfig);
        
        BeanUtils.copyProperties(config, timelineConfig.getErrorHandling());
        
        recordConfigChange("errorHandling", "全部配置", 
                          oldConfig.toString(), config.toString(), 
                          "system", "更新错误处理配置", "UPDATE");
        
        configUpdateCount.incrementAndGet();
        log.info("错误处理配置已更新");
    }
    
    @Override
    public void resetToDefaults() {
        TimelineEnhancementConfig defaultConfig = new TimelineEnhancementConfig();
        BeanUtils.copyProperties(defaultConfig, timelineConfig);
        
        recordConfigChange("all", "全部配置", 
                          "当前配置", "默认配置", 
                          "system", "重置为默认配置", "RESET");
        
        configUpdateCount.incrementAndGet();
        log.info("配置已重置为默认值");
    }
    
    @Override
    public boolean validateConfig(TimelineEnhancementConfig config) {
        try {
            // 验证时间段分割配置
            if (config.getSegmentation().getMaxSpanDays() <= 0 || 
                config.getSegmentation().getMaxSpanDays() > 365) {
                log.warn("时间段分割配置验证失败：maxSpanDays 超出有效范围");
                return false;
            }
            
            // 验证地理信息配置
            if (config.getGeographic().getCoordinateCacheTtl() <= 0) {
                log.warn("地理信息配置验证失败：coordinateCacheTtl 必须大于0");
                return false;
            }
            
            // 验证API配置
            if (config.getApi().getVolcengine().getMaxTokens() <= 0 || 
                config.getApi().getDeepseek().getMaxTokens() <= 0) {
                log.warn("API配置验证失败：maxTokens 必须大于0");
                return false;
            }
            
            // 验证监控配置
            if (config.getMonitoring().getMetricsInterval() <= 0) {
                log.warn("监控配置验证失败：metricsInterval 必须大于0");
                return false;
            }
            
            log.info("配置验证通过");
            return true;
            
        } catch (Exception e) {
            log.error("配置验证过程中发生异常", e);
            return false;
        }
    }
    
    @Override
    public boolean reloadConfigFromFile() {
        return errorHandlingService.executeWithFallback(
            () -> {
                try {
                    ClassPathResource resource = new ClassPathResource(CONFIG_FILE_PATH);
                    if (!resource.exists()) {
                        log.warn("配置文件不存在：{}", CONFIG_FILE_PATH);
                        throw new TimelineEnhancementException("CONFIG_FILE_NOT_FOUND", "CONFIG", 
                            "配置文件不存在: " + CONFIG_FILE_PATH);
                    }
                    
                    TimelineEnhancementConfig newConfig = yamlMapper.readValue(
                        resource.getInputStream(), TimelineEnhancementConfig.class);
                    
                    if (validateConfig(newConfig)) {
                        BeanUtils.copyProperties(newConfig, timelineConfig);
                        
                        recordConfigChange("all", "配置文件重载", 
                                          "旧配置", "新配置", 
                                          "system", "从文件重新加载配置", "RELOAD");
                        
                        configLoadCount.incrementAndGet();
                        log.info("配置文件重新加载成功");
                        return true;
                    } else {
                        throw new TimelineEnhancementException("CONFIG_VALIDATION_FAILED", "CONFIG", 
                            "配置文件验证失败");
                    }
                    
                } catch (IOException e) {
                    throw new TimelineEnhancementException("CONFIG_LOAD_ERROR", "CONFIG", 
                        "重新加载配置文件失败", e);
                }
            },
            () -> {
                log.warn("配置文件重载失败，保持当前配置不变");
                return false;
            }
        );
    }
    
    @Override
    public boolean saveConfigToFile() {
        try {
            // 这里简化实现，实际项目中可能需要更复杂的文件写入逻辑
            String configPath = "src/main/resources/" + CONFIG_FILE_PATH;
            File configFile = new File(configPath);
            
            yamlMapper.writeValue(configFile, timelineConfig);
            
            recordConfigChange("all", "配置文件保存", 
                              "内存配置", "文件配置", 
                              "system", "保存配置到文件", "SAVE");
            
            log.info("配置已保存到文件：{}", configPath);
            return true;
            
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
            return false;
        }
    }
    
    @Override
    public List<TimelineConfigChangeRecord> getConfigChangeHistory() {
        return new ArrayList<>(changeHistory);
    }
    
    @Override
    public Map<String, Object> getConfigStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("configLoadCount", configLoadCount.get());
        stats.put("configUpdateCount", configUpdateCount.get());
        stats.put("changeHistorySize", changeHistory.size());
        stats.put("hotReloadEnabled", hotReloadEnabled.get());
        stats.put("lastModified", lastModified.get());
        stats.put("currentTime", System.currentTimeMillis());
        
        // 添加各配置段的统计信息
        stats.put("segmentationConfig", timelineConfig.getSegmentation());
        stats.put("geographicConfig", timelineConfig.getGeographic());
        stats.put("apiConfig", timelineConfig.getApi());
        stats.put("monitoringConfig", timelineConfig.getMonitoring());
        
        return stats;
    }
    
    @Override
    public void enableHotReload() {
        if (hotReloadEnabled.compareAndSet(false, true)) {
            startFileWatcher();
            log.info("配置热更新已启用");
        }
    }
    
    @Override
    public void disableHotReload() {
        if (hotReloadEnabled.compareAndSet(true, false)) {
            stopFileWatcher();
            log.info("配置热更新已禁用");
        }
    }
    
    @Override
    public boolean isConfigFileChanged() {
        try {
            ClassPathResource resource = new ClassPathResource(CONFIG_FILE_PATH);
            if (resource.exists()) {
                long currentModified = resource.getFile().lastModified();
                return currentModified > lastModified.get();
            }
        } catch (IOException e) {
            log.error("检查配置文件变更状态失败", e);
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics() {
        return new HashMap<>(performanceMetrics);
    }
    
    /**
     * 记录配置变更
     */
    private void recordConfigChange(String section, String key, String oldValue, 
                                   String newValue, String changedBy, String reason, String changeType) {
        TimelineConfigChangeRecord record = new TimelineConfigChangeRecord(
            section, key, oldValue, newValue, changedBy, reason, changeType);
        
        changeHistory.add(record);
        
        // 保持历史记录数量在合理范围内
        if (changeHistory.size() > 1000) {
            changeHistory.remove(0);
        }
    }
    
    /**
     * 初始化性能指标
     */
    private void initializePerformanceMetrics() {
        performanceMetrics.put("configLoadTime", 0L);
        performanceMetrics.put("configUpdateTime", 0L);
        performanceMetrics.put("validationTime", 0L);
        performanceMetrics.put("fileReloadTime", 0L);
        performanceMetrics.put("averageLoadTime", 0.0);
        performanceMetrics.put("averageUpdateTime", 0.0);
    }
    
    /**
     * 启动文件监听器
     */
    private void startFileWatcher() {
        if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "timeline-config-watcher");
                t.setDaemon(true);
                return t;
            });
            
            // 定期检查配置文件变更
            scheduledExecutor.scheduleWithFixedDelay(this::checkConfigFileChanges, 
                                                   10, 10, TimeUnit.SECONDS);
        }
    }
    
    /**
     * 停止文件监听器
     */
    private void stopFileWatcher() {
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("关闭文件监听服务失败", e);
            }
        }
    }
    
    /**
     * 检查配置文件变更
     */
    private void checkConfigFileChanges() {
        if (!hotReloadEnabled.get()) {
            return;
        }
        
        try {
            if (isConfigFileChanged()) {
                log.info("检测到配置文件变更，开始重新加载");
                if (reloadConfigFromFile()) {
                    ClassPathResource resource = new ClassPathResource(CONFIG_FILE_PATH);
                    if (resource.exists()) {
                        lastModified.set(resource.getFile().lastModified());
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查配置文件变更时发生异常", e);
        }
    }
}