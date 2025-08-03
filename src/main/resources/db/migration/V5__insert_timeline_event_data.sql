-- 插入时间线事件关联数据
-- 假设时间线ID为12（巴黎地区事件时间线）关联一些中东事件作为示例

-- 为时间线ID=12关联一些事件
INSERT INTO `timeline_event` (`timeline_id`, `event_id`, `created_at`) VALUES
(12, 1, NOW()),
(12, 2, NOW()),
(12, 3, NOW()),
(12, 4, NOW()),
(12, 5, NOW()),
(12, 6, NOW()),
(12, 7, NOW()),
(12, 8, NOW()),
(12, 9, NOW()),
(12, 10, NOW());

-- 如果有其他时间线，也可以添加关联
-- 例如，为时间线ID=1关联前5个事件
INSERT INTO `timeline_event` (`timeline_id`, `event_id`, `created_at`) 
SELECT 1, id, NOW()
FROM `event` 
WHERE id <= 5
AND NOT EXISTS (SELECT 1 FROM `timeline_event` WHERE timeline_id = 1 AND event_id = `event`.id);

-- 为时间线ID=2关联后5个事件
INSERT INTO `timeline_event` (`timeline_id`, `event_id`, `created_at`) 
SELECT 2, id, NOW()
FROM `event` 
WHERE id > 5 AND id <= 10
AND NOT EXISTS (SELECT 1 FROM `timeline_event` WHERE timeline_id = 2 AND event_id = `event`.id);