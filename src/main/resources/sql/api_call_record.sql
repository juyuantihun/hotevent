-- API调用记录表
CREATE TABLE IF NOT EXISTS api_call_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    api_type VARCHAR(50) NOT NULL COMMENT 'API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    response_status VARCHAR(20) NOT NULL COMMENT '响应状态：SUCCESS, FAILED, TIMEOUT, RATE_LIMITED',
    token_usage INT COMMENT 'Token使用量',
    response_time INT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    call_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    request_id VARCHAR(100) COMMENT '请求ID，用于链路追踪',
    user_id VARCHAR(50) COMMENT '用户ID',
    request_size INT COMMENT '请求大小（字节）',
    response_size INT COMMENT '响应大小（字节）',
    cache_hit BOOLEAN DEFAULT FALSE COMMENT '是否使用缓存',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_api_type (api_type),
    INDEX idx_call_time (call_time),
    INDEX idx_response_status (response_status),
    INDEX idx_request_id (request_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT='API调用记录表，用于记录和分析API调用统计信息';

-- 插入示例数据
INSERT INTO api_call_record (api_type, request_params, response_status, token_usage, response_time, request_id, retry_count, call_time) VALUES
('DEEPSEEK_OFFICIAL', '{"model":"deepseek-chat","promptLength":150,"maxTokens":2000}', 'SUCCESS', 1500, 2500, 'req-001', 0, NOW()),
('VOLCENGINE_WEB', '{"model":"deepseek-chat","promptLength":200,"webSearchEnabled":true}', 'SUCCESS', 1800, 3200, 'req-002', 0, NOW()),
('DEEPSEEK_OFFICIAL', '{"model":"deepseek-chat","promptLength":100,"maxTokens":2000}', 'FAILED', 0, 5000, 'req-003', 2, NOW()),
('VOLCENGINE_WEB', '{"model":"deepseek-chat","promptLength":300,"webSearchEnabled":true}', 'TIMEOUT', 0, 30000, 'req-004', 1, NOW()),
('DEEPSEEK_OFFICIAL', '{"model":"deepseek-chat","promptLength":250,"maxTokens":2000}', 'SUCCESS', 2200, 1800, 'req-005', 0, NOW());