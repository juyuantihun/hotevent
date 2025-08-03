package com.hotech.events.service;

import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Event;
import com.hotech.events.entity.EventRelation;
import com.hotech.events.entity.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板服务接口
 * 负责动态生成DeepSeek API的提示词
 */
public interface PromptTemplateService {
    
    /**
     * 根据参数生成事件检索提示词
     * @param request 时间线生成请求
     * @return 生成的提示词
     */
    String generateEventFetchPrompt(TimelineGenerateRequest request);
    
    /**
     * 根据API类型和参数生成事件检索提示词
     * @param request 时间线生成请求
     * @param useWebSearch 是否使用联网搜索API
     * @return 生成的提示词
     */
    String generateEventFetchPrompt(TimelineGenerateRequest request, boolean useWebSearch);
    
    /**
     * 生成事件验证提示词
     * @param events 待验证的事件列表
     * @return 生成的提示词
     */
    String generateEventValidationPrompt(List<Event> events);
    
    /**
     * 生成事件验证提示词（EventData版本）
     * @param events 待验证的事件数据列表
     * @return 生成的提示词
     */
    String generateEventValidationPromptForEventData(java.util.List<com.hotech.events.dto.EventData> events);
    
    /**
     * 生成时间线编制提示词
     * @param events 事件列表
     * @param relations 事件关系列表
     * @return 生成的提示词
     */
    String generateTimelineOrganizePrompt(List<Event> events, List<EventRelation> relations);
    
    /**
     * 重新加载提示词模板
     */
    void reloadTemplates();
    
    /**
     * 根据模板类型获取激活的模板
     * @param templateType 模板类型
     * @return 模板对象
     */
    PromptTemplate getActiveTemplate(String templateType);
    
    /**
     * 保存或更新模板
     * @param template 模板对象
     * @return 保存结果
     */
    boolean saveOrUpdateTemplate(PromptTemplate template);
    
    /**
     * 获取所有激活的模板
     * @return 模板列表
     */
    List<PromptTemplate> getAllActiveTemplates();
    
    /**
     * 根据模板和参数生成提示词
     * @param template 模板内容
     * @param parameters 参数映射
     * @return 生成的提示词
     */
    String generatePromptFromTemplate(String template, Map<String, Object> parameters);
}