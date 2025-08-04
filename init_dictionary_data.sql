-- 初始化字典数据
-- 清空现有数据
DELETE FROM dictionary WHERE dict_type IN ('event_type', 'subject', 'object', 'relation_type', 'country');

-- 插入事件类型数据
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('政治事件', 'political_event', 'event_type', NULL, 1, 1, NOW(), NOW()),
('经济事件', 'economic_event', 'event_type', NULL, 2, 1, NOW(), NOW()),
('军事事件', 'military_event', 'event_type', NULL, 3, 1, NOW(), NOW()),
('社会事件', 'social_event', 'event_type', NULL, 4, 1, NOW(), NOW()),
('文化事件', 'cultural_event', 'event_type', NULL, 5, 1, NOW(), NOW()),
('科技事件', 'technology_event', 'event_type', NULL, 6, 1, NOW(), NOW()),
('环境事件', 'environmental_event', 'event_type', NULL, 7, 1, NOW(), NOW()),
('体育事件', 'sports_event', 'event_type', NULL, 8, 1, NOW(), NOW());

-- 插入事件主体数据
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('中国政府', 'china_government', 'subject', NULL, 1, 1, NOW(), NOW()),
('美国政府', 'usa_government', 'subject', NULL, 2, 1, NOW(), NOW()),
('俄罗斯政府', 'russia_government', 'subject', NULL, 3, 1, NOW(), NOW()),
('欧盟', 'european_union', 'subject', NULL, 4, 1, NOW(), NOW()),
('联合国', 'united_nations', 'subject', NULL, 5, 1, NOW(), NOW()),
('世界银行', 'world_bank', 'subject', NULL, 6, 1, NOW(), NOW()),
('国际货币基金组织', 'imf', 'subject', NULL, 7, 1, NOW(), NOW()),
('北约', 'nato', 'subject', NULL, 8, 1, NOW(), NOW()),
('中国企业', 'china_enterprise', 'subject', NULL, 9, 1, NOW(), NOW()),
('美国企业', 'usa_enterprise', 'subject', NULL, 10, 1, NOW(), NOW());

-- 插入事件客体数据
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('中国政府', 'china_government', 'object', NULL, 1, 1, NOW(), NOW()),
('美国政府', 'usa_government', 'object', NULL, 2, 1, NOW(), NOW()),
('俄罗斯政府', 'russia_government', 'object', NULL, 3, 1, NOW(), NOW()),
('欧盟', 'european_union', 'object', NULL, 4, 1, NOW(), NOW()),
('联合国', 'united_nations', 'object', NULL, 5, 1, NOW(), NOW()),
('世界银行', 'world_bank', 'object', NULL, 6, 1, NOW(), NOW()),
('国际货币基金组织', 'imf', 'object', NULL, 7, 1, NOW(), NOW()),
('北约', 'nato', 'object', NULL, 8, 1, NOW(), NOW()),
('中国企业', 'china_enterprise', 'object', NULL, 9, 1, NOW(), NOW()),
('美国企业', 'usa_enterprise', 'object', NULL, 10, 1, NOW(), NOW()),
('国际市场', 'international_market', 'object', NULL, 11, 1, NOW(), NOW()),
('全球经济', 'global_economy', 'object', NULL, 12, 1, NOW(), NOW());

-- 插入关系类型数据
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('合作', 'cooperation', 'relation_type', NULL, 1, 1, NOW(), NOW()),
('冲突', 'conflict', 'relation_type', NULL, 2, 1, NOW(), NOW()),
('谈判', 'negotiation', 'relation_type', NULL, 3, 1, NOW(), NOW()),
('制裁', 'sanction', 'relation_type', NULL, 4, 1, NOW(), NOW()),
('支持', 'support', 'relation_type', NULL, 5, 1, NOW(), NOW()),
('反对', 'opposition', 'relation_type', NULL, 6, 1, NOW(), NOW()),
('投资', 'investment', 'relation_type', NULL, 7, 1, NOW(), NOW()),
('贸易', 'trade', 'relation_type', NULL, 8, 1, NOW(), NOW()),
('援助', 'assistance', 'relation_type', NULL, 9, 1, NOW(), NOW()),
('竞争', 'competition', 'relation_type', NULL, 10, 1, NOW(), NOW());

-- 插入国家/地区数据（树形结构）
-- 亚洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('亚洲', 'asia', 'country', NULL, 1, 1, NOW(), NOW());

