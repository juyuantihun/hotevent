-- DeepSeek API使用记录表
CREATE TABLE IF NOT EXISTS deepseek_api_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    request_type VARCHAR(50) NOT NULL COMMENT '请求类型',
    request_params JSON COMMENT '请求参数',
    response_status VARCHAR(20) NOT NULL COMMENT '响应状态',
    token_usage INT COMMENT 'Token使用量',
    response_time_ms INT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request_type (request_type),
    INDEX idx_created_at (created_at),
    INDEX idx_response_status (response_status)
) COMMENT='DeepSeek API使用记录表';

-- 插入示例数据
INSERT INTO deepseek_api_usage (request_type, request_params, response_status, token_usage, response_time_ms, created_at) VALUES
('fetchEvents', '{"name":"测试时间线","regionIds":[1,2],"startTime":"2024-01-01T00:00:00","endTime":"2024-01-31T23:59:59"}', 'SUCCESS', 1500, 2500, NOW()),
('validateEvents', '{"eventCount":10}', 'SUCCESS', 800, 1800, NOW()),
('fetchEvents', '{"name":"国际事件","regionIds":[3,4,5],"startTime":"2024-02-01T00:00:00","endTime":"2024-02-28T23:59:59"}', 'FAILED', 0, 5000, NOW()),
('analyzeRelations', '{"eventCount":20}', 'SUCCESS', 1200, 3200, NOW());