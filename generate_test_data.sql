-- 为热点事件系统生成测试数据
-- 基于现有数据库结构生成合理的测试数据

SET NAMES utf8mb4;

-- 1. 插入国家数据
INSERT INTO `country` (`name`, `short_name`, `population`, `area`, `capital`, `language`, `currency`, `created_at`, `updated_at`) VALUES
('中华人民共和国', '中国', 1400000000, 9600000, '北京', '中文', '人民币', NOW(), NOW()),
('美利坚合众国', '美国', 330000000, 9834000, '华盛顿', '英语', '美元', NOW(), NOW()),
('俄罗斯联邦', '俄罗斯', 146000000, 17100000, '莫斯科', '俄语', '卢布', NOW(), NOW()),
('日本国', '日本', 125000000, 378000, '东京', '日语', '日元', NOW(), NOW()),
('大韩民国', '韩国', 52000000, 100000, '首尔', '韩语', '韩元', NOW(), NOW()),
('朝鲜民主主义人民共和国', '朝鲜', 26000000, 121000, '平壤', '朝鲜语', '朝鲜元', NOW(), NOW()),
('德意志联邦共和国', '德国', 83000000, 358000, '柏林', '德语', '欧元', NOW(), NOW()),
('法兰西共和国', '法国', 68000000, 644000, '巴黎', '法语', '欧元', NOW(), NOW()),
('大不列颠及北爱尔兰联合王国', '英国', 67000000, 244000, '伦敦', '英语', '英镑', NOW(), NOW()),
('以色列国', '以色列', 9000000, 21000, '耶路撒冷', '希伯来语', '以色列谢克尔', NOW(), NOW()),
('伊朗伊斯兰共和国', '伊朗', 84000000, 1648000, '德黑兰', '波斯语', '伊朗里亚尔', NOW(), NOW()),
('乌克兰', '乌克兰', 44000000, 604000, '基辅', '乌克兰语', '格里夫纳', NOW(), NOW()),
('加拿大', '加拿大', 38000000, 9985000, '渥太华', '英语/法语', '加元', NOW(), NOW()),
('澳大利亚联邦', '澳大利亚', 26000000, 7692000, '堪培拉', '英语', '澳元', NOW(), NOW()),
('印度共和国', '印度', 1380000000, 3287000, '新德里', '印地语/英语', '印度卢比', NOW(), NOW());

