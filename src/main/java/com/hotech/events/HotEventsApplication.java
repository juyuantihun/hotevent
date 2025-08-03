package com.hotech.events;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 国际热点事件管理系统主启动类
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration.class
})
@MapperScan({ "com.hotech.events.mapper", "com.hotech.events.repository.mybatis" })
@EnableScheduling
public class HotEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotEventsApplication.class, args);
    }
}