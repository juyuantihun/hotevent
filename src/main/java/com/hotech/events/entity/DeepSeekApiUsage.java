package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DeepSeek API使用记录实体
 */
@Data
@TableName("deepseek_api_usage")
public class DeepSeekApiUsage {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 请求类型
     */
    private String requestType;
    
    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;
    
    /**
     * 响应状态
     */
    private String responseStatus;
    
    /**
     * Token使用量
     */
    private Integer tokenUsage;
    
    /**
     * 响应时间（毫秒）
     */
    private Integer responseTimeMs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}