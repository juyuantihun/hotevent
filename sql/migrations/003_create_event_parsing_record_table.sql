/*
 事件解析记录表迁移脚本
 
 创建时间: 2025-07-27
 描述: 创建event_parsing_record表，用于记录事件解析过程的详细信息
 需求: 3.1, 3.2
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for event_parsing_record
-- ----------------------------
DROP TABLE IF EXISTS `event_parsing_record`;
CREATE TABLE `event_parsing_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_response` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '原始响应内容',
  `extracted_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '提取的JSON内容',
  `parsed_event_count` int(11) DEFAULT 0 COMMENT '解析出的事件数量',
  `parsing_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '解析方法：JSON_EXTRACT, TEXT_PARSE, REGEX_EXTRACT',
  `parsing_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '解析状态：SUCCESS, FAILED, PARTIAL',
  `error_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误详情',
  `api_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API类型：DEEPSEEK_OFFICIAL, VOLCENGINE_WEB',
  `request_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求参数摘要',
  `response_time` int(11) DEFAULT NULL COMMENT '响应时间（毫秒）',
  `parse_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解析时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parsing_status` (`parsing_status`) USING BTREE,
  INDEX `idx_parsing_method` (`parsing_method`) USING BTREE,
  INDEX `idx_api_type` (`api_type`) USING BTREE,
  INDEX `idx_parse_time` (`parse_time`) USING BTREE,
  INDEX `idx_parsed_event_count` (`parsed_event_count`) USING BTREE,
  -- 复合索引用于解析统计和调试查询优化
  INDEX `idx_api_status_time` (`api_type`, `parsing_status`, `parse_time`) USING BTREE,
  INDEX `idx_method_status_count` (`parsing_method`, `parsing_status`, `parsed_event_count`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件解析记录表' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;