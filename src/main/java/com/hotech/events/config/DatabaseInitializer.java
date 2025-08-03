package com.hotech.events.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库初始化器
 * 在应用启动时执行必要的数据库迁移脚本
 */
@Slf4j
@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 应用启动后执行数据库初始化
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        log.info("开始执行数据库初始化");

        try {
            // 执行数据库修复脚本
            executeSqlScript("db/fix_database.sql");
            
            // 执行地理坐标表初始化脚本
            executeSqlScript("db/geographic_coordinates_init.sql");
            
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            // 如果修复脚本失败，尝试执行原来的迁移脚本
            try {
                log.info("尝试执行备用迁移脚本");
                executeSqlScript("db/migration/add_system_config_fields.sql");
            } catch (Exception e2) {
                log.error("备用迁移脚本也失败", e2);
            }
        }
    }

    /**
     * 执行 SQL 脚本
     */
    private void executeSqlScript(String scriptPath) {
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            if (!resource.exists()) {
                log.warn("SQL脚本不存在: {}", scriptPath);
                return;
            }

            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("--")) {
                        sql.append(line).append(" ");
                    }
                }
            }

            // 分割多个 SQL 语句
            String[] statements = sql.toString().split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    try {
                        jdbcTemplate.execute(statement);
                        log.debug("执行SQL成功: {}", statement.substring(0, Math.min(50, statement.length())));
                    } catch (Exception e) {
                        // 忽略字段已存在等错误
                        if (!e.getMessage().contains("Duplicate column name") && 
                            !e.getMessage().contains("already exists")) {
                            log.warn("执行SQL失败: {}, 错误: {}", statement.substring(0, Math.min(50, statement.length())), e.getMessage());
                        }
                    }
                }
            }

            log.info("SQL脚本执行完成: {}", scriptPath);
        } catch (Exception e) {
            log.error("执行SQL脚本失败: {}", scriptPath, e);
        }
    }
}