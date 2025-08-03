-- 修复数据库表结构脚本
-- 请手动执行此脚本来修复数据库表结构问题

USE hot_events_db;

-- 1. 修复 system_config 表结构
-- 先检查表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_type` varchar(20) DEFAULT 'STRING' COMMENT '配置类型',
  `config_group` varchar(50) DEFAULT 'default' COMMENT '配置分组',
  `description` varchar(500) COMMENT '配置描述',
  `is_encrypted` tinyint(1) DEFAULT '0' COMMENT '是否加密',
  `is_required` tinyint(1) DEFAULT '0' COMMENT '是否必需',
  `default_value` text COMMENT '默认值',
  `validation_rule` text COMMENT '验证规则',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(50) COMMENT '创建人',
  `updated_by` varchar(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 添加缺失的字段（忽略错误，如果字段已存在）
-- 注意：这些语句可能会报错，如果字段已存在，请忽略错误
ALTER TABLE `system_config` ADD COLUMN `config_type` varchar(20) DEFAULT 'STRING' COMMENT '配置类型';
ALTER TABLE `system_config` ADD COLUMN `config_group` varchar(50) DEFAULT 'default' COMMENT '配置分组';
ALTER TABLE `system_config` ADD COLUMN `description` varchar(500) COMMENT '配置描述';
ALTER TABLE `system_config` ADD COLUMN `is_required` tinyint(1) DEFAULT '0' COMMENT '是否必需';
ALTER TABLE `system_config` ADD COLUMN `is_encrypted` tinyint(1) DEFAULT '0' COMMENT '是否加密';
ALTER TABLE `system_config` ADD COLUMN `default_value` text COMMENT '默认值';
ALTER TABLE `system_config` ADD COLUMN `validation_rule` text COMMENT '验证规则';
ALTER TABLE `system_config` ADD COLUMN `created_by` varchar(50) COMMENT '创建人';
ALTER TABLE `system_config` ADD COLUMN `updated_by` varchar(50) COMMENT '更新人';

-- 2. 创建 deepseek_api_usage 表
CREATE TABLE IF NOT EXISTS `deepseek_api_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `request_type` varchar(50) NOT NULL COMMENT '请求类型',
  `request_params` text COMMENT '请求参数',
  `response_status` varchar(20) NOT NULL COMMENT '响应状态',
  `token_usage` int DEFAULT '0' COMMENT 'Token使用量',
  `response_time_ms` int DEFAULT '0' COMMENT '响应时间(毫秒)',
  `error_message` text COMMENT '错误信息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_type` (`request_type`),
  KEY `idx_response_status` (`response_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DeepSeek API使用记录表';

-- 3. 创建系统错误日志表
CREATE TABLE IF NOT EXISTS `system_error_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `error_type` varchar(50) NOT NULL COMMENT '错误类型',
  `error_code` varchar(50) COMMENT '错误代码',
  `error_message` text COMMENT '错误信息',
  `stack_trace` text COMMENT '堆栈跟踪',
  `request_url` varchar(500) COMMENT '请求URL',
  `request_method` varchar(10) COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `user_id` varchar(50) COMMENT '用户ID',
  `session_id` varchar(100) COMMENT '会话ID',
  `ip_address` varchar(50) COMMENT 'IP地址',
  `user_agent` varchar(500) COMMENT '用户代理',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_error_type` (`error_type`),
  KEY `idx_error_code` (`error_code`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统错误日志表';

-- 4. 创建系统性能日志表
CREATE TABLE IF NOT EXISTS `system_performance_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `response_time_ms` bigint NOT NULL COMMENT '响应时间(毫秒)',
  `memory_usage_mb` bigint COMMENT '内存使用量(MB)',
  `cpu_usage_percent` decimal(5,2) COMMENT 'CPU使用率(%)',
  `thread_count` int COMMENT '线程数',
  `gc_count` int COMMENT 'GC次数',
  `gc_time_ms` bigint COMMENT 'GC时间(毫秒)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统性能日志表';

-- 5. 修复 event 表结构
-- 添加缺失的字段到 event 表（忽略错误，如果字段已存在）
ALTER TABLE `event` ADD COLUMN `event_title` varchar(500) COMMENT '事件标题';
ALTER TABLE `event` ADD COLUMN `credibility_score` decimal(3,2) DEFAULT '0.00' COMMENT '可信度评分';
ALTER TABLE `event` ADD COLUMN `validation_status` varchar(20) DEFAULT 'PENDING' COMMENT '验证状态';
ALTER TABLE `event` ADD COLUMN `source_urls` text COMMENT '来源URL列表';
ALTER TABLE `event` ADD COLUMN `fetch_method` varchar(50) COMMENT '获取方法';
ALTER TABLE `event` ADD COLUMN `last_validated_at` datetime COMMENT '最后验证时间';

-- 6. 检查并创建其他可能需要的表
-- 如果有其他表缺失，可以在这里添加

SHOW TABLES;