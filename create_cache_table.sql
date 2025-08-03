-- 创建时间线创建缓存表
-- 用于防止重复创建时间线

CREATE TABLE IF NOT EXISTS timeline_creation_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    request_fingerprint VARCHAR(64) NOT NULL COMMENT '请求指纹 - 基于请求参数生成的唯一标识',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    timeline_name VARCHAR(255) NOT NULL COMMENT '时间线名称',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    region_ids TEXT COMMENT '地区ID列表（JSON格式存储）',
    timeline_id BIGINT COMMENT '关联的时间线ID',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATING' COMMENT '状态：CREATING(创建中), COMPLETED(已完成), FAILED(失败)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    
    -- 索引
    INDEX idx_request_fingerprint (request_fingerprint),
    INDEX idx_user_id_created_at (user_id, created_at),
    INDEX idx_timeline_name_time_range (timeline_name, start_time, end_time),
    INDEX idx_timeline_id (timeline_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status (status),
    
    -- 外键约束
    FOREIGN KEY (timeline_id) REFERENCES timeline(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间线创建缓存表';

-- 为timeline表添加复合索引，优化重复检测查询性能
ALTER TABLE timeline ADD INDEX IF NOT EXISTS idx_name_time_range (name, start_time, end_time);
ALTER TABLE timeline ADD INDEX IF NOT EXISTS idx_name_status (name, status);
ALTER TABLE timeline ADD INDEX IF NOT EXISTS idx_created_at_status (created_at, status);