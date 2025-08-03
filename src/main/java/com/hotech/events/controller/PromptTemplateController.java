package com.hotech.events.controller;

import com.hotech.events.common.Result;
import com.hotech.events.config.PromptTemplateConfig;
import com.hotech.events.entity.PromptTemplate;
import com.hotech.events.service.PromptTemplateService;
// Swagger注解暂时注释，避免编译错误
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提示词模板管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt-template")
// @Api(tags = "提示词模板管理")
public class PromptTemplateController {
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    @Autowired
    private PromptTemplateConfig promptTemplateConfig;
    
    /**
     * 获取所有激活的模板
     */
    @GetMapping("/active")
    // @ApiOperation("获取所有激活的模板")
    public Result<List<PromptTemplate>> getAllActiveTemplates() {
        try {
            List<PromptTemplate> templates = promptTemplateService.getAllActiveTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取激活模板失败", e);
            return Result.error("获取激活模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取激活的模板
     */
    @GetMapping("/active/{templateType}")
    // @ApiOperation("根据类型获取激活的模板")
    public Result<PromptTemplate> getActiveTemplate(
            /* @ApiParam("模板类型") */ @PathVariable String templateType) {
        try {
            PromptTemplate template = promptTemplateService.getActiveTemplate(templateType);
            if (template != null) {
                return Result.success(template);
            } else {
                return Result.error("未找到指定类型的激活模板: " + templateType);
            }
        } catch (Exception e) {
            log.error("获取模板失败", e);
            return Result.error("获取模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存或更新模板
     */
    @PostMapping("/save")
    // @ApiOperation("保存或更新模板")
    public Result<String> saveOrUpdateTemplate(@RequestBody PromptTemplate template) {
        try {
            // 设置更新时间
            template.setUpdatedAt(LocalDateTime.now());
            if (template.getId() == null) {
                template.setCreatedAt(LocalDateTime.now());
            }
            
            boolean success = promptTemplateService.saveOrUpdateTemplate(template);
            if (success) {
                return Result.success("模板保存成功");
            } else {
                return Result.error("模板保存失败");
            }
        } catch (Exception e) {
            log.error("保存模板失败", e);
            return Result.error("保存模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新加载模板缓存
     */
    @PostMapping("/reload")
    // @ApiOperation("重新加载模板缓存")
    public Result<String> reloadTemplates() {
        try {
            promptTemplateService.reloadTemplates();
            return Result.success("模板缓存重新加载成功");
        } catch (Exception e) {
            log.error("重新加载模板缓存失败", e);
            return Result.error("重新加载模板缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新初始化默认模板
     */
    @PostMapping("/init-defaults")
    // @ApiOperation("重新初始化默认模板")
    public Result<String> initializeDefaultTemplates() {
        try {
            promptTemplateConfig.reloadTemplates();
            return Result.success("默认模板初始化成功");
        } catch (Exception e) {
            log.error("初始化默认模板失败", e);
            return Result.error("初始化默认模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试提示词生成
     */
    @PostMapping("/test-generation")
    // @ApiOperation("测试提示词生成")
    public Result<String> testPromptGeneration(
            /* @ApiParam("模板内容") */ @RequestParam String template,
            /* @ApiParam("参数JSON") */ @RequestParam String parametersJson) {
        try {
            // 这里可以实现一个简单的参数解析和提示词生成测试
            // 为了简化，暂时返回模板内容
            return Result.success("测试功能待实现，模板内容: " + template);
        } catch (Exception e) {
            log.error("测试提示词生成失败", e);
            return Result.error("测试提示词生成失败: " + e.getMessage());
        }
    }
}