-- 2. 插入组织数据
INSERT INTO `organization` (`name`, `short_name`, `type`, `country_id`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
('联合国', 'UN', '国际组织', NULL, '联合国是一个由主权国家组成的国际组织', 1, NOW(), NOW(), 'admin', 'admin'),
('北大西洋公约组织', 'NATO', '军事组织', NULL, '北约是欧洲和北美国家的政治和军事联盟', 1, NOW(), NOW(), 'admin', 'admin'),
('欧洲联盟', 'EU', '政治经济联盟', NULL, '欧盟是欧洲国家的政治和经济联盟', 1, NOW(), NOW(), 'admin', 'admin'),
('世界贸易组织', 'WTO', '国际组织', NULL, '世界贸易组织是处理国家间贸易规则的国际组织', 1, NOW(), NOW(), 'admin', 'admin'),
('中国共产党', 'CPC', '政党', 1, '中华人民共和国执政党', 1, NOW(), NOW(), 'admin', 'admin'),
('美国国务院', 'DOS', '政府机构', 2, '美国负责外交事务的联邦政府部门', 1, NOW(), NOW(), 'admin', 'admin'),
('俄罗斯联邦政府', 'RFG', '政府机构', 3, '俄罗斯联邦的行政机关', 1, NOW(), NOW(), 'admin', 'admin'),
('哈马斯', 'Hamas', '政治军事组织', NULL, '巴勒斯坦伊斯兰抵抗运动', 1, NOW(), NOW(), 'admin', 'admin'),
('真主党', 'Hezbollah', '政治军事组织', NULL, '黎巴嫩什叶派政治军事组织', 1, NOW(), NOW(), 'admin', 'admin'),
('伊斯兰革命卫队', 'IRGC', '军事组织', 11, '伊朗伊斯兰革命卫队', 1, NOW(), NOW(), 'admin', 'admin'),
('亚太经济合作组织', 'APEC', '经济组织', NULL, 'APEC是亚太地区经济合作论坛', 1, NOW(), NOW(), 'admin', 'admin'),
('上海合作组织', 'SCO', '政治军事组织', NULL, '上海合作组织是欧亚地区的政治、经济、军事组织', 1, NOW(), NOW(), 'admin', 'admin'),
('自由民主党', 'LDP', '政党', 4, '日本自由民主党', 1, NOW(), NOW(), 'admin', 'admin'),
('工党', 'Labour', '政党', 9, '英国工党', 1, NOW(), NOW(), 'admin', 'admin'),
('德国联邦议院', 'Bundestag', '政府机构', 7, '德国联邦议院', 1, NOW(), NOW(), 'admin', 'admin');

-- 3. 插入人物数据
INSERT INTO `person` (`name`, `gender`, `birth_date`, `country_id`, `organization_id`, `description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
('习近平', '男', '1953-06-15', 1, 5, '中华人民共和国主席、中国共产党中央委员会总书记', 1, NOW(), NOW(), 'admin', 'admin'),
('拜登', '男', '1942-11-20', 2, 6, '美国第46任总统', 1, NOW(), NOW(), 'admin', 'admin'),
('普京', '男', '1952-10-07', 3, 7, '俄罗斯联邦总统', 1, NOW(), NOW(), 'admin', 'admin'),
('岸田文雄', '男', '1957-07-29', 4, 13, '日本首相', 1, NOW(), NOW(), 'admin', 'admin'),
('尹锡悦', '男', '1960-12-18', 5, NULL, '韩国总统', 1, NOW(), NOW(), 'admin', 'admin'),
('金正恩', '男', '1984-01-08', 6, NULL, '朝鲜最高领导人', 1, NOW(), NOW(), 'admin', 'admin'),
('泽连斯基', '男', '1978-01-25', 12, NULL, '乌克兰总统', 1, NOW(), NOW(), 'admin', 'admin'),
('内塔尼亚胡', '男', '1949-10-21', 10, NULL, '以色列总理', 1, NOW(), NOW(), 'admin', 'admin'),
('哈梅内伊', '男', '1939-04-19', 11, 10, '伊朗最高领袖', 1, NOW(), NOW(), 'admin', 'admin'),
('马克龙', '男', '1977-12-21', 8, NULL, '法国总统', 1, NOW(), NOW(), 'admin', 'admin'),
('朔尔茨', '男', '1958-06-14', 7, 15, '德国总理', 1, NOW(), NOW(), 'admin', 'admin'),
('苏纳克', '男', '1980-05-12', 9, 14, '英国首相', 1, NOW(), NOW(), 'admin', 'admin'),
('特鲁多', '男', '1971-12-25', 13, NULL, '加拿大总理', 1, NOW(), NOW(), 'admin', 'admin'),
('阿尔巴尼斯', '男', '1963-03-02', 14, NULL, '澳大利亚总理', 1, NOW(), NOW(), 'admin', 'admin'),
('莫迪', '男', '1950-09-17', 15, NULL, '印度总理', 1, NOW(), NOW(), 'admin', 'admin');

-- 4. 更新字典表，将国家字典关联到实体
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 1 WHERE `dict_code` = 'CN' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 2 WHERE `dict_code` = 'US' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 3 WHERE `dict_code` = 'RU' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 4 WHERE `dict_code` = 'JP' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 5 WHERE `dict_code` = 'KR' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 10 WHERE `dict_code` = 'IL' AND `dict_type` = '国家';
UPDATE `dictionary` SET `entity_type` = 'country', `entity_id` = 11 WHERE `dict_code` = 'IR' AND `dict_type` = '国家';

-- 插入更多字典数据并关联实体
INSERT INTO `dictionary` (`dict_type`, `dict_code`, `dict_name`, `dict_description`, `parent_id`, `sort_order`, `status`, `is_auto_added`, `entity_type`, `entity_id`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
-- 组织字典
('组织', 'UN', '联合国', '联合国', 0, 1, 1, 0, 'organization', 1, NOW(), NOW(), 'admin', 'admin'),
('组织', 'NATO', '北约', '北大西洋公约组织', 0, 2, 1, 0, 'organization', 2, NOW(), NOW(), 'admin', 'admin'),
('组织', 'EU', '欧盟', '欧洲联盟', 0, 3, 1, 0, 'organization', 3, NOW(), NOW(), 'admin', 'admin'),
('组织', 'CPC', '中国共产党', '中国共产党', 0, 4, 1, 0, 'organization', 5, NOW(), NOW(), 'admin', 'admin'),
('组织', 'Hamas', '哈马斯', '巴勒斯坦伊斯兰抵抗运动', 0, 5, 1, 0, 'organization', 8, NOW(), NOW(), 'admin', 'admin'),

-- 人物字典
('人物', 'xi_jinping', '习近平', '中华人民共和国主席', 0, 1, 1, 0, 'person', 1, NOW(), NOW(), 'admin', 'admin'),
('人物', 'biden', '拜登', '美国总统', 0, 2, 1, 0, 'person', 2, NOW(), NOW(), 'admin', 'admin'),
('人物', 'putin', '普京', '俄罗斯总统', 0, 3, 1, 0, 'person', 3, NOW(), NOW(), 'admin', 'admin'),
('人物', 'netanyahu', '内塔尼亚胡', '以色列总理', 0, 4, 1, 0, 'person', 8, NOW(), NOW(), 'admin', 'admin'),
('人物', 'khamenei', '哈梅内伊', '伊朗最高领袖', 0, 5, 1, 0, 'person', 9, NOW(), NOW(), 'admin', 'admin');

-- 5. 插入事件数据
INSERT INTO `event` (`event_code`, `event_time`, `event_location`, `event_type`, `event_description`, `subject`, `object`, `longitude`, `latitude`, `source_type`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
('EVT_001', '2024-01-15 14:30:00', '加沙地带', '冲突', '以色列军队对加沙地带进行军事行动', '以色列', '哈马斯', 34.4668, 31.5017, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_002', '2024-01-16 09:15:00', '华盛顿特区', '外交', '美国国务卿与中国外交部长举行会谈', '美国', '中国', -77.0369, 38.9072, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek'),
('EVT_003', '2024-01-17 16:45:00', '乌克兰东部', '冲突', '俄乌边境地区发生武装冲突', '俄罗斯', '乌克兰', 37.6173, 55.7558, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_004', '2024-01-18 11:20:00', '联合国总部', '外交', '联合国安理会就中东问题举行紧急会议', '联合国', '中东各国', -73.9665, 40.7489, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek'),
('EVT_005', '2024-01-19 20:30:00', '德黑兰', '制裁', '伊朗宣布暂停执行核协议部分条款', '伊朗', '国际社会', 51.3890, 35.6892, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_006', '2024-01-20 08:45:00', '平壤', '外交', '朝鲜与俄罗斯签署军事合作协议', '朝鲜', '俄罗斯', 125.7625, 39.0392, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek'),
('EVT_007', '2024-01-21 15:10:00', '台海', '冲突', '台海地区紧张局势升级', '中国', '台湾', 120.9605, 23.6978, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_008', '2024-01-22 12:00:00', '布鲁塞尔', '制裁', '欧盟宣布对俄罗斯实施新一轮制裁', '欧盟', '俄罗斯', 4.3517, 50.8503, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek'),
('EVT_009', '2024-01-23 19:30:00', '首尔', '外交', '韩美日三方领导人举行峰会', '韩国', '美国', 126.9780, 37.5665, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_010', '2024-01-24 10:15:00', '东京', '经济', '日本央行调整货币政策', '日本', '全球市场', 139.6917, 35.6895, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek'),
('EVT_011', '2024-01-25 14:20:00', '红海', '冲突', '红海商船遭袭击事件', '也门胡塞武装', '国际商船', 43.3547, 15.5527, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_012', '2024-01-26 16:45:00', '慕尼黑', '外交', '慕尼黑安全会议讨论全球安全议题', '欧盟', '国际社会', 11.5820, 48.1351, 2, 1, NOW(), NOW(), 'deepseek', 'deepseek');

-- 6. 插入事件关键词数据
INSERT INTO `event_keyword` (`event_id`, `keyword`, `created_at`) VALUES
-- EVT_001 关键词
(1, '以色列', NOW()),
(1, '加沙', NOW()),
(1, '军事冲突', NOW()),
(1, '哈马斯', NOW()),
(1, '中东', NOW()),

-- EVT_002 关键词
(2, '美中关系', NOW()),
(2, '外交', NOW()),
(2, '国务卿', NOW()),
(2, '会谈', NOW()),
(2, '双边关系', NOW()),

-- EVT_003 关键词
(3, '俄乌冲突', NOW()),
(3, '边境', NOW()),
(3, '武装冲突', NOW()),
(3, '东欧', NOW()),
(3, '地缘政治', NOW()),

-- EVT_004 关键词
(4, '联合国', NOW()),
(4, '安理会', NOW()),
(4, '中东', NOW()),
(4, '紧急会议', NOW()),
(4, '国际调解', NOW()),

-- EVT_005 关键词
(5, '伊朗', NOW()),
(5, '核协议', NOW()),
(5, '制裁', NOW()),
(5, '核武器', NOW()),
(5, '国际法', NOW()),

-- EVT_006 关键词
(6, '朝俄合作', NOW()),
(6, '军事协议', NOW()),
(6, '平壤', NOW()),
(6, '东北亚', NOW()),
(6, '军事联盟', NOW()),

-- EVT_007 关键词
(7, '台海', NOW()),
(7, '两岸关系', NOW()),
(7, '紧张局势', NOW()),
(7, '军事演习', NOW()),
(7, '地区稳定', NOW()),

-- EVT_008 关键词
(8, '欧盟制裁', NOW()),
(8, '俄罗斯', NOW()),
(8, '经济制裁', NOW()),
(8, '国际制裁', NOW()),
(8, '经济战', NOW()),

-- EVT_009 关键词
(9, '韩美日', NOW()),
(9, '三方峰会', NOW()),
(9, '军事合作', NOW()),
(9, '印太战略', NOW()),
(9, '地区安全', NOW()),

-- EVT_010 关键词
(10, '日本央行', NOW()),
(10, '货币政策', NOW()),
(10, '经济政策', NOW()),
(10, '利率', NOW()),
(10, '金融市场', NOW()),

-- EVT_011 关键词
(11, '红海', NOW()),
(11, '商船袭击', NOW()),
(11, '胡塞武装', NOW()),
(11, '航运安全', NOW()),
(11, '国际贸易', NOW()),

-- EVT_012 关键词
(12, '慕尼黑安全会议', NOW()),
(12, '全球安全', NOW()),
(12, '国际合作', NOW()),
(12, '防务政策', NOW()),
(12, '多边主义', NOW());

-- 7. 插入事件关联关系数据
INSERT INTO `event_relation` (`source_event_id`, `target_event_id`, `relation_type`, `relation_description`, `confidence`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
(1, 4, '导致', '加沙冲突导致联合国安理会召开紧急会议', 0.85, 1, NOW(), NOW(), 'admin', 'admin'),
(3, 8, '导致', '俄乌冲突导致欧盟对俄实施新制裁', 0.90, 1, NOW(), NOW(), 'admin', 'admin'),
(5, 4, '影响', '伊朗核协议问题影响中东局势', 0.75, 1, NOW(), NOW(), 'admin', 'admin'),
(6, 3, '支持', '朝俄军事合作可能影响乌克兰局势', 0.60, 1, NOW(), NOW(), 'admin', 'admin'),
(2, 7, '关联', '美中外交会谈与台海局势相关', 0.70, 1, NOW(), NOW(), 'admin', 'admin'),
(9, 6, '对抗', '韩美日三方合作对朝俄合作的回应', 0.65, 1, NOW(), NOW(), 'admin', 'admin'),
(11, 1, '扩散', '红海袭击与中东冲突的扩散效应', 0.55, 1, NOW(), NOW(), 'admin', 'admin'),
(10, 8, '影响', '日本货币政策受俄乌冲突影响', 0.45, 1, NOW(), NOW(), 'admin', 'admin');

-- 8. 插入实体关系数据
INSERT INTO `entity_relationship` (`source_entity_type`, `source_entity_id`, `target_entity_type`, `target_entity_id`, `relationship_type`, `relationship_description`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
-- 国家与国家关系
('country', 1, 'country', 3, '邻国', '中国与俄罗斯是邻国关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 1, 'country', 6, '邻国', '中国与朝鲜是邻国关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 2, 'country', 4, '盟友', '美国与日本是盟友关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 2, 'country', 5, '盟友', '美国与韩国是盟友关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 10, 'country', 2, '盟友', '以色列与美国是盟友关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 11, 'country', 3, '支持', '伊朗得到俄罗斯支持', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 7, 'country', 8, '邻国', '德国与法国是邻国关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 9, 'country', 8, '邻国', '英国与法国是邻国关系', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 13, 'country', 2, '邻国', '加拿大与美国是邻国关系', 1, NOW(), NOW(), 'admin', 'admin'),

-- 国家与人物关系
('country', 1, 'person', 1, '国籍', '习近平具有中国国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 2, 'person', 2, '国籍', '拜登具有美国国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 3, 'person', 3, '国籍', '普京具有俄罗斯国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 4, 'person', 4, '国籍', '岸田文雄具有日本国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 5, 'person', 5, '国籍', '尹锡悦具有韩国国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 6, 'person', 6, '国籍', '金正恩具有朝鲜国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 12, 'person', 7, '国籍', '泽连斯基具有乌克兰国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 10, 'person', 8, '国籍', '内塔尼亚胡具有以色列国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 11, 'person', 9, '国籍', '哈梅内伊具有伊朗国籍', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 8, 'person', 10, '国籍', '马克龙具有法国国籍', 1, NOW(), NOW(), 'admin', 'admin'),

-- 组织与人物关系
('organization', 5, 'person', 1, '领导', '习近平领导中国共产党', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 6, 'person', 2, '隶属', '拜登隶属于美国国务院系统', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 7, 'person', 3, '领导', '普京领导俄罗斯联邦政府', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 13, 'person', 4, '隶属', '岸田文雄隶属于自由民主党', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 10, 'person', 9, '隶属', '哈梅内伊隶属于伊斯兰革命卫队', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 15, 'person', 11, '隶属', '朔尔茨隶属于德国联邦议院', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 14, 'person', 12, '隶属', '苏纳克隶属于英国工党', 1, NOW(), NOW(), 'admin', 'admin'),

-- 组织与组织关系
('organization', 2, 'organization', 3, '合作', '北约与欧盟在防务方面合作', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 8, 'organization', 9, '合作', '哈马斯与真主党在中东地区合作', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 1, 'organization', 4, '合作', '联合国与世贸组织在国际事务中合作', 1, NOW(), NOW(), 'admin', 'admin'),
('organization', 11, 'organization', 12, '合作', 'APEC与上海合作组织在亚太地区合作', 1, NOW(), NOW(), 'admin', 'admin'),

-- 国家与组织关系
('country', 2, 'organization', 2, '成员', '美国是北约成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 8, 'organization', 3, '成员', '法国是欧盟成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 9, 'organization', 2, '成员', '英国是北约成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 7, 'organization', 3, '成员', '德国是欧盟成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 1, 'organization', 12, '成员', '中国是上海合作组织成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 3, 'organization', 12, '成员', '俄罗斯是上海合作组织成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 4, 'organization', 11, '成员', '日本是APEC成员', 1, NOW(), NOW(), 'admin', 'admin'),
('country', 2, 'organization', 11, '成员', '美国是APEC成员', 1, NOW(), NOW(), 'admin', 'admin');

-- 显示插入结果统计
SELECT '=== 数据插入完成统计 ===' as info;

SELECT 
    'country' as table_name, COUNT(*) as record_count FROM country
UNION ALL
SELECT 'organization', COUNT(*) FROM organization
UNION ALL
SELECT 'person', COUNT(*) FROM person
UNION ALL
SELECT 'dictionary', COUNT(*) FROM dictionary
UNION ALL
SELECT 'event', COUNT(*) FROM event
UNION ALL
SELECT 'event_keyword', COUNT(*) FROM event_keyword
UNION ALL
SELECT 'event_relation', COUNT(*) FROM event_relation
UNION ALL
SELECT 'entity_relationship', COUNT(*) FROM entity_relationship;

SELECT '=== 实体关联验证 ===' as info;

-- 验证字典与实体的关联
SELECT 
    d.dict_type,
    d.dict_name as dictionary_name,
    d.entity_type,
    CASE 
        WHEN d.entity_type = 'country' THEN c.name
        WHEN d.entity_type = 'organization' THEN o.name
        WHEN d.entity_type = 'person' THEN p.name
        ELSE 'N/A'
    END as entity_name
FROM dictionary d
LEFT JOIN country c ON d.entity_type = 'country' AND d.entity_id = c.id
LEFT JOIN organization o ON d.entity_type = 'organization' AND d.entity_id = o.id
LEFT JOIN person p ON d.entity_type = 'person' AND d.entity_id = p.id
WHERE d.entity_type IS NOT NULL AND d.entity_id IS NOT NULL
ORDER BY d.dict_type, d.sort_order
LIMIT 20; 