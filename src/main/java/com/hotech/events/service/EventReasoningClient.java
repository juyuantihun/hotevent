package com.hotech.events.service;

import com.hotech.events.dto.event.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件推理客户端服务
 * 用于调用event项目的推理服务进行事件关系分析、链条分析等
 */
@Slf4j
@Service
public class EventReasoningClient {

    @Value("${app.event-service.base-url:http://localhost:8082}")
    private String eventServiceBaseUrl;

    private final WebClient webClient;

    public EventReasoningClient() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    /**
     * 分析事件关系
     * 
     * @param events 事件列表
     * @return 关系分析结果
     */
    public Map<String, Object> analyzeEventRelations(List<EventDTO> events) {
        log.info("调用event项目分析事件关系，事件数量: {}", events.size());
        
        try {
            // 修改为调用EventAnalysisController的API
            String url = eventServiceBaseUrl + "/api/event-analysis/batch-relation";
            
            // 构建请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("events", events);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(requestData)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            log.info("事件关系分析完成，响应: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("调用事件关系分析失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 分析事件链条
     * 
     * @param events 主要事件列表
     * @return 链条分析结果
     */
    public Map<String, Object> analyzeEventChains(List<EventDTO> events) {
        log.info("调用event项目分析事件链条，事件数量: {}", events.size());
        
        try {
            String url = eventServiceBaseUrl + "/api/enhanced-crawl/chain-analysis";
            
            // 构建请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("mainEvents", adaptEventsForReasoning(events));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(url)
                .body(Mono.just(requestData), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response != null && response.get("code") != null && response.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                log.info("事件链条分析成功，发现链条数量: {}", 
                    data.get("chains") != null ? ((List<?>) data.get("chains")).size() : 0);
                return data;
            } else {
                log.error("事件链条分析失败: {}", response != null ? response.get("msg") : "无响应");
            }
        } catch (Exception e) {
            log.error("调用事件链条分析服务失败", e);
        }
        
        return createEmptyResult();
    }

    /**
     * 分析事件相似度
     * 
     * @param targetEvent 目标事件
     * @param candidateEvents 候选事件列表
     * @return 相似度分析结果
     */
    public Map<String, Object> analyzeEventSimilarity(EventDTO targetEvent, List<EventDTO> candidateEvents) {
        log.info("调用event项目分析事件相似度，候选事件数量: {}", candidateEvents.size());
        
        try {
            String url = eventServiceBaseUrl + "/api/enhanced-crawl/similarity-analysis";
            
            // 构建请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("targetEvent", adaptEventForReasoning(targetEvent));
            requestData.put("candidateEvents", adaptEventsForReasoning(candidateEvents));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(url)
                .body(Mono.just(requestData), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            if (response != null && response.get("code") != null && response.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                log.info("事件相似度分析成功");
                return data;
            } else {
                log.error("事件相似度分析失败: {}", response != null ? response.get("msg") : "无响应");
            }
        } catch (Exception e) {
            log.error("调用事件相似度分析服务失败", e);
        }
        
        return createEmptyResult();
    }

    /**
     * 智能搜索热点事件
     * 
     * @param searchConfigs 搜索配置列表
     * @return 搜索结果
     */
    public Map<String, Object> searchHotEvents(List<Map<String, Object>> searchConfigs) {
        log.info("调用event项目智能搜索热点事件，配置数量: {}", searchConfigs.size());
        
        try {
            String url = eventServiceBaseUrl + "/api/enhanced-crawl/large-batch";
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(url)
                .body(Mono.just(searchConfigs), List.class)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .block();
            
            if (response != null && response.get("code") != null && response.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                log.info("智能搜索完成，发现事件数量: {}", 
                    data.get("totalNewEvents") != null ? data.get("totalNewEvents") : 0);
                return data;
            } else {
                log.error("智能搜索失败: {}", response != null ? response.get("msg") : "无响应");
            }
        } catch (Exception e) {
            log.error("调用智能搜索服务失败", e);
        }
        
        return createEmptyResult();
    }

    /**
     * 将EventDTO适配为推理服务需要的格式
     * 
     * @param events 事件列表
     * @return 适配后的事件数据
     */
    private List<Map<String, Object>> adaptEventsForReasoning(List<EventDTO> events) {
        return events.stream().map(this::adaptEventForReasoning).toList();
    }

    /**
     * 将单个EventDTO适配为推理服务需要的格式
     * 
     * @param event 事件DTO
     * @return 适配后的事件数据
     */
    private Map<String, Object> adaptEventForReasoning(EventDTO event) {
        Map<String, Object> eventData = new HashMap<>();
        
        eventData.put("id", event.getId());
        eventData.put("eventCode", event.getEventCode());
        eventData.put("title", event.getEventDescription()); // 使用eventDescription作为title
        eventData.put("description", event.getEventDescription());
        eventData.put("eventTime", event.getEventTime());
        eventData.put("location", event.getEventLocation());
        eventData.put("longitude", event.getLongitude());
        eventData.put("latitude", event.getLatitude());
        eventData.put("eventType", event.getEventType());
        eventData.put("subject", event.getSubject());
        eventData.put("object", event.getObject());
        eventData.put("keywords", event.getKeywords());
        eventData.put("sourceType", event.getSourceType());
        eventData.put("status", event.getStatus());
        
        return eventData;
    }

    /**
     * 创建空的结果
     * 
     * @return 空结果
     */
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "调用推理服务失败");
        result.put("relations", List.of());
        result.put("chains", List.of());
        result.put("similarities", List.of());
        return result;
    }

    /**
     * 检查event服务健康状态
     * 
     * @return 是否健康
     */
    public boolean isEventServiceHealthy() {
        try {
            String healthUrl = eventServiceBaseUrl + "/api/enhanced-crawl/status";
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            return response != null && response.get("code") != null && response.get("code").equals(200);
        } catch (Exception e) {
            log.warn("Event服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
} 