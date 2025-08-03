/*
 时间线创建缓存表迁移脚本
 
 创建时间: 2025-07-27
 描述: 创建timeline_creation_cache表，用于防止重复创建时间线
 需求: 1.1, 1.2
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for timeline_creation_cache
-- ----------------------------
DROP TABLE IF EXISTS `timeline_creation_cache`;
CREATE TABLE `timeline_creation_cache` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求指纹 - 基于请求参数生成的唯一标识',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  `timeline_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '时间线名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `region_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '地区ID列表（JSON格式存储）',
  `timeline_id` bigint(20) DEFAULT NULL COMMENT '关联的时间线ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CREATING' COMMENT '状态：CREATING(创建中), COMPLETED(已完成), FAILED(失败)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_request_fingerprint` (`request_fingerprint`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_timeline_name` (`timeline_name`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_created_at` (`created_at`) USING BTREE,
  INDEX `idx_expires_at` (`expires_at`) USING BTREE,
  INDEX `idx_timeline_id` (`timeline_id`) USING BTREE,
  -- 复合索引用于重复检测查询优化
  INDEX `idx_name_time_range` (`timeline_name`, `start_time`, `end_time`) USING BTREE,
  INDEX `idx_user_time_window` (`user_id`, `created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间线创建缓存表' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;