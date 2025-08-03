-- 事件验证记录表
CREATE TABLE IF NOT EXISTS event_validation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id BIGINT NOT NULL COMMENT '事件ID',
    validation_type VARCHAR(50) NOT NULL COMMENT '验证类型',
    validation_result TINYINT(1) NOT NULL COMMENT '验证结果 (1-通过, 0-失败)',
    credibility_score DECIMAL(3,2) COMMENT '可信度评分 (0.00-1.00)',
    validation_details JSON COMMENT '验证详情 (JSON格式)',
    validated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    
    INDEX idx_event_id (event_id),
    INDEX idx_validation_type (validation_type),
    INDEX idx_validated_at (validated_at),
    INDEX idx_validation_result (validation_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件验证记录表';

-- 为现有事件表添加验证相关字段
ALTER TABLE event ADD COLUMN IF NOT EXISTS credibility_score DECIMAL(3,2) DEFAULT 0.80 COMMENT '可信度评分';
ALTER TABLE event ADD COLUMN IF NOT EXISTS validation_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '验证状态: PENDING/PASSED/FAILED';
ALTER TABLE event ADD COLUMN IF NOT EXISTS source_urls JSON COMMENT '来源URL列表';
ALTER TABLE event ADD COLUMN IF NOT EXISTS fetch_method VARCHAR(20) DEFAULT 'MANUAL' COMMENT '获取方式: MANUAL/DEEPSEEK/API';
ALTER TABLE event ADD COLUMN IF NOT EXISTS last_validated_at DATETIME COMMENT '最后验证时间';

-- 添加索引
ALTER TABLE event ADD INDEX IF NOT EXISTS idx_validation_status (validation_status);
ALTER TABLE event ADD INDEX IF NOT EXISTS idx_credibility_score (credibility_score);
ALTER TABLE event ADD INDEX IF NOT EXISTS idx_fetch_method (fetch_method);
ALTER TABLE event ADD INDEX IF NOT EXISTS idx_last_validated_at (last_validated_at);