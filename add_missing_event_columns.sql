-- 添加 Event 表缺失的字段
-- 执行前请备份数据库

USE hot_events_db;

-- 添加事件标题字段
ALTER TABLE `event` ADD COLUMN `event_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件标题' AFTER `intensity_level`;

-- 添加可信度评分字段
ALTER TABLE `event` ADD COLUMN `credibility_score` decimal(3,2) NULL DEFAULT NULL COMMENT '可信度评分(0.00-1.00)' AFTER `event_title`;

-- 添加验证状态字段
ALTER TABLE `event` ADD COLUMN `validation_status` tinyint(1) NULL DEFAULT 0 COMMENT '验证状态：0-未验证，1-已验证，2-验证失败' AFTER `credibility_score`;

-- 添加来源URL字段
ALTER TABLE `event` ADD COLUMN `source_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '来源URL列表(JSON格式)' AFTER `validation_status`;

-- 添加获取方法字段
ALTER TABLE `event` ADD COLUMN `fetch_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '获取方法：manual-手动，api-API，crawler-爬虫' AFTER `source_urls`;

-- 添加最后验证时间字段
ALTER TABLE `event` ADD COLUMN `last_validated_at` datetime(0) NULL DEFAULT NULL COMMENT '最后验证时间' AFTER `fetch_method`;

-- 添加主体坐标ID字段
ALTER TABLE `event` ADD COLUMN `subject_coordinate_id` bigint(20) NULL DEFAULT NULL COMMENT '事件主体地理坐标ID（关联geographic_coordinates表）' AFTER `last_validated_at`;

-- 添加客体坐标ID字段
ALTER TABLE `event` ADD COLUMN `object_coordinate_id` bigint(20) NULL DEFAULT NULL COMMENT '事件客体地理坐标ID（关联geographic_coordinates表）' AFTER `subject_coordinate_id`;

-- 添加事件坐标ID字段
ALTER TABLE `event` ADD COLUMN `event_coordinate_id` bigint(20) NULL DEFAULT NULL COMMENT '事件发生地坐标ID（关联geographic_coordinates表）' AFTER `object_coordinate_id`;

-- 添加地理状态字段
ALTER TABLE `event` ADD COLUMN `geographic_status` tinyint(1) NULL DEFAULT 0 COMMENT '地理信息状态：0-未处理，1-已处理，2-处理失败' AFTER `event_coordinate_id`;

-- 添加地理信息更新时间字段
ALTER TABLE `event` ADD COLUMN `geographic_updated_at` datetime(0) NULL DEFAULT NULL COMMENT '地理信息更新时间' AFTER `geographic_status`;

-- 为新字段添加索引
CREATE INDEX `idx_event_title` ON `event`(`event_title`);
CREATE INDEX `idx_credibility_score` ON `event`(`credibility_score`);
CREATE INDEX `idx_validation_status` ON `event`(`validation_status`);
CREATE INDEX `idx_fetch_method` ON `event`(`fetch_method`);
CREATE INDEX `idx_geographic_status` ON `event`(`geographic_status`);

-- 显示表结构确认
DESCRIBE `event`;