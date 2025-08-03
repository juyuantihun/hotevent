-- 直接删除无关事件的SQL脚本

-- 1. 查看当前最早的几个事件
SELECT 
    id,
    event_title,
    event_description,
    event_time,
    fetch_method,
    created_at
FROM events 
ORDER BY event_time ASC, created_at ASC
LIMIT 10;

-- 2. 查找以色列-伊朗相关的无关事件
SELECT 
    id,
    event_title,
    event_description,
    event_time
FROM events 
WHERE 
    event_title LIKE '%以色列%' 
    OR event_title LIKE '%伊朗%'
    OR event_description LIKE '%以色列%'
    OR event_description LIKE '%伊朗%'
    OR event_title LIKE '%空袭%'
    OR event_title LIKE '%核设施%'
ORDER BY event_time ASC;

-- 3. 删除这些无关事件（请确认后执行）
DELETE FROM events 
WHERE 
    (event_title LIKE '%以色列%' AND (event_title LIKE '%伊朗%' OR event_title LIKE '%核设施%'))
    OR (event_description LIKE '%以色列%' AND (event_description LIKE '%伊朗%' OR event_description LIKE '%核设施%'))
    OR (event_title LIKE '%空袭%' AND event_title LIKE '%核设施%');

-- 4. 同时删除任何测试事件
DELETE FROM events 
WHERE 
    fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
    OR event_title LIKE '%测试事件%'
    OR event_title LIKE '%test_event%'
    OR event_title LIKE '%Test Event%';

-- 5. 验证删除结果
SELECT 
    COUNT(*) as total_events
FROM events;

-- 6. 查看删除后的前10个事件
SELECT 
    id,
    event_title,
    event_time,
    event_location
FROM events 
ORDER BY event_time ASC, created_at ASC
LIMIT 10;