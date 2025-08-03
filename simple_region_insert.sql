-- 简单的region数据插入脚本
-- 清空现有数据
DELETE FROM region;

-- 插入洲级别数据
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
(1, '亚洲', 'CONTINENT', NULL, NOW(), NOW()),
(2, '欧洲', 'CONTINENT', NULL, NOW(), NOW()),
(3, '北美洲', 'CONTINENT', NULL, NOW(), NOW());

-- 插入国家级别数据
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
(101, '中国', 'COUNTRY', 1, NOW(), NOW()),
(102, '日本', 'COUNTRY', 1, NOW(), NOW()),
(103, '韩国', 'COUNTRY', 1, NOW(), NOW()),
(201, '法国', 'COUNTRY', 2, NOW(), NOW()),
(202, '德国', 'COUNTRY', 2, NOW(), NOW()),
(203, '英国', 'COUNTRY', 2, NOW(), NOW()),
(301, '美国', 'COUNTRY', 3, NOW(), NOW()),
(302, '加拿大', 'COUNTRY', 3, NOW(), NOW());