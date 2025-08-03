-- 为地区表添加父地区ID字段，支持树形结构
ALTER TABLE `region` ADD COLUMN `parent_id` bigint(20) DEFAULT NULL COMMENT '父地区ID' AFTER `type`;

-- 添加父地区ID的索引
ALTER TABLE `region` ADD KEY `idx_region_parent_id` (`parent_id`);

-- 添加外键约束（可选，如果需要严格的引用完整性）
-- ALTER TABLE `region` ADD CONSTRAINT `fk_region_parent` FOREIGN KEY (`parent_id`) REFERENCES `region` (`id`) ON DELETE CASCADE;