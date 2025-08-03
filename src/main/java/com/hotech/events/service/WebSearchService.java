package com.hotech.events.service;

import java.util.List;
import java.util.Map;

/**
 * 联网搜索服务接口
 * 提供DeepSeek联网搜索功能的管理和配置
 */
public interface WebSearchService {
    
    /**
     * 检查联网搜索是否可用
     * @return 是否可用
     */
    boolean isWebSearchAvailable();
    
    /**
     * 启用联网搜索
     */
    void enableWebSearch();
    
    /**
     * 禁用联网搜索
     */
    void disableWebSearch();
    
    /**
     * 获取联网搜索配置
     * @return 配置信息
     */
    Map<String, Object> getWebSearchConfig();
    
    /**
     * 更新联网搜索配置
     * @param config 新的配置
     */
    void updateWebSearchConfig(Map<String, Object> config);
    
    /**
     * 测试联网搜索功能
     * @param query 测试查询
     * @return 测试结果
     */
    Map<String, Object> testWebSearch(String query);
    
    /**
     * 获取联网搜索统计信息
     * @return 统计信息
     */
    Map<String, Object> getWebSearchStats();
    
    /**
     * 清除联网搜索缓存
     */
    void clearWebSearchCache();
}