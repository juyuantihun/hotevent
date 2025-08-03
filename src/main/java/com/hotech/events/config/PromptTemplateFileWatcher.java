package com.hotech.events.config;

import com.hotech.events.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 提示词模板配置文件监听器
 * 实现配置文件热更新功能
 */
@Slf4j
@Component
public class PromptTemplateFileWatcher {
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    @Autowired
    private PromptTemplateConfig promptTemplateConfig;
    
    @Value("${prompt.template.config.path:config/prompt-templates.yml}")
    private String configFilePath;
    
    @Value("${prompt.template.watch.enabled:true}")
    private boolean watchEnabled;
    
    private WatchService watchService;
    private ExecutorService executorService;
    private volatile boolean running = false;
    
    /**
     * 初始化文件监听器
     */
    @PostConstruct
    public void initializeWatcher() {
        if (!watchEnabled) {
            log.info("提示词模板文件监听功能已禁用");
            return;
        }
        
        try {
            // 获取配置文件路径
            Path configPath = Paths.get(getClass().getClassLoader().getResource(configFilePath).toURI());
            Path configDir = configPath.getParent();
            
            // 创建文件监听服务
            watchService = FileSystems.getDefault().newWatchService();
            
            // 注册监听目录
            configDir.register(watchService, 
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);
            
            // 创建监听线程
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "PromptTemplateFileWatcher");
                thread.setDaemon(true);
                return thread;
            });
            
            running = true;
            executorService.submit(this::watchForChanges);
            
            log.info("提示词模板文件监听器启动成功，监听路径: {}", configDir);
            
        } catch (Exception e) {
            log.error("初始化提示词模板文件监听器失败", e);
        }
    }
    
    /**
     * 监听文件变化
     */
    private void watchForChanges() {
        while (running) {
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
                    
                    // 检查是否是我们关心的配置文件
                    if (fileName.toString().equals("prompt-templates.yml")) {
                        log.info("检测到提示词模板配置文件变化: {}", fileName);
                        
                        // 延迟一点时间，确保文件写入完成
                        Thread.sleep(1000);
                        
                        // 重新加载配置
                        reloadConfiguration();
                    }
                }
                
                // 重置监听键
                boolean valid = key.reset();
                if (!valid) {
                    log.warn("文件监听键失效，停止监听");
                    break;
                }
                
            } catch (InterruptedException e) {
                log.info("文件监听线程被中断");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("文件监听过程中发生异常", e);
            }
        }
    }
    
    /**
     * 重新加载配置
     */
    private void reloadConfiguration() {
        try {
            log.info("开始重新加载提示词模板配置");
            
            // 重新加载模板缓存
            promptTemplateService.reloadTemplates();
            
            // 重新初始化默认模板
            promptTemplateConfig.reloadTemplates();
            
            log.info("提示词模板配置重新加载完成");
            
        } catch (Exception e) {
            log.error("重新加载提示词模板配置失败", e);
        }
    }
    
    /**
     * 手动触发配置重新加载
     */
    public void manualReload() {
        log.info("手动触发提示词模板配置重新加载");
        reloadConfiguration();
    }
    
    /**
     * 销毁监听器
     */
    @PreDestroy
    public void destroy() {
        running = false;
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
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
        
        log.info("提示词模板文件监听器已关闭");
    }
    
    /**
     * 获取监听器状态
     */
    public boolean isRunning() {
        return running;
    }
}