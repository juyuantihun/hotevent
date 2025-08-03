package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件解析记录实体
 * 用于记录事件解析过程的详细信息
 * 
 * @author Kiro
 */
@Data
@TableName("event_parsing_record")
public class EventParsingRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 原始响应内容
     */
    private String originalResponse;
    
    /**
     * 提取的JSON内容
     */
    private String extractedJson;
    
    /**
     * 解析出的事件数量
     */
    private Integer parsedEventCount;
    
    /**
     * 解析方法
     */
    private String parsingMethod;
    
    /**
     * 解析状态：SUCCESS, FAILED, PARTIAL
     */
    private String parsingStatus;
    
    /**
     * 错误详情
     */
    private String errorDetails;
    
    /**
     * API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB
     */
    private String apiType;
    
    /**
     * 请求参数摘要
     */
    private String requestSummary;
    
    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;
    
    /**
     * 解析时间
     */
    private LocalDateTime parseTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}