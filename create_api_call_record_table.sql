-- 创建API调用记录表
CREATE TABLE IF NOT EXISTS api_call_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    api_type VARCHAR(50) NOT NULL COMMENT 'API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    response_status VARCHAR(20) NOT NULL COMMENT '响应状态：SUCCESS, FAILED, TIMEOUT, RATE_LIMITED',
    token_usage INT DEFAULT 0 COMMENT 'Token使用量',
    response_time INT DEFAULT 0 COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    call_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    request_id VARCHAR(100) COMMENT '请求ID，用于链路追踪',
    user_id VARCHAR(50) COMMENT '用户ID',
    request_size INT DEFAULT 0 COMMENT '请求大小（字节）',
    response_size INT DEFAULT 0 COMMENT '响应大小（字节）',
    cache_hit BOOLEAN DEFAULT FALSE COMMENT '是否使用缓存',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    
    INDEX idx_api_type (api_type),
    INDEX idx_call_time (call_time),
    INDEX idx_response_status (response_status),
    INDEX idx_request_id (request_id),
    INDEX idx_user_id (user_id),
    INDEX idx_api_type_time (api_type, call_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用记录表';

-- 创建分区（按月分区，提高查询性能）
-- ALTER TABLE api_call_record PARTITION BY RANGE (YEAR(call_time) * 100 + MONTH(call_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     PARTITION p202404 VALUES LESS THAN (202405),
--     PARTITION p202405 VALUES LESS THAN (202406),
--     PARTITION p202406 VALUES LESS THAN (202407),
--     PARTITION p202407 VALUES LESS THAN (202408),
--     PARTITION p202408 VALUES LESS THAN (202409),
--     PARTITION p202409 VALUES LESS THAN (202410),
--     PARTITION p202410 VALUES LESS THAN (202411),
--     PARTITION p202411 VALUES LESS THAN (202412),
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 插入一些测试数据
INSERT INTO api_call_record (api_type, request_params, response_status, token_usage, response_time, call_time, request_id) VALUES
('DEEPSEEK_OFFICIAL', '{"model":"deepseek-chat","prompt_length":100}', 'SUCCESS', 150, 2500, NOW() - INTERVAL 1 HOUR, 'test-req-001'),
('VOLCENGINE_WEB', '{"model":"deepseek-chat","web_search":true,"prompt_length":200}', 'SUCCESS', 200, 3500, NOW() - INTERVAL 2 HOUR, 'test-req-002'),
('DEEPSEEK_OFFICIAL', '{"model":"deepseek-chat","prompt_length":150}', 'FAILED', 0, 5000, NOW() - INTERVAL 3 HOUR, 'test-req-003'),
('VOLCENGINE_WEB', '{"model":"deepseek-chat","web_search":true,"prompt_length":300}', 'SUCCESS', 250, 4000, NOW() - INTERVAL 4 HOUR, 'test-req-004');