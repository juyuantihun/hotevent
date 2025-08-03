-- 简化的测试数据插入
USE hot_events;

-- 插入几个基本的事件数据
INSERT INTO `event` (`event_code`, `event_time`, `event_location`, `event_type`, `event_description`, `subject`, `object`, `longitude`, `latitude`, `source_type`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
('EVT_001', '2024-01-15 14:30:00', '加沙地带', '冲突', '以色列军队对加沙地带进行军事行动', '以色列', '哈马斯', 34.4668, 31.5017, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_002', '2024-01-16 09:15:00', '华盛顿特区', '外交', '美国国务卿与中国外交部长举行会谈', '美国', '中国', -77.0369, 38.9072, 2, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_003', '2024-01-17 16:45:00', '乌克兰东部', '冲突', '俄乌边境地区发生武装冲突', '俄罗斯', '乌克兰', 37.6173, 55.7558, 1, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_004', '2024-01-18 11:20:00', '联合国总部', '外交', '联合国安理会就中东问题举行紧急会议', '联合国', '中东各国', -73.9665, 40.7489, 2, 1, NOW(), NOW(), 'admin', 'admin'),
('EVT_005', '2024-01-19 20:30:00', '德黑兰', '制裁', '伊朗宣布暂停执行核协议部分条款', '伊朗', '国际社会', 51.3890, 35.6892, 1, 1, NOW(), NOW(), 'admin', 'admin');

-- 插入事件关键词
INSERT INTO `event_keyword` (`event_id`, `keyword`, `created_at`) VALUES
(1, '以色列', NOW()),
(1, '加沙', NOW()),
(1, '军事冲突', NOW()),
(2, '美中关系', NOW()),
(2, '外交', NOW()),
(3, '俄乌冲突', NOW()),
(3, '边境', NOW()),
(4, '联合国', NOW()),
(4, '安理会', NOW()),
(5, '伊朗', NOW()),
(5, '核协议', NOW());

-- 显示结果
SELECT '数据插入完成' as status;
SELECT COUNT(*) as event_count FROM event;
SELECT COUNT(*) as keyword_count FROM event_keyword; 