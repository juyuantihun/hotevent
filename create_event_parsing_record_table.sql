-- 创建事件解析记录表
CREATE TABLE IF NOT EXISTS event_parsing_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    original_response LONGTEXT COMMENT '原始响应内容',
    extracted_json TEXT COMMENT '提取的JSON内容',
    parsed_event_count INT DEFAULT 0 COMMENT '解析出的事件数量',
    parsing_method VARCHAR(50) COMMENT '解析方法：DIRECT_JSON, EXTRACTED_JSON, TEXT_PARSING',
    parsing_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '解析状态：SUCCESS, FAILED, PARTIAL',
    error_details TEXT COMMENT '错误详情',
    api_type VARCHAR(30) COMMENT 'API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB',
    request_summary VARCHAR(500) COMMENT '请求参数摘要',
    response_time INT COMMENT '响应时间（毫秒）',
    parse_time DATETIME COMMENT '解析时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_parsing_status (parsing_status),
    INDEX idx_api_type (api_type),
    INDEX idx_parse_time (parse_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件解析记录表';