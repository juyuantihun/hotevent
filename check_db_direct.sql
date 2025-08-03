-- 检查时间线数据
USE hot_events_db;

-- 1. 检查时间线表
SELECT '=== 时间线表 ===' as info;
SELECT id, name, status, event_count, relation_count, created_at 
FROM timeline 
ORDER BY created_at DESC 
LIMIT 5;

-- 2. 检查事件表
SELECT '=== 事件表 ===' as info;
SELECT COUNT(*) as total_events FROM event;

-- 3. 检查时间线事件关联表
SELECT '=== 时间线事件关联 ===' as info;
SELECT timeline_id, COUNT(*) as event_count 
FROM timeline_event 
GROUP BY timeline_id 
ORDER BY timeline_id DESC 
LIMIT 5;

-- 4. 检查最新时间线的详细信息
SELECT '=== 最新时间线详情 ===' as info;
SELECT t.id as timeline_id, t.name, t.status, t.event_count, 
       COUNT(te.event_id) as actual_event_count
FROM timeline t 
LEFT JOIN timeline_event te ON t.id = te.timeline_id 
WHERE t.id = (SELECT MAX(id) FROM timeline)
GROUP BY t.id, t.name, t.status, t.event_count;

-- 5. 检查最新时间线的具体事件
SELECT '=== 最新时间线的事件 ===' as info;
SELECT e.id, e.event_title, e.event_time, e.event_location
FROM timeline t
JOIN timeline_event te ON t.id = te.timeline_id
JOIN event e ON te.event_id = e.id
WHERE t.id = (SELECT MAX(id) FROM timeline)
LIMIT 5;