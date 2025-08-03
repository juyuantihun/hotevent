package com.hotech.events.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 安全增强配置类
 * 提供额外的安全配置和防护措施
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class SecurityEnhancementConfig {

    /**
     * 配置HTTP防火墙
     * 防止各种HTTP攻击
     */
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        
        // 拒绝URL编码的斜杠
        firewall.setAllowUrlEncodedSlash(false);
        
        // 拒绝URL编码的百分号
        firewall.setAllowUrlEncodedPercent(false);
        
        // 拒绝URL编码的句号
        firewall.setAllowUrlEncodedPeriod(false);
        
        // 拒绝反斜杠
        firewall.setAllowBackSlash(false);
        
        // 拒绝空字节
        firewall.setAllowNull(false);
        
        // 拒绝分号
        firewall.setAllowSemicolon(false);
        
        // 允许的HTTP方法
        firewall.setAllowedHttpMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        log.info("HTTP防火墙配置完成");
        return firewall;
    }

    /**
     * 配置CORS（跨域资源共享）
     * 限制跨域访问
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（生产环境应该配置具体的域名）
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 允许发送凭据
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS配置完成");
        return source;
    }
}