-- 查找和删除无关事件的SQL脚本

-- 1. 首先查看所有事件，按时间排序
SELECT 
    id,
    event_title,
    event_description,
    event_time,
    event_location,
    subject,
    object,
    event_type,
    fetch_method,
    created_at
FROM events 
ORDER BY event_time ASC, created_at ASC
LIMIT 20;

-- 2. 查找可能的无关事件（基于关键词）
SELECT 
    id,
    event_title,
    event_description,
    event_time,
    fetch_method
FROM events 
WHERE 
    event_title LIKE '%以色列%' 
    OR event_title LIKE '%伊朗%'
    OR event_title LIKE '%空袭%'
    OR event_title LIKE '%核设施%'
    OR event_title LIKE '%军事目标%'
    OR event_description LIKE '%以色列%'
    OR event_description LIKE '%伊朗%'
    OR fetch_method = 'FALLBACK_GENERATOR'
    OR fetch_method = 'SIMPLE_FALLBACK'
ORDER BY event_time ASC;

-- 3. 查找测试事件
SELECT 
    id,
    event_title,
    event_description,
    fetch_method
FROM events 
WHERE 
    event_title LIKE '%测试%'
    OR event_title LIKE '%test%'
    OR fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
ORDER BY created_at ASC;

-- 4. 删除无关的以色列-伊朗事件（请根据实际情况调整条件）
-- DELETE FROM events 
-- WHERE 
--     (event_title LIKE '%以色列%' AND event_title LIKE '%伊朗%')
--     OR (event_title LIKE '%空袭%' AND event_title LIKE '%核设施%');

-- 5. 删除测试事件（请根据实际情况调整条件）
-- DELETE FROM events 
-- WHERE 
--     fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
--     OR event_title LIKE '%测试事件%'
--     OR event_title LIKE '%test_event%';

-- 6. 验证删除结果
-- SELECT COUNT(*) as remaining_events FROM events;
-- SELECT 
--     id,
--     event_title,
--     event_time
-- FROM events 
-- ORDER BY event_time ASC
-- LIMIT 10;