package com.hotech.events.controller;

import com.hotech.events.service.DeepSeekService;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 火山方舟DeepSeek API测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/volcengine/test")
// @Api(tags = "火山方舟DeepSeek测试")
public class VolcengineTestController {

    @Autowired
    private DeepSeekService deepSeekService;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Value("${app.deepseek.model}")
    private String model;

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    /**
     * 测试火山方舟DeepSeek API连接
     */
    @GetMapping("/connection")
    // @ApiOperation("测试API连接")
    public ResponseEntity<Map<String, Object>> testConnection() {
        log.info("开始测试火山方舟DeepSeek API连接");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 显示配置信息（隐藏敏感信息）
            result.put("apiUrl", apiUrl);
            result.put("model", model);
            result.put("apiKey", maskApiKey(apiKey));
            
            // 测试连接
            Boolean isConnected = deepSeekService.checkConnection();
            result.put("connected", isConnected);
            result.put("status", isConnected ? "成功" : "失败");
            result.put("message", isConnected ? "火山方舟DeepSeek API连接正常" : "火山方舟DeepSeek API连接失败");
            
            log.info("火山方舟DeepSeek API连接测试结果: {}", isConnected ? "成功" : "失败");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试火山方舟DeepSeek API连接时发生异常", e);
            result.put("connected", false);
            result.put("status", "异常");
            result.put("message", "测试连接时发生异常: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 测试简单对话
     */
    @GetMapping("/chat")
    // @ApiOperation("测试简单对话")
    public ResponseEntity<Map<String, Object>> testChat() {
        log.info("开始测试火山方舟DeepSeek简单对话");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里需要直接调用底层API，因为现有的service方法可能不适合简单测试
            // 暂时返回配置信息
            result.put("apiUrl", apiUrl);
            result.put("model", model);
            result.put("apiKey", maskApiKey(apiKey));
            result.put("message", "火山方舟DeepSeek配置已更新，请使用连接测试验证");
            result.put("note", "如需测试对话功能，请使用具体的业务接口");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试火山方舟DeepSeek对话时发生异常", e);
            result.put("status", "异常");
            result.put("message", "测试对话时发生异常: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取当前配置信息
     */
    @GetMapping("/config")
    // @ApiOperation("获取当前配置")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("apiUrl", apiUrl);
        config.put("model", model);
        config.put("apiKey", maskApiKey(apiKey));
        config.put("provider", "火山方舟 (Volcengine)");
        config.put("version", "v3");
        config.put("description", "已配置为使用火山方舟提供的DeepSeek API服务");
        
        return ResponseEntity.ok(config);
    }

    /**
     * 掩码API密钥，只显示前4位和后4位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "****" + suffix;
    }
}