package com.hotech.events.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 动态API配置管理器
 * 负责根据需求动态切换DeepSeek API配置
 */
@Slf4j
@Component
public class DynamicApiConfigManager {

    // 官方API配置
    @Value("${app.deepseek.official.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String officialApiUrl;

    @Value("${app.deepseek.official.api-key:}")
    private String officialApiKey;

    @Value("${app.deepseek.official.model:deepseek-chat}")
    private String officialModel;

    // 火山引擎API配置
    @Value("${app.deepseek.volcengine.api-url:https://ark.cn-beijing.volces.com/api/v3/bots/chat/completions}")
    private String volcengineApiUrl;

    @Value("${app.deepseek.volcengine.api-key:}")
    private String volcengineApiKey;

    @Value("${app.deepseek.volcengine.model:bot-20250725163638-5gn4n}")
    private String volcengineModel;

    // 默认配置
    @Value("${app.deepseek.web-search.enabled:true}")
    private boolean defaultWebSearchEnabled;

    /**
     * API配置类
     */
    public static class ApiConfig {
        private final String apiUrl;
        private final String apiKey;
        private final String model;
        private final boolean supportsWebSearch;

        public ApiConfig(String apiUrl, String apiKey, String model, boolean supportsWebSearch) {
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.model = model;
            this.supportsWebSearch = supportsWebSearch;
        }

        public String getApiUrl() { return apiUrl; }
        public String getApiKey() { return apiKey; }
        public String getModel() { return model; }
        public boolean isSupportsWebSearch() { return supportsWebSearch; }
    }

    /**
     * 获取官方API配置
     */
    public ApiConfig getOfficialApiConfig() {
        log.debug("获取官方API配置: url={}, model={}", officialApiUrl, officialModel);
        return new ApiConfig(officialApiUrl, officialApiKey, officialModel, false);
    }

    /**
     * 获取火山引擎API配置
     */
    public ApiConfig getVolcengineApiConfig() {
        log.debug("获取火山引擎API配置: url={}, model={}", volcengineApiUrl, volcengineModel);
        return new ApiConfig(volcengineApiUrl, volcengineApiKey, volcengineModel, true);
    }

    /**
     * 根据是否需要联网搜索获取相应的API配置
     */
    public ApiConfig getApiConfig(boolean useWebSearch) {
        if (useWebSearch) {
            log.info("选择火山引擎API配置（支持联网搜索）");
            return getVolcengineApiConfig();
        } else {
            log.info("选择官方API配置（不支持联网搜索）");
            return getOfficialApiConfig();
        }
    }

    /**
     * 检查API配置是否有效
     */
    public boolean isApiConfigValid(ApiConfig config) {
        if (config == null) {
            return false;
        }
        
        boolean isValid = config.getApiUrl() != null && !config.getApiUrl().isEmpty() &&
                         config.getApiKey() != null && !config.getApiKey().isEmpty() &&
                         config.getModel() != null && !config.getModel().isEmpty();
        
        if (!isValid) {
            log.warn("API配置无效: url={}, keyPresent={}, model={}", 
                    config.getApiUrl(), 
                    config.getApiKey() != null && !config.getApiKey().isEmpty(),
                    config.getModel());
        }
        
        return isValid;
    }

    /**
     * 获取默认的联网搜索开关状态
     */
    public boolean isDefaultWebSearchEnabled() {
        return defaultWebSearchEnabled;
    }
}