SET @asia_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('中国', 'china', 'country', @asia_id, 1, 1, NOW(), NOW()),
('日本', 'japan', 'country', @asia_id, 2, 1, NOW(), NOW()),
('韩国', 'south_korea', 'country', @asia_id, 3, 1, NOW(), NOW()),
('印度', 'india', 'country', @asia_id, 4, 1, NOW(), NOW()),
('新加坡', 'singapore', 'country', @asia_id, 5, 1, NOW(), NOW()),
('泰国', 'thailand', 'country', @asia_id, 6, 1, NOW(), NOW());

-- 欧洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('欧洲', 'europe', 'country', NULL, 2, 1, NOW(), NOW());

SET @europe_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('德国', 'germany', 'country', @europe_id, 1, 1, NOW(), NOW()),
('法国', 'france', 'country', @europe_id, 2, 1, NOW(), NOW()),
('英国', 'united_kingdom', 'country', @europe_id, 3, 1, NOW(), NOW()),
('意大利', 'italy', 'country', @europe_id, 4, 1, NOW(), NOW()),
('西班牙', 'spain', 'country', @europe_id, 5, 1, NOW(), NOW()),
('俄罗斯', 'russia', 'country', @europe_id, 6, 1, NOW(), NOW());

-- 北美洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('北美洲', 'north_america', 'country', NULL, 3, 1, NOW(), NOW());

SET @north_america_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('美国', 'usa', 'country', @north_america_id, 1, 1, NOW(), NOW()),
('加拿大', 'canada', 'country', @north_america_id, 2, 1, NOW(), NOW()),
('墨西哥', 'mexico', 'country', @north_america_id, 3, 1, NOW(), NOW());

-- 南美洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('南美洲', 'south_america', 'country', NULL, 4, 1, NOW(), NOW());

SET @south_america_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('巴西', 'brazil', 'country', @south_america_id, 1, 1, NOW(), NOW()),
('阿根廷', 'argentina', 'country', @south_america_id, 2, 1, NOW(), NOW()),
('智利', 'chile', 'country', @south_america_id, 3, 1, NOW(), NOW());

-- 非洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('非洲', 'africa', 'country', NULL, 5, 1, NOW(), NOW());

SET @africa_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('南非', 'south_africa', 'country', @africa_id, 1, 1, NOW(), NOW()),
('埃及', 'egypt', 'country', @africa_id, 2, 1, NOW(), NOW()),
('尼日利亚', 'nigeria', 'country', @africa_id, 3, 1, NOW(), NOW());

-- 大洋洲
INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('大洋洲', 'oceania', 'country', NULL, 6, 1, NOW(), NOW());

SET @oceania_id = LAST_INSERT_ID();

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('澳大利亚', 'australia', 'country', @oceania_id, 1, 1, NOW(), NOW()),
('新西兰', 'new_zealand', 'country', @oceania_id, 2, 1, NOW(), NOW());

-- 中国省份（作为中国的子级）
SET @china_id = (SELECT id FROM dictionary WHERE dict_code = 'china' AND dict_type = 'country');

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('北京市', 'beijing', 'country', @china_id, 1, 1, NOW(), NOW()),
('上海市', 'shanghai', 'country', @china_id, 2, 1, NOW(), NOW()),
('广东省', 'guangdong', 'country', @china_id, 3, 1, NOW(), NOW()),
('浙江省', 'zhejiang', 'country', @china_id, 4, 1, NOW(), NOW()),
('江苏省', 'jiangsu', 'country', @china_id, 5, 1, NOW(), NOW()),
('山东省', 'shandong', 'country', @china_id, 6, 1, NOW(), NOW()),
('河南省', 'henan', 'country', @china_id, 7, 1, NOW(), NOW()),
('四川省', 'sichuan', 'country', @china_id, 8, 1, NOW(), NOW());

-- 美国州份（作为美国的子级）
SET @usa_id = (SELECT id FROM dictionary WHERE dict_code = 'usa' AND dict_type = 'country');

INSERT INTO dictionary (dict_name, dict_code, dict_type, parent_id, sort_order, status, create_time, update_time) VALUES
('加利福尼亚州', 'california', 'country', @usa_id, 1, 1, NOW(), NOW()),
('纽约州', 'new_york', 'country', @usa_id, 2, 1, NOW(), NOW()),
('德克萨斯州', 'texas', 'country', @usa_id, 3, 1, NOW(), NOW()),
('佛罗里达州', 'florida', 'country', @usa_id, 4, 1, NOW(), NOW()),
('伊利诺伊州', 'illinois', 'country', @usa_id, 5, 1, NOW(), NOW()),
('宾夕法尼亚州', 'pennsylvania', 'country', @usa_id, 6, 1, NOW(), NOW());

COMMIT;