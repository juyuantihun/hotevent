package com.hotech.events.service.impl;

import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Event;
import com.hotech.events.entity.EventRelation;
import com.hotech.events.entity.PromptTemplate;
import com.hotech.events.mapper.PromptTemplateMapper;
import com.hotech.events.service.PromptTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 提示词模板服务实现类
 */
@Slf4j
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;

    @Autowired
    private com.hotech.events.mapper.RegionMapper regionMapper;

    /**
     * 模板缓存，提高性能
     */
    private final Map<String, PromptTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * 模板类型常量
     */
    public static final String TEMPLATE_TYPE_EVENT_FETCH = "event_fetch";
    public static final String TEMPLATE_TYPE_EVENT_VALIDATION = "event_validation";
    public static final String TEMPLATE_TYPE_TIMELINE_ORGANIZE = "timeline_organize";

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 参数替换的正则表达式
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    @Override
    public String generateEventFetchPrompt(TimelineGenerateRequest request) {
        return generateEventFetchPrompt(request, false);
    }

    @Override
    public String generateEventFetchPrompt(TimelineGenerateRequest request, boolean useWebSearch) {
        log.debug("生成事件检索提示词，请求参数: {}, 使用联网搜索: {}", request, useWebSearch);

        // 根据是否使用联网搜索选择不同的模板
        String templateType = useWebSearch ? "event_fetch_web_enhanced" : TEMPLATE_TYPE_EVENT_FETCH;
        PromptTemplate template = getActiveTemplate(templateType);

        if (template == null) {
            log.warn("未找到{}模板，尝试使用默认模板", templateType);
            template = getActiveTemplate(TEMPLATE_TYPE_EVENT_FETCH);
            if (template == null) {
                log.warn("未找到任何事件检索模板，使用默认模板");
                return getDefaultEventFetchPrompt(request, useWebSearch);
            }
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timelineName", request.getName());
        parameters.put("timelineDescription", request.getDescription());
        parameters.put("regions", formatRegions(request.getRegionIds()));
        parameters.put("startTime",
                request.getStartTime() != null ? request.getStartTime().format(DATE_TIME_FORMATTER) : "未指定");
        parameters.put("endTime",
                request.getEndTime() != null ? request.getEndTime().format(DATE_TIME_FORMATTER) : "未指定");
        parameters.put("responseFormat", getEventFetchResponseFormat());

        String prompt = generatePromptFromTemplate(template.getTemplateContent(), parameters);

        if (useWebSearch) {
            log.info("使用联网搜索增强模板生成提示词，长度: {}", prompt.length());
        }

        return prompt;
    }

    @Override
    public String generateEventValidationPrompt(List<Event> events) {
        log.debug("生成事件验证提示词，事件数量: {}", events.size());

        PromptTemplate template = getActiveTemplate(TEMPLATE_TYPE_EVENT_VALIDATION);
        if (template == null) {
            log.warn("未找到事件验证模板，使用默认模板");
            return getDefaultEventValidationPrompt(events);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventsList", formatEventsForValidation(events));
        parameters.put("responseFormat", getEventValidationResponseFormat());

        return generatePromptFromTemplate(template.getTemplateContent(), parameters);
    }

    @Override
    public String generateEventValidationPromptForEventData(List<com.hotech.events.dto.EventData> events) {
        log.debug("生成事件验证提示词（EventData版本），事件数量: {}", events.size());

        PromptTemplate template = getActiveTemplate(TEMPLATE_TYPE_EVENT_VALIDATION);
        if (template == null) {
            log.warn("未找到事件验证模板，使用默认模板");
            return getDefaultEventValidationPromptForEventData(events);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventsList", formatEventDataForValidation(events));
        parameters.put("responseFormat", getEventValidationResponseFormat());

        return generatePromptFromTemplate(template.getTemplateContent(), parameters);
    }

    @Override
    public String generateTimelineOrganizePrompt(List<Event> events, List<EventRelation> relations) {
        log.debug("生成时间线编制提示词，事件数量: {}, 关系数量: {}", events.size(), relations.size());

        PromptTemplate template = getActiveTemplate(TEMPLATE_TYPE_TIMELINE_ORGANIZE);
        if (template == null) {
            log.warn("未找到时间线编制模板，使用默认模板");
            return getDefaultTimelineOrganizePrompt(events, relations);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventsList", formatEventsForTimeline(events));
        parameters.put("relationsList", formatRelationsForTimeline(relations));
        parameters.put("responseFormat", getTimelineOrganizeResponseFormat());

        return generatePromptFromTemplate(template.getTemplateContent(), parameters);
    }

    @Override
    public void reloadTemplates() {
        log.info("重新加载提示词模板");
        templateCache.clear();

        List<PromptTemplate> activeTemplates = promptTemplateMapper.selectAllActive();
        for (PromptTemplate template : activeTemplates) {
            templateCache.put(template.getTemplateType(), template);
            log.debug("加载模板: {} - {}", template.getTemplateType(), template.getTemplateName());
        }

        log.info("模板加载完成，共加载 {} 个模板", templateCache.size());
    }

    @Override
    public PromptTemplate getActiveTemplate(String templateType) {
        PromptTemplate template = templateCache.get(templateType);
        if (template == null) {
            // 缓存中没有，从数据库查询
            template = promptTemplateMapper.selectActiveByType(templateType).stream().findFirst().orElse(null);
            if (template != null) {
                templateCache.put(templateType, template);
            }
        }
        return template;
    }

    @Override
    public boolean saveOrUpdateTemplate(PromptTemplate template) {
        try {
            // 检查是否已存在
            PromptTemplate existing = promptTemplateMapper.selectByNameAndType(
                    template.getTemplateName(), template.getTemplateType());

            if (existing != null) {
                template.setId(existing.getId());
                promptTemplateMapper.updateById(template);
            } else {
                promptTemplateMapper.insert(template);
            }

            // 更新缓存
            if (Boolean.TRUE.equals(template.getIsActive())) {
                templateCache.put(template.getTemplateType(), template);
            } else {
                templateCache.remove(template.getTemplateType());
            }

            log.info("保存模板成功: {} - {}", template.getTemplateType(), template.getTemplateName());
            return true;
        } catch (Exception e) {
            log.error("保存模板失败", e);
            return false;
        }
    }

    @Override
    public List<PromptTemplate> getAllActiveTemplates() {
        return promptTemplateMapper.selectAllActive();
    }

    @Override
    public String generatePromptFromTemplate(String template, Map<String, Object> parameters) {
        if (!StringUtils.hasText(template)) {
            return "";
        }

        String result = template;
        Matcher matcher = PARAM_PATTERN.matcher(template);

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = parameters.get(paramName);
            String replacement = paramValue != null ? paramValue.toString() : "";
            result = result.replace("{" + paramName + "}", replacement);
        }

        return result;
    }

    /**
     * 格式化地区信息
     */
    private String formatRegions(List<Long> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) {
            return "全球范围";
        }

        try {
            // 根据regionIds查询具体的地区名称
            List<com.hotech.events.entity.Region> regions = regionMapper.selectBatchIds(regionIds);

            if (regions.isEmpty()) {
                log.warn("未找到地区信息，regionIds: {}", regionIds);
                return regionIds.stream()
                        .map(id -> "未知地区(ID:" + id + ")")
                        .collect(Collectors.joining(", "));
            }

            return regions.stream()
                    .map(region -> region.getName() + "(" + region.getType() + ")")
                    .collect(Collectors.joining(", "));

        } catch (Exception e) {
            log.error("查询地区信息失败，regionIds: {}", regionIds, e);
            // 如果查询失败，返回ID信息作为备用
            return regionIds.stream()
                    .map(id -> "地区ID:" + id)
                    .collect(Collectors.joining(", "));
        }
    }

    /**
     * 格式化事件列表用于验证
     */
    private String formatEventsForValidation(List<Event> events) {
        return events.stream()
                .map(event -> String.format(
                        "事件ID: %d\n标题: %s\n描述: %s\n时间: %s\n地点: %s\n主体: %s\n客体: %s\n类型: %s",
                        event.getId(),
                        event.getEventTitle() != null ? event.getEventTitle() : "未知",
                        event.getEventDescription() != null ? event.getEventDescription() : "无描述",
                        event.getEventTime() != null ? event.getEventTime().format(DATE_TIME_FORMATTER) : "未知时间",
                        event.getEventLocation() != null ? event.getEventLocation() : "未知地点",
                        event.getSubject() != null ? event.getSubject() : "未知主体",
                        event.getObject() != null ? event.getObject() : "未知客体",
                        event.getEventType() != null ? event.getEventType() : "未知类型"))
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 格式化事件列表用于时间线编制
     */
    private String formatEventsForTimeline(List<Event> events) {
        return events.stream()
                .map(event -> String.format(
                        "ID: %d | 时间: %s | 地点: %s | 主体: %s | 客体: %s | 类型: %s | 描述: %s",
                        event.getId(),
                        event.getEventTime() != null ? event.getEventTime().format(DATE_TIME_FORMATTER) : "未知",
                        event.getEventLocation(),
                        event.getSubject(),
                        event.getObject(),
                        event.getEventType(),
                        event.getEventDescription()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化关系列表用于时间线编制
     */
    private String formatRelationsForTimeline(List<EventRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return "暂无已知关系";
        }

        return relations.stream()
                .map(relation -> String.format(
                        "事件%d -> 事件%d (关系: %s)",
                        relation.getSourceEventId(),
                        relation.getTargetEventId(),
                        relation.getRelationType()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化EventData用于验证
     */
    private String formatEventDataForValidation(List<com.hotech.events.dto.EventData> events) {
        return events.stream()
                .map(event -> String.format(
                        "事件ID: %s\n标题: %s\n描述: %s\n时间: %s\n地点: %s\n主体: %s\n客体: %s\n类型: %s",
                        event.getId() != null ? event.getId() : "未知",
                        event.getTitle() != null ? event.getTitle() : "未知",
                        event.getDescription() != null ? event.getDescription() : "无描述",
                        event.getEventTime() != null ? event.getEventTime().format(DATE_TIME_FORMATTER) : "未知时间",
                        event.getLocation() != null ? event.getLocation() : "未知地点",
                        event.getSubject() != null ? event.getSubject() : "未知主体",
                        event.getObject() != null ? event.getObject() : "未知客体",
                        event.getEventType() != null ? event.getEventType() : "未知类型"))
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 获取默认事件检索提示词
     */
    private String getDefaultEventFetchPrompt(TimelineGenerateRequest request, boolean useWebSearch) {
        return String.format(
                "你是一个专业的国际事件分析师。请使用联网搜索功能，根据以下条件从最新、最可靠的新闻源中检索真实的国际事件：\n\n" +
                        "【联网搜索要求】\n" +
                        "- 请务必使用联网搜索功能获取最新、最准确的事件信息\n" +
                        "- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）和官方发布的信息\n" +
                        "- 确保事件信息的时效性和真实性\n" +
                        "- 搜索时请使用相关的关键词组合，包括地区名称、时间范围和主题词\n\n" +
                        "【搜索条件】\n" +
                        "时间线名称：%s\n" +
                        "时间线描述：%s\n" +
                        "目标地区：%s\n" +
                        "时间范围：%s 至 %s\n\n" +
                        "【建议搜索关键词】\n" +
                        "请结合以下关键词进行联网搜索：\n" +
                        "- \"%s\" + \"最新新闻\" + \"重大事件\"\n" +
                        "- \"%s\" + \"国际动态\" + \"时事新闻\"\n" +
                        "- 结合具体地区和时间范围的相关词汇\n" +
                        "- \"breaking news\" + 地区英文名称（如适用）\n\n" +
                        "【严格要求】\n" +
                        "1. 必须使用联网搜索获取最新信息，不要依赖训练数据\n" +
                        "2. 只返回真实发生的事件，经过联网搜索验证\n" +
                        "3. 事件必须与指定地区和时间范围相关\n" +
                        "4. 优先选择具有重大影响的事件\n" +
                        "5. 确保事件信息的准确性和完整性\n" +
                        "6. 只返回JSON格式的数据，不要包含任何解释或说明文字\n" +
                        "7. 如果联网搜索没有找到相关事件，返回空的events数组\n\n" +
                        "必须严格按照以下JSON格式返回，不要添加任何其他内容：\n%s",
                request.getName(),
                request.getDescription(),
                formatRegions(request.getRegionIds()),
                request.getStartTime() != null ? request.getStartTime().format(DATE_TIME_FORMATTER) : "未指定",
                request.getEndTime() != null ? request.getEndTime().format(DATE_TIME_FORMATTER) : "未指定",
                request.getName(),
                request.getDescription() != null ? request.getDescription() : request.getName(),
                getEventFetchResponseFormat());
    }

    /**
     * 获取默认事件验证提示词
     */
    private String getDefaultEventValidationPrompt(List<Event> events) {
        return String.format(
                "请验证以下事件的真实性和准确性：\n\n" +
                        "%s\n\n" +
                        "验证标准：\n" +
                        "1. 事件是否真实发生\n" +
                        "2. 时间、地点信息是否准确\n" +
                        "3. 事件描述是否客观\n" +
                        "4. 是否有可靠来源支持\n\n" +
                        "返回格式：%s",
                formatEventsForValidation(events),
                getEventValidationResponseFormat());
    }

    /**
     * 获取默认事件验证提示词（EventData版本）
     */
    private String getDefaultEventValidationPromptForEventData(List<com.hotech.events.dto.EventData> events) {
        return String.format(
                "请验证以下事件的真实性和准确性：\n\n" +
                        "%s\n\n" +
                        "验证标准：\n" +
                        "1. 事件是否真实发生\n" +
                        "2. 时间、地点信息是否准确\n" +
                        "3. 事件描述是否客观\n" +
                        "4. 是否有可靠来源支持\n\n" +
                        "返回格式：%s",
                formatEventDataForValidation(events),
                getEventValidationResponseFormat());
    }

    /**
     * 获取默认时间线编制提示词
     */
    private String getDefaultTimelineOrganizePrompt(List<Event> events, List<EventRelation> relations) {
        return String.format(
                "请将以下已验证的事件组织成连贯的时间线：\n\n" +
                        "事件列表：\n%s\n\n" +
                        "事件关系：\n%s\n\n" +
                        "组织原则：\n" +
                        "1. 按时间顺序排列\n" +
                        "2. 识别因果关系\n" +
                        "3. 突出关键节点\n" +
                        "4. 保持逻辑连贯\n\n" +
                        "返回格式：%s",
                formatEventsForTimeline(events),
                formatRelationsForTimeline(relations),
                getTimelineOrganizeResponseFormat());
    }

    /**
     * 获取事件检索响应格式
     */
    private String getEventFetchResponseFormat() {
        return "{\n" +
                "  \"events\": [\n" +
                "    {\n" +
                "      \"title\": \"事件标题\",\n" +
                "      \"description\": \"详细描述\",\n" +
                "      \"eventTime\": \"2024-01-01T12:00:00\",\n" +
                "      \"location\": \"具体地点\",\n" +
                "      \"subject\": \"事件主体\",\n" +
                "      \"object\": \"事件客体\",\n" +
                "      \"eventType\": \"事件类型\",\n" +
                "      \"keywords\": [\"关键词1\", \"关键词2\"],\n" +
                "      \"sources\": [\"来源1\", \"来源2\"],\n" +
                "      \"credibilityScore\": 0.95\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 获取事件验证响应格式
     */
    private String getEventValidationResponseFormat() {
        return "{\n" +
                "  \"validationResults\": [\n" +
                "    {\n" +
                "      \"eventId\": \"事件ID\",\n" +
                "      \"isValid\": true,\n" +
                "      \"credibilityScore\": 0.95,\n" +
                "      \"issues\": [],\n" +
                "      \"suggestions\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 获取时间线编制响应格式
     */
    private String getTimelineOrganizeResponseFormat() {
        return "{\n" +
                "  \"timeline\": {\n" +
                "    \"events\": [\n" +
                "      {\n" +
                "        \"eventId\": 1,\n" +
                "        \"order\": 1,\n" +
                "        \"importance\": \"high\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"relations\": [\n" +
                "      {\n" +
                "        \"sourceEventId\": 1,\n" +
                "        \"targetEventId\": 2,\n" +
                "        \"relationType\": \"cause\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }
}