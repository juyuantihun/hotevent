package com.hotech.events.service;

import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.entity.Region;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek服务接口
 */
public interface DeepSeekService {
    
    /**
     * 检查DeepSeek API连接状态
     * @return 连接是否正常
     */
    Boolean checkConnection();
    
    /**
     * 从网络获取指定地区和时间范围内的事件
     * @param regions 地区列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    List<Map<String, Object>> fetchEvents(List<Region> regions, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分析事件之间的关联关系
     * @param events 事件列表
     * @return 事件关联关系列表
     */
    List<Map<String, Object>> analyzeEventRelations(List<Map<String, Object>> events);
    
    /**
     * 将事件组织成时间线
     * @param events 事件列表
     * @param relations 事件关联关系列表
     * @return 时间线列表（可能有多条）
     */
    List<Map<String, Object>> organizeTimelines(List<Map<String, Object>> events, List<Map<String, Object>> relations);
    
    /**
     * 获取最新事件
     * @param limit 限制数量
     * @return 事件列表
     */
    List<EventDTO> fetchLatestEvents(int limit);
    
    /**
     * 根据关键词获取事件
     * @param keywords 关键词列表
     * @param limit 限制数量
     * @return 事件列表
     */
    List<EventDTO> fetchEventsByKeywords(List<String> keywords, int limit);
    
    /**
     * 根据日期范围获取事件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 事件列表
     */
    List<EventDTO> fetchEventsByDateRange(String startDate, String endDate, int limit);
    
    /**
     * 解析GDELT数据
     * @param gdeltData GDELT格式数据
     * @return 事件列表
     */
    List<EventDTO> parseGdeltData(String gdeltData);
    
    /**
     * 使用DeepSeek聊天API生成事件分析
     * @param events 事件列表
     * @param prompt 分析提示
     * @return 分析结果
     */
    Map<String, Object> generateEventAnalysis(List<Map<String, Object>> events, String prompt);
}