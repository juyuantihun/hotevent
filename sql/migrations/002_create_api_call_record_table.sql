/*
 API调用记录表迁移脚本
 
 创建时间: 2025-07-27
 描述: 创建api_call_record表，用于记录和分析API调用统计信息
 需求: 3.1, 3.2
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for api_call_record
-- ----------------------------
DROP TABLE IF EXISTS `api_call_record`;
CREATE TABLE `api_call_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '请求参数（JSON格式）',
  `response_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '响应状态：SUCCESS, FAILED, TIMEOUT, RATE_LIMITED',
  `token_usage` int(11) DEFAULT NULL COMMENT 'Token使用量',
  `response_time` int(11) DEFAULT NULL COMMENT '响应时间（毫秒）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `call_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  `request_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求ID，用于链路追踪',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  `request_size` int(11) DEFAULT NULL COMMENT '请求大小（字节）',
  `response_size` int(11) DEFAULT NULL COMMENT '响应大小（字节）',
  `cache_hit` tinyint(1) DEFAULT 0 COMMENT '是否使用缓存：0-否，1-是',
  `retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_type` (`api_type`) USING BTREE,
  INDEX `idx_response_status` (`response_status`) USING BTREE,
  INDEX `idx_call_time` (`call_time`) USING BTREE,
  INDEX `idx_request_id` (`request_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  -- 复合索引用于API监控和统计查询优化
  INDEX `idx_api_type_status_time` (`api_type`, `response_status`, `call_time`) USING BTREE,
  INDEX `idx_user_api_time` (`user_id`, `api_type`, `call_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API调用记录表' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;