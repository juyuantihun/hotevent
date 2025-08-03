package com.hotech.events.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Neo4j驱动配置类
 * 使Neo4j连接失败时不会影响应用程序的正常运行
 */
@Slf4j
@Configuration
public class Neo4jDriverConfig {

    @Value("${spring.neo4j.uri:bolt://localhost:7687}")
    private String uri;

    @Value("${spring.neo4j.authentication.username:neo4j}")
    private String username;

    @Value("${spring.neo4j.authentication.password:neo4j}")
    private String password;

    /**
     * 配置Neo4j驱动，使其在Neo4j连接失败时不会影响应用程序的正常运行
     */
    @Bean
    @ConditionalOnProperty(name = "spring.neo4j.enabled", havingValue = "true", matchIfMissing = false)
    public Driver neo4jDriver() {
        try {
            log.info("正在连接Neo4j数据库: {}", uri);
            return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        } catch (Exception e) {
            log.error("Neo4j连接失败，将使用备用方案", e);
            return null;
        }
    }
}