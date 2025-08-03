package com.hotech.events.config;

import com.hotech.events.entity.PromptTemplate;
import com.hotech.events.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 提示词模板配置类
 * 负责初始化默认模板和配置热更新
 */
@Slf4j
@Component
public class PromptTemplateConfig {

    @Autowired
    private PromptTemplateService promptTemplateService;

    /**
     * 应用启动后初始化默认模板
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultTemplates() {
        log.info("开始初始化默认提示词模板");

        try {
            // 从配置文件加载模板
            ClassPathResource resource = new ClassPathResource("config/prompt-templates.yml");
            if (!resource.exists()) {
                log.warn("提示词模板配置文件不存在，跳过初始化");
                return;
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Object> config = yaml.load(inputStream);
                Map<String, Object> templates = (Map<String, Object>) config.get("templates");

                if (templates != null) {
                    initializeTemplate(templates, "event_fetch", "事件检索");
                    initializeTemplate(templates, "event_validation", "事件验证");
                    initializeTemplate(templates, "timeline_organize", "时间线编制");
                }
            }

            log.info("默认提示词模板初始化完成");

        } catch (Exception e) {
            log.error("初始化默认提示词模板失败", e);
        }
    }

    /**
     * 初始化单个模板
     */
    private void initializeTemplate(Map<String, Object> templates, String templateType, String description) {
        try {
            Map<String, Object> templateConfig = (Map<String, Object>) templates.get(templateType);
            if (templateConfig == null) {
                log.warn("模板配置不存在: {}", templateType);
                return;
            }

            // 检查是否已存在激活的模板
            PromptTemplate existingTemplate = promptTemplateService.getActiveTemplate(templateType);
            if (existingTemplate != null) {
                log.debug("模板已存在，跳过初始化: {}", templateType);
                return;
            }

            // 创建新模板
            PromptTemplate template = new PromptTemplate();
            template.setTemplateName((String) templateConfig.get("name"));
            template.setTemplateType(templateType);
            template.setTemplateContent((String) templateConfig.get("content"));
            template.setVersion((String) templateConfig.get("version"));
            template.setIsActive(true);
            template.setCreatedAt(LocalDateTime.now());
            template.setUpdatedAt(LocalDateTime.now());

            boolean success = promptTemplateService.saveOrUpdateTemplate(template);
            if (success) {
                log.info("初始化{}模板成功: {}", description, template.getTemplateName());
            } else {
                log.error("初始化{}模板失败", description);
            }

        } catch (Exception e) {
            log.error("初始化{}模板时发生异常", description, e);
        }
    }

    /**
     * 手动重新加载模板（可通过管理接口调用）
     */
    public void reloadTemplates() {
        log.info("手动重新加载提示词模板");
        promptTemplateService.reloadTemplates();
        initializeDefaultTemplates();
    }
}