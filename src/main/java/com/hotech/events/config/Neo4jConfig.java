package com.hotech.events.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import lombok.extern.slf4j.Slf4j;

/**
 * Neo4j配置类
 * 使Neo4j连接失败时不会影响应用程序的正常运行
 */
@Slf4j
@Configuration
@EnableNeo4jRepositories(basePackages = "com.hotech.events.repository")
public class Neo4jConfig {

    /**
     * 配置Neo4jTemplate，使其在Neo4j连接失败时不会影响应用程序的正常运行
     */
    @Bean
    @ConditionalOnProperty(name = "spring.neo4j.enabled", havingValue = "true", matchIfMissing = true)
    public Neo4jTemplate neo4jTemplate(Neo4jClient neo4jClient, Neo4jMappingContext neo4jMappingContext) {
        try {
            return new Neo4jTemplate(neo4jClient, neo4jMappingContext);
        } catch (Exception e) {
            log.error("Neo4j连接失败，将使用备用方案", e);
            return null;
        }
    }
}