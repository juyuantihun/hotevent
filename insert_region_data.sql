-- 插入地区数据，匹配Region实体类的结构
-- 确保表结构正确
ALTER TABLE `region` DROP COLUMN IF EXISTS `name_en`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `level`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `code`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `longitude`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `latitude`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `status`;
ALTER TABLE `region` DROP COLUMN IF EXISTS `description`;

-- 添加parent_id字段（如果不存在）
ALTER TABLE `region` ADD COLUMN IF NOT EXISTS `parent_id` bigint(20) DEFAULT NULL COMMENT '父地区ID' AFTER `type`;

-- 清空现有数据
TRUNCATE TABLE `region`;

-- 插入洲级别数据
INSERT INTO `region` (`id`, `name`, `type`, `parent_id`, `created_at`, `updated_at`) VALUES
(1, '亚洲', 'CONTINENT', NULL, NOW(), NOW()),
(2, '欧洲', 'CONTINENT', NULL, NOW(), NOW()),
(3, '北美洲', 'CONTINENT', NULL, NOW(), NOW());

-- 插入国家级别数据
INSERT INTO `region` (`id`, `name`, `type`, `parent_id`, `created_at`, `updated_at`) VALUES
(101, '中国', 'COUNTRY', 1, NOW(), NOW()),
(102, '日本', 'COUNTRY', 1, NOW(), NOW()),
(103, '韩国', 'COUNTRY', 1, NOW(), NOW()),
(201, '法国', 'COUNTRY', 2, NOW(), NOW()),
(202, '德国', 'COUNTRY', 2, NOW(), NOW()),
(203, '英国', 'COUNTRY', 2, NOW(), NOW()),
(301, '美国', 'COUNTRY', 3, NOW(), NOW()),
(302, '加拿大', 'COUNTRY', 3, NOW(), NOW());

-- 插入省份级别数据（中国）
INSERT INTO `region` (`id`, `name`, `type`, `parent_id`, `created_at`, `updated_at`) VALUES
(10101, '北京市', 'PROVINCE', 101, NOW(), NOW()),
(10102, '上海市', 'PROVINCE', 101, NOW(), NOW()),
(10103, '广东省', 'PROVINCE', 101, NOW(), NOW());

-- 插入城市级别数据
INSERT INTO `region` (`id`, `name`, `type`, `parent_id`, `created_at`, `updated_at`) VALUES
(1010101, '北京', 'CITY', 10101, NOW(), NOW()),
(1010201, '上海', 'CITY', 10102, NOW(), NOW()),
(1010301, '广州', 'CITY', 10103, NOW(), NOW()),
(1010302, '深圳', 'CITY', 10103, NOW(), NOW());