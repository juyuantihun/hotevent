package com.hotech.events.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL执行工具
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Component
public class SqlExecutor {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 执行SQL脚本文件
     * 
     * @param filePath SQL文件路径
     * @return 执行结果
     */
    public boolean executeSqlFile(String filePath) {
        try {
            List<String> sqlStatements = readSqlFile(filePath);
            
            for (String sql : sqlStatements) {
                if (sql.trim().isEmpty() || sql.trim().startsWith("--")) {
                    continue;
                }
                
                log.info("执行SQL: {}", sql);
                jdbcTemplate.execute(sql);
            }
            
            log.info("SQL脚本执行完成，共执行{}条语句", sqlStatements.size());
            return true;
        } catch (Exception e) {
            log.error("执行SQL脚本失败", e);
            return false;
        }
    }
    
    /**
     * 读取SQL文件
     * 
     * @param filePath 文件路径
     * @return SQL语句列表
     * @throws IOException IO异常
     */
    private List<String> readSqlFile(String filePath) throws IOException {
        List<String> sqlStatements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                currentStatement.append(line).append(" ");
                
                if (line.endsWith(";")) {
                    sqlStatements.add(currentStatement.toString().trim());
                    currentStatement = new StringBuilder();
                }
            }
        }
        
        return sqlStatements;
    }
    
    /**
     * 执行单个SQL语句
     * 
     * @param sql SQL语句
     * @return 执行结果
     */
    public boolean executeSql(String sql) {
        try {
            log.info("执行SQL: {}", sql);
            jdbcTemplate.execute(sql);
            return true;
        } catch (Exception e) {
            log.error("执行SQL失败: {}", sql, e);
            return false;
        }
    }
} 