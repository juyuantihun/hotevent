-- 插入地区树形结构测试数据

-- 清空现有数据
DELETE FROM region_item;
DELETE FROM region;

-- 插入洲级地区（顶级）
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
(1, '亚洲', 'CONTINENT', NULL, NOW(), NOW()),
(2, '欧洲', 'CONTINENT', NULL, NOW(), NOW()),
(3, '北美洲', 'CONTINENT', NULL, NOW(), NOW());

-- 插入国家级地区
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
-- 亚洲国家
(101, '中国', 'COUNTRY', 1, NOW(), NOW()),
(102, '日本', 'COUNTRY', 1, NOW(), NOW()),
(103, '韩国', 'COUNTRY', 1, NOW(), NOW()),
-- 欧洲国家
(201, '法国', 'COUNTRY', 2, NOW(), NOW()),
(202, '德国', 'COUNTRY', 2, NOW(), NOW()),
(203, '英国', 'COUNTRY', 2, NOW(), NOW()),
-- 北美洲国家
(301, '美国', 'COUNTRY', 3, NOW(), NOW()),
(302, '加拿大', 'COUNTRY', 3, NOW(), NOW());

-- 插入省份/州级地区
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
-- 中国省份
(10101, '北京市', 'PROVINCE', 101, NOW(), NOW()),
(10102, '上海市', 'PROVINCE', 101, NOW(), NOW()),
(10103, '广东省', 'PROVINCE', 101, NOW(), NOW()),
(10104, '浙江省', 'PROVINCE', 101, NOW(), NOW()),
-- 日本都道府县
(10201, '东京都', 'PROVINCE', 102, NOW(), NOW()),
(10202, '大阪府', 'PROVINCE', 102, NOW(), NOW()),
-- 美国州
(30101, '纽约州', 'PROVINCE', 301, NOW(), NOW()),
(30102, '加利福尼亚州', 'PROVINCE', 301, NOW(), NOW()),
(30103, '德克萨斯州', 'PROVINCE', 301, NOW(), NOW());

-- 插入城市级地区
INSERT INTO region (id, name, type, parent_id, created_at, updated_at) VALUES
-- 北京市区县
(1010101, '海淀区', 'CITY', 10101, NOW(), NOW()),
(1010102, '朝阳区', 'CITY', 10101, NOW(), NOW()),
(1010103, '西城区', 'CITY', 10101, NOW(), NOW()),
-- 上海市区县
(1010201, '浦东新区', 'CITY', 10102, NOW(), NOW()),
(1010202, '黄浦区', 'CITY', 10102, NOW(), NOW()),
-- 广东省城市
(1010301, '广州市', 'CITY', 10103, NOW(), NOW()),
(1010302, '深圳市', 'CITY', 10103, NOW(), NOW()),
(1010303, '珠海市', 'CITY', 10103, NOW(), NOW()),
-- 纽约州城市
(3010101, '纽约市', 'CITY', 30101, NOW(), NOW()),
(3010102, '奥尔巴尼', 'CITY', 30101, NOW(), NOW()),
-- 加州城市
(3010201, '洛杉矶', 'CITY', 30102, NOW(), NOW()),
(3010202, '旧金山', 'CITY', 30102, NOW(), NOW()),
(3010203, '圣地亚哥', 'CITY', 30102, NOW(), NOW());

-- 重置自增ID
ALTER TABLE region AUTO_INCREMENT = 4000000;