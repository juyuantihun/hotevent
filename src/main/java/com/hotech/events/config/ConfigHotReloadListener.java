package com.hotech.events.config;

import com.hotech.events.service.PromptTemplateService;
import com.hotech.events.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置热更新监听器
 * 监听配置文件变化并自动重新加载
 * 
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Component
public class ConfigHotReloadListener {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    @Value("${prompt.template.watch.enabled:true}")
    private boolean watchEnabled;
    
    @Value("${prompt.template.config.path:config/prompt-templates.yml}")
    private String configPath;
    
    private WatchService watchService;
    private ExecutorService executorService;
    private final ConcurrentHashMap<String, Long> lastModifiedTimes = new ConcurrentHashMap<>();
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (watchEnabled) {
            startFileWatcher();
        }
        log.info("配置热更新监听器已启动，监听状态: {}", watchEnabled);
    }
    
    /**
     * 启动文件监听器
     */
    @Async
    public void startFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "config-hot-reload");
                thread.setDaemon(true);
                return thread;
            });
            
            // 监听配置目录
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            configDir.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
            
            // 监听应用配置目录
            Path appConfigDir = Paths.get("src/main/resources");
            if (Files.exists(appConfigDir)) {
                appConfigDir.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            }
            
            executorService.submit(this::watchForChanges);
            log.info("文件监听器启动成功，监听目录: {}", configDir.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("启动文件监听器失败", e);
        }
    }
    
    /**
     * 监听文件变化
     */
    private void watchForChanges() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    String fileNameStr = fileName.toString();
                    
                    // 处理配置文件变化
                    if (isConfigFile(fileNameStr)) {
                        handleConfigFileChange(fileNameStr, kind);
                    }
                }
                
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("监听文件变化时发生错误", e);
            }
        }
    }
    
    /**
     * 判断是否为配置文件
     */
    private boolean isConfigFile(String fileName) {
        return fileName.endsWith(".yml") || 
               fileName.endsWith(".yaml") || 
               fileName.endsWith(".properties") ||
               fileName.equals("application.yml") ||
               fileName.equals("application.properties") ||
               fileName.contains("prompt-templates");
    }
    
    /**
     * 处理配置文件变化
     */
    private void handleConfigFileChange(String fileName, WatchEvent.Kind<?> kind) {
        try {
            // 防止重复处理（文件保存时可能触发多次事件）
            String key = fileName + "_" + kind.name();
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastModifiedTimes.get(key);
            
            if (lastTime != null && (currentTime - lastTime) < 1000) {
                return; // 1秒内的重复事件忽略
            }
            lastModifiedTimes.put(key, currentTime);
            
            log.info("检测到配置文件变化: file={}, event={}", fileName, kind.name());
            
            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                log.warn("配置文件被删除: {}", fileName);
                return;
            }
            
            // 延迟处理，确保文件写入完成
            Thread.sleep(500);
            
            if (fileName.contains("prompt-templates")) {
                // 重新加载提示词模板
                promptTemplateService.reloadTemplates();
                log.info("提示词模板已重新加载: {}", fileName);
            } else if (fileName.contains("application")) {
                // 重新加载系统配置
                systemConfigService.refreshCache();
                log.info("系统配置已重新加载: {}", fileName);
            }
            
        } catch (Exception e) {
            log.error("处理配置文件变化失败: fileName={}", fileName, e);
        }
    }
    
    /**
     * 定时检查配置完整性
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void checkConfigIntegrity() {
        try {
            if (!systemConfigService.checkRequiredConfigs()) {
                log.warn("发现缺失的必需配置，请检查系统配置");
            }
        } catch (Exception e) {
            log.error("检查配置完整性失败", e);
        }
    }
    
    /**
     * 手动触发配置重新加载
     */
    public void manualReload() {
        try {
            log.info("手动触发配置重新加载...");
            
            // 重新加载系统配置
            systemConfigService.refreshCache();
            
            // 重新加载提示词模板
            promptTemplateService.reloadTemplates();
            
            log.info("配置重新加载完成");
        } catch (Exception e) {
            log.error("手动重新加载配置失败", e);
        }
    }
    
    /**
     * 获取监听器状态
     */
    public boolean isWatchEnabled() {
        return watchEnabled && watchService != null;
    }
    
    /**
     * 获取监听统计信息
     */
    public String getWatchStats() {
        return String.format("监听状态: %s, 处理事件数: %d", 
                           isWatchEnabled() ? "启用" : "禁用", 
                           lastModifiedTimes.size());
    }
    
    // @PreDestroy
    public void destroy() {
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
            log.info("配置热更新监听器已关闭");
        } catch (IOException e) {
            log.error("关闭配置热更新监听器失败", e);
        }
    }
}